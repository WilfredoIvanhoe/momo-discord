package io.ph.bot.model.feeds;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Represents a single subreddit feed
 * @author Paul
 *
 */
public class RedditFeedObject {
	private static Pusher pusher = new Pusher("50ed18dd967b455393ed");

	private IChannel discoChannel;
	private Channel channel;
	private boolean showImages;
	
	public RedditFeedObject(IChannel discoChannel, String subreddit, boolean showImages) {
		this.discoChannel = discoChannel;
		this.showImages = showImages;
		this.subscribe(subreddit);
	}
	static {
		pusher.connect(new ConnectionEventListener() {
			@Override
			public void onConnectionStateChange(ConnectionStateChange change) {
				LoggerFactory.getLogger(RedditFeedObject.class).info("State change: " + change.getPreviousState() +
						" -> " + change.getCurrentState());
			}
			@Override
			public void onError(String message, String code, Exception e) {
				LoggerFactory.getLogger(RedditFeedObject.class).info("There was a problem connecting: " + message);
			}
		}, ConnectionState.ALL);
	}
	public void subscribe(String subreddit) {
		this.channel = pusher.subscribe(subreddit);
		channel.bind("new-listing", new SubscriptionEventListener() {
			@Override
			public void onEvent(String subreddit, String event, String data) {
				RedditFeedObject.this.trigger(subreddit, Json.parse(data).asObject());
			}
		});
	}

	public void trigger(String subreddit, JsonObject data) {
		EmbedBuilder em = new EmbedBuilder();
		String postTitle = data.getString("title", "Default Title");
		String author = data.getString("author", "Default Author");
		String descriptionText = null;
		if(data.getBoolean("is_self", true)) {
			descriptionText = data.getString("selftext", "");
			if(descriptionText.length() > 500)
				descriptionText = descriptionText.substring(0, 500) + "...";
			
		} else if(this.showImages && !data.getString("url", "null").contains("reddituploads")) {
			try {
				String mime = Util.getMIMEFromURL(new URL(data.getString("url", "null")));
				if(!mime.contains("png") && !mime.contains("jpeg")
						&& data.getString("url", "null").contains("imgur")) {
					mime = Util.getMIMEFromURL(new URL("https://i.imgur.com/" 
						+ data.get("url").asString().substring(data.get("url").asString().lastIndexOf("/") + 1, 
								data.get("url").asString().length()) +".png"));
				}
				if(mime.contains("png") || mime.contains("jpeg")) {
					em.withImage(data.get("url").asString());
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		em.withAuthorName("New post to /r/" + subreddit);
		em.withAuthorUrl("https://reddit.com" + data.getString("permalink", "/r/all"));
		em.withColor(Color.CYAN);
		StringBuilder sb = new StringBuilder();
		sb.append("**Title**: " + postTitle + "\n");
		sb.append("**Author**: " + author + "\n");
		if(descriptionText != null) {
			sb.append(descriptionText + "\n");
		}
		sb.append("https://redd.it/" + data.get("id").asString());
		em.withDesc(sb.toString());
		MessageUtils.sendMessage(this.discoChannel, em.build());
	}
}
