package io.ph.bot.model.anime;

import java.io.IOException;
import java.net.URLEncoder;
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
	private static final String kitsuApiLink = "https://kitsu.io/api/edge/";
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
	 * @param search Search term
	 */
	public static ArrayList<KitsuAnime> forName(String search) {
		try {
			JsonValue j = Util.jsonFromUrl(kitsuApiLink + "anime?filter[text]=" + URLEncoder.encode(search, "UTF-8"));
			ArrayList<KitsuAnime> toReturn = new ArrayList<KitsuAnime>();
			int limit = 0;
			for(JsonValue jv : j.asObject().get("data").asArray()) {
				if(limit++ >= 10) {
					// send message
					break;
				}
				JsonObject jo = jv.asObject();
				System.out.println(jo.getString("id", "-1"));
				if(jo.get("attributes").asObject().get("titles").asObject().get("en").isNull()) {
					System.out.println(jo.get("attributes").asObject().get("titles").asObject().getString("en_jp", "en_jp name"));
				} else {
					System.out.println(jo.get("attributes").asObject().get("titles").asObject().getString("en", "en name"));
				}	 
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
