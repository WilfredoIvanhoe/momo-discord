package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.bot.rest.RESTCache;
import io.ph.bot.rest.pokemon.PokemonAPI;
import io.ph.bot.rest.pokemon.model.Pokemon;
import io.ph.bot.rest.pokemon.model.Type;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Search Pokemon by name or ID
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "pokemon",
		aliases = {"poke"},
		permission = Permission.NONE,
		description = "Lookup a Pokemon by name or ID. Gen 7 is not supported yet",
		example = "meloetta-aria"
		)
public class PokemonSearch implements Command {
	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "pokemon", "[name|id]", "*[name|id]* - search a Pokemon by its name or ID");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(PokemonAPI.ENDPOINT)
				.client(RESTCache.client)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		PokemonAPI api = retrofit.create(PokemonAPI.class);
		Call<Pokemon> pokemonCall = api.getPokemon(contents);
		try {
			Pokemon pokemon;
			if((pokemon = pokemonCall.execute().body()) == null) {
				em.withColor(Color.RED).withTitle("Error").withDesc(String.format("Pokemon not found for **%s%%", contents));
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			em.withTitle(StringUtils.capitalize(pokemon.getName()));
			em.withThumbnail(pokemon.getSprites().getFrontDefault());
			em.appendField("National Dex", pokemon.getId() + "", true);
			if(pokemon.getGameIndices().size() > 0)
				em.appendField("First seen", StringUtils.capitalize(Lists.reverse(pokemon.getGameIndices())
						.get(0).getVersion().getName().replaceAll("-", " ")), true);
			em.appendField("Abilities", Lists.reverse(pokemon.getAbilities()).stream()
					.map(a -> a.toString() + (a.getIsHidden() ? " (H)" : "")).collect(Collectors.joining(", ")), true);
			em.appendField("Typing", Joiner.on(" & ").join(Lists.reverse(pokemon.getTypes())), true);
			em.withFooterText("Stat total: " +pokemon.getStats().stream()
					.mapToInt(type -> type.getBaseStat())
					.sum());
			em.withColor(getColor(Lists.reverse(pokemon.getTypes()).get(0)));
		} catch (IOException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something went funny connecting");
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	private Color getColor(Type type) {
		switch(type.getType().getName()) {
		case "normal":
			return Color.decode("#A8A77A");
		case "fighting":
			return Color.decode("#C22E28");
		case "flying":
			return Color.decode("#A98FF3");
		case "poison":
			return Color.decode("#A33EA1");
		case "ground":
			return Color.decode("#E2BF65");
		case "rock":
			return Color.decode("#B6A136");
		case "bug":
			return Color.decode("#A6B91A");
		case "ghost":
			return Color.decode("#735797");
		case "steel":
			return Color.decode("#B7B7CE");
		case "fire":
			return Color.decode("#EE8130");
		case "water":
			return Color.decode("#6390F0");
		case "grass":
			return Color.decode("#7AC74C");
		case "electric":
			return Color.decode("#F7D02C");
		case "psychic":
			return Color.decode("#F95587");
		case "ice":
			return Color.decode("#96D9D6");
		case "dragon":
			return Color.decode("#6F35FC");
		case "dark":
			return Color.decode("#705746");
		case "fairy":
			return Color.decode("#D685AD");
		default: 
			return Color.BLACK;
		}
	}
}
