package io.ph.bot.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.eclipsesource.json.Json;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;

/**
 * TODO: This
 * @author Paul
 *
 */
public class AniListAnime {

	private static final String API_URL = "https://anilist.co/api/";
	private String title;
	private int episodeCount;
	private String airingStatus;
	private String startDate;
	private String endDate;
	private double rating;
	private int id;
	private String synopsis;
	private String imageLink;
	private String url;
	private String type;
	
	
	public AniListAnime(String title, int episodeCount, String airingStatus, String startDate, String endDate,
			double rating, int id, String synopsis, String imageLink, String malLink, String type) {
		super();
		this.title = title;
		this.episodeCount = episodeCount;
		this.airingStatus = airingStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.rating = rating;
		this.id = id;
		this.synopsis = synopsis;
		this.imageLink = imageLink;
		this.url = malLink;
		this.type = type;
	}

	private static String getRequestToken() throws NoAPIKeyException {
		String clientId = Bot.getInstance().getApiKeys().get("anilistid");
		String clientKey = Bot.getInstance().getApiKeys().get("anilistsecret");
		HttpsURLConnection conn;
		String accessToken = null;
		try {
			conn = (HttpsURLConnection) new URL(API_URL 
					+ "auth/access_token?grant_type=client_credentials&client_id=" + clientId
					+ "&client_secret=" + clientKey).openConnection();
			conn.setRequestProperty("User-Agent", 
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true); 
			conn.setDoInput(true); 
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			inputLine = in.readLine();
			accessToken = Json.parse(inputLine).asObject().getString("access_token", "null");
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return accessToken;
	}



	public String getTitle() {
		return title;
	}



	public int getEpisodeCount() {
		return episodeCount;
	}



	public String getAiringStatus() {
		return airingStatus;
	}



	public String getStartDate() {
		return startDate;
	}



	public String getEndDate() {
		return endDate;
	}



	public double getRating() {
		return rating;
	}



	public int getId() {
		return id;
	}



	public String getSynopsis() {
		return synopsis;
	}



	public String getImageLink() {
		return imageLink;
	}



	public String getUrl() {
		return url;
	}



	public String getType() {
		return type;
	}
}
