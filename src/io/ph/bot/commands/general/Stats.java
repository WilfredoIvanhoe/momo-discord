package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.MacroObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Basic, harmless stats
 * @author Paul
 * TODO: Expand on this
 */
@CommandData (
		defaultSyntax = "stats",
		aliases = {},
		permission = Permission.NONE,
		description = "Display stats for the server",
		example = "(no parameters)"
		)
public class Stats implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle(msg.getGuild().getName());
		em.appendField("Users", msg.getGuild().getUsers().size() + "", true);
		em.appendField("Creation Date", msg.getGuild().getCreationDate().format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.toString(), true);
		em.appendField("Server ID", msg.getGuild().getID(), true);
		Object[] topMacro = null;
		if((topMacro = MacroObject.topMacro(msg.getGuild().getID())) != null)
				em.appendField("Top macro", "**" + topMacro[1] + "** by **"
						+ msg.getGuild().getUserByID((String) topMacro[2]).getDisplayName(msg.getGuild()) 
						+ "**: " + topMacro[0] + " hits", false);
		em.withColor(Color.CYAN);
		MessageUtils.sendMessage(msg.getChannel(), em.build());

	}

}
