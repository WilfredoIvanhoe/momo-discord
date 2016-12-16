package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RoleBuilder;

/**
 * Setup the muted role
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "setup",
		aliases = {"initialize"},
		permission = Permission.MANAGE_SERVER,
		description = "Perform initial setup by creating the muted role and initializing the guild",
		example = "(no parameters)"
		)
public class Setup implements Command {

	@Override
	public void run(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		EmbedBuilder em = new EmbedBuilder();
		if((!g.getMutedRoleId().equals("") || g.getMutedRoleId() != null)
				&& msg.getGuild().getRoleByID(g.getMutedRoleId()) != null) {
			em.withColor(Color.RED);
			em.withTitle("Error");
			em.withDesc("Looks like I'm already setup here...");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		IRole mutedRole = null;
		RoleBuilder rb = new RoleBuilder(msg.getGuild());
		rb.setMentionable(false);
		rb.withColor(new Color(217, 0, 90));
		rb.withName("muted");
		rb.withPermissions(Permissions.getDeniedPermissionsForNumber(3212288));

		try {
			mutedRole = rb.build();
			g.setMutedRoleId(mutedRole.getID());
			for(IChannel channel : msg.getGuild().getChannels()) {
				channel.overrideRolePermissions(mutedRole, 
						Permissions.getDeniedPermissionsForNumber(0), Permissions.getAllowedPermissionsForNumber(2048));
			}
			em.withColor(Color.GREEN);
			em.withTitle("Success");
			em.withDesc("Setup your muted role and saved configuration");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Internal error: please report to bot owner");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like I don't have permissions to create the muted role"
					+ " or to overwrite channel permissions");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		} catch (RateLimitException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Rate limited! Please try again later");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		}

	}

}
