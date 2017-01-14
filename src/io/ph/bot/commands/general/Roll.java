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
 * Role a die
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "roll",
		aliases = {"dice"},
		permission = Permission.NONE,
		description = "Roll a die! (or many dice with the format #d#)",
		example = "2d6"
		)
public class Roll implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em.withColor(Color.GREEN)
			.withTitle(msg.getAuthor().getDisplayName(msg.getGuild()) + " rolled " 
			+ (ThreadLocalRandom.current().nextInt(6) + 1) + " out of 6");
		} else if(contents.contains("d")) {
			String[] split = contents.split("d");
			if(split.length == 2 && Util.isInteger(split[0]) && Util.isInteger(split[1])) {
				int max = Integer.parseInt(split[0]) * Integer.parseInt(split[1]);
				int roll = ThreadLocalRandom.current().nextInt(Integer.parseInt(split[0]), max + 1);
				em.withColor(Color.GREEN)
				.withTitle(msg.getAuthor().getDisplayName(msg.getGuild()) + " rolled " 
						+ roll + " with a " + split[0] + "d" + split[1]);
			}
		} else {
			if(Util.isInteger(contents))
				em.withColor(Color.GREEN)
				.withTitle(msg.getAuthor().getDisplayName(msg.getGuild()) + " rolled "
						+ (ThreadLocalRandom.current().nextInt(Integer.parseInt(contents)) + 1) 
						+ " out of " + Integer.parseInt(contents));
			else
				em = MessageUtils.commandErrorMessage(msg, 
						"roll", "[#]", "*[#]* - how large the die is (leave # blank to default to 6)\n"
								+ "You can also use multiple dice, such as 2d6");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
