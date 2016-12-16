package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandSyntax;
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
@CommandSyntax (
		defaultSyntax = "wow",
		aliases = {},
		permission = Permission.NONE,
		description = "Look up a character in the World of Warcraft armory",
		example = "character-name darkspear"
		)
public class WoW implements Command {

	@Override
	public void run(IMessage msg) {
		String[] split = Util.removeFirstArrayEntry(msg.getContent().split(" "));
		if(split.length < 2) {
			EmbedBuilder em = MessageUtils.commandErrorMessage(msg, "wow", "character-name realm-name", 
					"*character-name* - Name of your character",
					"*realm-name* - Server name");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		String serverName = Util.combineStringArray(Util.removeFirstArrayEntry(split));
		
		IMessage tempMessage = null;
		try {
			tempMessage = MessageUtils.sendMessage(msg.getChannel(), 
					new EmbedBuilder().withColor(Color.CYAN).withDesc("Searching...").build());
			WoWCharacter wow = new WoWCharacter(serverName, split[0]);
			
			EmbedBuilder em = new EmbedBuilder().withColor(Color.GREEN);
			em.withTitle(wow.getUsername() + " of " + wow.getRealm());
			StringBuilder sb = new StringBuilder();
			sb.append("**Level " + wow.getLevel() + "** " + wow.getGender() + " "  + wow.getRace() + " " + wow.getGameClass() +"\n");
			sb.append("**Item Level**: " + wow.getItemLevel() + "\n");
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
