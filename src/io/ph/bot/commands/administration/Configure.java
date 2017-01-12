package io.ph.bot.commands.administration;

import java.awt.Color;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.commands.moderation.Strawpoll;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.bot.procedural.ProceduralAnnotation;
import io.ph.bot.procedural.ProceduralCommand;
import io.ph.bot.procedural.ProceduralListener;
import io.ph.bot.procedural.StepType;
import io.ph.util.MessageUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "configure",
		aliases = {"config"},
		permission = Permission.MANAGE_SERVER,
		description = "Configure various settings for your server",
		example = "(no parameters)"
		)
@ProceduralAnnotation (
		title = "Bot configuration",
		steps = {"Limit $joinrole to a single role?"}, 
		types = {StepType.YES_NO},
		breakOut = "finish"
		)
public class Configure extends ProceduralCommand implements Command {

	public Configure(IMessage msg) {
		super(msg);
		super.setTitle(getTitle());
	}
	
	public Configure() {
		super(null);
	}

	@Override
	public void executeCommand(IMessage msg) {
		Configure instance = new Configure(msg);
		ProceduralListener.getInstance().addListener(msg, instance);
		instance.sendMessage(getSteps()[super.getCurrentStep()]);
	}

	@Override
	public void finish() {
		boolean answer = (boolean) super.getResponses().get(0);
		Guild.guildMap.get(super.getStarter().getGuild().getID()).getGuildConfig().setLimitToOneRole(answer);
		EmbedBuilder em = new EmbedBuilder();
		em.withColor(Color.GREEN)
		.withTitle("Success")
		.withDesc("Configured my settings for your server!");
		MessageUtils.sendMessage(super.getStarter().getChannel(), em.build());
		super.exit();
	}

}
