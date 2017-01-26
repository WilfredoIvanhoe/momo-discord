package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import sx.blah.discord.handle.obj.IUser;

public class TrackDetails {
	private String url;
	private IUser queuer;
	private AudioTrack track;

	public TrackDetails(String url, IUser queuer, AudioTrack track) {
		this.url = url;
		this.queuer = queuer;
		this.track = track;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return the queuer
	 */
	public IUser getQueuer() {
		return queuer;
	}

}
