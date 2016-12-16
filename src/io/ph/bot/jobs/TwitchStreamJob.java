package io.ph.bot.jobs;

import java.awt.Color;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.BadUsernameException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.TwitchObject;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Periodically poll the Twitch.tv API
 * This is set to poll every 180 seconds, because it's relatively expensive
 * to poll Twitch.tv too often.
 * 
 * Moved Twitch.tv database to a new file to prevent locking in case
 * something tries to access it at the same time, since this process can take
 * a little bit to finish up
 * @author Paul
 *
 */
public class TwitchStreamJob implements Job {

	private static boolean announce = false;
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Instant start = Instant.now();
		
		Connection conn = null;
		PreparedStatement checkStatusStmt = null;
		PreparedStatement changeStatusStmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionPool.getTwitchDatabase();
			for(IGuild guild : Bot.getInstance().getBot().getGuilds()) {
				if(Guild.guildMap.get(guild.getID()).getSpecialChannels().getTwitch().equals(""))
					continue;

				String sql = "SELECT twitch_username, status FROM `global_twitch` WHERE guild_id = ?";
				checkStatusStmt = conn.prepareStatement(sql);
				checkStatusStmt.setString(1, guild.getID());
				rs = checkStatusStmt.executeQuery();
				while(rs.next()) {
					try {
						TwitchObject to = TwitchObject.forName(rs.getString(1), guild.getID());
						if(to.getStatus() != (rs.getInt(2) == 0 ? false : true)) {
							sql = "UPDATE `global_twitch` SET status = ? WHERE twitch_username = ? AND guild_id = ?";
							changeStatusStmt = conn.prepareStatement(sql);
							changeStatusStmt.setInt(1, (to.getStatus() == true ? 1 : 0));
							changeStatusStmt.setString(2, to.getTwitchUsername().toLowerCase());
							changeStatusStmt.setString(3, guild.getID());
							changeStatusStmt.execute();
							
							if(announce) {
								EmbedBuilder em = new EmbedBuilder();
								if(to.getStatus()) {
									em.withColor(Color.CYAN).withAuthorName
									(to.getTwitchUsername() + " is now streaming " + to.getStreamingGame() + " on Twitch.tv");
									em.withAuthorUrl("https://twitch.tv/" + to.getTwitchUsername());
									StringBuilder sb = new StringBuilder();
									sb.append(to.getStreamDescription()+"\n");
									sb.append("https://twitch.tv/" + to.getTwitchUsername());
									em.withDesc(sb.toString());
									em.withImage(to.getPreviewImage());
								} else {
									em.withColor(Color.CYAN).withTitle(to.getTwitchUsername() + " has stopped streaming on Twitch.tv");
								}
								MessageUtils.sendMessage(guild.getChannelByID(Guild.guildMap.get(guild.getID()).getSpecialChannels().getTwitch()), 
										em.build());
							}
						}

					} catch (NoAPIKeyException e) {
						// Handled before getting here
					} catch (BadUsernameException e) {
						// Won't happen unless user deletes or Twitch.tv API is giving 404s
						e.printStackTrace();
					} catch (IOException e) {
						// Twitch.tv API is having issues
						e.printStackTrace();
					}
				}

				SQLUtils.closeQuietly(rs);
				SQLUtils.closeQuietly(changeStatusStmt);
				SQLUtils.closeQuietly(checkStatusStmt);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(changeStatusStmt);
			SQLUtils.closeQuietly(checkStatusStmt);
			SQLUtils.closeQuietly(conn);
			long gap = TimeUnit.MILLISECONDS.toSeconds(Duration.between(start, Instant.now()).toMillis());
			if(Bot.getInstance().isDebug())
				LoggerFactory.getLogger(TwitchStreamJob.class).info("Checked Twitch.tv streams. Duration: {} seconds", gap);
			announce = true;
		}

	}

}
