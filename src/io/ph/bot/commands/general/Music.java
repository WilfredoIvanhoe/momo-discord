package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.audio.GetAudio;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Guild.GuildMusic;
import io.ph.bot.model.Guild.MusicMeta;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;

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
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(Guild.guildMap.get(msg.getGuild().getID()).getMusicManager() == null) {
			em.withColor(Color.RED).withTitle("Error").withDesc("I don't have a music channel setup in this server! \n"
					+ "If you have the Manage Server role, run " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "setupmusic");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(contents.equals("")) {
			String prefix = Util.getPrefixForGuildId(msg.getGuild().getID());
			em = MessageUtils.commandErrorMessage(msg, "music", "[Youtube|Soundcloud|" + prefix + "theme-result-#]", 
					"*[Youtube|Soundcloud|"	+ prefix + "theme-result-#]* - URL of song to play. "
							+ "In the case of a theme command result, its number in the list",
							"`" + prefix + "music now` shows current song",
							"`" + prefix + "music next` shows queued songs",
							"`" + prefix + "music skip` casts a vote to skip the song");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		GuildMusic m = Guild.guildMap.get(msg.getGuild().getID()).getMusicManager();
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
			IVoiceChannel voice = msg.getGuild().getVoiceChannelByID(Guild.guildMap.get(msg.getGuild().getID()).getSpecialChannels().getVoice());
			int current = voice.getConnectedUsers().size();
			if(current <= 0)
				current = 1;
			int maxVotes = (int) Math.floor(current/2);
			if(maxVotes > 5)
				maxVotes = 5;
			if(++current >= maxVotes) {
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
				m.setSkipVotes(m.getSkipVotes() + 1);
				m.getSkipVoters().add(msg.getAuthor().getID());
				em.withColor(Color.GREEN).withTitle("Voted to skip").withDesc("Votes needed to pass: " + current + "/" + maxVotes);
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
			if(m.getCurrentSong().getTrackName() != null || m.getCurrentSong().getTrackName().equals(""))
				em.withTitle("Current track: " + m.getCurrentSong().getTrackName());
			else
				em.withTitle("Current track");
			StringBuilder sb = new StringBuilder();
			sb.append("Queued by " + msg.getGuild().getUserByID(m.getCurrentSong().getUserId()).getDisplayName(msg.getGuild()));
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
			for(MusicMeta meta : m.getMusicMeta()) {
				sb.append("**" + (++count) + ")** ");
				if(!meta.getTrackName().equals("") || meta.getTrackName() == null) {
					sb.append(meta.getTrackName() + " | ");
				} else {
					sb.append("Unknown track name | ");
				}
				sb.append("Queued by " + msg.getGuild().getUserByID(meta.getUserId()).getDisplayName(msg.getGuild()) + "\n");
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
			m.getMusicMeta().clear();
			m.setSkipVotes(0);
			m.getSkipVoters().clear();
			m.setCurrentSong(null);
			em.withColor(Color.GREEN).withTitle("Music stopped").withDesc("Playlist cleared");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(Util.isInteger(contents)) {
			int index = Integer.parseInt(contents);
			if((index) > Guild.guildMap.get(msg.getGuild().getID())
					.getHistoricalSearches().getHistoricalThemes().size() || index < 1) {
				MessageUtils.sendErrorEmbed(msg.getChannel(), "Invalid input",
						"Giving a number will play music on a previous "
								+ Util.getPrefixForGuildId(msg.getGuild().getID()) + "theme search. This # is too large");
				return;
			}
			String[] theme = Guild.guildMap.get(msg.getGuild().getID())
					.getHistoricalSearches().getHistoricalThemes().get(index);
			Runnable r = new GetAudio(msg.getAuthor().getID(), theme[0], theme[1], msg.getChannel(), m);
			new Thread(r).start();
			return;
		}

		Runnable r = new GetAudio(msg.getAuthor().getID(), "", contents, msg.getChannel(), m);
		new Thread(r).start();
	}

}
