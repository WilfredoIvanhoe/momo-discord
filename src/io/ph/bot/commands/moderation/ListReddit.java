package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.feed.RedditFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "listreddit",
		aliases = {"listredditfeeds"},
		permission = Permission.KICK,
		description = "List all reddit feeds for your server",
		example = "(no parameters)"
		)
public class ListReddit implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		List<RedditFeedObserver> thisGuilds = new ArrayList<RedditFeedObserver>();
		for(List<RedditFeedObserver> list : RedditEventListener.getFeed().values()) {
			for(RedditFeedObserver observer : list) {
				if(observer.getDiscoChannel() != null && observer.getDiscoChannel().getGuild().equals(msg.getGuild())) {
					thisGuilds.add(observer);
				}
			}
		}
		if(thisGuilds.isEmpty()) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Your server does not have any reddit feeds setup");
		} else {
			Collections.sort(thisGuilds, (f, s) -> {
				return f.getDiscoChannel().getName().compareTo(s.getDiscoChannel().getName());
			});
			StringBuilder sb = new StringBuilder();
			String prevChannelName = "";
			for(RedditFeedObserver observer : thisGuilds) {
				if(!prevChannelName.equals(observer.getDiscoChannel().getName())) {
					if(prevChannelName.length() > 0)
						em.appendField("\\#" + prevChannelName, sb.toString(), false);
					sb.setLength(0);
					prevChannelName = observer.getDiscoChannel().getName();
				}
				sb.append(String.format("/r/%s\n", observer.getSubreddit()));
			}
			em.appendField("\\#" + prevChannelName, sb.toString(), false);
			em.withTitle("Reddit feed list");
			em.withColor(Color.CYAN);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
