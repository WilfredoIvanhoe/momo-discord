package io.ph.bot.audio;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

public class GuildTrackManager extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<TrackDetails> queue;
	private TrackDetails currentSong;
	private String guildId;

	/**
	 * @param player The audio player this scheduler uses
	 */
	public GuildTrackManager(AudioPlayer player, String guildId) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.guildId = guildId;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(TrackDetails track) {
		if(this.player.getPlayingTrack() == null) {
			this.currentSong = track;
			player.startTrack(track.getTrack(), false);
		} else {
			queue.offer(track);
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		player.startTrack((currentSong = queue.poll()).getTrack(), false);
	}

	/**
	 * Next track if queue isn't empty, stop if not
	 */
	public void skipTrack() {
		player.stopTrack();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext || !queue.isEmpty()) {
			nextTrack();
		} else {
			IChannel ch;
			if((ch = Bot.getInstance().getBot()
					.getChannelByID(Guild.guildMap.get(this.currentSong.getGuildId())
							.getSpecialChannels().getMusic())) != null) {
				EmbedBuilder em = new EmbedBuilder();
				em.withTitle("Queue finished!")
				.withColor(Color.MAGENTA)
				.withDesc("Your queue is all dried up");
				MessageUtils.sendMessage(ch, em.build());
			}
			currentSong = null;
		}
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		IChannel ch;
		if((ch = Bot.getInstance().getBot()
				.getChannelByID(Guild.guildMap.get(this.guildId)
						.getSpecialChannels().getMusic())) != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.withTitle("New track: " + this.getCurrentSong().getTitle() == null ? 
					track.getInfo().title :
						this.getCurrentSong().getTitle())
			.withColor(Color.MAGENTA);
			if(currentSong != null)
				em.withDesc(String.format("<@%s>, your song is now playing\n"
						+ "%s", this.currentSong.getQueuer().getID(), this.currentSong.getUrl()));
			MessageUtils.sendMessage(ch, em.build());
		}
	}

	/*@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		// An already playing track threw an exception (track end event will still be received separately)
	}*/

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		if(!queue.isEmpty())
			nextTrack();
	}

	public BlockingQueue<TrackDetails> getQueue() {
		return this.queue;
	}
	
	/**
	 * Get the true queue size
	 * @return Current song + rest of queue
	 */
	public int getQueueSize() {
		return this.queue.size() + (currentSong == null ? 0 : 1);
	}

	public boolean isEmpty() {
		return this.currentSong == null && this.queue.isEmpty();
	}

	public TrackDetails getCurrentSong() {
		return currentSong;
	}
}
