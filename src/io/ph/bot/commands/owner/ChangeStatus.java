package io.ph.bot.commands.owner;

import io.ph.bot.State;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandSyntax;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

@CommandSyntax (
		defaultSyntax = "changestatus",
		aliases = {"status"},
		permission = Permission.BOT_OWNER,
		description = "Change bot's game status",
		example = "New status"
		)
public class ChangeStatus implements Command {

	@Override
	public void run(IMessage msg) {
		State.changeBotStatus(Util.getCommandContents(msg));
	}

}
