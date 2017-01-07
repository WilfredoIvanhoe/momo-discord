package io.ph.bot.audio.sources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Playlists;
import com.google.api.services.youtube.model.PlaylistItem;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Guild;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

public class YoutubePlaylist {

	public static String queuePlaylist(URL url, IMessage msg) throws IOException, NoAPIKeyException, FileTooLargeException {
		System.out.println("Queueing a playlist: " + Util.extractYoutubePlaylistId(url.toString()));
		YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
			@Override
			public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {
			}
		}).setApplicationName("momo discord bot").build();
		String playlistId;
		YouTube.PlaylistItems.List search = youtube.playlistItems().list("snippet,contentDetails")
				.setMaxResults((long) 50)
				.setKey(Bot.getInstance().getApiKeys().get("youtube"))
				.setPlaylistId((playlistId = Util.extractYoutubePlaylistId(url.toString())));
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		List<PlaylistItem> list = search.execute().getItems();
		if(list.isEmpty()) {
			System.out.println("Couldn't find playlist for ID: " + playlistId);
			throw new MalformedURLException();
		}
		for(PlaylistItem p : list) {
			System.out.println("Iterating for video: " + p.getSnippet().getTitle() + " | " + p.getContentDetails().getVideoId());
			try {
				MusicSource source = 
						new Youtube(new URL(String.format("https://youtube.com/watch?v=%s", p.getContentDetails().getVideoId())), msg);
				g.getMusicManager().addMusicSource(source);
			} catch(FileTooLargeException e) {
				System.out.println("File is too large");
				continue;
			}
		}
		Playlists.List plistSearch = youtube.playlists().list("snippet")
				.setKey(Bot.getInstance().getApiKeys().get("youtube"))
				.setId(playlistId);
		return plistSearch.execute().getItems().get(0).getSnippet().getTitle();
	}
	
}
