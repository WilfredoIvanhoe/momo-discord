package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.BadCharacterException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Permission;
import io.ph.bot.model.games.WoWCharacter;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Process a wow character and display an embed with useful stats
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "wow",
		aliases = {},
		permission = Permission.NONE,
		description = "Look up a character in the World of Warcraft armory",
		example = "character-name na darkspear"
		)
public class WoW implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		if(split.length < 3) {
			em = MessageUtils.commandErrorMessage(msg, "wow", "character-name region realm-name", 
					"*character-name* - Name of your character",
					"*region* - Region in 2 letter format (i.e. NA or EU)",
					"*realm-name* - Server name");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		if(!split[1].equalsIgnoreCase("na") && !split[1].equalsIgnoreCase("eu") 
				&& !split[1].equalsIgnoreCase("tw")) {
			em.withColor(Color.RED);
			em.withTitle("Error");
			em.withDesc("Valid regions: NA, EU, TW");
			return;
		}
		String serverName = Util.combineStringArray(Util.removeFirstArrayEntry(split));
		serverName = Util.combineStringArray(Util.removeFirstArrayEntry(serverName.split(" ")));

		IMessage tempMessage = null;
		try {
			tempMessage = MessageUtils.sendMessage(msg.getChannel(), 
					new EmbedBuilder().withColor(Color.CYAN).withDesc("Searching...").build());
			WoWCharacter wow = new WoWCharacter(serverName, split[0], split[1].toLowerCase());

			em.withColor(Color.GREEN);
			em.withTitle(wow.getUsername() + " of " + wow.getRealm());
			StringBuilder sb = new StringBuilder();
			sb.append("**Level " + wow.getLevel() + "** " + wow.getGender() + " "  + wow.getRace() + " " + wow.getGameClass() +"\n");
			sb.append("**Item Level**: " + wow.getItemLevel() + "\n");
			if(wow.getGuild() != null)
				sb.append("**Guild**: " + wow.getGuild() + " | **Members**: " + wow.getGuildMembers()+"\n");
			sb.append("**LFR Kills**:        " + wow.getLfrKills() + "\n");
			sb.append("**Normal Kills**: " + wow.getNormalKills() + "\n");
			sb.append("**Heroic Kills**:   " + wow.getHeroicKills() + "\n");
			sb.append("**Mythic Kills**:  " + wow.getMythicKills() + "\n");

			em.withDesc(sb.toString());
			em.withFooterText("Achievement Points: " + wow.getAchievementPoints());
			em.withImage(wow.getThumbnail());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error finding your character", 
					Util.getPrefixForGuildId(msg.getGuild().getID()) + "wow character-name realm-name");
		} catch (IOException e) {
			e.printStackTrace();
			MessageUtils.sendMessage(msg.getChannel(), "Blizzard server timed out. Please try again later");
		} catch (BadCharacterException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error finding your character", 
					Util.getPrefixForGuildId(msg.getGuild().getID()) + "wow character-name realm-name");
		} catch (NoAPIKeyException e) {
			MessageUtils.sendErrorEmbed(msg.getChannel(), "Error", "Sorry, looks like this bot isn't setup to do Battle.net lookups");
		} finally {
			try {
				tempMessage.delete();
			} catch (Exception e2) { }
		}
	}
}
