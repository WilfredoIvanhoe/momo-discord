package io.ph.bot.feed;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.rest.imgur.ImgurAPI;
import io.ph.bot.rest.imgur.album.Album;
import io.ph.bot.rest.imgur.image.Image;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import net.dean.jraw.models.Submission;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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
		int imagesInAlbum = 0;
		if(!post.getTitle().toLowerCase().contains("spoiler") && !post.getSubmissionFlair().getText().toLowerCase().contains("spoiler")) {
			if(post.isSelfPost() && this.showPreview && !RedditEventListener.redditClient.getSubmission(post.getId()).isNsfw()) {
				descriptionText = post.getSelftext();
				if(descriptionText.length() > 500)
					descriptionText = descriptionText.substring(0, 500) + "...";
			} else if(!post.isSelfPost()
					&& (this.showNsfw || (this.showImages && !RedditEventListener.redditClient.getSubmission(post.getId()).isNsfw()))) {
				if(post.getUrl().contains("reddituploads")) {
					em.withImage(post.getUrl().replaceAll("amp;", ""));
				} else {
					try {
						String mime = Util.getMIMEFromURL(new URL(post.getUrl().replaceAll("amp;", "")));
						if(mime.contains("png") || mime.contains("jpeg")) {
								em.withImage(post.getUrl());
						} else if(post.getUrl().contains("imgur")) {
							Retrofit retrofit = new Retrofit.Builder()
									.baseUrl(ImgurAPI.ENDPOINT)
									.addConverterFactory(GsonConverterFactory.create())
									.build();
							ImgurAPI imgur = retrofit.create(ImgurAPI.class);
							Pattern p = Pattern.compile("(?:https?:\\/\\/(?:m.)?imgur\\.com\\/(?:[a|gallery]+\\/)?)(.*?)(?:[#\\/].*|$)");
							Matcher m = p.matcher(post.getUrl());
							if(m.find()) {
								if(post.getUrl().contains("/a/")) {
									Call<Album> album = imgur.getAlbum(m.group(1), "Client-ID " + Bot.getInstance().getApiKeys().get("imgur"));
									Album a = album.execute().body();
									em.withImage(a.getData().getImages().get(0).getLink());
									imagesInAlbum = a.getData().getImagesCount();
								} else {
									Call<Image> image = imgur.getImage(m.group(1), "Client-ID " + Bot.getInstance().getApiKeys().get("imgur"));
									em.withImage(image.execute().body().getData().getLink());
								}
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (NoAPIKeyException e) {
						LoggerFactory.getLogger(RedditFeedObserver.class).error("No Imgur API key set! Cannot extract full URL. Bot.properties needs imgur=*****");
					} catch (IOException e) {
						System.err.println("Error for post: " + post.getPermalink());
						// API failed
						e.printStackTrace();
					} catch (Exception e) {
						System.err.println("Error for post: " + post.getPermalink());
						e.printStackTrace();
					}
				}
			}
		}
		em.ignoreNullEmptyFields();
		em.withAuthorName("New post on /r/" + post.getSubredditName());
		em.appendField("Title", post.getTitle(), true);
		em.appendField("Author", String.format("/u/**%s**", post.getAuthor()), true);
		em.withAuthorUrl(post.getShortURL());
		em.withColor(Color.MAGENTA);
		em.appendField("Reddit Link", post.getShortURL(), true);
		if(imagesInAlbum > 1) {
			em.appendField("Album Link (" + imagesInAlbum + " images)", post.getUrl(), true);
		}
		if(descriptionText != null && descriptionText.length() > 0) {
			em.appendField("Preview", descriptionText, false);
		}
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

