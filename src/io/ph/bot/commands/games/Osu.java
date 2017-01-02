package io.ph.bot.commands.games;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Permission;
import io.ph.bot.rest.osu.OsuAPI;
import io.ph.bot.rest.osu.OsuUser;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * osu! user lookup
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "osu",
		aliases = {"circles"},
		permission = Permission.NONE,
		description = "Lookup an osu! user",
		example = "username"
		)
public class Osu implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		String contents = Util.getCommandContents(msg);
		if(contents.isEmpty()) {
			em = MessageUtils.commandErrorMessage(msg, "osu", "username", "*username* - osu! username to lookup");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(OsuAPI.ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		OsuAPI osuApi = retrofit.create(OsuAPI.class);
		try {
			Call<List<OsuUser>> callUser = osuApi.getUser(contents, Bot.getInstance().getApiKeys().get("osu"));
			List<OsuUser> userList = callUser.execute().body();
			if(userList.isEmpty()) {
				em.withColor(Color.RED).withTitle("Error").withDesc(String.format("osu! user not found for **%s**", contents));
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			OsuUser user = userList.get(0);
			em.withAuthorName("osu! results for " + user.getUsername());
			em.withAuthorUrl("https://osu.ppy.sh/u/" + user.getUserId());
			em.appendField("Ranking", NumberFormat.getInstance().format(Integer.parseInt(user.getPpRank())), true);
			em.appendField("PP", NumberFormat.getInstance().format((int) Double.parseDouble(user.getPpRaw())), true);
			em.appendField("Accuracy", (new DecimalFormat("#,###.00")).format(Double.parseDouble(user.getAccuracy())) + "%", true);
			em.appendField("Play Count", NumberFormat.getInstance().format(Integer.parseInt(user.getPlaycount())), true);
			em.appendField("SS Count", NumberFormat.getInstance().format(Integer.parseInt(user.getCountRankSs())), true);
			em.appendField("S Count", NumberFormat.getInstance().format(Integer.parseInt(user.getCountRankS())), true);
			em.withThumbnail(String.format("https://a.ppy.sh/%s_1.jpg", user.getUserId()));
			em.withColor(Color.PINK);
		} catch (NoAPIKeyException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like this bot isn't setup to do osu! lookups");
			e.printStackTrace();
		} catch (IOException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Something went funny with the osu! servers");
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	
}
