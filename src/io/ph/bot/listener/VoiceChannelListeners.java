package io.ph.bot.listener;

import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;

public class VoiceChannelListeners {

	@EventSubscriber
	public void onVoiceChannelDeleteEvent(VoiceChannelDeleteEvent e) {
		Guild g = Guild.guildMap.get(e.getVoiceChannel().getGuild().getID());
		if(e.getVoiceChannel().getID().equals(g.getSpecialChannels().getVoice())) {
			g.getSpecialChannels().setVoice("");
			LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted their music voice channel.",
					e.getVoiceChannel().getGuild().getID());
		}
	}

	@EventSubscriber
	public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent e) {
		/*if(e.getVoiceChannel().getID().equals(Guild.guildMap.get(e.getGuild().getID()).getSpecialChannels().getVoice())
				&& !e.getVoiceChannel().isConnected()) {
			try {
				e.getVoiceChannel().join();
			} catch (MissingPermissionsException e1) { }
		}*/
	}

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent e) {
		Guild g;
		if(e.getVoiceChannel().getID().equals((g = Guild.guildMap.get(e.getGuild().getID())).getSpecialChannels().getVoice())
				&& Bot.getInstance().getBot().getConnectedVoiceChannels().contains(e.getVoiceChannel())) {
			if(g.getMusicManager().getQueueSize() == 0 && e.getVoiceChannel().getUsersHere().size() == 1)
				e.getVoiceChannel().leave();
		}
	}

	@EventSubscriber
	public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent e) {
		Guild g;
		if(e.getOldChannel().getID().equals((g = Guild.guildMap.get(e.getGuild().getID())).getSpecialChannels().getVoice())
				&& Bot.getInstance().getBot().getConnectedVoiceChannels().contains(e.getOldChannel())) {
			if(g.getMusicManager().getQueueSize() == 0 && e.getVoiceChannel().getUsersHere().size() == 1)
				e.getOldChannel().leave();
		}
	}

}
