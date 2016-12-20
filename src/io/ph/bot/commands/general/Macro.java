package io.ph.bot.commands.general;

import java.awt.Color;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoMacroFoundException;
import io.ph.bot.model.MacroObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Create, delete, search, call information, and call macros
 * A macro is a way of mapping a word or phrase to an output
 * This can be used for actions such as reaction images, or saving something for later use
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "macro",
		aliases = {"m"},
		permission = Permission.NONE,
		description = "Create, delete, edit, search, or get information on a macro\n"
				+ "A macro is a quick way to bind text or links to a shortcut",
		example = "create \"test macro\" contents\n"
				+ "delete test macro\n"
				+ "edit \"test macro\" new contents\n"
				+ "info test macro\n"
				+ "test macro"
		)
public class Macro implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.equals("")) {
			String prefix = Util.getPrefixForGuildId(msg.getGuild().getID());
			em = MessageUtils.commandErrorMessage(msg, "macro", "[create|delete|edit|search|info] name [contents]", 
					"*[create]* - Create a new macro. Format is `" + prefix + "macro create name contents`\n"
							+ "\tIf the macro name has spaces, surround it in \"\"",
							"*[delete]* - Delete a macro. Format is `" + prefix + "macro delete name`\n"
									+ "\tIf you are not a mod, you can only delete your own macros",
									"*[edit]* - Edit a macro. Format is `" + prefix + "macro edit name contents`"
									+ "\tIf the macro name has spaces, surround it in \"\". You can only edit your own macros",
									"*[search]* - Search for a macro. Format is `" + prefix + "macro search name`",
									"*[info]* - Display information on a macro",
									"*name* - Call a macro - Just `" + prefix + "macro name`");
			em.withFooterText("Parameters in brackets [] are optional");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}

		String param = Util.getParam(msg);
		if(param.equalsIgnoreCase("create")) {
			contents = Util.getCommandContents(contents);
			if(contents.equals("") || contents.split(" ").length < 2) {
				em = MessageUtils.commandErrorMessage(msg, "macro", "create *name contents*", 
						"You have designated to create a macro, but your command does not meet all requirements",
						"*name* - Name of the macro. If it is multi-worded, you can surround it in \"quotation marks\"",
						"*contents* - Contents of the macro");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			String[] resolved = resolveMacroNameAndContents(contents);
			MacroObject m = new MacroObject(msg.getAuthor().getName(), resolved[0], resolved[1],
					0, msg.getAuthor().getID(), msg.getGuild().getID());
			try {
				if(m.create()) {
					em.withTitle("Success").withColor(Color.GREEN).withDesc("Macro **" + resolved[0] + "** created");
				} else {
					em.withTitle("Error").withColor(Color.RED).withDesc("Macro **" + resolved[0] + "** already exists");
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		} else if(param.equalsIgnoreCase("delete")) {
			contents = Util.getCommandContents(contents);
			if(contents.equals("")) {
				em = MessageUtils.commandErrorMessage(msg, "macro", "delete *name*", 
						"You have designated to delete a macro, but your command does not meet all requirements",
						"*name* - Name of the macro. No quotation marks for multi-worded macros");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			try {
				MacroObject m = MacroObject.forName(contents, msg.getGuild().getID());
				if(m.delete(msg.getAuthor().getID())) {
					em.withTitle("Success").withColor(Color.GREEN).withDesc("Macro **" + contents + "** deleted");
				} else {
					em.withTitle("Error").withColor(Color.RED).withDesc("You cannot delete macro **" + contents + "**");
					em.withFooterText("Users can only delete their own macros");
				}
			} catch (NoMacroFoundException e) {
				em.withTitle("Error").withColor(Color.RED).withDesc("Macro **" + contents + "** does not exist");
			}
		} else if(param.equalsIgnoreCase("edit")) {
			contents = Util.getCommandContents(contents);
			if(contents.equals("")) {
				em = MessageUtils.commandErrorMessage(msg, "macro", "edit *name content*", 
						"You have designated to edit a macro, but your command does not meet all requirements",
						"*name* - Name of the macro. Need \"quotation marks\" for multi-worded macros",
						"*content* - Content of the macro");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			String[] resolved = resolveMacroNameAndContents(contents);
			try {
				MacroObject m = MacroObject.forName(resolved[0], msg.getGuild().getID());
				if(m.edit(msg.getAuthor().getID(), resolved[1])) {
					em.withTitle("Success").withColor(Color.GREEN).withDesc("Macro **" + resolved[0] + "** edited");
				} else {
					em.withTitle("Error").withColor(Color.RED).withDesc("You cannot edit macro **" + contents + "**");
					em.withFooterText("Users can only edit their own macros");
				}
			} catch (NoMacroFoundException e) {
				em.withTitle("Error").withColor(Color.RED).withDesc("Macro **" + resolved[0] + "** does not exist");
			}
		} else if(param.equalsIgnoreCase("search")) {
			contents = Util.getCommandContents(contents);
			if(contents.equals("")) {
				em = MessageUtils.commandErrorMessage(msg, "macro", "search *[name|user]*", 
						"You have designated to search for a macro, but your command does not meet all requirements",
						"*name* - Name of the macro to search for. No quotation marks needed for multi-word macros",
						"*user* - An @ mention of a user to search for");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			String[] result;
			StringBuilder sb = new StringBuilder();
			// Search mentions a user
			if(msg.getMentions().size() == 1) {
				if((result = MacroObject.searchByUser(msg.getMentions().get(0).getID(), msg.getGuild().getID())) != null) {
					em.withTitle("Search results for user " + msg.getMentions().get(0).getDisplayName(msg.getGuild()))
						.withColor(Color.GREEN);
					int i = 0;
					for(String s : result) {
						if(i++ == 75) {
							em.withFooterText("Search limited to 75 results");
							break;
						}
						sb.append(s + ", ");
					}
					sb.setLength(sb.length() - 2);
					em.withDesc(sb.toString());
				} else {
					em.withTitle("No macros found").withColor(Color.RED).withDesc("No results for user **" 
							+ msg.getMentions().get(0).getDisplayName(msg.getGuild()) + "**");
				}
			} else {
				if((result = MacroObject.searchForName(contents, msg.getGuild().getID())) != null) {
					em.withTitle("Search results for " + contents).withColor(Color.GREEN);
					int i = 0;
					for(String s : result) {
						if(i++ == 75) {
							em.withFooterText("Search limited to 75 results");
							break;
						}
						sb.append(s + ", ");
					}
					sb.setLength(sb.length() - 2);
					em.withDesc(sb.toString());
				} else {
					em.withTitle("No macros found").withColor(Color.RED).withDesc("No results for **" 
							+ contents + "**");
				}
			}
		} else if(param.equalsIgnoreCase("info")) {
			contents = Util.getCommandContents(contents);
			if(contents.equals("")) {
				em = MessageUtils.commandErrorMessage(msg, "macro", "info *name*", 
						"You have designated to search for a macro, but your command does not meet all requirements",
						"*name* - Name of the macro to display info for. No quotation marks needed for multi-word macros");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			try {
				MacroObject m = MacroObject.forName(contents, msg.getGuild().getID());
				em.withTitle("Information on " + contents);
				IUser u = msg.getGuild().getUserByID(m.getUserId());
				em.appendField("Creator", u == null ? m.getFallbackUsername() : u.getDisplayName(msg.getGuild()), true);
				em.appendField("Hits", m.getHits()+"", true);
				em.appendField("Date created", m.getDate().toString(), true);
				em.withColor(Color.GREEN);
				//sb.append(Bot.getInstance().getBot().getGuil)
			} catch (NoMacroFoundException e) {
				em.withTitle("Error").withColor(Color.RED).withDesc("Macro **" + contents + "** does not exist");
			}
		} else {
			try {
				MacroObject m = MacroObject.forName(contents, msg.getGuild().getID(), true);
				MessageUtils.sendMessage(msg.getChannel(), m.getMacroContent());
				return;
			} catch (NoMacroFoundException e) {
				em.withTitle("Error").withColor(Color.RED).withDesc("Macro **" + contents + "** does not exist");
			}
		}

		MessageUtils.sendMessage(msg.getChannel(), em.build());

	}

	/**
	 * Resolve macro name and contents from a create statement
	 * This works to involve quotations around a spaced macro name
	 * @param s The parameters of a create statement - The contents past the $macro create bit
	 * @return Two index array: [0] is the macro name, [1] is the contents
	 * Prerequisite: s.split() must have length of >= 2
	 */
	private String[] resolveMacroNameAndContents(String s) {
		String[] toReturn = new String[2];
		if(s.contains("\"") && StringUtils.countMatches(s, "\"") > 1) {
			int secondIndexOfQuotes = s.indexOf("\"", s.indexOf("\"") + 1);
			toReturn[0] = s.substring(s.indexOf("\"") + 1, secondIndexOfQuotes);
			toReturn[1] = s.substring(secondIndexOfQuotes + 2);
		} else {
			toReturn[0] = s.split(" ")[0];
			toReturn[1] = Util.getCommandContents(s);
		}
		return toReturn;
	}
}
