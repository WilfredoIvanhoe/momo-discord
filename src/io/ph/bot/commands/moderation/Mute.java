package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.events.UserMutedEvent;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
/**
 * Mute a user
 * Can mute for a temporary amount of time
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "mute",
		aliases = {},
		permission = Permission.KICK,
		description = "Assign the Muted role to a user. Can be temporary by using the \"temp\" parameter",
		example = "temp 1d2h target"
		)
public class Mute implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		if(Guild.guildMap.get(msg.getGuild().getID()).getMutedRoleId().equals("")) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like this server doesn't have a designated muted role.\n"
					+ "You can generate one automatically by running `" + Util.getPrefixForGuildId(msg.getGuild().getID()) + "setup` if you have "
					+ "the Manage Servers permission");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}

		String t = Util.getCommandContents(msg);
		if(t.equals("") || (Util.getParam(msg).equalsIgnoreCase("temp") && t.split(" ").length < 3)) {
			em = MessageUtils.commandErrorMessage(msg, "mute", "[temp] [#w#d#h#m] username", 
					"*[temp]* - Temporarily mute a user for #w#d#h#m time",
					"*[#w#d#h#m]* - Can only be used when you enter *temp*\n"
							+ "\tFor example, `" + Util.getPrefixForGuildId(msg.getGuild().getID()) + "mute temp 1d20m username` lasts 1 day 20 minutes",
					"*username* - Username or user mention to mute");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}

		try {
			IUser target;
			em.withColor(Color.GREEN).withTitle("Success");
			Instant now = null;
			if(Util.getParam(msg).equalsIgnoreCase("temp")) {
				now = Util.resolveInstantFromString(Util.getParam(t));
				String targetS = Util.getCommandContents(Util.getCommandContents(t));
				target = Util.resolveUserFromMessage(targetS, msg.getGuild());
				if(target == null) {
					em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + targetS + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				em.withDesc(target.getName() + " has been muted until...").withTimestamp(now.toEpochMilli()).withFooterText("Local time");
			} else {
				target = Util.resolveUserFromMessage(msg, msg.getGuild());
				if(target == null) {
					em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + t + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				em.withDesc(target.getName() + " has been muted");
			}
			IRole targetRole = msg.getGuild().getRoleByID(Guild.guildMap.get(msg.getGuild().getID()).getMutedRoleId());
			if(target.getRolesForGuild(msg.getGuild()).contains(targetRole)) {
				em.withColor(Color.RED).withTitle("Error").withDesc(target.getName() + " is already muted!");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			target.addRole(targetRole);
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
					stmt.setString(5, "mute");
					stmt.execute();
				} catch(SQLException e) {
					if(e.getErrorCode() == 19) {
						em.withColor(Color.RED).withTitle("Error").withDesc(target.getName() + " is already under a temporary mute");
						MessageUtils.sendMessage(msg.getChannel(), em.build());
					} else
						e.printStackTrace();
					return;
				} finally {
					SQLUtils.closeQuietly(stmt);
					SQLUtils.closeQuietly(conn);
				}
			}
			Bot.getInstance().getBot().getDispatcher().dispatch(new UserMutedEvent(msg.getAuthor(), target, msg.getGuild()));
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permission to do that");
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
