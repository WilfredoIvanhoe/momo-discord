package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.util.ArrayList;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.JishoObject;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Search Jisho for a term, english or japanese
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "jisho",
		aliases = {"jisyo", "eewa", "waee", "nihongo"},
		permission = Permission.NONE,
		description = "Lookup Jisho.org for a Japanese term",
		example = "house"
		)
public class Jisho implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String word = Util.getCommandContents(msg);
		ArrayList<JishoObject> jA = JishoObject.searchVocabulary(word);
		if(jA.size() == 0 || jA == null) {
			em.withColor(Color.RED).withTitle("No results found")
				.withDesc("No results found for: " + word).withTimestamp(System.currentTimeMillis());
		} else {
			em.withColor(Color.CYAN);
			em.withAuthorName("Jisho results for " + word);
			em.withAuthorUrl("http://jisho.org/search/" + word);
			JishoObject j = jA.get(0);
			//No guarantee this service will be up in the future, if someone is using this in like 2026
			em.withThumbnail("http://iriguchi.moe/includes/kanji.php?kanji=" + j.getKanji());
			em.appendField("Reading", j.getKana(), true);
			em.appendField("Kanji", j.getKanji(), true);
			em.appendField("Definition(s)", j.getEnglishDefinitions(), false);
			em.ignoreNullEmptyFields();
			
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}
