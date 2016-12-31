package io.ph.bot.feed;

import java.awt.Color;
import java.io.Serializable;

import io.ph.bot.Bot;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;
import twitter4j.MediaEntity;
import twitter4j.Status;

public class TwitterFeedObserver implements Serializable {
	private static final long serialVersionUID = -4749221302251416947L;

	private String discoChannel;
	private String twitterHandle;
	private boolean showPictures;

	public TwitterFeedObserver(String discoChannel, String twitterHandle, boolean showPictures) {
		this.discoChannel = discoChannel;
		this.twitterHandle = twitterHandle;
		this.showPictures = showPictures;
	}

	public boolean trigger(Status status) {
		if(getDiscoChannel() == null || getDiscoChannel().isDeleted()) {
			return false;
		}
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle("New tweet from \\@" + status.getUser().getScreenName());
		em.withColor(Color.MAGENTA);
		String text = status.getText();
		em.withThumbnail(status.getUser().getProfileImageURL());
		String url = null;
		for(MediaEntity e : status.getMediaEntities()) {
			if(this.showPictures && url == null && (e.getType().equals("photo")))
				url = e.getMediaURL();
			text = text.replaceAll(e.getURL(), "");
		}
		if(url != null)
			em.withImage(url);
		em.withUrl("https://twitter.com/" + status.getUser().getScreenName() 
				+ "/status/" + status.getId());
		em.withDesc(text);
		if(status.getMediaEntities().length > 0 && url == null || status.getMediaEntities().length > 1) {
			em.withFooterText("Tweet contains more media");
		} else {
			em.withFooterText("Local time");
		}
		em.withTimestamp(status.getCreatedAt().getTime());
		MessageUtils.sendMessage(getDiscoChannel(), em.build());
		return true;
	}

	public int subscribe(long twitterId) {
		return TwitterEventListener.addTwitterFeed(twitterId, this);
	}

	public IChannel getDiscoChannel() {
		return Bot.getInstance().getBot().getChannelByID(discoChannel);
	}

	public String getTwitterHandle() {
		return twitterHandle;
	}
}
