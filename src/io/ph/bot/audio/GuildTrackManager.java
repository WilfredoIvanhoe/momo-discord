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
	private final BlockingQueue<AudioTrack> queue;
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
	public void queue(AudioTrack track) {
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		player.startTrack(queue.poll(), false);
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();
		} else {
			//TODO: Send message to channel saying dry queue
		}
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		IChannel ch;
		if((ch = Bot.getInstance().getBot()
				.getChannelByID(Guild.guildMap.get(this.guildId)
						.getSpecialChannels().getMusic())) != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.withTitle("New track")
			.withColor(Color.MAGENTA)
			.withDesc(track.getInfo().title + " by " + track.getInfo().author);
			System.out.println(track.getIdentifier() + " | " + track.getDuration() + " | " + track.getPosition());
			MessageUtils.sendMessage(ch, em.build());
			System.out.println("Track has started: " + track.getInfo().title);
		}
	}

	/*@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		// An already playing track threw an exception (track end event will still be received separately)
	}*/

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		// Audio track has been unable to provide us any audio, might want to just start a new track
		nextTrack();
	}
}
