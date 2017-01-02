package io.ph.bot.feed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Reddit event listener
 * @author Paul
 * TODO: Sometimes double sends posts
 */
@SuppressWarnings("unchecked")
public class RedditEventListener implements Job {
	private static File serializedFile = new File("resources/feeds/Reddit.bin");
	public static RedditClient redditClient;

	private static Map<String, List<RedditFeedObserver>> redditFeed = new HashMap<String, List<RedditFeedObserver>>();

	public static void addRedditFeed(String subreddit, RedditFeedObserver observer) {
		subreddit = subreddit.toLowerCase();
		if(!redditFeed.containsKey(subreddit)) {
			ArrayList<RedditFeedObserver> toAdd = new ArrayList<RedditFeedObserver>();
			toAdd.add(observer);
			redditFeed.put(subreddit, toAdd);
		} else {
			redditFeed.get(subreddit).add(observer);
		}
	}

	/**
	 * Remove a reddit feed by two params
	 * @param subreddit Name of subreddit to remove
	 * @param guild Guild to remove from
	 * @return True if removed, false if the feed never existed
	 */
	public static boolean removeRedditFeed(String subreddit, IGuild guild) {
		subreddit = subreddit.toLowerCase();
		RedditFeedObserver observer;
		if((observer = getObserver(subreddit, guild)) == null)
			return false;
		redditFeed.get(subreddit).remove(observer);
		saveFeed();
		return true;
	}

	/**
	 * Get observer from two params
	 * @param subreddit Subreddit name
	 * @param guild Guild
	 * @return RedditFeedObserver if found, null if not
	 */
	public static RedditFeedObserver getObserver(String subreddit, IGuild guild) {
		subreddit = subreddit.toLowerCase();
		if(!redditFeed.containsKey(subreddit)) {
			return null;
		}
		for(RedditFeedObserver observer : redditFeed.get(subreddit)) {
			if(observer.getDiscoChannel().getGuild().equals(guild))
				return observer;
		}
		return null;
	}

	static String cutoffId;
	public static void update() {
		try {
			SubredditPaginator allNew = new SubredditPaginator(redditClient);
			allNew.setLimit(100);
			allNew.setSorting(Sorting.NEW);
			allNew.setSubreddit("all");
			Listing<Submission> posts = allNew.next();
			for(Submission post : posts) {
				if(post.getId().equals(cutoffId))
					break;
				if(redditFeed.containsKey(post.getSubredditName().toLowerCase())) {
					if(redditFeed.get(post.getSubredditName().toLowerCase()).isEmpty()) {
						redditFeed.remove(post.getSubredditName().toLowerCase());
						saveFeed();
						continue;
					}
					Iterator<RedditFeedObserver> iter = redditFeed.get(post.getSubredditName().toLowerCase()).iterator();
					while(iter.hasNext()) {
						RedditFeedObserver observer = iter.next();
						if(!observer.trigger(post)) {
							iter.remove();
							saveFeed();
						}
					}
				}
			}
			cutoffId = posts.get(0).getId();
		} catch(NetworkException e) {
			try {
				reAuthenticate();
				update();
			} catch (NetworkException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if(Bot.getInstance().isDebug())
			LoggerFactory.getLogger(RedditEventListener.class).debug("Reddit job executing...");
		update();
	}

	public static void saveFeed() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializedFile));
			oos.writeObject(redditFeed);
			oos.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, List<RedditFeedObserver>> getFeed() {
		return redditFeed;
	}
	static Credentials credentials;
	static {
		try {
			UserAgent userAgent = UserAgent.of("discord bot", "io.ph.bot", Bot.BOT_VERSION, Bot.getInstance().getApiKeys().get("redditusername"));
			redditClient = new RedditClient(userAgent);
			credentials = Credentials.script(Bot.getInstance().getApiKeys().get("redditusername"),
					Bot.getInstance().getApiKeys().get("redditpassword"),
					Bot.getInstance().getApiKeys().get("redditkey"),
					Bot.getInstance().getApiKeys().get("redditsecret"));
			OAuthData authData = authenticate();
			redditClient.authenticate(authData);

			if(!serializedFile.createNewFile()) {
				serializedFile.getParentFile().mkdirs();
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedFile));
				redditFeed = (Map<String, List<RedditFeedObserver>>) ois.readObject();
				ois.close();
			} else {
				saveFeed();
			}
		} catch (NoAPIKeyException e) {
			e.printStackTrace();
			LoggerFactory.getLogger(RedditEventListener.class).warn("Reddit API keys not set. Cannot perform live Reddit updates");
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LoggerFactory.getLogger(RedditEventListener.class).warn("Reddit API keys not set. Cannot do live Reddit updates");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void reAuthenticate() {
		redditClient.getOAuthHelper().revokeAccessToken(credentials);
		redditClient.deauthenticate();
		try {
			OAuthData authData = authenticate();
			redditClient.authenticate(authData);
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		}
	}

	private static OAuthData authenticate() throws NetworkException, OAuthException {
		return redditClient.getOAuthHelper().easyAuth(credentials);
	}

	public static String debugList() {
		StringBuilder sb = new StringBuilder();
		for(List<RedditFeedObserver> list : redditFeed.values()) {
			for(RedditFeedObserver observer : list) {
				sb.append(observer.getSubreddit() + " on " + observer.getDiscoChannel().getID() + "\n");
			}
		}
		return sb.toString();
	}
}
