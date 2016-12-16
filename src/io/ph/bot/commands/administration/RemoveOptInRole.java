package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Remove role from joinable role list for server
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "removejoinablerole",
		aliases = {"removeoptinrole", "disablerole"},
		permission = Permission.MANAGE_ROLES,
		description = "Disable a joinable role.\n"
				+ "Designate a role that cannot be joined. Note: This will not remove users who are already in that role, "
				+ "and they cannot leave the role of their own volition",
		example = "role-to-remove"
		)
public class RemoveOptInRole implements Command {
	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		em.withColor(Color.RED).withTitle("Error").withDesc("**" + role + " is not a joinable role");
		for(IRole r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role)
					&& Guild.guildMap.get(msg.getGuild().getID()).removeJoinableRole(r.getID())) {
				em.withColor(Color.GREEN).withTitle("Success").withDesc("**" + role + "** is not joinable anymore");
				break;
			}
		}
		em.withTimestamp(System.currentTimeMillis());
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
