package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Change welcome message the server sends when a new person is joined
 * $user$ and $server$ are replaced with the new user and the server respectively
 * Note: this will not send if there is no welcome channel set
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "changewelcome",
		aliases = {"welcomemessage", "changewelcomemessage"},
		permission = Permission.MANAGE_SERVER,
		description = "Change the server's welcome message. Use $user$ and $server$ to replace with the new user and the server name, respectively",
		example = "Welcome $user$ to $server$!"
		)
public class ChangeWelcomeMessage implements Command {
	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		Guild.guildMap.get(msg.getGuild().getID()).getGuildConfig().setWelcomeMessage(contents);
		em.withColor(Color.GREEN).withTimestamp(System.currentTimeMillis());
		if(contents.equals(""))
			em.withTitle("Reset welcome message");
		else
			em.withTitle("Changed welcome message").withDesc(contents);
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
