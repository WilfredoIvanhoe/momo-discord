package io.ph.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import io.ph.bot.model.Guild;
import sx.blah.discord.handle.audio.impl.DefaultProvider;
import sx.blah.discord.handle.obj.IGuild;

public class AudioManager {
	private static final AudioPlayerManager playerManager;
	static {
		playerManager = new DefaultAudioPlayerManager();
		playerManager.setFrameBufferDuration(20000);
		playerManager.enableGcMonitoring();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public static AudioPlayerManager getMasterManager() {
		return playerManager;
	}

	public static GuildMusicManager getGuildManager(IGuild guild) {
		GuildMusicManager g = Guild.guildMap.get(guild.getID()).getMusicManager();
		if(guild.getAudioManager().getAudioProvider() instanceof DefaultProvider)
			guild.getAudioManager().setAudioProvider(g.getAudioProvider());
		return g;
	}
}
