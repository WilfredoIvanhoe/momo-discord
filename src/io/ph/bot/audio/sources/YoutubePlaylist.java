package io.ph.bot.audio.sources;

import java.io.IOException;
import java.net.URL;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Guild;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

public class YoutubePlaylist {

	public static void queuePlaylist(URL url, IMessage msg) throws IOException, NoAPIKeyException, FileTooLargeException {
		YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
			@Override
			public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {
			}
		}).setApplicationName("momo discord bot").build();
		String playlistId;
		YouTube.PlaylistItems.List search = youtube.playlistItems().list("snippet,contentDetails")
				.setKey(Bot.getInstance().getApiKeys().get("youtube"))
				.setId((playlistId = Util.extractYoutubePlaylistId(url.toString())));
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		for(PlaylistItem p : search.execute().getItems()) {
			try {
				MusicSource source = new Youtube(new URL(String.format("https://youtube.com/watch?v=%s&list=%s", p.getId(), playlistId)), msg);
				g.getMusicManager().addMusicSource(source);
			} catch(FileTooLargeException e) {
				continue;
			}
		}
	}
	
}
