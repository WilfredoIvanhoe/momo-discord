package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
/**
 * Unban a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "unban",
		aliases = {},
		permission = Permission.BAN,
		description = "Unban a user",
		example = "target"
		)
public class Unban implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		String content = Util.getCommandContents(msg);
		try {
			IUser target = Util.resolveBannedUserFromString(content, msg.getGuild());
			if(target == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No banned user found for **" + content + "**");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}

			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = ConnectionPool.getGlobalDatabaseConnection();
				String sql = "DELETE FROM `global_punish` WHERE muted_id = ? AND guild_id = ? AND type = ?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, target.getID());
				stmt.setString(2, msg.getGuild().getID());
				stmt.setString(3, "ban");
				stmt.execute();
			} catch(SQLException e) {
				em.withColor(Color.RED).withTitle("Error").withDesc("Something went wrong with the database.");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				e.printStackTrace();
				return;
			} finally {
				SQLUtils.closeQuietly(stmt);
				SQLUtils.closeQuietly(conn);
			}
			msg.getGuild().pardonUser(target.getID());
			em.withColor(Color.GREEN).withTitle("Success");
			em.withDesc(target.getName() + " has been unbanned").withTimestamp(System.currentTimeMillis()).withFooterText("Local time");
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permission to unban users");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
			return;
		} catch (RateLimitException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Rate limit! Try again soon :(");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
			return;
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something went very wrong...");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
			return;
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
