package io.ph.bot.audio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class MusicSource {	
	private File sourceFile;
	private URL url;
	private String title;
	private IUser queuer;
	private IChannel channel;
	private int fileSeed;
	
	private boolean queued;

	public MusicSource(URL url, IMessage msg) throws FileTooLargeException, IOException, NoAPIKeyException {
		this.url = url;
		this.queuer = msg.getAuthor();
		this.channel = msg.getChannel();
		this.fileSeed = (new Random()).nextInt(100000);
		this.setQueued(false);
		this.downloadLocally();
	}	
	protected abstract void downloadLocally() throws FileTooLargeException, IOException, NoAPIKeyException;

	/**
	 * Return file size of URL based on its content-length header
	 * @param url URL to connect to
	 * @return Content length
	 */
	protected static int getFileSize(URL url) {
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

	public String getTitle() {
		if(title == null || title.isEmpty())
			return null;
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setSource(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public File getSource() {
		if(sourceFile == null) {
			this.sourceFile = new File("resources/tempdownloads/" + this.fileSeed);
			if(!sourceFile.exists())
				this.sourceFile = new File("resources/tempdownloads/" + this.fileSeed + ".mp3");
		}
		return this.sourceFile;
	}
	public URL getUrl() {
		return url;
	}
	public IUser getQueuer() {
		return queuer;
	}
	public IChannel getChannel() {
		return channel;
	}
	public int getFileSeed() {
		return fileSeed;
	}
	public String getSongLength() {
		try {
			return Util.getMp3Duration(getSource());
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public boolean isQueued() {
		return queued;
	}
	public void setQueued(boolean queued) {
		this.queued = queued;
	}
}
