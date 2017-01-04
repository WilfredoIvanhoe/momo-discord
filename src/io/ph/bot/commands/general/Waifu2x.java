package io.ph.bot.commands.general;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Random;

import org.apache.tika.mime.MimeTypes;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
/**
 * Scale up an image with Waifu2x
 * TODO: Allow user to set parameters
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "waifu2x",
		aliases = {"upscale"},
		permission = Permission.NONE,
		description = "Upscale an image with Waifu2x. Either 1 direct link or attachment",
		example = "[image attachment|direct url]"
		)
public class Waifu2x implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		int i = (new Random()).nextInt(100000);
		File f = new File(String.format("resources/tempdownloads/%d", i));
		String filename = null;
		try {
			if(msg.getAttachments().size() == 0) {
				String contents = Util.getCommandContents(msg);
				Util.saveFile(new URL(contents), f);
				if(contents.lastIndexOf(".") < contents.lastIndexOf("/"))
					filename = contents.substring(contents.lastIndexOf("/") + 1);
				else
					filename = contents.substring(contents.lastIndexOf("/") + 1, contents.lastIndexOf("."));
			} else {
				String contents = msg.getAttachments().get(0).getFilename();
				Util.saveFile(new URL(msg.getAttachments().get(0).getUrl()), f);
				filename = contents.substring(contents.lastIndexOf("/") + 1, contents.lastIndexOf("."));
			}
		} catch (MalformedURLException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("This command requires an image as an attachment or a valid direct URL");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		} catch (IOException e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Error downloading this file");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		InputStream in = null;
		IMessage tempMessage = MessageUtils.buildAndReturn(msg.getChannel(), em.withColor(Color.CYAN).withDesc("Working...").build());
		try {
			if(!Util.getMimeFromFile(f).contains("jpeg") && !Util.getMimeFromFile(f).contains("png")) {
				em.withColor(Color.RED).withTitle("Error").withDesc("This command requires an image as an attachment");
				MessageUtils.sendMessage(msg.getChannel(), em.build());
				return;
			}
			msg.getChannel().sendFile("", false, (in = waifu2x(f)), 
					filename 
					+ "-2x"
					+ MimeTypes.getDefaultMimeTypes().forName(Util.getMimeFromFile(f)).getExtension());

		} catch (Exception e) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Error occured while 2x'ing your image. You've probably reached the resolution limit");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			e.printStackTrace();
		} finally {
			try {
				tempMessage.delete();
				f.delete();
				in.close();
			} catch (Exception e) { }
		}
	}

	
	/**
	 * Upload an image file and 2x upscale it with waifu2x
	 * @param f Image file to upload
	 * @return InputStream of image
	 * @throws IOException Something bad happened en route, check that stacktrace
	 * @throws InvalidMIMEException 
	 */
	private static InputStream waifu2x(File f) throws IOException {
		final String WAIFU2X_API = "http://waifu2x.udp.jp/api";
		HttpURLConnection conn = (HttpURLConnection) (new URL(WAIFU2X_API)).openConnection();
		String bound = Long.toHexString(System.currentTimeMillis());
		String CRLF = "\r\n";
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);
		conn.setRequestProperty("Content-Length", f.length() + "");
		conn.setRequestProperty("User-Agent", 
				"Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; Googlebot/2.1; +http://www.google.com/bot.html) Safari/537.36");
		conn.setRequestProperty("Cookie", "style=art; scale=2; noise=1;");
		conn.setRequestProperty("scale", "2");
		conn.setDoOutput(true);
		OutputStream output = conn.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);
		writer.append("--" + bound).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"url\"").append(CRLF);
		writer.append(CRLF).append(CRLF).flush();
		writer.append("--" + bound).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"style\"").append(CRLF).append(CRLF);
		writer.append("art").append(CRLF).flush();
		writer.append("--" + bound).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"noise\"").append(CRLF).append(CRLF);
		writer.append("2").append(CRLF).flush();
		writer.append("--" + bound).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"scale\"").append(CRLF).append(CRLF);
		writer.append("2").append(CRLF).flush();
		writer.append("--" + bound).append(CRLF);
		writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"").append(CRLF);
		writer.append("Content-Type: image/png").append(CRLF);
		writer.append(CRLF).flush();
		Files.copy(f.toPath(), output);
		output.flush();
		writer.append(CRLF).flush();
		writer.append("--" + bound + "--").append(CRLF).flush();
		return conn.getInputStream();
	}
}
