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
 * Change server's command prefix
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "changeprefix",
		aliases = {"changecommandprefix", "commandprefix"},
		permission = Permission.MANAGE_SERVER,
		description = "Change the server's command prefix",
		example = "#"
		)
public class ChangeCommandPrefix implements Command {
	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "changeprefix", "new-prefix", "*new-prefix* - The new command prefix you are changing to");
			em.withFooterText("The command prefix is the special character you have to use to call my commands");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(contents.contains(" ")) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Cannot have spaces in your command prefix");
			return;
		}
		Guild.guildMap.get(msg.getGuild().getID()).getGuildConfig().setCommandPrefix(contents);
		em.withColor(Color.GREEN).withTitle("Success").withDesc("Changed command prefix to " + contents)
		.withTimestamp(System.currentTimeMillis());
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}


}
