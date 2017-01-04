package io.ph.bot.commands.general;

import java.awt.Color;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
@CommandData (
		defaultSyntax = "rolecount",
		aliases = {"rolelist", "rolestats"},
		permission = Permission.NONE,
		description = "Get meta information about your joinable roles",
		example = "(no parameters)"
		)
public class RoleCount implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		if(g.getJoinableRoles().isEmpty()) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like your server doesn't have any joinable roles!");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		StringBuilder sb = new StringBuilder();
		Supplier<Stream<String>> stream = () -> g.getJoinableRoles().stream().sorted((a, b) -> Integer.compare(msg.getGuild().getUsersByRole(msg.getGuild().getRoleByID(b)).size(),
				(msg.getGuild().getUsersByRole(msg.getGuild().getRoleByID(a)).size())));
		stream.get().forEach(s ->{
			sb.append(String.format("**%s** | %d\n", msg.getGuild().getRoleByID(s).getName(), 
					msg.getGuild().getUsersByRole(msg.getGuild().getRoleByID(s)).size()));
		});
		
		em.withColor(msg.getGuild().getRoleByID(stream.get().findFirst().get()).getColor());
		em.withTitle("Role ranking");
		em.withDesc(sb.toString());
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
