package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Get a rating for an input string
 * @author Paul
 */
@CommandData (
		defaultSyntax = "rate",
		aliases = {"ratewaifu"},
		permission = Permission.NONE,
		description = "Rate something (maybe someone's waifu)",
		example = "Tohsaka Rin"
		)
public class Rate implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		String s = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		if(s.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "rate", "something to rate", "*something to rate* - Anything to rate between 1 and 10");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		em.withTitle("Here's my verdict");
		em.withDesc("**" + scale(s.toLowerCase().hashCode(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 10) + "/10**");
		em.withColor(Color.CYAN);
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

	private long scale(long val, long low, long high, long lowScale, long highScale) {
		return (((highScale - lowScale) * (val - low) / (high - low))) + lowScale;
	}
}
