package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.feed.TwitterFeedObserver;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import twitter4j.TwitterException;
import twitter4j.User;

@CommandData (
		defaultSyntax = "twitter",
		aliases = {"twitterfeed"},
		permission = Permission.KICK,
		description = "Subscribe to a Twitter account and get tweets delivered to this channel",
		example = "FF_XIV_EN"
		)
@ProceduralAnnotation (
		title = "Twitter feed",
		steps = {"Show images in updates?"}, 
		types = {StepType.YES_NO},
		breakOut = "finish"
		)
public class TwitterFeed extends ProceduralCommand implements Command {

	public TwitterFeed() {
		super(null);
	}
	public TwitterFeed(IMessage msg) {
		super(msg);
	}

	private EmbedBuilder em = new EmbedBuilder();
	@Override
	public void executeCommand(IMessage msg) {		
		if(TwitterEventListener.twitterClient == null) {
			//message
			return;
		}

		User u;
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "twitter", "twitter-handle",
					"**twitter-handle** - Twitter account to register your feed to.",
					"If you want to list all your feed subscriptions, do " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "listtwitter");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			u = TwitterEventListener.twitterClient.lookupUsers(new String[]{contents}).get(0);
			if(TwitterEventListener.getObserver(u.getId(), msg.getGuild()) != null) {
				if(TwitterEventListener.getObserver(u.getId(), msg.getGuild()).getDiscoChannel().equals(msg.getChannel())) {
					em.withColor(Color.RED).withTitle("Error").withDesc("The Twitter account **" + u.getScreenName() 
						+ "** is already feeding to this channel");
					MessageUtils.sendMessage(msg.getChannel(), em.build());
					return;
				} else {
					TwitterEventListener.removeTwitterFeed(u.getId(), msg.getGuild());
				}
			}
		} catch(TwitterException e) {
			em.withColor(Color.RED).withTitle("Error");
			if(e.getErrorCode() == 17) {
				em.withDesc(String.format("Twitter account **%s** does not exist", contents));
			} else {
				em.withDesc("Unspecified error");
			}
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		TwitterFeed instance = new TwitterFeed(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
		instance.addCache(u);
	}
	@Override
	public void finish() {
		IMessage msg = super.getStarter();
		User u = (User) super.getCache().get(0);
		int timeLeft = (new TwitterFeedObserver(msg.getChannel().getID(), u.getScreenName(), (boolean) super.getResponses().get(0)))
				.subscribe(u.getId());
		em.withColor(Color.CYAN);
		em.withTitle("Success");
		String when;
		if(timeLeft == -1)
			when = "within " + TwitterEventListener.DELAY;
		else
			when = "in " + timeLeft;
		em.withDesc("Subscribed to feeds from **" + u.getScreenName() + "**\nChanges will take effect " + when + " seconds");
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	
}
