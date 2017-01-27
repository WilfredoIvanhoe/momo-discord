package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import sx.blah.discord.handle.obj.IUser;

public class TrackDetails {
	private String url;
	private IUser queuer;
	private AudioTrack track;
	private String guildId;

	public TrackDetails(String url, IUser queuer, AudioTrack track, String guildId) {
		this.url = url;
		this.queuer = queuer;
		this.track = track;
		this.guildId = guildId;
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
	
	/**
	 * @return the audio track
	 */
	public AudioTrack getTrack() {
		return this.track;
	}
	
	/**
	 * @return
	 */
	public String getGuildId() {
		return this.guildId;
	}

	@Override
	public String toString() {
		return "TrackDetails [url=" + url + ", queuer=" + queuer + ", track=" + track + ", guildId=" + guildId + "]";
	}

}
