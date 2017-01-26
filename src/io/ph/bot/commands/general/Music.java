package io.ph.bot.commands.general;

import java.awt.Color;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.ph.bot.Bot;
import io.ph.bot.audio.AudioManager;
import io.ph.bot.audio.GuildMusicManager;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
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
						+ "skip\n"
						+ "volume"
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
		GuildMusicManager m = g.getMusicManager();
		if(contents.startsWith("skip")) {
			if(m.getSkipVoters().contains(msg.getAuthor().getID())) {
				em.withColor(Color.RED).withTitle("Error").withDesc("You have already voted to skip!");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			if(m.getAudioPlayer().getPlayingTrack() == null) {
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
			if(++currentVotes >= maxVotes || Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
				m.setSkipVotes(0);
				m.getSkipVoters().clear();
				// TODO: skip
				if(currentVotes >= maxVotes)
					em.withColor(Color.GREEN)
					.withTitle("Success")
					.withDesc("Vote to skip passed");
				else
					em.withColor(Color.GREEN)
					.withTitle("Force skip")
					.withDesc("Force skipped by " + msg.getAuthor().getDisplayName(msg.getGuild()));
				m.getTrackManager().nextTrack();
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
			if(m.getAudioPlayer().getPlayingTrack() == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No song currently playing");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			AudioTrack t;
			em.withTitle("Current track");
			//TODO: now details
			em.appendField("Name", (t = m.getAudioPlayer().getPlayingTrack()).getInfo().title, true);
			em.appendField("Progress", Util.formatTime(t.getPosition()) + "/" + Util.formatTime(t.getDuration()), true);
			em.appendField("Source", AudioManager.getUrl(t), false);
			em.withColor(Color.CYAN);
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(contents.startsWith("next") || contents.startsWith("list")) {
			if(m.getAudioPlayer().getPlayingTrack() == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc("No song currently playing");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withTitle("Coming up");
			// TODO: This
			em.withColor(Color.CYAN).withDesc("Do this");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(contents.startsWith("stop") && Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
			m.reset();
			em.withColor(Color.GREEN).withTitle("Music stopped").withDesc("Queue cleared");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} else if(contents.startsWith("volume") && Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
			int input;
			if(!Util.isInteger(Util.getCommandContents(contents)) 
					|| (input = Integer.parseInt(Util.getCommandContents(contents))) > 100 || input < 0) {
				em.withColor(Color.RED)
				.withTitle("Error")
				.withDesc("Please set volume between 0 and 100");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withColor(Color.GREEN)
			.withTitle("Success")
			.withDesc("Set volume to " + contents);
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			g.getMusicManager().getAudioPlayer().setVolume(input);
			return;
		} else if(Util.isInteger(contents)) {
			int index = Integer.parseInt(contents);
			if((index) > g.getHistoricalSearches().getHistoricalMusic().size() || index < 1) {
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
		IVoiceChannel v;
		if(!Util.connectedToChannel((v = Bot.getInstance().getBot()
				.getVoiceChannelByID(g.getSpecialChannels().getVoice()))))
			v.join();
		GuildMusicManager.loadAndPlay(msg.getChannel(), contents);
		//MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
