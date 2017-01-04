package io.ph.bot.commands.japanese;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.LoggerFactory;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.exception.NoSearchResultException;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.bot.model.Theme;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Search for a theme from the Themes.moe api
 * @author Paul
 */
@CommandData (
		defaultSyntax = "theme",
		aliases = {"animetheme"},
		permission = Permission.NONE,
		description = "Search for an anime theme song off Themes.moe",
		example = "shinsekai yori"
		)
public class ThemeSearch implements Runnable, Command {

	private IMessage msg;
	
	public ThemeSearch() { }
	
	private ThemeSearch(IMessage msg) {
		this.msg = msg;
	}
	@Override
	public void executeCommand(IMessage msg) {
		Runnable t = new ThemeSearch(msg);
		new Thread(t).start();
	}
	
	private void process(IMessage msg) {
		String search = Util.getCommandContents(msg);
		if(search.equals("")) {
			MessageUtils.sendMessage(msg.getChannel(),
					MessageUtils.commandErrorMessage(msg, "themes", "anime-name", "**anime-name** - name of the anime you are searching for").build());
			return;
		}
		IMessage tempMessage = null;
		EmbedBuilder em = new EmbedBuilder();
		if(Util.isInteger(search)) {
			Map<Integer, ArrayList<Theme>> historical 
				= Guild.guildMap.get(msg.getGuild().getID()).getHistoricalSearches().getHistoricalThemeSearchResults();
			int given = Integer.parseInt(search);
			if((given) > historical.size() || given < 1) {
				MessageUtils.sendErrorEmbed(msg.getChannel(), "Invalid input",
						"Giving a number will provide detailed information on a previous "
								+ Util.getPrefixForGuildId(msg.getGuild().getID()) + "themes search. This # is too large");
				return;
			}
			
			em.withColor(Color.GREEN);
			StringBuilder sb = new StringBuilder();
			int i = 0;
			String title = null;
			for(Theme t : historical.get(given)) {
				sb.append("**" + (++i) + ") " + t.getType() + "** <" + t.getLink() + ">  \"" + t.getSongTitle() +  "\"\n");
				title = t.getAnimeTitle();
			}
			em.withTitle(title);
			em.withDesc(sb.toString());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		try {
			tempMessage = MessageUtils.buildAndReturn(msg.getChannel(), 
					new EmbedBuilder().withColor(Color.CYAN).withDesc("Searching...").build());
			Map<String, ArrayList<Theme>> map = Theme.getThemeResults(search);
			if(map.size() == 1) {
				String key = null;
				for(Map.Entry<String, ArrayList<Theme>> entry : map.entrySet()) {
					key = entry.getKey();
					break;
				}
				em.withTitle(key);
				em.withColor(Color.GREEN);
				StringBuilder sb = new StringBuilder();
				int i = 0;
				Guild.guildMap.get(msg.getGuild().getID()).getHistoricalSearches().getHistoricalMusic().clear();
				for(Theme t : map.get(key)) {
					sb.append("**" + (++i) + ") " + t.getType() + "** <" + t.getLink() + ">  \"" + t.getSongTitle() +  "\"\n");
					Guild.guildMap.get(msg.getGuild().getID())
						.getHistoricalSearches().addHistoricalMusic(i, new String[] {t.getSongTitle(), t.getLink()});
				}
				if(Guild.guildMap.get(msg.getGuild().getID()).getMusicManager() != null) {
					em.withFooterText("Use "+ Util.getPrefixForGuildId(msg.getGuild().getID()) + "music # to play");
				}
				em.withDesc(sb.toString());
			} else {
				em.withTitle("Multiple results found");
				em.withColor(Color.WHITE);
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(Map.Entry<String, ArrayList<Theme>> entry : map.entrySet()) {
					Guild.guildMap.get(msg.getGuild().getID()).getHistoricalSearches().getHistoricalThemeSearchResults().put(++i, entry.getValue());
					sb.append("**" + i + ")** " + entry.getKey() + "\n");
				}
				StringBuilder footer = new StringBuilder();
				footer.append("use " + Util.getPrefixForGuildId(msg.getGuild().getID()) + "theme # to search");
				
				em.withFooterText(footer.toString());
				em.withDesc(sb.toString());
			}
		
		} catch (NoSearchResultException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("No search results for " + search)
				.withFooterText("Note: Not all anime are indexed at Themes.moe");
			e.printStackTrace();
		} catch (IOException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Themes.moe may be having issues - please try again later");
			e.printStackTrace();
		} catch (NoAPIKeyException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Looks like this bot doesn't have access to Themes.moe");
			LoggerFactory.getLogger(ThemeSearch.class).error("You do not have an API key for Themes.moe setup in Bot.properties");
		} finally {
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			try {
				tempMessage.delete();
			} catch(Exception e) {}
		}
	}
	@Override
	public void run() {
		process(this.msg);
	}

}
