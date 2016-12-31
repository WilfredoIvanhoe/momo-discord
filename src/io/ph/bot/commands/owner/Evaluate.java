package io.ph.bot.commands.owner;

import java.awt.Color;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Skeleton of a scripting engine
 * Pretty cool stuff with Java 8
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "evaluate",
		aliases = {"eval", "exec", "execute"},
		permission = Permission.BOT_OWNER,
		description = "Evaluate with the Nashorn scripting engine",
		example = "Bot.getGuilds().size();"
		)
public class Evaluate implements Command {
	static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	@Override
	public void executeCommand(IMessage msg) {
		String contents = Util.getCommandContents(msg);
		EmbedBuilder em = new EmbedBuilder();
		em.withTitle("Evaluation engine");
		em.withColor(Color.MAGENTA);
		em.appendField("Command", contents, false);
		try {
			Object o;
			if(contents.startsWith("Bot.")) {
				o = engine.eval("Java.type('io.ph.bot.Bot').getInstance().getBot()" + contents.substring(3));
			} else {
				o = engine.eval(String.format("Java.type('io.ph.bot.%s').%s", contents.substring(0, contents.indexOf(" ")),
						contents.substring(contents.indexOf(" ") + 1)));
			}
			if(o == null)
				o = "Void return";
			em.appendField("Results", o.toString(), false);
		} catch (ScriptException e) {
			em.appendField("Results", String.format("```java\n%s```", e.toString()), false);
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
}
