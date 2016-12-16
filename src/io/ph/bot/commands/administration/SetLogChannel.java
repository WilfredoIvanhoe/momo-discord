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
		defaultSyntax = "logchannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the log channel for server.\n"
				+ "If this is set, the bot will send messages detailing certain events (people leaving/joining, bans etc)",
		example = "(no parameters)"
		)
public class SetLogChannel implements Command {

	@Override
	public void run(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		String currentChannel = msg.getChannel().getID();
		EmbedBuilder em = new EmbedBuilder().withTitle("Success");
		if(currentChannel.equals(g.getSpecialChannels().getLog())) {
			em.withColor(Color.CYAN).withDesc("Removed **" + msg.getChannel().getName() + "** as log channel");
			g.getSpecialChannels().setLog("");
		} else {
			em.withColor(Color.GREEN).withDesc("Set **" + msg.getChannel().getName() + "** as log channel");
			g.getSpecialChannels().setLog(currentChannel);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
