package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Change Twitch channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "twitchchannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the announcement channel for Twitch.tv announcements.\n"
				+ "If set, the bot will notify when a registered user goes online or offline",
		example = "(no parameters)"
		)
public class SetTwitchChannel implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		String currentChannel = msg.getChannel().getID();
		EmbedBuilder em = new EmbedBuilder().withTitle("Success");
		if(currentChannel.equals(g.getSpecialChannels().getTwitch())) {
			em.withColor(Color.CYAN).withDesc("Removed **" + msg.getChannel().getName() + "** as Twitch.tv announcement channel");
			g.getSpecialChannels().setTwitch("");
		} else {
			em.withColor(Color.GREEN).withDesc("Set **" + msg.getChannel().getName() + "** as Twitch.tv announcement channel channel");
			g.getSpecialChannels().setTwitch(currentChannel);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
