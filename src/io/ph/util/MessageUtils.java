package io.ph.util;

import java.awt.Color;

import io.ph.bot.Bot;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

public class MessageUtils {
	/**
	 * Send a message to a channel
	 * @param channel Channel target
	 * @param content Content of message
	 * @return 
	 */
	public static void sendMessage(IChannel channel, String content) {
		sendMessage(channel, content, null, false);
	}
	public static void sendMessage(IChannel channel, EmbedObject embed) {
		sendMessage(channel, "", embed, false);
	}

	/**
	 * Send a message to a channel
	 * @param channel Channel target
	 * @param content Content of message
	 * @param embed EmbedObject to format with
	 */
	private static void sendMessage(IChannel channel, String content, EmbedObject embed, boolean bypass) {
		if(!bypass)
			content = content.replaceAll("@", "\\\\@");
		if(content.equals("") && embed == null)
			return;
		final String s = content;
		RequestBuffer.request(() -> {
			try {
				new MessageBuilder(Bot.getInstance().getBot()).withChannel(channel).withContent(s).withEmbed(embed).build();
			} catch (MissingPermissionsException | DiscordException e) {
				e.printStackTrace();
				return;
			}
		});
	}
	/**
	 * Send a message that shows mentions (for user welcomes)
	 * @param channel Channel to send in
	 * @param content Message to send
	 * @param bypass Bypass to true
	 */
	public static void sendMessage(IChannel channel, String content, boolean bypass) {
		sendMessage(channel, content, null, true);
	}
	public static void sendErrorEmbed(IChannel channel, String title, String description) {
		EmbedBuilder em = new EmbedBuilder().withColor(Color.red).withTitle(title).withDesc(description)
				.withTimestamp(System.currentTimeMillis());
		sendMessage(channel, em.build());
	}

	/**
	 * Create an EmbedBuilder template to notify a user of correct command usage
	 * @param originalCommand Original IMessage sent
	 * @param command Name of the command
	 * @param params String of the parameters you want to represent
	 * @param paramDescription Variable array of parameters and how you want to describe them
	 * @return
	 * TODO: Get rid of this ugly thing
	 */
	public static EmbedBuilder commandErrorMessage(IMessage originalCommand, String command, String params, String... paramDescription) {
		String prefix = Util.getPrefixForGuildId(originalCommand.getGuild().getID());
		StringBuilder sb = new StringBuilder();
		for(String s : paramDescription) {
			sb.append(s+"\n");
		}
		return new EmbedBuilder().withColor(Color.RED).withTitle("Usage: " + prefix + command + " " + params)
				.withDesc(sb.toString());
	}

	public static IMessage buildAndReturn(IChannel channel, EmbedObject embed) {
		RequestBuffer.request(() -> {
			return new MessageBuilder(Bot.getInstance().getBot()).withChannel(channel).withEmbed(embed);
		});
		return null;

	}
	/**
	 * Send a private message to target user
	 * @param target User to send to
	 * @param content Content of message
	 */
	public static void sendPrivateMessage(IUser target, String content) {
		try {
			IPrivateChannel privChannel = Bot.getInstance().getBot().getOrCreatePMChannel(target);
			privChannel.sendMessage(content);
		} catch (DiscordException e) {
			e.printStackTrace();
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}
	public static void sendPrivateMessage(IUser target, EmbedObject embed) {
		try {
			IPrivateChannel privChannel = Bot.getInstance().getBot().getOrCreatePMChannel(target);
			privChannel.sendMessage("", embed, false);
		} catch (DiscordException e) {
			e.printStackTrace();
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}

	}
}
