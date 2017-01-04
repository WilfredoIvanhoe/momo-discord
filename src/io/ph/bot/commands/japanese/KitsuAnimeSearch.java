package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.lang.WordUtils;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.bot.rest.RESTCache;
import io.ph.bot.rest.anime.KitsuAPI;
import io.ph.bot.rest.anime.kitsu.KitsuAnime;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Search for an anime by name on Kitsu.io
 * Does not give multi results back like $mal does... But is overall, more effective
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "anime",
		aliases = {"kitsu"},
		permission = Permission.NONE,
		description = "Search for an anime from Kitsu.io\n",
		example = "shinsekai yori"
		)
public class KitsuAnimeSearch implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		em.ignoreNullEmptyFields();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "kitsu", "anime-name", "*anime-name* - name of anime to search for");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(KitsuAPI.ENDPOINT)
				.client(RESTCache.client)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		KitsuAPI api = retrofit.create(KitsuAPI.class);
		Call<KitsuAnime> call = api.getAnime(contents);
		try {
			KitsuAnime anime;
			if((anime = call.execute().body()) == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc(String.format("No anime results found for **%s**", contents));
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}

			em.withColor(Color.GREEN);
			em.withTitle(anime.getData().get(0).getAttributes().getCanonicalTitle());
			em.withUrl("https://kitsu.io/anime/" + anime.getData().get(0).getAttributes().getSlug());
			if(anime.getData().get(0).getAttributes().getCoverImage() != null)
				em.withImage(anime.getData().get(0).getAttributes().getCoverImage().getOriginal());
			em.appendField("Type", WordUtils.capitalize(anime.getData().get(0).getType()), true);
			if(Util.isDouble(anime.getData().get(0).getAttributes().getAverageRating() + ""))
				em.appendField("Rating", 
						(new DecimalFormat(".##").format(anime.getData().get(0).getAttributes().getAverageRating())) + "/5", true);
			em.appendField("Episodes", anime.getData().get(0).getAttributes().getEpisodeCount() == null ? "not yet aired" : 
				anime.getData().get(0).getAttributes().getEpisodeCount() + "", true);
			StringBuilder aired = new StringBuilder();
			if(anime.getData().get(0).getAttributes().getStartDate() != null)
				aired.append(anime.getData().get(0).getAttributes().getStartDate());
			if(anime.getData().get(0).getAttributes().getEndDate() != null)
				aired.append(" -\n" + anime.getData().get(0).getAttributes().getEndDate());
			if(aired.length() > 0)
				em.appendField("Airing Dates", aired.toString(), true);
			if(anime.getData().get(0).getAttributes().getSynopsis().length() > 500)
				anime.getData().get(0).getAttributes().setSynopsis(anime.getData().get(0).getAttributes().getSynopsis().substring(0, 300) + "...");
			em.appendField("Synopsis", anime.getData().get(0).getAttributes().getSynopsis(), false);
			em.withFooterText("information from kitsu.io");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
