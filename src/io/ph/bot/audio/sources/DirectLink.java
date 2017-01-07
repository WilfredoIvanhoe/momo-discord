package io.ph.bot.audio.sources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import io.ph.bot.audio.MusicSource;
import io.ph.bot.exception.FileTooLargeException;
import io.ph.bot.exception.NoAPIKeyException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sx.blah.discord.handle.obj.IMessage;

public class DirectLink extends MusicSource {
	public DirectLink(URL url, IMessage msg) throws FileTooLargeException, IOException, NoAPIKeyException {
		super(url, msg);
	}

	@Override
	public void downloadLocally() throws FileTooLargeException, IOException {
		if((getFileSize(super.getUrl()) / (1024*1024) > 25)
				|| getFileSize(super.getUrl()) == -1) {
			throw new FileTooLargeException(super.getUrl());
		}
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(super.getUrl())
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
				.build();
		Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new IOException("Unexpected response " + response);
		File copied = new File(super.getSource().getCanonicalPath() + super.getUrl().toString().substring(super.getUrl().toString().lastIndexOf(".")));
		super.getSource().delete();
		super.setSource(copied);
		Files.copy(response.body().byteStream(), super.getSource().toPath());
		try {
			AudioFile f = AudioFileIO.read(super.getSource());
			Tag tag = f.getTag();
			super.setTitle(tag.getFirst(FieldKey.TITLE));
		} catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
			System.err.println("Could not read Title tag");
			e.printStackTrace();
		}
	}
}
