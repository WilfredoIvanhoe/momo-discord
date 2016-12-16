package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Pick between options separated by delimiter "or"
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "pick",
		aliases = {"choose"},
		permission = Permission.NONE,
		description = "Pick between multiple choices",
		example = "play games or sleep or do homework"
		)
public class Pick implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String[] splitMessage = Util.combineStringArray(Util.removeFirstArrayEntry(msg.getContent().split(" "))).split(" or ");
		if(splitMessage.length < 2) {
			em = MessageUtils.commandErrorMessage(msg, "pick", "X or Y or Z", new String[]{"Use \"or\" between your choices"});
		} else {
			int rand = ThreadLocalRandom.current().nextInt(0, splitMessage.length);
			em.withTitle("I choose " + splitMessage[rand]).withColor(Color.CYAN);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
