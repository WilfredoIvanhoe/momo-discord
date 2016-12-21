package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.Random;

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
		description = "Roll a dice!",
		example = "[optional #]"
		)
public class Roll implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		Random r = new Random();
		EmbedBuilder em;
		if(msg.getContent().split(" ").length == 1) {
			em = new EmbedBuilder().withColor(Color.GREEN)
					.withTitle(msg.getAuthor().getDisplayName(msg.getGuild()) + " rolled " + (r.nextInt(6) + 1) + " out of 6");
		} else {
			String arg = msg.getContent().split(" ")[1];
			if(Util.isInteger(arg))
				em = new EmbedBuilder().withColor(Color.GREEN).withAuthorIcon(msg.getAuthor().getAvatarURL())
				.withAuthorName(msg.getAuthor().getDisplayName(msg.getGuild()) + " rolled " + (r.nextInt(Integer.parseInt(arg)) + 1) 
						+ " out of " + Integer.parseInt(arg));
			else
				em = MessageUtils.commandErrorMessage(msg, "roll", "[#]", new String[]{"*[#]* - how large the die is (leave # blank to default to 6)"});
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
