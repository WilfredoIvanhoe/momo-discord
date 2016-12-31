package io.ph.bot.feed;

import java.awt.Color;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import io.ph.bot.Bot;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dean.jraw.models.Submission;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Observer class for reddit feed
 * @author Paul
 * TODO: Potential unnecessary memory leak if guild registers for subreddits w/ little to no activity,
 * then deletes channel - this won't cleanup the referenced discordchannel until a post is called
 */
public class RedditFeedObserver implements Serializable {
	private static final long serialVersionUID = 6327948398431989242L;

	// How many seconds to wait before reprocessing post
	private static final int POST_DELAY = 30;
	
	private String discoChannel;
	private String subreddit;
	private boolean showImages;
	// Determine NSFW
	private boolean showNsfw;
	// Show text preview on self posts
	private boolean showPreview;
	
	public RedditFeedObserver(String discoChannel, String subreddit, boolean showImages, boolean showNsfw, boolean showPreview) {
		this.discoChannel = discoChannel;
		this.showImages = showImages;
		this.showNsfw = showNsfw;
		this.showPreview = showPreview;
		this.subreddit = subreddit;
		this.subscribe(subreddit);
	}

	public void subscribe(String subreddit) {
		RedditEventListener.addRedditFeed(subreddit, this);
		RedditEventListener.saveFeed();
	}

	public boolean trigger(Submission post) {
		if(getDiscoChannel() == null || getDiscoChannel().isDeleted()) {
			return false;
		}
		Util.setTimeout(() -> processPost(post.getId()), POST_DELAY * 1000, true);
		return true;
	}
	/**
	 * Delayed post processing
	 * @param postId Post ID to recheck for certain flags
	 */
	private void processPost(String postId) {
		Submission post = RedditEventListener.redditClient.getSubmission(postId);
		if(post == null || post.isHidden() || post.getAuthor().equals("[deleted]"))
			return;
		EmbedBuilder em = new EmbedBuilder();
		String descriptionText = null;
		if(post.isSelfPost() && this.showPreview) {
			if(!post.getTitle().toLowerCase().contains("spoiler") && !post.isNsfw()) {
				descriptionText = post.getSelftext();
				if(descriptionText.length() > 500)
					descriptionText = descriptionText.substring(0, 500) + "...";
			}
		} else if(this.showImages && !post.getUrl().contains("reddituploads")) {
			try {
				String mime = Util.getMIMEFromURL(new URL(post.getUrl()));
				if(!mime.contains("png") && !mime.contains("jpeg")
						&& post.getUrl().contains("imgur")) {
					mime = Util.getMIMEFromURL(new URL("https://i.imgur.com/" 
							+ post.getUrl().substring(post.getUrl().lastIndexOf("/") + 1, 
									post.getUrl().length()) +".png"));
				}
				if(mime.contains("png") || mime.contains("jpeg")) {
					if(this.showNsfw || !RedditEventListener.redditClient.getSubmission(post.getId()).isNsfw())
						em.withImage(post.getUrl());
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else if(this.showImages && !post.getUrl().contains("reddituploads")) {
			if(this.showNsfw || !RedditEventListener.redditClient.getSubmission(post.getId()).isNsfw())
				em.withImage(post.getUrl());
		}
		em.withAuthorName("New post on /r/" + post.getSubredditName());
		em.appendField("Title", post.getTitle(), true);
		em.appendField("Author", String.format("/u/**%s**", post.getAuthor()), true);
		em.withAuthorUrl(post.getShortURL());
		em.withColor(Color.MAGENTA);
		StringBuilder sb = new StringBuilder();
		if(descriptionText != null && descriptionText.length() > 0) {
			sb.append(descriptionText);
			em.appendField("Preview", sb.toString(), false);
		}
		em.appendField("Link", post.getShortURL(), false);
		if(post.isNsfw() || post.getTitle().toLowerCase().contains("spoiler")) {
			em.withFooterText("Post marked as NSFW/spoilers");
		}
		MessageUtils.sendMessage(getDiscoChannel(), em.build());
	}

	static boolean isNsfw(Submission post) {
		return RedditEventListener.redditClient.getSubmission(post.getId()).isNsfw();
	}

	public IChannel getDiscoChannel() {
		return Bot.getInstance().getBot().getChannelByID(discoChannel);
	}

	public String getSubreddit() {
		return subreddit;
	}	
}
