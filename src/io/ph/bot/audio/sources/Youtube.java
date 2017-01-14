package io.ph.bot.audio.sources;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

public class Youtube extends MusicSource {

	public Youtube(URL url, IMessage msg) throws FileTooLargeException, IOException, NoAPIKeyException {
		super(url, msg);
	}

	@Override
	protected void downloadLocally() throws FileTooLargeException, IOException, NoAPIKeyException {
		YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
			@Override
			public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {
			}
		}).setApplicationName("momo discord bot").build();
		YouTube.Videos.List search = youtube.videos().list("snippet,contentDetails")
				.setKey(Bot.getInstance().getApiKeys().get("youtube")).setId(Util.extractYoutubeId(super.getUrl().toString()));
		System.out.println("Youtube#downloadLocally at ID " + Util.extractYoutubeId(super.getUrl().toString()));
		Video v;
		try {
			v = search.execute().getItems().get(0);
		} catch(IndexOutOfBoundsException e) {
			throw new IOException("Invalid Youtube key");
		}
		Duration d = Duration.parse(v.getContentDetails().getDuration());
		if((d.get(ChronoUnit.SECONDS) / 60) > 9) {
			throw new FileTooLargeException(super.getUrl());
		}
		super.setTitle(v.getSnippet().getTitle());
	}

	public synchronized void processVideo() {
		String command = "youtube-dl --external-downloader ffmpeg -o resources/tempdownloads/"
				+ super.getFileSeed() + ".mp4 -f mp4 --extract-audio --audio-format mp3 " + super.getUrl();
		/*try {
			File copied = new File(super.getSource().getCanonicalPath() + ".mp3");
			super.getSource().delete();
			super.setSource(copied);
			System.out.println("Set source to: " + copied.getName() + " for object address: " + this.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			if(!p.waitFor(3, TimeUnit.MINUTES)) {
				p.destroy();
				super.getSource().delete();
				super.setSource(null);
				return;
			}
		} catch (Exception e) {
			super.getSource().delete();
			super.setSource(null);
			p.destroy();
		}
	}
}
