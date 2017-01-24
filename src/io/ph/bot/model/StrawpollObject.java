package io.ph.bot.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import io.ph.util.Util;

public class StrawpollObject {
	private String title;
	private boolean multiVote;
	private String[] options;
	private int[] votes;

	//They claim to support HTTPS but they don't. Wew
	private static String baseApiUrl = "http://www.strawpoll.me/api/v2/polls";

	public StrawpollObject(String title, boolean multiVote, String[] options, int[] votes) {
		this.title = title;
		this.multiVote = multiVote;
		this.options = options;
		this.votes = votes;
	}

	public StrawpollObject(String title, boolean multiVote, String... options) {
		this.title = title;
		this.multiVote = multiVote;
		this.options = options;
	}

	/**
	 * Create the poll as represented by this object
	 * @return ID of the created poll
	 * @throws IOException Something bad happened when accessing the resource
	 */
	public int createPoll() throws IOException {
		JsonObject jo = new JsonObject();
		jo.add("title", this.title);
		jo.add("multi", this.multiVote);
		JsonArray jOptions = new JsonArray();
		for(String s : this.options) {
			if(s == null)
				continue;
			jOptions.add(s);
		}
		jo.add("options", jOptions);

		HttpURLConnection conn = (HttpURLConnection) (new URL(baseApiUrl)).openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", jo.toString().length() + "");
		conn.getOutputStream().write(jo.toString().getBytes("UTF-8"));
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

		return Json.parse(rd).asObject().getInt("id", 1);
	}
	/**
	 * Get strawpoll from its ID
	 * @param id ID to lookup
	 * @return Strawpoll Object
	 * @throws IOException Strawpoll.me is down or denied our API access
	 */
	public static StrawpollObject fromId(int  id) throws IOException {
		JsonValue jv = Util.jsonFromUrl(baseApiUrl + "/" + id);
		JsonObject jo = jv.asObject();
		String title = jo.getString("title", "title");
		boolean multiVote = jo.getBoolean("multi", false);

		JsonArray jOptions = jo.get("options").asArray();
		String[] options = new String[jOptions.size()];
		JsonArray jVotes = jo.get("votes").asArray();
		int[] votes = new int[jVotes.size()];
		for(int i = 0; i < options.length; i++) {
			options[i] = jOptions.get(i).asString();
			votes[i] = jVotes.get(i).asInt();
		}
		return new StrawpollObject(title, multiVote, options, votes);
	}

	public String getTitle() {
		return title;
	}

	public boolean isMultiVote() {
		return multiVote;
	}

	public String[] getOptions() {
		return options;
	}

	public int[] getVotes() {
		return votes;
	}
}
