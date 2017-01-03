package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Search for a Youtube video - Display 5 results, then use $music to play the song
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "youtube",
		aliases = {"yt"},
		permission = Permission.NONE,
		description = "Search Youtube for a search query and return the first 5 results",
		example = "kyoukara omoide"
		)
public class Youtube implements Command {
	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.length() == 0) {
			em.withColor(Color.RED).withTitle("Error").withDesc("You need to specify a search query");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			YouTube youtube = getYoutubeClient();
			YouTube.Search.List search = youtube.search().list("id");
			search.setKey(Bot.getInstance().getApiKeys().get("youtube")).setQ(contents).setType("video").setFields("items(id/videoId)");
			search.setMaxResults((long) 5);
			List<SearchResult> searchResultList = search.execute().getItems();
			if(searchResultList.isEmpty()) {
				em.withColor(Color.RED).withTitle("Error").withDesc(String.format("No video results for search query **%s**", contents));
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			List<String> idsToQuery = new ArrayList<String>();
			for(SearchResult res : searchResultList) {
				idsToQuery.add(res.getId().getVideoId());
			}
			String joinedIds = Joiner.on(",").join(idsToQuery);
			YouTube.Videos.List searchTwo = youtube.videos().list("id,snippet,contentDetails")
					.setKey(Bot.getInstance().getApiKeys().get("youtube")).setId(joinedIds);
			StringBuilder sb = new StringBuilder();
			int count = 0;
			Guild.guildMap.get(msg.getGuild().getID()).getHistoricalSearches().getHistoricalMusic().clear();
			List<Video> videos = searchTwo.execute().getItems();
			for(Video v : videos) {
				count++;
				long seconds = Duration.parse(v.getContentDetails().getDuration()).getSeconds();
				String formattedDuration = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
				sb.append(String.format("%d) **%s**\n\tUploaded by: **%s**\n\thttps://youtu.be/%s | %s\n", 
						count, v.getSnippet().getTitle(), v.getSnippet().getChannelTitle(), v.getId(), formattedDuration));
				Guild.guildMap.get(msg.getGuild().getID())
					.getHistoricalSearches().addHistoricalMusic(count, 
						new String[] {v.getSnippet().getTitle(), "https://youtube.com/watch?v=" + v.getId()});
			}
			em.withTitle("Youtube results for " + contents);
			em.withColor(Color.GREEN);
			// em.withThumbnail(videos.get(0).getSnippet().getThumbnails().getMedium().getUrl());
			em.withDesc(sb.toString());
			if(Guild.guildMap.get(msg.getGuild().getID()).getSpecialChannels().getVoice().length() > 0)
				em.withFooterText(String.format("Use %smusic # on these results to play music", Util.getPrefixForGuildId(msg.getGuild().getID())));
		} catch(IOException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something went wrong searching Youtube");
		} catch (NoAPIKeyException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("This bot does not have Youtube search setup");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	
	private static YouTube getYoutubeClient() {
		return new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
			@Override
			public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {

			}
		}).setApplicationName("momo discord bot").build();
	}
}
