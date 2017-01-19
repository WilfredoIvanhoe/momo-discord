package io.ph.bot.listener;

import java.awt.Color;
import java.io.File;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Guild.GuildMusic;
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
		/*if(g.getMusicManager().getCurrentSong() == null)
			g.getMusicManager().setCurrentSong(g.getMusicManager().getOverflowQueue().peek());*/
		MusicSource source = g.getMusicManager().pollSource();
		if(!g.getSpecialChannels().getMusic().equals("")) {
			EmbedBuilder em = new EmbedBuilder();
			em.withTitle("New track" + ((source != null && source.getTitle() != null) ? ": " + source.getTitle() : ""));
			StringBuilder sb = new StringBuilder();
			sb.append("<@" + source.getQueuer().getID() + ">, your song is now playing");
			if(source.getUrl() != null)
				sb.append("\n" + source.getUrl());
			em.withDesc(sb.toString());
			em.withColor(Color.CYAN);
			MessageUtils.sendMessage(Bot.getInstance().getBot().getChannelByID(g.getSpecialChannels().getMusic()), em.build());
		}
		g.getMusicManager().queueNext();
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
		IVoiceChannel ch;
		if(g.getMusicManager().getOverflowQueue().size() == 0) {
			g.getMusicManager().setCurrentSong(null);
			if(Bot.getInstance().getBot().getChannelByID(g.getSpecialChannels().getMusic()) != null) {
				EmbedBuilder em = new EmbedBuilder()
						.withTitle("Empty queue")
						.withDesc("Looks like your queue is all dried up!")
						.withColor(Color.MAGENTA);
				MessageUtils.sendMessage(Bot.getInstance().getBot().getChannelByID(g.getSpecialChannels().getMusic()), em.build());
				if((ch = Bot.getInstance().getBot().getConnectedVoiceChannels().stream()
						.filter(v -> v.getGuild().getID().equals(guild.getID()))
						.findAny().orElse(null)) != null) {
					ch.leave();
				}
			}
		}
		
		if((ch = Bot.getInstance().getBot().getConnectedVoiceChannels().stream()
				.filter(v -> v.getGuild().getID().equals(guild.getID()))
				.findAny().orElse(null)) != null && ch.getConnectedUsers().size() == 1) {
			GuildMusic m = g.getMusicManager();
			m.reset();
			//g.initMusicManager(guild);
			ch.leave();
		}
	}
}
