package io.ph.bot.model.anime;

import org.jsoup.Jsoup;

import com.eclipsesource.json.JsonObject;

import io.ph.bot.exception.NoAPIKeyException;

public class MALAnime {
	private static String baseHummingbirdUrl = "https://hummingbird.me/api/v1/";
	private static String baseMalUrl = "https://myanimelist.net/api/";

	private String title;
	private int episodeCount;
	private String airingStatus;
	private String startDate;
	private String endDate;
	private double malRating;
	private int malId;
	private String synopsis;
	private String imageLink;
	private String malLink;
	private String type;
	
	public MALAnime(JsonObject jo) throws NoAPIKeyException {
		this.title = jo.getString("title", "");
		this.episodeCount = jo.getInt("episodes", 0);
		this.airingStatus = jo.getString("status", "Finished Airing");
		if(this.airingStatus.startsWith("Curr")) {
			this.startDate = jo.getString("started_airing", "2000-01-01");
			this.endDate = jo.getString("end_date", "").equals("0000-00-00") ? null : jo.getString("end_date", "");
		} else if(this.airingStatus.startsWith("Fini")) {
			this.startDate = jo.getString("start_date", "2000-01-01");
			this.endDate = jo.getString("end_date", "2000-01-01");
		} else {
			this.startDate = jo.getString("start_date", "").equals("0000-00-00") ? null : jo.getString("start_date", "");
			this.endDate = jo.getString("end_date", "").equals("0000-00-00") ? null : jo.getString("end_date", "");
		}
		this.malRating = jo.getDouble("score", 0.00);
		this.malId = jo.getInt("id", 0);
		this.malLink = "https://myanimelist.net/anime/" + this.malId;

		this.synopsis = Jsoup.parse(jo.getString("synopsis", "")).text();
		this.synopsis = Jsoup.parse(jo.getString("synopsis", "")).text()
				.replaceAll("\\[[^]]+\\]", "");
		this.imageLink = jo.getString("image", "");
		this.type = jo.getString("type", null);
	}


	public String getType() {
		return this.type;
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
	public double getMalRating() {
		return malRating;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public String getImageLink() {
		return imageLink;
	}
	public String getStartDate() {
		return startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public String getMalLink() {
		return malLink;
	}
	public int getMalId() {
		return malId;
	}
}
