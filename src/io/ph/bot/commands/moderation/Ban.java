package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.events.UserBanEvent;
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
 * Ban a user
 * Can ban for a temporary amount of time
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "ban",
		aliases = {"b"},
		permission = Permission.BAN,
		description = "Ban a user. Can be temporary by using the \"temp\" parameter",
		example = "temp 1w2d username"
		)
public class Ban implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		String t = Util.getCommandContents(msg);
		if(t.equals("") || (Util.getParam(msg).equalsIgnoreCase("temp") && t.split(" ").length < 3)) {
			em = MessageUtils.commandErrorMessage(msg, "ban", "[temp] [#w#d#h#m] username", 
					"*[temp]* - Temporarily ban a user for #w#d#h#m time",
					"*[#w#d#h#m]* - Can only be used when you enter *temp*\n"
					+ "\tFor example, `" + Util.getPrefixForGuildId(msg.getGuild().getID()) + "ban temp 1d20m username` lasts 1 day 20 minutes",
					"*username* - Username or user mention to ban");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		
		
		try {
			IUser target;
			em.withColor(Color.GREEN).withTitle("Success");
			Instant now = null;
			if(Util.getParam(msg).equalsIgnoreCase("temp")) {
				now = Util.resolveInstantFromString(Util.getParam(t));
				String contents = Util.getCommandContents(Util.getCommandContents(t));
				target = Util.resolveUserFromString(contents, msg.getGuild());
				if(target == null) {
					em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + contents + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				em.withDesc(target.getName() + " has been banned until...").withTimestamp(now.toEpochMilli()).withFooterText("Local time");
			} else {
				target = Util.resolveUserFromString(t, msg.getGuild());
				if(target == null) {
					em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + t + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				em.withDesc(target.getName() + " has been banned");
			}
			if(msg.getGuild().getBannedUsers().contains(target)) {
				em.withColor(Color.RED).withTitle("Error").withDesc(target.getName() + " is already banned");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			msg.getGuild().banUser(target);
			Bot.getInstance().getBot().getDispatcher().dispatch(new UserBanEvent(msg.getAuthor(), target, msg.getGuild()));
			if(Util.getParam(msg).equalsIgnoreCase("temp")) {
				Connection conn = null;
				PreparedStatement stmt = null;
				try {
					conn = ConnectionPool.getGlobalDatabaseConnection();
					String sql = "INSERT INTO `global_punish` (muted_id, muter_id, guild_id, unmute_time, type) VALUES (?,?,?,?,?)";
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, target.getID());
					stmt.setString(2, msg.getAuthor().getID());
					stmt.setString(3, msg.getGuild().getID());
					stmt.setString(4, now.toString());
					stmt.setString(5, "ban");
					stmt.execute();
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					SQLUtils.closeQuietly(stmt);
					SQLUtils.closeQuietly(conn);
				}
			}
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permission to do that. Check my ranking on the Roles list");
			e.printStackTrace();
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Ban failed. Check your spelling, probably");
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
