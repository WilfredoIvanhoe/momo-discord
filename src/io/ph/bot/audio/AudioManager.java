package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.bot.model.Guild;
import sx.blah.discord.handle.obj.IGuild;

public class AudioManager {
	private static final AudioPlayerManager playerManager;
	static {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public static AudioPlayerManager getMasterManager() {
		return playerManager;
	}

	public static GuildMusicManager getGuildManager(IGuild guild) {
		GuildMusicManager g = Guild.guildMap.get(guild.getID()).getMusicManager();
		guild.getAudioManager().setAudioProvider(g.getAudioProvider());
		return g;
	}

	public static String getUrl(AudioTrack t) {
		switch(t.getSourceManager().getSourceName()) {
		case "youtube":
			return String.format("https://www.youtube.com/watch?v=%s", t.getIdentifier());
		case "http":
			return t.getIdentifier();
		case "bandcamp":
			return String.format("https://bandcamp.com/track/%s", t.getIdentifier());
		
		default:
			return null;
		}
	}
}
