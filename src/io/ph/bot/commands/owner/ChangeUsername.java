package io.ph.bot.commands.owner;

import io.ph.bot.State;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Change bot username
 * @author Paul
 */
@CommandData (
		defaultSyntax = "changeusername",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Change bot username",
		example = "New Username"
		)
public class ChangeUsername implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		State.changeBotUsername(Util.getCommandContents(msg));
	}

}
