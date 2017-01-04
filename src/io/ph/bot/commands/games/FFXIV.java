package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadCharacterException;
import io.ph.bot.model.Permission;
import io.ph.bot.model.games.FFXIVCharacter;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Lookup a FFXIV character and display useful statistics given by its Lodestone
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "ffxiv",
		aliases = {"ff14"},
		permission = Permission.NONE,
		description = "Lookup a character in the Final Fantasy 14 Lodestone search",
		example = "sargatanas first-name last-name"
		)
public class FFXIV implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		if(split.length != 3) {
			EmbedBuilder em = MessageUtils.commandErrorMessage(msg, "ffxiv", "server first-name last-name", 
					"*server* - Name of the server you are on",
					"*first-name* - First name of your character",
					"*last-name* - Last name of your character");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		IMessage tempMessage = null;
		try {
			tempMessage = MessageUtils.buildAndReturn(msg.getChannel(), 
					new EmbedBuilder().withColor(Color.CYAN).withDesc("Searching...").build());
			FFXIVCharacter xiv = new FFXIVCharacter(split[0], split[1], split[2]);
			EmbedBuilder em = new EmbedBuilder().withColor(Color.GREEN);
			em.withAuthorName(xiv.getFirstName() + " " + xiv.getLastName() + " of " + xiv.getServer());
			em.withAuthorUrl(xiv.getLodestoneLink());
			em.withThumbnail(xiv.getJobImageLink());
			StringBuilder sb = new StringBuilder();
			sb.append("**" + xiv.getGender() + " " + xiv.getRace() + "** | **" + xiv.getFaction() + "**\n");
			sb.append("**Nameday**: " + xiv.getNameday() + " | **Guardian**: " + xiv.getGuardian() + "\n");
			sb.append("**Grand Company**: " + xiv.getGrandCompany() + "\n");
			sb.append("**Free Company**: " + xiv.getFreeCompany());
			em.withDesc(sb.toString());
			em.withImage(xiv.getImageLink());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (IOException e) {
			e.printStackTrace();
			MessageUtils.sendMessage(msg.getChannel(), "Square Enix server timed out. Please try again later");
		} catch (BadCharacterException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error finding your character", 
					Util.getPrefixForGuildId(msg.getGuild().getID()) + "ffxiv server-name first-name last-name");
		} finally {
			try {
				tempMessage.delete();
			} catch (Exception e2) { }
		}
	}
}
