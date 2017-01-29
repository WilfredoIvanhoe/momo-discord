package io.ph.bot.commands.general;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Send the invite link to this channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "invite",
		aliases = {},
		permission = Permission.NONE,
		description = "Send a link to invite me to a server",
		example = "(no parameters)"
		)
public class Invite implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		if(Bot.getInstance().getBotInviteLink() == null)
			return;
		MessageUtils.sendMessage(msg.getChannel(), Bot.getInstance().getBotInviteLink());
	}
}
