package io.ph.bot.audio.sources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sx.blah.discord.handle.obj.IMessage;

public class Webm extends MusicSource {

	public Webm(URL url, IMessage msg) throws FileTooLargeException, IOException, NoAPIKeyException {
		super(url, msg);
	}

	@Override
	protected void downloadLocally() throws FileTooLargeException, IOException, NoAPIKeyException {
		FFmpeg ffmpeg;
		if((getFileSize(super.getUrl()) / (1024*1024) > 40)
				|| getFileSize(super.getUrl()) == -1) {
			throw new FileTooLargeException(super.getUrl());
		}
		ffmpeg = new FFmpeg();
		FFmpegBuilder build = new FFmpegBuilder();
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
				.url(super.getUrl())
				.build();
		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) 
			throw new IOException("Unexpected response " + response);
		Files.copy(response.body().byteStream(), super.getSource().toPath());

		build.setInput("resources/tempdownloads/" + super.getFileSeed());
		build.addOutput("resources/tempdownloads/" + super.getFileSeed() + "out.mp3").setFormat("mp3").disableVideo().setAudioChannels(1);
		FFmpegExecutor exe = new FFmpegExecutor(ffmpeg, new FFprobe());
		exe.createJob(build).run();

		File after = new File("resources/tempdownloads/" + super.getFileSeed() + "out.mp3");
		super.getSource().delete();
		super.setSource(after);
	}

}
