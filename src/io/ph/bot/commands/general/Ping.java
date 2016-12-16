package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Ping the bot and get a response
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "ping",
		aliases = {},
		permission = Permission.NONE,
		description = "Ping the bot for response time",
		example = "(no parameters)"
	)
public class Ping implements Command {
	String[] responses = {
			"Pong!",
			"It's not like I wanted to pong...",
			"Is this really necessary?",
			"I'm still here!",
			"Not going anywhere",
			"Polo. Oh, wait",
			"PongnoP",
			"Your waifu is trash",
			":^)"
	};
	@Override
	public void run(IMessage msg) {
		Long l = msg.getTimestamp().atZone(ZoneId.systemDefault()).toEpochSecond();
		LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
		int r = new Random().nextInt(responses.length);
		EmbedBuilder em = new EmbedBuilder();
		em.withColor(Color.CYAN).withDesc(responses[r]);
		em.withFooterText("Delay: " + (now.atZone(ZoneId.systemDefault()).toEpochSecond() - l) + "ms");
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
