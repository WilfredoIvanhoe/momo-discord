package io.ph.bot.listener;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.State;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.events.UserBanEvent;
import io.ph.bot.events.UserMutedEvent;
import io.ph.bot.events.UserUnmutedEvent;
import io.ph.bot.feed.TwitterEventListener;
import io.ph.bot.jobs.WebSyncJob;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.scheduler.JobScheduler;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.NickNameChangeEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent;
import sx.blah.discord.handle.impl.events.guild.role.RoleDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Listeners {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent e) {
		LoggerFactory.getLogger(Listeners.class).info("Connecting to voice channels...");
		int connectedVoice = 0;
		for(IGuild guild : Bot.getInstance().getBot().getGuilds()) {
			Guild g = Guild.guildMap.get(guild.getID());
			if(g.getSpecialChannels().getVoice().length() > 0) {
				IVoiceChannel target = guild.getVoiceChannelByID(g.getSpecialChannels().getVoice());
				if(target == null) {
					g.getSpecialChannels().setVoice("");
					continue;
				}
				try {
					target.join();
					g.initMusicManager(guild);
					connectedVoice++;
				} catch (MissingPermissionsException e1) {
					e1.printStackTrace();
				}
			}
		}
		JobScheduler.initializeEventSchedule();
		State.changeBotAvatar(new File("resources/avatar/" + Bot.getInstance().getAvatar()));
		LoggerFactory.getLogger(Listeners.class).info("Connected to {} music channels", connectedVoice);
		TwitterEventListener.initTwitter();
		Bot.getInstance().getLogger().info("Bot is now online");
	}

	@EventSubscriber
	public void onRoleDeleteEvent(RoleDeleteEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(g.removeJoinableRole(e.getRole().getID()))  {
			LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted a joinable role", e.getGuild().getID());
		}
	}

	@EventSubscriber
	public void onMessageRecievedEvent(MessageReceivedEvent e) {
		WebSyncJob.messageCount++;
		if(e.getMessage().getChannel().isPrivate()) {
			// Private message
			EmbedBuilder em = new EmbedBuilder();
			Command c;
			if((c = CommandHandler.getCommand(e.getMessage().getContent().toLowerCase())) == null) {
				em.withTitle("Invalid command").withColor(Color.RED).withDesc(e.getMessage().getContent() + " is not a valid command");
				MessageUtils.sendPrivateMessage(e.getMessage().getAuthor(), em.build());
				return;
			}
			em.withTitle(e.getMessage().getContent()).withColor(Color.CYAN).appendField("Primary Command", c.getDefaultCommand(), true);
			String[] aliases = c.getAliases();
			if(aliases.length > 0) {
				em.appendField("Aliases", 
						Arrays.toString(aliases).substring(1, Arrays.toString(aliases).length() - 1) + "\n", true);
			}
			em.appendField("Permissions", c.getPermission().toString(), true).appendField("Description", c.getDescription(), false)
			.appendField("Example", c.getDefaultCommand() + " " + c.getExample().replaceAll("\n", "\n" + c.getDefaultCommand() + " "), false);
			MessageUtils.sendPrivateMessage(e.getMessage().getAuthor(), em.build());
			return;
		}
		Guild g = Guild.guildMap.get(e.getMessage().getGuild().getID());
		if(g.getGuildConfig().isDisableInvites() 
				&& !Util.userHasPermission(e.getAuthor(), e.getGuild(), Permission.KICK)) {
			if(e.getMessage().getContent().toLowerCase().contains("discord.gg/")) {
				e.getMessage().delete();
			}
		}
		if(e.getMessage().getContent().startsWith(g.getGuildConfig().getCommandPrefix())) {
			CommandHandler.processCommand(e.getMessage());
			return;
		}
		if(g.getGuildConfig().getMessagesPerFifteen() > 0
				&& !Util.userHasPermission(e.getAuthor(), e.getGuild(), Permission.KICK)) {
			Integer counter;
			if((counter = g.getUserTimerMap().get(e.getAuthor().getID())) == null) {
				counter = 0;
			}
			if(++counter > g.getGuildConfig().getMessagesPerFifteen()) {
				e.getMessage().delete();
				EmbedBuilder em = new EmbedBuilder();
				em.withColor(Color.RED)
				.withTitle("Error")
				.withDesc("Whoa, slow down there! You're sending too many messages");
				MessageUtils.sendPrivateMessage(e.getAuthor(), em.build());
			} else {
				g.getUserTimerMap().put(e.getAuthor().getID(), counter);
			}
		}
		try {
			if(g.getFeatureStatus("reactions")) {
				if(e.getMessage().getContent().equalsIgnoreCase("shit")) {
					e.getMessage().addReaction("💩");
				}
			}
		} catch (MissingPermissionsException | RateLimitException | DiscordException e1) {
			e1.printStackTrace();
		}
		ProceduralListener.getInstance().update(e.getMessage());
	}

	/**
	 * Guild create event fires when a guild connects or the bot joins the guild
	 * @param e
	 */
	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent e) {
		File f;
		if(!(f = new File("resources/guilds/" + e.getGuild().getID() +"/")).exists()) {
			try {
				FileUtils.forceMkdir(f);
				FileUtils.copyFile(new File("resources/guilds/template.properties"), 
						new File("resources/guilds/" + e.getGuild().getID()+"/GuildProperties.properties"));
				FileUtils.copyFile(new File("resources/guilds/template.db"), 
						new File("resources/guilds/" + e.getGuild().getID()+"/Data.db"));
				FileUtils.copyFile(new File("resources/guilds/template.json"), 
						new File("resources/guilds/" + e.getGuild().getID()+"/IdlePlaylist.json"));

				Bot.getInstance().getLogger().info("Guild has joined: {}", e.getGuild().getName());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		Guild g = new Guild(e.getGuild());

		if(g.getGuildConfig().isFirstTime()) {
			MessageUtils.sendMessage(e.getGuild().getChannels().get(0), "Hi, I'm Momo! You are my "
					+ Util.ordinal(Bot.getInstance().getBot().getGuilds().size()) + " server.\n"
					+ "If you want a list of commands, use `$commandlist`. If you want some tutorials on my features, "
					+ "do `$howto`");
			Guild.guildMap.get(e.getGuild().getID()).getGuildConfig().setFirstTime(false);
		}
	}

	@EventSubscriber
	public void onGuildLeaveEvent(GuildLeaveEvent e) {
		try {
			FileUtils.deleteDirectory(new File("resources/guilds/" + e.getGuild().getID() + "/"));
			Guild.guildMap.remove(e.getGuild().getID());
			Bot.getInstance().getLogger().info("Guild has left: {}", e.getGuild().getName());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	@EventSubscriber
	public void onUserJoinListener(UserJoinEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
					.withAuthorName(e.getUser().getName() + " has joined the server")
					.withColor(Color.GREEN).withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
		if(!g.getSpecialChannels().getWelcome().equals("")) {
			String msg = g.getGuildConfig().getWelcomeMessage();
			msg = msg.replaceAll("\\$user\\$", "<@"+e.getUser().getID()+">");
			msg = msg.replaceAll("\\$server\\$", e.getGuild().getName());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getWelcome()), msg, true);
		}
	}

	@EventSubscriber
	public void onUserLeaveListener(UserLeaveEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		if(!g.getSpecialChannels().getLog().equals("")) {
			EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
					.withAuthorName(e.getUser().getName() + " has left the server")
					.withColor(Color.RED).withTimestamp(System.currentTimeMillis());
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
	}

	@EventSubscriber
	public void onNicknameChangeListener(NickNameChangeEvent e) {
		Guild g = Guild.guildMap.get(e.getGuild().getID());
		EmbedBuilder em = new EmbedBuilder().withAuthorIcon(e.getUser().getAvatarURL())
				.withColor(Color.CYAN).withTimestamp(System.currentTimeMillis());
		if(!g.getSpecialChannels().getLog().equals("")) {
			if(e.getOldNickname().isPresent() && e.getNewNickname().isPresent()) {
				em.withDesc("**" + e.getOldNickname().get() + "** to **" + e.getNewNickname().get() + "**");
				em.withAuthorName(e.getUser().getName() + " changed their nickname");
			} else if(e.getOldNickname().isPresent() && !e.getNewNickname().isPresent()) {
				em.withDesc("**" + e.getOldNickname().get() + "** to **" + e.getUser().getName() + "**");
				em.withAuthorName(e.getUser().getName() + " removed their nickname");
			} else if(!e.getOldNickname().isPresent() && e.getNewNickname().isPresent()) {
				em.withDesc("**" + e.getUser().getName() + "** to **" + e.getNewNickname().get() + "**");
				em.withAuthorName(e.getUser().getName() + " added a nickname");
			}
			MessageUtils.sendMessage(e.getGuild().getChannelByID(g.getSpecialChannels().getLog()), em.build());
		}
	}

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
	public void onChannelCreateEvent(ChannelCreateEvent e) {
		Guild g = Guild.guildMap.get(e.getChannel().getGuild().getID());
		if(!g.getMutedRoleId().equals("")) {
			IRole target;
			if((target = e.getChannel().getGuild().getRoleByID(g.getMutedRoleId())) == null)
				return;
			try {
				e.getChannel().overrideRolePermissions(target,
						Permissions.getDeniedPermissionsForNumber(0), Permissions.getAllowedPermissionsForNumber(2048));
			} catch (Exception e1) {
			} 
		}
	}

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
	public void onChannelDeleteEvent(ChannelDeleteEvent e) {
		try {
			Guild g = Guild.guildMap.get(e.getChannel().getGuild().getID());
			if(e.getChannel().getID().equals(g.getSpecialChannels().getLog())) {
				g.getSpecialChannels().setLog("");
				LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted their log channel.",
						e.getChannel().getGuild().getID());
			} else if(e.getChannel().getID().equals(g.getSpecialChannels().getMusic())) {
				g.getSpecialChannels().setMusic("");
				LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted their music channel.",
						e.getChannel().getGuild().getID());
			} else if(e.getChannel().getID().equals(g.getSpecialChannels().getTwitch())) {
				g.getSpecialChannels().setTwitch("");
				LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted their twitch channel.",
						e.getChannel().getGuild().getID());
			} else if(e.getChannel().getID().equals(g.getSpecialChannels().getWelcome())) {
				g.getSpecialChannels().setWelcome("");
				LoggerFactory.getLogger(Listeners.class).info("Guild {} deleted their welcome channel.",
						e.getChannel().getGuild().getID());
			}
		} catch(NullPointerException e1) {
			System.err.println("NPE onChannelDelete");
		}
	}

	@EventSubscriber
	public void onMentionEvent(MentionEvent e) {
		if(e.getMessage().mentionsEveryone() || e.getMessage().mentionsHere())
			return;
		if(e.getMessage().getAuthor().isBot())
			return;
		Guild g = Guild.guildMap.get(e.getMessage().getGuild().getID());
		if(g.getFeatureStatus("cleverbot")) {
			if(g.getCleverBot() != null) {
				String msg = e.getMessage().getContent().replaceAll("<@" + Bot.getInstance().getBot().getOurUser().getID() + ">", "");
				if(msg.trim().equals("reset")) {
					g.resetCleverBot();
					return;
				}
				try {
					String s = g.getCleverBot().think(msg);
					MessageUtils.sendMessage(e.getMessage().getChannel(), s);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
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

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent e) {
		if(e.getUser().equals(Bot.getInstance().getBot().getOurUser())) {
			try {
				if(Bot.getInstance().getBot().getVoiceChannelByID(e.getVoiceChannel().getID()) != null) {
					Bot.getInstance().getBot().getVoiceChannelByID(e.getVoiceChannel().getID()).join();
					LoggerFactory.getLogger(Listeners.class).warn("Auto rejoined voice channel {} in {}",
							e.getVoiceChannel().getName(), e.getVoiceChannel().getGuild().getName());
				}
			} catch (MissingPermissionsException e1) { }
		}
		/*if(e.getChannel().getID().equals(Guild.guildMap.get(e.getChannel().getGuild().getID()).getSpecialChannels().getVoice())
				&& e.getChannel().getConnectedUsers().size() == 1) {
			Util.setTimeout(() -> killMusic(e.getChannel()), 15000, true);
		}*/
	}

	/*@EventSubscriber
	public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent e) {
		if(e.getOldChannel().getID().equals(Guild.guildMap.get(e.getOldChannel().getGuild().getID()).getSpecialChannels().getVoice())
				&& e.getOldChannel().getConnectedUsers().size() == 1) {
			Util.setTimeout(() -> killMusic(e.getOldChannel()), 15000, true);
		}
	}*/

	static void killMusic(IVoiceChannel v) {
		if(v.getConnectedUsers().size() > 1) {
			return;
		}
		// Code to kill
	}
}
