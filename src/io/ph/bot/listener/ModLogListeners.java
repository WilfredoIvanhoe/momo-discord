package io.ph.bot.listener;

import java.awt.Color;

import io.ph.bot.events.UserBanEvent;
import io.ph.bot.events.UserMutedEvent;
import io.ph.bot.events.UserUnmutedEvent;
import io.ph.bot.model.Guild;
import io.ph.util.MessageUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent;
import sx.blah.discord.util.EmbedBuilder;

public class ModLogListeners {
	
	@EventSubscriber
	public void onUserBanEvent(UserBanEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
					.withAuthorName(e.getUser().getName() + " has been banned by " + e.getBanner().getName())
					.withColor(Color.RED).withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
	}

	@EventSubscriber
	public void onUserPardonEvent(UserPardonEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			try {
				EmbedBuilder em = new EmbedBuilder().withTitle(e.getUser().getName() + " has been unbanned")
						.withColor(Color.GREEN).withTimestamp(System.currentTimeMillis());
				MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
			} catch(NullPointerException e1) {
				// Throws a NPE when the bot pardons, but still lets the message go through. Weird, D4j bug?
			}
		}
	}
	
	@EventSubscriber
	public void onUserMutedEvent(UserMutedEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
					.withAuthorName(e.getUser().getName() + " has been muted by " + e.getMuter().getName())
					.withColor(Color.RED).withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
	}

	@EventSubscriber
	public void onUserUnmutedEvent(UserUnmutedEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
					.withAuthorName(e.getUser().getName() + " has been unmuted")
					.withColor(Color.GREEN).withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
	}

}
