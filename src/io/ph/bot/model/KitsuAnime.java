package io.ph.bot.model;

import java.io.IOException;
import java.util.ArrayList;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import io.ph.util.Util;

/**
 * TODO: This after their API stabilizes
 * @author Paul
 *
 */
public class KitsuAnime {
	private String title;
	private int episodeCount;
	private String airingStatus;
	private String startDate;
	private String endDate;
	private double rating;
	private int id;
	private String synopsis;
	private String imageLink;
	private String malLink;
	private String type;
	
	//anime?filter[attr]=
	private static String kitsuApiLink = "https://kitsu.io/api/edge/";
	public KitsuAnime() {
		
	}
	
	public KitsuAnime(String title, int episodeCount, String airingStatus, String startDate, String endDate,
			double rating, int id, String synopsis, String imageLink, String malLink, String type) {
		this.title = title;
		this.episodeCount = episodeCount;
		this.airingStatus = airingStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.rating = rating;
		this.id = id;
		this.synopsis = synopsis;
		this.imageLink = imageLink;
		this.malLink = malLink;
		this.type = type;
	}

	/**
	 * This might (and will) break soon due to the soon-to-be-added OAuth
	 * Be warned
	 * @param search Search slug
	 */
	public static ArrayList<KitsuAnime> forName(String search) {
		try {
			JsonValue j = Util.jsonFromUrl(kitsuApiLink + "anime?filter[text]=" + search, true);
			ArrayList<KitsuAnime> toReturn = new ArrayList<KitsuAnime>();
			for(JsonValue jv : j.asObject().get("data").asArray()) {
				JsonObject jo = jv.asObject();
			}
			
			return toReturn;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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

	public int getMalId() {
		return id;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public String getImageLink() {
		return imageLink;
	}

	public String getMalLink() {
		return malLink;
	}

	public String getType() {
		return type;
	}
}
