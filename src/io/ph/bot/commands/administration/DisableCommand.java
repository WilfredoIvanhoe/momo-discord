package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadCommandNameException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Disable a command
 * @author Paul
 */
@CommandData (
		defaultSyntax = "disable",
		aliases = {"disablecommand"},
		permission = Permission.MANAGE_ROLES,
		description = "Disable a command.\n"
				+ "Only normal user commands can be disabled. You can see the status by using the commandstatus command. "
				+ "Use \"disable all\" to disable all user commands",
		example = " macro"
		)
public class DisableCommand implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "disable", "command", 
					"*command* - Command you want to disable",
					"If you need valid options, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "commandstatus");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(content.equals("all")) {
			g.disableAllCommands();
			em.withColor(Color.GREEN).withTitle("Success").withDesc("All commands have been disabled");
			return;
		}
		try {
			if(g.disableCommand(content)) {
				em.withColor(Color.GREEN).withTitle("Success").withDesc("**" + content + "** has been disabled");
			} else {
				em.withColor(Color.CYAN).withTitle("Hmm...").withDesc("**" + content + "** is already disabled");
			}
		} catch(BadCommandNameException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("**" + content + "** is not a valid command.\n"
					+ "If you need valid options, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "commandstatus");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
