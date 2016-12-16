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
 * Magic eight ball for a response
 * @author Paul
 */
@CommandData (
		defaultSyntax = "eightball",
		aliases = {"magicball"},
		permission = Permission.NONE,
		description = "Ask the magic eight ball a question",
		example = "Is this thing rigged?"
		)
public class EightBall implements Command {
	String[] responses = new String[]
			{
				"It is certain",
				"It is decidedly so",
				"Without a doubt",
				"Yes, definitely",
				"You may rely on it",
				"As I see it, yes",
				"Most likely",
				"Outlook good",
				"Yes",
				"Signs point to yes",
				
				"Reply hazy, try again",
				"Ask again later",
				"Better not tell you now",
				"Cannot predict now",
				"Concentrate and ask again",
				
				"Don't count on it",
				"My reply is no",
				"My sources say no",
				"Outlook not so good",
				"Very doubtful"
			};

	@Override
	public void run(IMessage msg) {
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			EmbedBuilder em = MessageUtils.commandErrorMessage(msg, "8ball", "question", "*question* - your question for the magic 8 ball");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		int r = new Random().nextInt(responses.length);
		em.withTitle(content).withDesc(responses[r]);
		Color c;
		if(r < 10)
			c = Color.GREEN;
		else if(r >= 10 && r <= 14)
			c = Color.CYAN;
		else
			c = Color.RED;
		em.withColor(c);
		MessageUtils.sendMessage(msg.getChannel(), em.build());
		
	}
}

