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

	public static void loadAndPlay(final IChannel channel, final String trackUrl) {
		GuildMusicManager musicManager = AudioManager.getGuildManager(channel.getGuild());
		AudioManager.getMasterManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			EmbedBuilder em = new EmbedBuilder();
			@Override
			public void trackLoaded(AudioTrack track) {
				em.withTitle("Music queued")
				.withColor(Color.GREEN)
				.withDesc(track.getInfo().title
						+ " was queued by empty")
				.withFooterText("Place in queue: todo");
				MessageUtils.sendMessage(channel, em.build());
				play(channel.getGuild(), track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}
				em.withTitle("Playlist queued")
				.withColor(Color.GREEN)
				.withDesc("Playlist *" + playlist.getName() + "* queued")
				.withFooterText("Todo this");
				MessageUtils.sendMessage(channel, em.build());

				play(channel.getGuild(), firstTrack);
			}

			@Override
			public void noMatches() {
				em.withTitle("Error")
				.withColor(Color.RED)
				.withDesc("Error queueing your track");
				MessageUtils.sendMessage(channel, em.build());
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				em.withTitle("Error loading")
				.withColor(Color.RED)
				.withDesc("Error playing: " + exception.getMessage());
				MessageUtils.sendMessage(channel, em.build());
			}
		});
	}
	private static void play(IGuild guild, AudioTrack track) {
		/*Bot.getInstance().getBot()
			.getVoiceChannelByID(Guild.guildMap.get(guild.getID()).getSpecialChannels().getVoice()).join();*/
		AudioManager.getGuildManager(guild).trackManager.queue(track);
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
		//TODO: Clear it all
	}
	
	public boolean emptyQueue() {
		//TODO: do this
		return false;
	}
	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioProvider getAudioProvider() {
		return new AudioProvider(this.audioPlayer);
	}
}
