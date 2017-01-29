package io.ph.bot.audio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class GuildMusicManager {
	private AudioPlayer audioPlayer;
	private GuildTrackManager trackManager;
	private AudioProvider audioProvider;

	private int skipVotes;
	private Set<String> skipVoters;

	public GuildMusicManager(AudioPlayerManager manager, String guildId) {
		this.audioPlayer = manager.createPlayer();
		this.trackManager = new GuildTrackManager(audioPlayer, guildId);
		this.audioPlayer.addListener(trackManager);
		this.skipVotes = 0;
		this.skipVoters = new HashSet<String>();
		this.audioProvider = new AudioProvider(this.audioPlayer);
	}

	public static void loadAndPlay(final IChannel channel, final String trackUrl, final String titleOverride, final IUser user) {
		GuildMusicManager musicManager = AudioManager.getGuildManager(channel.getGuild());
		AudioManager.getMasterManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			EmbedBuilder em = new EmbedBuilder();
			@Override
			public void trackLoaded(AudioTrack track) {
				if(track.getDuration() / 1000 > (15 * 60)) {
					em.withTitle("Error")
					.withColor(Color.RED)
					.withDesc("Song duration too long. Please keep it under 15 minutes");
					MessageUtils.sendMessage(channel, em.build());
					return;
				}
				em.withTitle("Music queued")
				.withColor(Color.GREEN)
				.withDesc(String.format("%s was queued by %s",  
						titleOverride == null ? track.getInfo().title : titleOverride,
								user.getDisplayName(channel.getGuild())))
				.withFooterText(String.format("Place in queue: %d | Time until play: %s",
						AudioManager.getGuildManager(channel.getGuild())
						.getTrackManager().getQueueSize(),
						Util.formatTime(AudioManager.getGuildManager(channel.getGuild())
								.getTrackManager().getDurationOfQueue() + 
								(musicManager.getAudioPlayer().getPlayingTrack() == null 
								? 0 : (musicManager.getAudioPlayer().getPlayingTrack().getDuration()
										- musicManager.getAudioPlayer().getPlayingTrack().getPosition())))));
				MessageUtils.sendMessage(channel, em.build());
				play(channel.getGuild(), track, trackUrl, titleOverride, user);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
				
				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				} else  {
					trackLoaded(firstTrack);
					return;
				}
				
				em.withTitle("Playlist queued")
				.withColor(Color.GREEN)
				.withDesc("Playlist *" + playlist.getName() + "* queued by " + user.getDisplayName(channel.getGuild()))
				.withFooterText(String.format("Playlist size: %d | Queue size: %d",
						playlist.getTracks().size(),
						AudioManager.getGuildManager(channel.getGuild()).getTrackManager().getQueueSize() + playlist.getTracks().size()));
				MessageUtils.sendMessage(channel, em.build());
				playlist.getTracks().stream()
				.forEach(t -> {
					if(t.getDuration() / 1000 < (15 * 60))
						play(channel.getGuild(), t, trackUrl, titleOverride, user);
				});
			}

			@Override
			public void noMatches() {
				em.withTitle("Error")
				.withColor(Color.RED)
				.withDesc("Error queueing your track - not found");
				MessageUtils.sendMessage(channel, em.build());
			}

			@Override
			public void loadFailed(FriendlyException e) {
				em.withTitle("Error loading")
				.withColor(Color.RED)
				.withDesc("Error loading and playing: " + e.getMessage());
				e.printStackTrace();
				MessageUtils.sendMessage(channel, em.build());
			}
		});
	}
	private static void play(IGuild guild, AudioTrack track, String trackUrl, String titleOverride, IUser user) {
		TrackDetails details = new TrackDetails(trackUrl, titleOverride, user, track, guild.getID());
		AudioManager.getGuildManager(guild).trackManager.queue(details);
	}

	public GuildTrackManager getTrackManager() {
		return this.trackManager;
	}

	public AudioPlayer getAudioPlayer() {
		return this.audioPlayer;
	}

	public int getSkipVotes() {
		return skipVotes;
	}

	public void setSkipVotes(int skipVotes) {
		this.skipVotes = skipVotes;
	}

	public Set<String> getSkipVoters() {
		return skipVoters;
	}

	public void reset() {
		this.skipVoters.clear();
		this.skipVotes = 0;
		this.getTrackManager().getQueue().clear();
		this.audioPlayer.stopTrack();
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioProvider getAudioProvider() {
		return this.audioProvider;
	}

	/**
	 * Shuffle the queue
	 */
	public void shuffle() {
		List<TrackDetails> temp = new ArrayList<TrackDetails>();
		this.trackManager.getQueue().drainTo(temp);
		Collections.shuffle(temp);
		temp.stream().forEach(t -> this.trackManager.getQueue().offer(t));
	}
}
