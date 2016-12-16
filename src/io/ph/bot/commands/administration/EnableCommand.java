package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandSyntax;
import io.ph.bot.exception.BadCommandNameException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Enable a command
 * @author Paul
 */
@CommandSyntax (
		defaultSyntax = "enable",
		aliases = {"enablecommand"},
		permission = Permission.MANAGE_CHANNELS,
		description = "Enable a command.\n"
				+ "Only normal user commands can be enabled. You can see the status by using the commandstatus command",
		example = "macro"
		)
public class EnableCommand implements Command {

	@Override
	public void run(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		String content = Util.getCommandContents(msg);
		if(content.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "enable", "command", 
					"*command* - Command you want to enable",
					"If you need valid options, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "commandstatus");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			if(g.enableCommand(content)) {
				em.withColor(Color.GREEN).withTitle("Success").withDesc("**" + content + "** has been enabled");
			} else {
				em.withColor(Color.CYAN).withTitle("Hmm...").withDesc("**" + content + "** is already enabled");
			}
		} catch(BadCommandNameException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("**" + content + "** is not a valid command.\n"
					+ "If you need valid options, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "commandstatus");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
