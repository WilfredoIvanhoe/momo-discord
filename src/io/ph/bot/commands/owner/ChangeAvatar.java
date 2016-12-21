package io.ph.bot.commands.owner;

import java.io.File;
import java.net.URL;

import io.ph.bot.Bot;
import io.ph.bot.State;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

@CommandData (
		defaultSyntax = "changeavatar",
		aliases = {"avatar"},
		permission = Permission.BOT_OWNER,
		description = "Upload a discord attachment and change the avatar to said attachment",
		example = "(image attachment)"
		)
public class ChangeAvatar implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		if(msg.getContent().contains("reset")) {
			State.changeBotAvatar(new File("resources/avatar/" + Bot.getInstance().getAvatar()));
			return;
		}
		if(msg.getAttachments().isEmpty())
			return;
		File f = new File("resources/avatar/tempava.png");
		try {
			Util.saveFile(new URL(msg.getAttachments().get(0).getUrl()), f);
			State.changeBotAvatar(f);
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
