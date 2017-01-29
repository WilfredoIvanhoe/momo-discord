package io.ph.bot.commands.owner;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

@CommandData (
		defaultSyntax = "update",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Start an update timer in the status to say \"Restart in n\" where n is minutes. Doesn't actually kill the bot at 0",
		example = "5"
		)
public class Update implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		try {
			StatusChangeJob.commenceUpdateCountdown(Integer.parseInt(Util.getCommandContents(msg)));
			MessageUtils.sendMessage(msg.getChannel(), "Set a timer for " + Util.getCommandContents(msg));
		} catch(NumberFormatException e) {
			MessageUtils.sendMessage(msg.getChannel(), Util.getCommandContents(msg) + " is not a valid integer");
		}
	}

}
