package io.ph.bot.rest.anime;

import io.ph.bot.rest.anime.kitsu.KitsuAnime;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface KitsuAPI {
	final String ENDPOINT = "https://kitsu.io/api/edge/";

	@Headers({"Accept: application/vnd.api+json",
			"Content-Type: application/vnd.api+json"})
	@GET("anime")
	Call<KitsuAnime> getAnime(@Query("filter[text]") String animeName);
}
