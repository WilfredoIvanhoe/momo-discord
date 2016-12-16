package io.ph.bot.listener;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Guild.MusicMeta;
import io.ph.util.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackSkipEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

public class AudioListeners {

	@EventSubscriber
	public void onTrackFinishEvent(TrackFinishEvent e) {
		if(e.getOldTrack().getMetadata().get("file") != null) {
			((File) e.getOldTrack().getMetadata().get("file")).delete();
		}
		if(Guild.guildMap.get(e.getPlayer().getGuild().getID()).getMusicManager().getMusicMeta().size() == 0)
			Guild.guildMap.get(e.getPlayer().getGuild().getID()).getMusicManager().setCurrentSong(null);
	}

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
	public void onTrackSkipEvent(TrackSkipEvent e) {
		if(e.getTrack().getMetadata().get("file") != null) {
			((File) e.getTrack().getMetadata().get("file")).delete();
		}
		if(Guild.guildMap.get(e.getPlayer().getGuild().getID()).getMusicManager().getMusicMeta().size() == 0)
			Guild.guildMap.get(e.getPlayer().getGuild().getID()).getMusicManager().setCurrentSong(null);
	}
}
