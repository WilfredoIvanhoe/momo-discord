package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Kick a user
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "kick",
		aliases = {"k"},
		permission = Permission.KICK,
		description = "Kick a user",
		example = "target"
		)
public class Kick implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		try {
			IUser target = Util.resolveUserFromMessage(msg);
			if(target == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No user found for **" + target + "**");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			msg.getGuild().kickUser(target);
			em.withColor(Color.GREEN).withTitle("Success").withDesc(target.getName() + " has been kicked");
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permission to do that");
			e.printStackTrace();
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Kick failed. Check your spelling, probably");
			e.printStackTrace();
		}
	}

}
