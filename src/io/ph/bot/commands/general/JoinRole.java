package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandSyntax;
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
 * User join a role that is designated as joinable by administrators
 * @author Paul
 *
 */
@CommandSyntax (
		defaultSyntax = "joinrole",
		aliases = {"addto"},
		permission = Permission.NONE,
		description = "Assign yourself a designated joinable role",
		example = "role-name"
		)
public class JoinRole implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.getCommandContents(msg);
		if(role.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "joinrole", "role-name", 
					"*role-name* - Name of the role you want to join");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		for(IRole r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role) 
					&& Guild.guildMap.get(msg.getGuild().getID()).isJoinableRole(r.getID())) {
				if(msg.getAuthor().getRolesForGuild(msg.getGuild()).contains(r)) {
					em.withColor(Color.CYAN).withTitle("Hmm...").withDesc("You're already in this role!");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
				try {
					msg.getAuthor().addRole(r);
					em.withColor(Color.GREEN).withTitle("Success").withDesc("You are now in the role **" + role + "**");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				} catch (MissingPermissionsException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Looks like I don't have permissions to assign roles. Check the hierarchy!");
					e.printStackTrace();
					return;
				} catch (RateLimitException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Rate limit :( Try again soon");
					e.printStackTrace();
					return;
				} catch (DiscordException e) {
					MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Something went wayyyy wrong");
					e.printStackTrace();
					return;
				}
			}
		}
		em.withColor(Color.RED).withTitle("Error").withDesc("That role doesn't exist or isn't joinable");
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
