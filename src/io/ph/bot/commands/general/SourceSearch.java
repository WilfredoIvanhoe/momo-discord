package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.model.Permission;
import io.ph.bot.rest.RESTCache;
import io.ph.bot.rest.saucenao.Result;
import io.ph.bot.rest.saucenao.SauceNaoAPI;
import io.ph.bot.rest.saucenao.SauceNaoResult;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageList;

/**
 * Search SauceNao for either a given image URL or attachment
 * If no valid image is found, then go back up to 10 messages searching for one
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "source",
		aliases = {"sauce"},
		permission = Permission.NONE,
		description = "Check SauceNao for the source on an image\n"
				+ "Will first check your message for an image URL or attachment. "
				+ "If nothing is found, it will go back at most 10 messages searching for a valid iamge",
		example = "http://i.imgur.com/oRPyLuc.jpg"
		)
public class SourceSearch implements Command {
	private static final int SEARCH = 10;
	EmbedBuilder em;
	@Override
	public void executeCommand(IMessage msg) {
		URL url = resolveUrl(msg);
		em = new EmbedBuilder();
		if(url == null) {
			em.withColor(Color.RED).withTitle("Error").withDesc("No image found in your message or the previous " + SEARCH + " messages");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		Interceptor interceptor;
		Builder builder = new Retrofit.Builder()
				.baseUrl(SauceNaoAPI.ENDPOINT)
				.addConverterFactory(GsonConverterFactory.create());
		if((interceptor = queryInterceptor()) != null) {
			builder.client((new OkHttpClient.Builder()).addInterceptor(interceptor).build());
		} else {
			em.withColor(Color.RED).withTitle("Error").withDesc("This bot isn't setup to search SauceNao");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		Retrofit rf = builder.build();
		
		SauceNaoAPI api = rf.create(SauceNaoAPI.class);
		Call<SauceNaoResult> sauceCall = api.getSauce(url);
		try {
			SauceNaoResult sauce = sauceCall.execute().body();
			if(sauce.getResults().isEmpty()) {
				em.withColor(Color.RED).withTitle("Error").withDesc(String.format("No results found on SauceNao for <%s>", url.toString()));
				em.withThumbnail(url.toString());
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			resolveData(sauce);
		} catch (IOException e) {
			e.printStackTrace();
		}
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	
	private void resolveData(SauceNaoResult sauce) {
		Result image = sauce.getResults().get(0);
		if(image.getHeader().getThumbnail() != null)
			em.withThumbnail(image.getHeader().getThumbnail());
		em.ignoreNullEmptyFields();
		em.withColor(Color.MAGENTA);
		em.appendField("Similarity", String.format("%s%%", image.getHeader().getSimilarity()), true);
		switch(image.getHeader().getIndexId()) {
		case 5: // Pixiv
			em.withTitle(String.format("%s by %s", image.getData().getTitle(), image.getData().getMemberName()));
			
			em.appendField("Artist", String.format("http://www.pixiv.net/member.php?id=%d", image.getData().getMemberId()), true);
			em.appendField("Original", String.format("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=%d",
					image.getData().getPixivId()), true);
			break;
		case 9: //Danbooru
			em.withTitle(String.format("Artist: %s", image.getData().getCreator()));
			em.appendField("Artist", image.getData().getCreator(), true);
			em.appendField("Original", String.format("https://danbooru.donmai.us/posts/%d", image.getData().getDanbooruId()), true);
			break;
		default:
			em.withTitle("Source unknown").withColor(Color.RED).withDesc("Please report this to Kagumi: Error for " + image.getHeader().getIndexName());
			break;
		}
	}
	/**
	 * Return interceptor that adds on these API keys - null if not
	 * @return Interceptor
	 * @throws NoAPIKeyException No API key, returns null
	 */
	private static Interceptor queryInterceptor() {
		return new Interceptor() {  
		    @Override
		    public Response intercept(Chain chain) throws IOException {
		        Request original = chain.request();
		        HttpUrl originalHttpUrl = original.url();
		        HttpUrl url;
				try {
					url = originalHttpUrl.newBuilder()
					        .addQueryParameter("output_type", "2")
					        .addQueryParameter("db", "999")
					        .addQueryParameter("api_key", Bot.getInstance().getApiKeys().get("saucenao"))
					        .addQueryParameter("numres", "1")
					        .build();
				} catch (NoAPIKeyException e) {
					return null;
				}
		        Request.Builder requestBuilder = original.newBuilder()
		                .url(url);
		        Request request = requestBuilder.build();
		        return chain.proceed(request)
		        		.newBuilder()
		        		.header("Cache-Control", "public, max-age=" + RESTCache.CACHE_AGE)
		        		.build();
		    }
		};
	}
	/**
	 * Resolve a URL from either current msg/attachments or past 5 msgs
	 * @param msg Message to check and index from
	 * @return URL if image, null if not
	 */
	private static URL resolveUrl(IMessage msg) {
		URL url;
		if(Util.getCommandContents(msg).isEmpty() && msg.getAttachments().isEmpty()) {
			//System.out.println("Checking for empty sauce");
			MessageList list = msg.getChannel().getMessages();
			for(int i = 0; i < SEARCH; i++) {
				try {
					//System.out.println(list.get(i).getAttachments().isEmpty() + " | " + list.get(i).getContent());
					if(!list.get(i).getAttachments().isEmpty()) {
						if(checkMime((url = new URL(list.get(i).getAttachments().get(0).getUrl()))) != null)
							return url;
					} else {
						if(checkMime((url = new URL(list.get(i).getContent()))) != null)
							return url;
					}
				} catch (MalformedURLException e) {	}
			}
		} else {
			try {
				if(!msg.getAttachments().isEmpty()) {
					if(checkMime((url = new URL(msg.getAttachments().get(0).getUrl()))) != null)
						return url;
				} else {
					if(checkMime((url = new URL(Util.getCommandContents(msg)))) != null)
						return url;
				}
			} catch (MalformedURLException e) {	}
		}
		return null;
	}

	private static URL checkMime(URL url) {
		String mime = Util.getMimeFromUrl(url);
		if(mime != null && (mime.contains("jpeg") || mime.contains("png")))
			return url;
		return null;
	}
}
