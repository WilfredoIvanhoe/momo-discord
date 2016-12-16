package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadUsernameException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.exception.NoPermissionException;
import io.ph.bot.exception.UnspecifiedException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.bot.model.TwitchObject;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Register a Twitch.tv account to be polled for streaming/not streaming notifications
 * If the server does not have a Twitch channel setup, then this will deny functionality
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "unregistertwitch",
		aliases = {},
		permission = Permission.NONE,
		description = "Unregister a username from Twitch.tv notifications",
		example = "TSM_theoddone"
		)
public class UnregisterTwitch implements Command {

	@Override
	public void run(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		if(Guild.guildMap.get(msg.getGuild().getID()).getSpecialChannels().getTwitch().equals("")) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like this server does not have a Twitch.tv announcement channel setup\n"
					+ "If you have the Manage Server permission, set a channel with " 
					+ Util.getPrefixForGuildId(msg.getGuild().getID()) + "twitchchannel");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		String contents = Util.getCommandContents(msg);
		if(contents.equals("")) {
			em = MessageUtils.commandErrorMessage(msg, "twitch", "twitch-username", 
					"*twitch-username* - Username you want to unregister");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		TwitchObject to;
		try {
			to = new TwitchObject(contents, msg.getGuild().getID(), msg.getAuthor().getID());
		} catch (NoAPIKeyException e1) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like the person running this bot does not have a Twitch.tv API key setup yet");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} catch (IOException e1) {
			em.withColor(Color.RED).withTitle("Error").withDesc("There was a problem accessing the Twitch.tv API");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e1.printStackTrace();
			return;
		} catch (UnspecifiedException e1) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something bad happened with that username...");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e1.printStackTrace();
			return;
		} catch (BadUsernameException e1) {
			em.withColor(Color.RED).withTitle("Error").withDesc("That isn't a valid username!");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			if(to.unregister(msg.getAuthor().getID())) {
				em.withTitle("Success").withColor(Color.GREEN).withDesc("Unregistered **" + contents + "** from Twitch.tv updates");
			} else {
				em.withTitle("Error").withColor(Color.RED).withDesc("Twitch username **" + contents + "** not registered for updates");
			}
		} catch(NoPermissionException e) {
			em.withTitle("Error").withColor(Color.RED).withDesc("You cannot delete this Twitch.tv update registration");
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());


	}

}
