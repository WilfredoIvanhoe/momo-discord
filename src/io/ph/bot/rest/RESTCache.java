package io.ph.bot.rest;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class RESTCache {
	public static OkHttpClient client;
	private static final File cacheDir = new File("resources/cache/");
	
	private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
		@Override public Response intercept(Chain chain) throws IOException {
			Response originalResponse = chain.proceed(chain.request());
			int maxAge = 60 * 60 * 24 * 7;
			return originalResponse.newBuilder()
					.header("Cache-Control", "public, max-age=" + maxAge)
					.build();
		}
	};
	
	static {
		int cacheSize = 30 * 1024 * 1024;
		Cache cache = new Cache(cacheDir, cacheSize);
		client = new OkHttpClient.Builder()
				.cache(cache)
				.addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
				.build();
	}
	
}
