package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.events.UserUnmutedEvent;
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
 * Unmute a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "unmute",
		aliases = {},
		permission = Permission.KICK,
		description = "Remove the muted role from a user",
		example = "target"
		)
public class Unmute implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		if(Util.getCommandContents(msg).isEmpty()) {
			em.withTitle("Error")
			.withColor(Color.RED)
			.withDesc("No target specified");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		String content = Util.getCommandContents(msg);
		if(Guild.guildMap.get(msg.getGuild().getID()).getMutedRoleId().equals("")) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like this server doesn't have a designated muted role.\n"
					+ "You can generate one automatically by running `" + Util.getPrefixForGuildId(msg.getGuild().getID()) + "setup` if you have"
					+ "the Manage Servers permission");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		IRole role = msg.getGuild().getRoleByID(Guild.guildMap.get(msg.getGuild().getID()).getMutedRoleId());
		IUser target = Util.resolveUserFromMessage(msg);
		if(target == null) {
			em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + content + "**");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(!target.getRolesForGuild(msg.getGuild()).contains(role)) {
			em.withColor(Color.RED).withTitle("Error").withDesc(target.getName() + " is not muted");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = ConnectionPool.getGlobalDatabaseConnection();
				String sql = "DELETE FROM `global_punish` WHERE muted_id = ? AND guild_id = ? AND type = ?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, target.getID());
				stmt.setString(2, msg.getGuild().getID());
				stmt.setString(3, "mute");
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
			target.removeRole(role);
			em.withColor(Color.GREEN).withTitle("Success");
			em.withDesc(target.getName() + " has been unmuted").withTimestamp(System.currentTimeMillis()).withFooterText("Local time");
			Bot.getInstance().getBot().getDispatcher().dispatch(new UserUnmutedEvent(target, msg.getGuild()));
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permission to assign roles");
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
