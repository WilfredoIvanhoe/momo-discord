package io.ph.bot.commands.general;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Make the bot say something
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "say",
		aliases = {"echo"},
		permission = Permission.NONE,
		description = "Have the bot say something",
		example = "Hi, it's me!"
		)
public class Say implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		try {
			msg.delete();
			MessageUtils.sendMessage(msg.getChannel(), Util.getCommandContents(msg));
		} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
			e.printStackTrace();
		}

	}


}
