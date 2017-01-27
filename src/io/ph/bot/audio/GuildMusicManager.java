package io.ph.bot.audio;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class GuildMusicManager {
	private AudioPlayer audioPlayer;
	private GuildTrackManager trackManager;

	private int skipVotes;
	private Set<String> skipVoters;

	public GuildMusicManager(AudioPlayerManager manager, String guildId) {
		this.audioPlayer = manager.createPlayer();
		this.trackManager = new GuildTrackManager(audioPlayer, guildId);
		this.audioPlayer.addListener(trackManager);
		this.skipVotes = 0;
		this.skipVoters = new HashSet<String>();
	}

	public static void loadAndPlay(final IChannel channel, final String trackUrl, final String titleOverride, final IUser user) {
		GuildMusicManager musicManager = AudioManager.getGuildManager(channel.getGuild());
		AudioManager.getMasterManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			EmbedBuilder em = new EmbedBuilder();
			@Override
			public void trackLoaded(AudioTrack track) {
				em.withTitle("Music queued")
				.withColor(Color.GREEN)
				.withDesc(titleOverride == null ? 
						track.getInfo().title :
						titleOverride
						+ " was queued by " + user.getDisplayName(channel.getGuild()))
				.withFooterText("Place in queue: " + AudioManager.getGuildManager(channel.getGuild())
					.getTrackManager().getQueueSize());
				MessageUtils.sendMessage(channel, em.build());
				play(channel.getGuild(), track, trackUrl, titleOverride, user);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}
				em.withTitle("Playlist queued")
				.withColor(Color.GREEN)
				.withDesc("Playlist *" + playlist.getName() + "* queued by " + user.getDisplayName(channel.getGuild()))
				.withFooterText(String.format("Playlist size: %d | Queue size: %d",
						playlist.getTracks().size(),
						AudioManager.getGuildManager(channel.getGuild()).getTrackManager().getQueueSize()));
				MessageUtils.sendMessage(channel, em.build());
				playlist.getTracks().stream()
					.forEach(t -> play(channel.getGuild(), t, trackUrl, titleOverride, user));
			}

			@Override
			public void noMatches() {
				em.withTitle("Error")
				.withColor(Color.RED)
				.withDesc("Error queueing your track");
				MessageUtils.sendMessage(channel, em.build());
			}

			@Override
			public void loadFailed(FriendlyException e) {
				em.withTitle("Error loading")
				.withColor(Color.RED)
				.withDesc("Error playing: " + e.getMessage());
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
		return new AudioProvider(this.audioPlayer);
	}
}
