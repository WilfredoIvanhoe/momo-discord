package io.ph.bot.commands.administration;

import java.awt.Color;
import java.util.Map.Entry;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Check on enabled/disabled commands
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "commandstatus",
		aliases = {"status"},
		permission = Permission.MANAGE_SERVER,
		description = "Check all toggleable commands, listing which are enabled and which are disabled",
		example = "(no parameters)"
		)
public class CommandStatus implements Command {

	@Override
	public void run(IMessage msg) {
		String contents = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		if(contents.length() > 0) {
			if(Guild.guildMap.get(msg.getGuild().getID()).validCommandToEdit(msg.getContent())) {
				em.withColor(Color.RED).withTitle(contents + " is not a valid command");
			} else {
				em.withColor(Guild.guildMap.get(msg.getGuild().getID()).getCommandStatus(contents) == true ? Color.GREEN : Color.RED);
				em.withTitle("Status of " + Util.getPrefixForGuildId(msg.getGuild().getID()) + contents);
				em.withDesc(Guild.guildMap.get(msg.getGuild().getID()).getCommandStatus(contents) == true ? "Enabled" : "Disabled");
			}
		} else {
			em.withColor(Color.CYAN).withTitle("Status of all user commands");
			StringBuilder sb = new StringBuilder();
			StringBuilder sbDis = new StringBuilder();
			sb.append("**Enabled**: ");
			sbDis.append("**Disabled**: ");
			for(Entry<String, Boolean> entry : Guild.guildMap.get(msg.getGuild().getID()).getCommandStatus().entrySet()) {
				if(entry.getValue())
					sb.append(entry.getKey() + ", ");
				else
					sbDis.append(entry.getKey() + ", ");
			}
			sb.setLength(sb.length() - 2);
			sbDis.setLength(sbDis.length() - 2);
			sb.append("\n").append(sbDis.toString());
			em.withDesc(sb.toString());
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
