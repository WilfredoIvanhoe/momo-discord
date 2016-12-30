package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
/**
 * Prune x messages (default 40)
 * @author Paul
 */
@CommandData (
		defaultSyntax = "prune",
		aliases = {},
		permission = Permission.KICK,
		description = "Prune n messages (default 40, up to 75). Can prune a specific user's messages with a mention.\n",
		example = "25 @target\n"
				+ "50\n"
				+ "@target"
		)
public class Prune implements Command {
	private static final int MAX_PRUNE = 75;
	private static final int DEFAULT_PRUNE = 40;

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder().withTimestamp(System.currentTimeMillis());
		String t = Util.getCommandContents(msg);
		int i = 0;
		IUser target = null;
		List<IMessage> toPrune = new ArrayList<IMessage>();
		if(t.length() == 0) {
			// Prune the default amount
			for(IMessage m : msg.getChannel().getMessages()) {
				if(i++ == DEFAULT_PRUNE + 1)
					break;
				toPrune.add(m);
			}
			em.withColor(Color.GREEN).withTitle("Success").withDesc("Pruned " + DEFAULT_PRUNE + " messages");
		} else if(Util.isInteger(Util.getParam(msg))) {
			// User specified a number as the first parameter
			int num = Integer.parseInt(Util.getParam(msg)) > MAX_PRUNE ? MAX_PRUNE : Integer.parseInt(Util.getParam(msg));
			num = num < 1 ? 1 : num;
			if(t.split(" ").length > 1) {
				// User specified a target after the #
				if(msg.getMentions().size() == 0)
					target = Util.resolveUserFromMessage(t.substring(t.indexOf(" ") + 1), msg.getGuild());
				else
					target = msg.getMentions().get(0);
				if(target == null) {
					em.withColor(Color.RED).withTitle("Error").withDesc("User " + t.substring(t.indexOf(" ") + 1) + " does not exist");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				}
			}
			if(target == null) {
				// User didn't specify a target, just prune the #
				// Force delete the command message as well
				num++;
				for(IMessage m : msg.getChannel().getMessages()) {
					if(i++ == num)
						break;
					toPrune.add(m);
				}
				em.withColor(Color.GREEN).withTitle("Success").withDesc("Pruned " + (i - 2) + " messages");
			} else {
				// Target specified, only their messages
				int targetCounter = 0;
				for(IMessage m : msg.getChannel().getMessages()) {
					if(i++ == MAX_PRUNE || targetCounter == num)
						break;
					if(m.getAuthor().equals(target)) {
						targetCounter++;
						toPrune.add(m);
					}
				}
				em.withColor(Color.GREEN).withTitle("Success").withDesc("Pruned " + targetCounter + " of **" 
						+ target.getDisplayName(msg.getGuild()) + "**'s messages");
			}
		} else if((target = Util.resolveUserFromMessage(msg)) != null) {
			// User only specified a target
			int targetCounter = 0;
			for(IMessage m : msg.getChannel().getMessages()) {
				if(i++ == MAX_PRUNE + 1 || targetCounter == DEFAULT_PRUNE)
					break;
				if(m.getAuthor().equals(target)) {
					targetCounter++;
					toPrune.add(m);
				}
			}
			em.withColor(Color.GREEN).withTitle("Success").withDesc("Pruned " + targetCounter + " of **" 
					+ target.getDisplayName(msg.getGuild()) + "**'s messages");
		} else if(Util.isInteger(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1])) {
			// There's a possibility they did $prune username #
			String targ = Util.combineStringArray(Util.removeLastArrayEntry(Util.getCommandContents(msg).split(" ")));
			if(!msg.getMentions().isEmpty()) {
				target = msg.getMentions().get(0);
			} else {
				target = Util.resolveUserFromMessage(targ, msg.getGuild());
			}
			if(target != null) {
				int num = Integer.parseInt(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1]) > MAX_PRUNE ? 
						MAX_PRUNE : Integer.parseInt(msg.getContent().split(" ")[msg.getContent().split(" ").length - 1]);
				num = num < 1 ? 1 : num;
				int targetCounter = 0;
				for(IMessage m : msg.getChannel().getMessages()) {
					if(i++ == MAX_PRUNE || targetCounter == num)
						break;
					if(m.getAuthor().equals(target)) {
						targetCounter++;
						toPrune.add(m);
					}
				}
				em.withColor(Color.GREEN).withTitle("Success").withDesc("Pruned " + targetCounter + " of **" 
						+ target.getDisplayName(msg.getGuild()) + "**'s messages");
			}
		}
		try {
			msg.getChannel().getMessages().bulkDelete(toPrune);
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something broke. Are you sure there are messages to be pruned?");
			e.printStackTrace();
		} catch (RateLimitException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Rate limits! Try again soon...");
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have permissions to delete messages");
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
