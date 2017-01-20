package io.ph.bot.audio.sources;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import com.eclipsesource.json.JsonObject;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.util.Util;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sx.blah.discord.handle.obj.IMessage;

public class Soundcloud extends MusicSource {

	public Soundcloud(URL url, IMessage msg) throws FileTooLargeException, IOException, NoAPIKeyException {
		super(url, msg);
	}

	@Override
	protected void downloadLocally() throws FileTooLargeException, IOException, NoAPIKeyException {
		JsonObject jo = Util.jsonFromUrl("https://api.soundcloud.com/resolve.json?url="
				+ super.getUrl().toString()
				+ "&client_id=" + Bot.getInstance().getApiKeys().get("soundcloud"))
				.asObject();
		if(jo.getInt("duration", Integer.MAX_VALUE) > (15 * 60 * 1000)) {
			throw new FileTooLargeException(super.getUrl());
		}
		super.setTitle(jo.getString("title", ""));
		int id = jo.getInt("id", 1);
		URL download = new URL("https://api.soundcloud.com/tracks/" 
				+ id + "/stream?client_id=" 
				+ Bot.getInstance().getApiKeys().get("soundcloud"));
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
				.url(download)
				.build();
		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) 
			throw new IOException("Unexpected response " + response);
		Files.copy(response.body().byteStream(), super.getSource().toPath());
	}

}
