package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandSyntax;
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
@CommandSyntax (
		defaultSyntax = "musicchannel",
		aliases = {},
		permission = Permission.MANAGE_SERVER,
		description = "Change the announcement channel for music purposes.\n"
				+ "If set, the bot will ping the user who queued up the next song",
		example = "(no parameters)"
		)
public class SetMusicChannel implements Command {

	@Override
	public void run(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		String currentChannel = msg.getChannel().getID();
		EmbedBuilder em = new EmbedBuilder().withTitle("Success");
		if(currentChannel.equals(g.getSpecialChannels().getMusic())) {
			em.withColor(Color.CYAN).withDesc("Removed **" + msg.getChannel().getName() + "** as music channel");
			g.getSpecialChannels().setMusic("");
		} else {
			em.withColor(Color.GREEN).withDesc("Set **" + msg.getChannel().getName() + "** as music channel");
			g.getSpecialChannels().setMusic(currentChannel);
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
