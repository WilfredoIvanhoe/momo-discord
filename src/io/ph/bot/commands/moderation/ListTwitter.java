package io.ph.bot.commands.moderation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.feed.TwitterFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "listtwitter",
		aliases = {"listtwitterfeeds", "twitterlist"},
		permission = Permission.KICK,
		description = "List all Twitter feeds for your server",
		example = "(no parameters)"
		)
public class ListTwitter implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		List<TwitterFeedObserver> thisGuilds = new ArrayList<TwitterFeedObserver>();
		for(List<TwitterFeedObserver> list : TwitterEventListener.getFeed().values()) {
			for(TwitterFeedObserver observer : list) {
				if(observer.getDiscoChannel() != null && observer.getDiscoChannel().getGuild().equals(msg.getGuild())) {
					thisGuilds.add(observer);
				}
			}
		}
		if(thisGuilds.isEmpty()) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Your server does not have any Twitter feeds setup");
		} else {
			Collections.sort(thisGuilds, (f, s) -> {
				return f.getDiscoChannel().getName().compareTo(s.getDiscoChannel().getName());
			});
			StringBuilder sb = new StringBuilder();
			String prevChannelName = "";
			for(TwitterFeedObserver observer : thisGuilds) {
				if(observer.getDiscoChannel() == null)
					continue;
				if(!prevChannelName.equals(observer.getDiscoChannel().getName())) {
					if(prevChannelName.length() > 0)
						em.appendField("\\#" + prevChannelName, sb.toString(), false);
					sb.setLength(0);
					prevChannelName = observer.getDiscoChannel().getName();
				}
				sb.append(observer.getTwitterHandle() + "\n");
			}
			em.appendField("\\#" + prevChannelName, sb.toString(), false);
			em.withTitle("Twitter feed list");
			em.withColor(Color.CYAN);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
