package io.ph.bot.commands.moderation;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.bot.model.feeds.RedditFeedObject;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Set or remove reddit feed from given channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "redditfeed",
		aliases = {},
		permission = Permission.KICK,
		description = "Set this channel as a reddit feed for a given subreddit",
		example = "askreddit"
		)
@ProceduralAnnotation (
		title = "Reddit feed",
		steps = {"Show all images in updates?"}, 
		types = {StepType.YES_NO},
		breakOut = "finish"
		)
public class RedditFeed extends ProceduralCommand implements Command {

	public RedditFeed(IMessage msg) {
		super(msg);
	}
	private Guild g;
	
	/**
	 * Necessary constructor to register to commandhandler
	 */
	public RedditFeed() {
		super(null);
	}

	@Override
	public void executeCommand(IMessage msg) {
		RedditFeed instance = new RedditFeed(msg);
		ProceduralListener.getInstance().addListener(msg.getAuthor(), instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void finish() {
		IMessage msg = super.getStarter();
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		String currentChannel = msg.getChannel().getID();
		String contents = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle("Success");
		em.withColor(Color.GREEN);
		em.withDesc(String.format("Registered **%s** for notifications", contents));
		// TODO: check if legit subreddit
		g.getFeeds().getRedditFeed().add(new RedditFeedObject(msg.getChannel(), contents, (boolean) super.getResponses().get(0)));
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
