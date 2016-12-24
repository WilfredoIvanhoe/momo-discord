package io.ph.bot.commands.owner;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Diagnostics about the bot
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "diagnostics",
		aliases = {},
		permission = Permission.BOT_OWNER,
		description = "Diagnostic information on the bot",
		example = "(no parameters)"
		)
public class Diagnostics implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(Bot.getInstance().getBot().getOurUser().getAvatarURL());
		em.withAuthorName(Bot.getInstance().getBot().getOurUser().getDisplayName(msg.getGuild()) + " diagnostics");
		Runtime r = Runtime.getRuntime();
		NumberFormat format = NumberFormat.getInstance();
		em.appendField("Total shards", Bot.getInstance().getBot().getShardCount() + "", true);
		em.appendField("Connected guilds", Bot.getInstance().getBot().getGuilds().size() + "", true);
		em.appendField("Connected users", Bot.getInstance().getBot().getUsers().size() + "", true);
		em.appendField("Connected text channels", Bot.getInstance().getBot().getChannels().size() + "", true);
		em.appendField("Connected music channels", Bot.getInstance().getBot().getConnectedVoiceChannels().size() + "", true);
		em.appendField("Memory usage", format.format(r.totalMemory() / (1024 * 1024)) + "MB", true);
		em.appendField("CPU usage", getCpuLoad() + "%", true);
		em.appendField("Threads", Thread.activeCount() + "", true);
		em.appendField("Version", Bot.BOT_VERSION, true);
		em.withColor(Color.CYAN);
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

	private double getCpuLoad() {
		// http://stackoverflow.com/questions/18489273/how-to-get-percentage-of-cpu-usage-of-os-from-java
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

			if (list.isEmpty())
				return Double.NaN;

			Attribute att = (Attribute)list.get(0);
			Double value  = (Double)att.getValue();

			// usually takes a couple of seconds before we get real values
			if (value == -1.0)    
				return Double.NaN;
			// returns a percentage value with 1 decimal point precision
			return ((int)(value * 1000) / 10.0);
		} catch(Exception e) {
			return Double.NaN;
		}
	}
}
