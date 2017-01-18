package io.ph.bot.commands.general;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
/**
 * Information & intro
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "info",
		aliases = {"information"},
		permission = Permission.NONE,
		description = "Information on the bot",
		example = "(no parameters)"
		)
public class Info implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		String toSend = String.format("Hi! I'm %s, a pretty comprehensive open-source Discord bot.\n"
				+ "If you want to take a look at my inner workings, "
				+ "feel free to go to my repository at <http://momobot.io/github>\n"
				+ "If you want help or support, join my Discord server here: http://momobot.io/join and look for Kagumi\n"
				+ "If you just want to get started, try %ssetup and %shelp\n"
				+ "Full command list: <http://momobot.io/public/commands.html>\n"
				+ "I am currently running version %s",
				Bot.getInstance().getBot().getOurUser().getDisplayName(msg.getGuild()),
				Guild.guildMap.get(msg.getGuild().getID()).getGuildConfig().getCommandPrefix(),
				Guild.guildMap.get(msg.getGuild().getID()).getGuildConfig().getCommandPrefix(),
				Bot.BOT_VERSION);
		MessageUtils.sendMessage(msg.getChannel(), toSend);
	}

}
