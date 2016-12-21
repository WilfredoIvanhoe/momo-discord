package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Get help with commands
 * @author Paul
 */
@CommandData (
		defaultSyntax = "help",
		aliases = {},
		permission = Permission.NONE,
		description = "Either list all commands or get help for one",
		example = "(optional command name)"
		)
public class Help implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		String command = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		if(command.length() > 0) {
			//Help about a specific command
			Command c;
			if((c = CommandHandler.getCommand(command)) == null) {
				em.withTitle("Invalid command").withColor(Color.RED).withDesc(command + " is not a valid command");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withTitle(command);
			em.withColor(Color.CYAN);
			em.appendField("Primary Command", c.getDefaultCommand(), true);
			String[] aliases = c.getAliases();
			if(aliases.length > 0) {
				em.appendField("Aliases", 
						Arrays.toString(aliases).substring(1, Arrays.toString(aliases).length() - 1) + "\n", true);
			}
			em.appendField("Permissions", c.getPermission().toString(), true).appendField("Description", c.getDescription(), false);
			em.appendField("Example", Util.getPrefixForGuildId(msg.getGuild().getID()) + c.getDefaultCommand()
			+ " " + c.getExample(), false);
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} else {			
			List<Command> coll = (List<Command>) CommandHandler.getAllCommands();
			Collections.sort(coll, (f, s) -> {
				if(f.getPermission().compareTo(s.getPermission()) != 0)
					return f.getPermission().compareTo(s.getPermission());
				return f.getDefaultCommand().compareTo(s.getDefaultCommand());
			});
			StringBuilder sb = new StringBuilder();
			String prevPermissions = "";
			for(Command c : coll) {
				if(!prevPermissions.equals(c.getPermission().toString())) {
					if(prevPermissions.length() > 0)
						em.appendField(prevPermissions, sb.toString(), false);
					sb.setLength(0);
					prevPermissions = c.getPermission().toString();
				}
				sb.append(c.getDefaultCommand() + "\n");
			}
			em.appendField(prevPermissions, sb.toString(), false);
			em.withTitle("Command list");
			em.withColor(Color.CYAN);
			em.withFooterText("PM me a command name to get more information");
			MessageUtils.sendPrivateMessage(msg.getAuthor(), em.build());
		}

	}

}
