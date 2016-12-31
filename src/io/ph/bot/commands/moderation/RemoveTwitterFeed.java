package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Remove a registration for this server from a reddit feed
 * @author Paul
 */
@CommandData (
		defaultSyntax = "removetwitter",
		aliases = {"untwitter"},
		permission = Permission.KICK,
		description = "Remove a twitter feed from this channel",
		example = "FF_XIV_EN"
		)
public class RemoveTwitterFeed implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "removetwitter", "twitter-handle",
					"**twitter-handle** - Twitter to unregister your feed from.",
					"If you want to list all your feed subscriptions, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "listtwitter");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			User u = TwitterEventListener.twitterClient.lookupUsers(new String[]{contents}).get(0);
			if(TwitterEventListener.removeTwitterFeed(u.getId(), msg.getGuild())) {
				em.withColor(Color.GREEN).withTitle("Success").withDesc("Removed **" + contents + "** from your Twitter feeds");
			} else {
				em.withColor(Color.RED).withTitle("Error").withDesc("**" + contents + "** is not a current Twitter feed...");
			}
		} catch(TwitterException e) {
			em.withColor(Color.RED).withTitle("Error");
			if(e.getErrorCode() == 17) {
				em.withDesc(String.format("**%s** is not a valid Twitter username", contents));
			} else {
				em.withDesc("Something went wrong accessing Twitter!");
			}
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
