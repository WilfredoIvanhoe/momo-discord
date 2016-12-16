package io.ph.bot.jobs;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Periodically remind users of things they set for the future
 * @author Paul
 *
 */
public class ReminderJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Instant start = Instant.now();
		//LocalDateTime now = LocalDateTime.now(); 
		Connection conn = null;
		PreparedStatement prep = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT user_id, guild_name, reminder, remind_time FROM `global_reminders`");
			while(rs.next()) {
				//LocalDateTime dueDate = LocalDateTime.parse(rs.getString(4));
				Instant dueDate = Instant.parse(rs.getString(4));
				if(start.isBefore(dueDate))
					continue;
				
				String userId = rs.getString(1);
				String guildName = rs.getString(2);
				String reminder = rs.getString(3);
				EmbedBuilder em = new EmbedBuilder();
				em.withColor(Color.CYAN).withTitle("Reminder from " + guildName);
				em.withDesc(reminder);
				em.withTimestamp(System.currentTimeMillis());
				MessageUtils.sendPrivateMessage(Bot.getInstance().getBot().getUserByID(userId), em.build());
				String sql = "DELETE FROM `global_reminders` WHERE user_id = ? AND remind_time = ?";
				prep = conn.prepareStatement(sql);
				prep.setString(1, userId);
				prep.setString(2, rs.getString(4));
				prep.execute();
				SQLUtils.closeQuietly(prep);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(prep);
			SQLUtils.closeQuietly(conn);
			long gap = TimeUnit.MILLISECONDS.toSeconds(Duration.between(start, Instant.now()).toMillis());
			if(Bot.getInstance().isDebug())
				LoggerFactory.getLogger(ReminderJob.class).info("Checked global reminders. Duration: {} seconds", gap);
		}

	}

}
