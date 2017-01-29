package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Information & intro
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "info",
		aliases = {"information"},
		permission = Permission.NONE,
		description = "Information on the bot",
		example = "(no parameters)"
		)
public class Info implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle("Hi, I'm " + Bot.getInstance().getBot().getOurUser().getDisplayName(msg.getGuild()))
		.ignoreNullEmptyFields()
		.withColor(Color.MAGENTA)
		.appendField("Repository", "<https://momobot.io/github>", true)
		.appendField("Help server", "<https://momobot.io/join>", true)
		.appendField("Invite link", Bot.getInstance().getBotInviteLink(), true)
		.appendField("Command list", "<https://momobot.io/commands.html>", true)
		.withDesc("I can do a lot of things! Too many to list here, though. Feel free to take a look "
				+ "through the links below, though, to get a quick rundown of my features")
		.withThumbnail(Bot.getInstance().getBot().getOurUser().getAvatarURL())
		.withFooterText(String.format("Version %s | Made with <3 by %s", 
				Bot.BOT_VERSION,
				"Kagumi"));
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
