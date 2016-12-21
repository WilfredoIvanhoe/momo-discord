package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Setup a music channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "setupmusic",
		aliases = {"initializemusic"},
		permission = Permission.MANAGE_SERVER,
		description = "Setup a music channel and have the bot join it on startup",
		example = "(no parameters)"
		)
public class SetupMusic implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		if((!g.getSpecialChannels().getVoice().equals("") || g.getSpecialChannels().getVoice() != null)
				&& msg.getGuild().getVoiceChannelByID(g.getSpecialChannels().getVoice()) != null) {
			em.withColor(Color.RED);
			em.withTitle("Error");
			em.withDesc("Looks like I already have a music channel here");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		IVoiceChannel voice = null;
		try {
			voice = msg.getGuild().createVoiceChannel("music");
			voice.join();
			g.getSpecialChannels().setVoice(voice.getID());
			em.withColor(Color.GREEN);
			em.withTitle("Success");
			em.withDesc("Setup your music channel. I suggest doing " + g.getGuildConfig().getCommandPrefix() + "musicchannel to setup a "
					+ "music announcement channel");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (DiscordException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Discord error: " + e.getMessage());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like I don't have permissions to create the music channel");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		} catch (RateLimitException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Rate limited! Please try again later");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		}
		
	}

}
