package io.ph.bot.rest.saucenao;

import java.net.URL;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SauceNaoAPI {
final String ENDPOINT = "https://saucenao.com/";
	
	@GET("search.php")
	Call<SauceNaoResult> getSauce(@Query("url") URL url);
}
