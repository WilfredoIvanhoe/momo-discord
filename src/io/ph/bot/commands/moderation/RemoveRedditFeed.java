package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Remove a registration for this server from a reddit feed
 * @author Paul
 */
@CommandData (
		defaultSyntax = "removereddit",
		aliases = {"unreddit", "unsubreddit", "removeredditfeed"},
		permission = Permission.KICK,
		description = "Remove a reddit feed from this channel",
		example = "awwnime"
		)
public class RemoveRedditFeed implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "removereddit", "subreddit",
					"**subreddit** - Subreddit to unregister your feed from.",
					"If you want to list all your feed subscriptions, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "listreddit");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(RedditEventListener.removeRedditFeed(contents, msg.getGuild())) {
			em.withColor(Color.GREEN).withTitle("Success").withDesc("Removed /r/**" + contents + "** from your reddit feeds.\n"
					+ "Changes will take place in about 30 seconds");
		} else {
			em.withColor(Color.RED).withTitle("Error").withDesc("/r/**" + contents + "** is not a current feed...");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
