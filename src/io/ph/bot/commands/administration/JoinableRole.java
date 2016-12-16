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
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RoleBuilder;

/**
 * Create a role that a user can then join in. If it already exists, use that instead
 * Role can be created already. If not, this creates a blank role and adds it to the list
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "joinablerole",
		aliases = {"optionalrole", "optinrole", "enablerole"},
		permission = Permission.MANAGE_ROLES,
		description = "Designate a joinable role\n"
				+ "Create or designate a pre-existing role as joinable. Users can then join it with the joinrole command",
		example = "role-to-join"
		)
public class JoinableRole implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String role = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" ")));
		for(IRole r : msg.getGuild().getRoles()) {
			if(r.getName().equalsIgnoreCase(role)) {
				if(Guild.guildMap.get(msg.getGuild().getID()).addJoinableRole(r.getID()))
					em.withColor(Color.GREEN).withTitle("Success").withDesc("**" + role + "** is now joinable")
					.withTimestamp(System.currentTimeMillis());
				else
					em.withColor(Color.CYAN).withTitle("Hmm...").withDesc("**" + role + "** is already joinable")
					.withTimestamp(System.currentTimeMillis());
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
		}
		RoleBuilder r = new RoleBuilder(msg.getGuild());
		r.withName(role);
		try {
			IRole newRole = r.build();
			Guild.guildMap.get(msg.getGuild().getID()).addJoinableRole(newRole.getID());
			em.withColor(Color.GREEN).withTitle("Created new role").withDesc("**" + role + "** is now joinable")
			.withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (MissingPermissionsException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "It looks like I can't create a role for you.\nI'm missing some permissions!");
			e.printStackTrace();
		} catch (RateLimitException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Rate limits :( Try again soon");
			e.printStackTrace();
		} catch (DiscordException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Something went wrong!");
			e.printStackTrace();
		}
	}

}
