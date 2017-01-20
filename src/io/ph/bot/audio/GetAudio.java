package io.ph.bot.audio;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild.GuildMusic;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Download a URL (youtube, *.webm, or standard .mp3) then check if
 * true playlist size is < 2. If not, wait for TrackFinishEvent to poll
 * then convert it to an AudioInputStream
 * @author Paul
 * @deprecated
 */
@Deprecated
public class GetAudio {

	private File preparedFile;
	private String url;
	private String userId;
	private String title;
	private IChannel channel;
	private GuildMusic guildMusic;
	public GetAudio(String userId, String title, String url, IChannel channel, GuildMusic m) {
		this.url = url;
		this.userId = userId;
		this.title = title;
		this.guildMusic = m;
		this.channel = channel;
	}

	public void process() {
		int rand = new Random().nextInt(100000);
		EmbedBuilder em = new EmbedBuilder();
		if(url.contains("youtu.be") || url.contains("youtube")) {
			
		} else if(url.contains(".webm")) {
			
		} else if(url.endsWith(".mp3") || url.endsWith(".flac")) {
			this.preparedFile = new File("resources/tempdownloads/" + rand + url.substring(url.lastIndexOf(".")));
			try {
				URL link = new URL(url);
				if((getFileSize(link) / (1024*1024) > 25)
						|| getFileSize(link) == -1) {
					em.withColor(Color.RED);
					em.withTitle("Error");
					em.withDesc("Please keep file size below 25 megabytes");
					MessageUtils.sendMessage(channel, em.build());
					return;
				}
				em.withTitle("Music queued")
				.withDesc(Bot.getInstance().getBot().getUserByID(this.userId).getDisplayName(channel.getGuild())
						+ " queued a track")
				.withFooterText("Place in queue: " + (this.guildMusic.getQueueSize() + 1))
				.withColor(Color.GREEN);
				MessageUtils.sendMessage(channel, em.build());

				Util.saveFile(link, preparedFile);
				
				try {
					AudioFile f = AudioFileIO.read(this.preparedFile);
					Tag tag = f.getTag();
					this.title = tag.getFirst(FieldKey.TITLE);
				} catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
					System.err.println("Could not read Title tags");
				}
			} catch (MalformedURLException e) {
				em.withTitle("Error").withColor(Color.RED).withDesc("Your URL is invalid!");
				MessageUtils.sendMessage(channel, em.build());
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if(this.preparedFile == null) {
			em.withTitle("Error").withColor(Color.RED).withDesc("Invalid audio source");
			MessageUtils.sendMessage(channel, em.build());
			return;
		}
		String songLength = "null";
		try {
			songLength = Util.getMp3Duration(this.preparedFile);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		//this.guildMusic.queueMusicMeta(this.userId, this.title, this.url, songLength, this);
	}

	/**
	 * Return file size of URL based on its content-length header
	 * @param url URL to connect to
	 * @return Content length
	 */
	private int getFileSize(URL url) {
		HttpsURLConnection conn = null;
		try {
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			conn.disconnect();
		}
	}

}
