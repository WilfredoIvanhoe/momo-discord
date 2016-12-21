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
 * Change music channel
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "welcomechannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the welcome channel for server.\n"
				+ "If this and a welcome message are set, the bot will send the welcome message to the designated channel",
		example = "(no parameters)"
		)
public class SetWelcomeChannel implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		String currentChannel = msg.getChannel().getID();
		EmbedBuilder em = new EmbedBuilder().withTitle("Success");
		if(currentChannel.equals(g.getSpecialChannels().getWelcome())) {
			em.withColor(Color.CYAN).withDesc("Removed **" + msg.getChannel().getName() + "** as welcome channel");
			g.getSpecialChannels().setWelcome("");
		} else {
			em.withColor(Color.GREEN).withDesc("Set **" + msg.getChannel().getName() + "** as welcome channel");
			g.getSpecialChannels().setWelcome(currentChannel);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
