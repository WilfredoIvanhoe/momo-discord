package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Leave role designated as joinable
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "leaverole",
		aliases = {},
		permission = Permission.NONE,
		description = "Leave a role that is designated as joinable",
		example = "role-name"
		)
public class LeaveRole implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		if(role.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "leaverole", "role-name", 
					"*role-name* - Name of the role you want to leave");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		for(IRole r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role)) {
				if(!Guild.guildMap.get(msg.getGuild().getID()).isJoinableRole(r.getID())) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "The role **" + role + "** is not a valid role");
					return;
				}
				if(!msg.getGuild().getRolesForUser(msg.getAuthor()).contains(r)) {
					em.withColor(Color.CYAN).withTitle("Hmm...").withDesc("Looks like you aren't in this role");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				
				try {
					msg.getAuthor().removeRole(r);
					em.withColor(Color.GREEN).withTitle("Success").withDesc("You are now removed from the role **" + role + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				} catch (MissingPermissionsException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "I don't have permissions to remove roles. Check the hierarchy!");
					e.printStackTrace();
					return;
				} catch (RateLimitException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Rate limits :( Try again soon!");
					e.printStackTrace();
					return;
				} catch (DiscordException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Something went really bad");
					e.printStackTrace();
					return;
				}
			}
			
		}
		em.withColor(Color.RED).withTitle("Error").withDesc("That role doesn't exist or isn't leaveable");
		MessageUtils.sendMessage(msg.getChannel(), em.build());

	}

}
