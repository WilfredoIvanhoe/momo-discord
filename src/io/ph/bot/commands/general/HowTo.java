package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "howto",
		aliases = {"gettingstarted"},
		permission = Permission.NONE,
		description = "PM a starting guide to the user",
		example = ""
		)
public class HowTo implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.withColor(Color.CYAN);
		em.withTitle("Getting started with Momo");
		em.withDesc("This is a quick introuduction to using my features and commands. "
				+ "It's by no means comprehensive, but it should be a good starting point!");
		em.appendField("About", "I am an open-source bot written in the Java programming language. "
				+ "You can find my source code at http://momobot.io/github.", true);
		em.appendField("Commands", String.format("Commands are separated into permissions native to Discord. "
				+ "These permissions include Manage server, Manage roles, and all the way down to Kick. "
				+ "Any commands that are usable by general users can be disabled by admins\n"
				+ "You can find a full listing of commands at %shelp", 
				Util.getPrefixForGuildId(msg.getGuild().getID())), false);
		em.appendField("Live updates", 
				String.format("I can provide live updates from various websites, including Reddit, Twitter, and Twitch.tv. "
				+ "To set this up, make sure you have the correct roles - then you can use %stwitchchannel, %<sreddit, and %<stwitter. "
				+ "From there, it's simple to follow the instructions to get live feeds from your favorite sources.",
				Util.getPrefixForGuildId(msg.getGuild().getID())), false);
		em.appendField("Music", String.format("Did you know I can also play music from various sources? "
				+ "You can use the %smusic command to queue up songs from direct attachments, links, and YouTube videos. "
				+ "You can also use the command with the search result from a %<syoutube or %<stheme command. "
				+ "Make sure you do %<ssetupmusic beforehand so I can create the correct voice channel!", 
				Util.getPrefixForGuildId(msg.getGuild().getID())), false);
		em.withFooterText(String.format("Current version: %s", Bot.BOT_VERSION));
		MessageUtils.sendPrivateMessage(msg.getAuthor(), em.build());
	}

}
