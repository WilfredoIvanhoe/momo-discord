package io.ph.bot.listener;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Guild.GuildMusic;
import io.ph.bot.model.Guild.MusicMeta;
import io.ph.util.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackSkipEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

/**
 * Event listeners related to the audio functionality of Momo
 * @author Paul
 *
 */
public class AudioListeners {

	@EventSubscriber
	public void onTrackStartEvent(TrackStartEvent e) {
		Guild g = Guild.guildMap.get(e.getPlayer().getGuild().getID());
		MusicMeta m = g.getMusicManager().pollMetaData();
		if(g.getSpecialChannels().getMusic().equals(""))
			return;
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle("New track: " + m.getTrackName());
		StringBuilder sb = new StringBuilder();
		sb.append("<@" + m.getUserId() + ">, your song is now playing");
		if(m.getUrl() != null)
			sb.append("\n" + m.getUrl());
		em.withDesc(sb.toString());
		em.withColor(Color.CYAN);
		MessageUtils.sendMessage(Bot.getInstance().getBot().getChannelByID(g.getSpecialChannels().getMusic()), em.build());

		if(g.getMusicManager().getAudioPlayer().getPlaylistSize() < 2 && g.getMusicManager().getAudioSize() > 0) {
			try {
				g.getMusicManager().getAudioPlayer().queue(g.getMusicManager().pollGetAudio().getPreparedFile());
			} catch (IOException | UnsupportedAudioFileException e1) {
				e1.printStackTrace();
			}
		}
	}
	@EventSubscriber
	public void onTrackFinishEvent(TrackFinishEvent e) {
		if(e.getOldTrack().getMetadata().get("file") != null) {
			((File) e.getOldTrack().getMetadata().get("file")).delete();
		}
		handleFinishedTrack(e.getPlayer().getGuild());
	}

	@EventSubscriber
	public void onTrackSkipEvent(TrackSkipEvent e) {
		if(e.getTrack().getMetadata().get("file") != null) {
			((File) e.getTrack().getMetadata().get("file")).delete();
		}
		handleFinishedTrack(e.getPlayer().getGuild());
	}
	
	private static void handleFinishedTrack(IGuild guild) {
		Guild g = Guild.guildMap.get(guild.getID());
		if(g.getMusicManager().getMusicMeta().size() == 0)
			g.getMusicManager().setCurrentSong(null);
		IVoiceChannel channel = guild.getVoiceChannelByID(g.getSpecialChannels().getVoice());
		if(channel.getConnectedUsers().size() == 1) {
			GuildMusic m = g.getMusicManager();
			m.getAudioPlayer().clear();
			m.getMusicMeta().clear();
			m.setSkipVotes(0);
			m.getSkipVoters().clear();
			m.setCurrentSong(null);
		}
	}
}
