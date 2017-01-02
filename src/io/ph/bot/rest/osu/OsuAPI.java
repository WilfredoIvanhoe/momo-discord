package io.ph.bot.rest.osu;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OsuAPI {
	final String ENDPOINT = "https://osu.ppy.sh/api/";

	@GET("get_user")
	Call<List<OsuUser>> getUser(@Query("u") String username,
			@Query("k") String osuKey);

}
