package io.ph.bot.commands.general;

import java.awt.Color;
import java.time.format.DateTimeFormatter;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "userinfo",
		aliases = {"user"},
		permission = Permission.NONE,
		description = "Information on a user",
		example = "@target"
		)
public class UserInfo implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		IUser target;
		if(contents.isEmpty())
			target = msg.getAuthor();
		else if((target = Util.resolveUserFromMessage(msg)) == null) {
			em.withTitle("Error")
			.withColor(Color.RED)
			.withDesc("No user found for " + contents);
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		int mutualServers = (int) Bot.getInstance().getBot().getGuilds().stream()
				.filter(g -> g.getUserByID(msg.getAuthor().getID()) != null)
				.count();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		em.withTitle("User info for " + target.getDisplayName(msg.getGuild()))
		.withColor(Color.MAGENTA)
		.appendField("User", target.getName() + "#" + target.getDiscriminator(), true)
		.appendField("Creation date", msg.getAuthor().getCreationDate().format(formatter), true)
		.appendField("Mutual servers", mutualServers + "", true);
		try {
			em.appendField("Server join date", msg.getGuild().getJoinTimeForUser(msg.getAuthor()).format(formatter), true);
		} catch(DiscordException e) {

		}
		em.withImage(msg.getAuthor().getAvatarURL());
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}


}
