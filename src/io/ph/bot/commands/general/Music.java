package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.audio.sources.DirectLink;
import io.ph.bot.audio.sources.Webm;
import io.ph.bot.audio.sources.Youtube;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Guild.GuildMusic;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * Play music in designated channel
 * @author Paul
 */
@CommandData (
		defaultSyntax = "music",
		aliases = {"play"},
		permission = Permission.NONE,
		description = "Play or get information on the music playlist\n"
				+ "Can only be used if you have setup the music voice channel with the command setupmusic",
				example = "https://youtu.be/dQw4w9WgXcQ\n"
						+ "now\n"
						+ "next\n"
						+ "skip"
		)
public class Music implements Command {
	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		String titleOverride = null;
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		if(g.getMusicManager() == null) {
			IVoiceChannel v;
			if((v = Bot.getInstance().getBot()
					.getVoiceChannelByID(g.getSpecialChannels().getVoice())) != null) {
				try {
					v.join();
					g.initMusicManager(msg.getGuild());
				} catch (MissingPermissionsException e) {
					e.printStackTrace();
				}
			} else {
				em.withColor(Color.RED).withTitle("Error").withDesc("I don't have a music channel setup in this server! \n"
						+ "If you have the Manage Server role, run " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "setupmusic");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
		}
		if(contents.equals("") && msg.getAttachments().isEmpty()) {
			String prefix = Util.getPrefixForGuildId(msg.getGuild().getID());
			em = MessageUtils.commandErrorMessage(msg, "music", "[Youtube|Soundcloud|" + prefix + "theme-result|" + prefix + "youtube-result]", 
					"*[Youtube|Soundcloud|"	+ prefix + "theme-result-#]* - URL of song to play. "
							+ "In the case of a theme or youtube command result, its number in the list",
							"`" + prefix + "music now` shows current song",
							"`" + prefix + "music next` shows queued songs",
							"`" + prefix + "music skip` casts a vote to skip the song");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		GuildMusic m = g.getMusicManager();
		if(contents.startsWith("skip")) {
			if(m.getSkipVoters().contains(msg.getAuthor().getID())) {
				em.withColor(Color.RED).withTitle("Error").withDesc("You have already voted to skip!");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			if(m.getAudioPlayer().getCurrentTrack() == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No song currently playing");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			IVoiceChannel voice = msg.getGuild().getVoiceChannelByID(g.getSpecialChannels().getVoice());
			int current = voice.getConnectedUsers().size();
			int currentVotes = m.getSkipVotes();
			if(current <= 0)
				current = 1;
			int maxVotes = (int) Math.floor(current/2);
			if(maxVotes > 5)
				maxVotes = 5;
			if(++currentVotes >= maxVotes) {
				m.setSkipVotes(0);
				m.getSkipVoters().clear();
				m.getAudioPlayer().skip();
				em.withColor(Color.GREEN).withTitle("Success").withDesc("Vote to skip passed");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			} else if(Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
				m.setSkipVotes(0);
				m.getSkipVoters().clear();
				m.getAudioPlayer().skip();
				em.withColor(Color.GREEN).withTitle("Force skip").withDesc("Force skipped by " + msg.getAuthor().getDisplayName(msg.getGuild()));
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			} else {
				m.setSkipVotes(currentVotes);
				m.getSkipVoters().add(msg.getAuthor().getID());
				em.withColor(Color.GREEN).withTitle("Voted to skip").withDesc("Votes needed to pass: " + currentVotes + "/" + maxVotes);
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
		} else if(contents.startsWith("now")) {
			if(m.getAudioPlayer().getCurrentTrack() == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No song currently playing");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withTitle("Current song");
			if(m.getCurrentSong().getTitle() != null && !m.getCurrentSong().getTitle().isEmpty())
				em.withTitle("Current track: " + m.getCurrentSong().getTitle());
			else
				em.withTitle("Current track");
			StringBuilder sb = new StringBuilder();
			sb.append("Queued by " + m.getCurrentSong().getQueuer().getDisplayName(msg.getGuild()));
			if(m.getCurrentSong().getUrl() != null)
				sb.append("\n" + m.getCurrentSong().getUrl());
			String currentTime = Util.formatTime((int) m.getAudioPlayer().getCurrentTrack().getCurrentTrackTime());
			em.withFooterText(currentTime + "/" + m.getCurrentSong().getSongLength());
			em.withDesc(sb.toString());
			em.withColor(Color.CYAN);
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(contents.startsWith("next") || contents.startsWith("list")) {
			if(m.getAudioPlayer().getCurrentTrack() == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No song currently playing");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withTitle("Coming up");
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for(MusicSource source : m.getOverflowQueue()) {
				sb.append("**" + (++count) + ")** ");
				if(source.getTitle() != null && !source.getTitle().equals("")) {
					sb.append(source.getTitle() + " | ");
				} else {
					sb.append("Unknown track name | ");
				}
				sb.append("Queued by " + source.getQueuer().getDisplayName(msg.getGuild()) + "\n");
				if(count == 10) {
					em.withFooterText("Queue search limited to 10 results");
					break;
				}
			}
			em.withColor(Color.CYAN).withDesc(sb.toString());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(contents.startsWith("stop") && Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
			m.getAudioPlayer().clear();
			m.getOverflowQueue().clear();
			m.setSkipVotes(0);
			m.getSkipVoters().clear();
			m.setCurrentSong(null);
			em.withColor(Color.GREEN).withTitle("Music stopped").withDesc("Playlist cleared");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(Util.isInteger(contents)) {
			int index = Integer.parseInt(contents);
			if((index) > g
					.getHistoricalSearches().getHistoricalMusic().size() || index < 1) {
				MessageUtils.sendErrorEmbed(msg.getChannel(), "Invalid input",
						"Giving a number will play music on a previous theme or youtube search. This # is too large");
				return;
			}
			String[] historicalResult = g
					.getHistoricalSearches().getHistoricalMusic().get(index);
			titleOverride = historicalResult[0];
			contents = historicalResult[1];
		}
		if(!msg.getAttachments().isEmpty()) {
			contents = msg.getAttachments().get(0).getUrl();
		} 
		MusicSource source;
		try {
			if(contents.contains("youtu.be") || contents.contains("youtube")) {
				source = new Youtube(new URL(contents), msg);
			} else if(contents.contains(".webm")) {
				source = new Webm(new URL(contents), msg);
			} else if(contents.endsWith(".mp3") || contents.endsWith(".flac")) {
				source = new DirectLink(new URL(contents), msg);
			} else {
				em.withColor(Color.RED)
				.withTitle("Error")
				.withDesc("I currently do not support that type of link or attachment");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withColor(Color.GREEN)
			.withTitle("Queued" + ((source.getTitle() != null) ? " " + source.getTitle() : ""))
			.withDesc(source.getQueuer().getDisplayName(msg.getGuild()) + " queued a track")
			.withFooterText("Place in queue: " + (g.getMusicManager().getQueueSize() + 1));
			if(titleOverride != null)
				source.setTitle(titleOverride);
			if(!Bot.getInstance().getBot().getVoiceChannelByID(g.getSpecialChannels().getVoice()).isConnected()) {
				try {
					Bot.getInstance().getBot().getVoiceChannelByID(g.getSpecialChannels().getVoice()).join();
				} catch (MissingPermissionsException e) {
					e.printStackTrace();
				}
			}
			g.getMusicManager().addMusicSource(source);
		} catch (MalformedURLException e) {
			em.withColor(Color.RED)
			.withTitle("Error")
			.withDesc("Bad URL");
		} catch (FileTooLargeException e) {
			em.withColor(Color.RED)
			.withTitle("Error")
			.withDesc("File size too large. Please keep sizes under 25 megabytes");
		} catch (IOException e) {
			em.withColor(Color.RED)
			.withTitle("Error")
			.withDesc("Error occured queueing your file");
			e.printStackTrace();
		} catch (NoAPIKeyException e) {
			em.withColor(Color.RED)
			.withTitle("Error")
			.withDesc("Bot not setup with correct API keys to use this service");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
