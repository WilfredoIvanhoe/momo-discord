package io.ph.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.tika.Tika;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * Various utility methods
 * TODO: Organize these...
 */
public class Util {

	/**
	 * Resolve a user from an IMessage. Username must be the only parameter in the IMessage
	 * First checks nicknames then usernames
	 * @param s String to check
	 * @return User if found, null if not found
	 */
	public static IUser resolveUserFromMessage(IMessage msg) {
		if(msg.getMentions().size() > 0)
			return msg.getMentions().get(0);
		return resolveUserFromMessage(getCommandContents(msg.getContent()), msg.getGuild());
	}

	/**
	 * Resolve a user from a String - String must be only their username
	 * First checks nicknames then usernames
	 * @param s String to check
	 * @return User if found, null if not found
	 */
	public static IUser resolveUserFromMessage(String toCheck, IGuild guild) {
		for(IUser u : guild.getUsers()) {
			if(u.getNicknameForGuild(guild).isPresent()) {
				if(u.getNicknameForGuild(guild).get().toLowerCase().startsWith(toCheck.toLowerCase())) {
					return u;
				}
			}
		}
		for(IUser u : guild.getUsers()) {
			if(u.getName().toLowerCase().startsWith(toCheck.toLowerCase()))
				return u;
		}
		return null;
	}
	/**
	 * Resolve a banned user from their name
	 * First checks nicknames then usernames
	 * @param s String to check
	 * @param guild Guild to check in
	 * @return User if found, null if not found
	 * @throws DiscordException 
	 * @throws RateLimitException 
	 */
	public static IUser resolveBannedUserFromString(String s, IGuild guild) throws RateLimitException, DiscordException {
		for(IUser u : guild.getBannedUsers()) {
			if(u.getNicknameForGuild(guild).isPresent()) {
				if(u.getNicknameForGuild(guild).get().toLowerCase().startsWith(s.toLowerCase())) {
					return u;
				}
			}
		}
		for(IUser u : guild.getBannedUsers()) {
			if(u.getName().toLowerCase().startsWith(s.toLowerCase()))
				return u;
		}
		return null;
	}
	/**
	 * Similar implementation to JavaScript setTimeout
	 * @param runnable Runnable to run
	 * @param delay Delay in milliseconds
	 * @param async Run in async or blocking
	 */
	public static void setTimeout(Runnable runnable, int delay, boolean async){
		if(async) {
			new Thread(() -> {
				try {
					Thread.sleep(delay);
					runnable.run();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}).start();
		} else {
			try {
				Thread.sleep(delay);
				runnable.run();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	/**
	 * Get command prefix for guild id
	 * @param s Guild ID
	 * @return Command prefix
	 */
	public static String getPrefixForGuildId(String s) {
		return Guild.guildMap.get(s).getGuildConfig().getCommandPrefix();
	}
	/**
	 * Combine a string array into a single String
	 * @param arr String array
	 * @return String of combination
	 */
	public static String combineStringArray(String[] arr) {
		StringBuilder sb = new StringBuilder();
		for(String s : arr) {
			sb.append(s+" ");
		}
		return sb.toString().trim();
	}

	/**
	 * Return ordinal number (number + st, nd, rd, th)
	 * @param i Number to ordinalize (is that a word?)
	 * @return Ordinal String
	 */
	public static String ordinal(int i) {
		int mod100 = i % 100;
		int mod10 = i % 10;
		if(mod10 == 1 && mod100 != 11) {
			return i + "st";
		} else if(mod10 == 2 && mod100 != 12) {
			return i + "nd";
		} else if(mod10 == 3 && mod100 != 13) {
			return i + "rd";
		} else {
			return i + "th";
		}
	}
	/**
	 * Remove the first item from a String array
	 * @param arr String array to manipulate
	 * @return Array without first element
	 */
	public static String[] removeFirstArrayEntry(String[] arr) {
		String[] toReturn = new String[arr.length - 1];
		for(int i = 1; i < arr.length; i++) {
			toReturn[i - 1] = arr[i];
		}
		return toReturn;
	}
	/**
	 * Remove the first item from a String array
	 * @param arr String array to manipulate
	 * @return Array without first element
	 */
	public static String[] removeLastArrayEntry(String[] arr) {
		String[] toReturn = new String[arr.length - 1];
		for(int i = 0; i < arr.length - 1; i++) {
			toReturn[i] = arr[i];
		}
		return toReturn;
	}

	/**
	 * Save a URL to a file with a user-agent header
	 * @param url URL of file
	 * @param destinationFile File to download to
	 * @throws IOException 
	 */
	public static void saveFile(URL url, File destinationFile) throws IOException {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			if(url.getProtocol().equals("https")) {
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			} else {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			}
			out = new FileOutputStream(destinationFile);
			int c;
			byte[] b = new byte[1024];
			while ((c = in.read(b)) != -1)
				out.write(b, 0, c);
			out.flush();
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}
	/**
	 * Get a MIME type from a URL
	 * @param url URL to check
	 * @return String of MIME type
	 * @throws IOException i/o went bad
	 */
	public static String getMimeFromUrl(URL url) {
		InputStream in;
		try {
			if(url.getProtocol().equals("https")) {
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			} else {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			}
			return new Tika().detect(in);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Use Apache Tika to detect MIME type
	 * @param f File to detect
	 * @return String of MIME type
	 * @throws IOException Bad file
	 */
	public static String getMimeFromFile(File f) throws IOException {
		final Tika tika = new Tika();
		return tika.detect(f);
	}
	/**
	 * Returns json value for given String url
	 * @param url url to connect to
	 * @return
	 */
	public static JsonValue jsonFromUrl(String url) throws IOException {
		return Json.parse(stringFromUrl(url));
	}

	/**
	 * Returns string for given String url
	 * @param url url to connect to
	 * @return
	 */
	public static String stringFromUrl(String url) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader rd;
		URL link = new URL(url);
		if(link.getProtocol().equals("https")) {
			HttpsURLConnection conn = (HttpsURLConnection) link.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.connect();
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		} else {
			HttpURLConnection conn = (HttpURLConnection) link.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.connect();
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		}
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}


	/**
	 * Regular expression to extract Youtube Video ID from given URL
	 * @param url URL to extract from
	 * @return ID if found, null if not
	 */
	public static String extractYoutubeId(String url) {
		String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(url);
		if(matcher.find()) {
			return matcher.group();
		} else {
			return null;  
		}
	}

	/**
	 * Extract youtube playlist ID from given URL
	 * @param url URL to extract from
	 * @return ID if found, null if not
	 */
	public static String extractYoutubePlaylistId(String url) {
		String pattern = "(?<=watch\\?list=|list=|embed\\/)[^#\\&\\?]*";
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(url);
		if(matcher.find()) {
			return matcher.group();
		} else {
			return null;  
		}
	}

	/**
	 * Update guild properties for given propertyName
	 * @param guildId Guild ID to update
	 * @param propertyName Name of property to update
	 * @param newValue New value
	 * @param add Append with the limiter "," if true, replace if false
	 */
	public static void updateGuildProperties(String guildId, String propertyName, String newValue, boolean add) {
		File propertiesFile = new File("resources/guilds/"+guildId+"/GuildSettings.properties");
		InputStream is = null;
		OutputStream os = null;
		Properties props = new Properties();
		try {
			is = new FileInputStream(propertiesFile);
			props.load(is);
			is.close();
			if(add) {
				String oldValue = props.getProperty(propertyName);
				StringBuilder sb = new StringBuilder().append(oldValue);
				if(oldValue.equals("")) {
					sb.append(newValue);
				} else {
					sb.append(","+newValue);
				}
				newValue = sb.toString();
			}
			props.setProperty(propertyName, newValue);
			os = new FileOutputStream(propertiesFile);
			props.store(os, null);
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if given user owns the given guild
	 * @param user User to check
	 * @param guild Guild to check
	 * @return True if owner, false if not
	 */
	public static boolean isOwner(IUser user, IGuild guild) {
		if (user.getID().equals(guild.getOwnerID()))
			return true;
		return false;
	}

	/**
	 * Lower level checking individual role 
	 * @param guild Guild to check in
	 * @param user User to check for
	 * @param permission String of permission
	 * @return user has permissions or not
	 */
	public static boolean roleHasPermission(IGuild guild, IRole role, Permission permission) {
		switch(permission) {
		case KICK:
			for(Permissions p : role.getPermissions())
				if(p.hasPermission(2) || p.hasPermission(4) || p.hasPermission(268435456) || p.hasPermission(16) || p.hasPermission(32))
					return true;
			break;
		case BAN:
			for(Permissions p : role.getPermissions())
				if(p.hasPermission(4) || p.hasPermission(268435456) || p.hasPermission(16) || p.hasPermission(32))
					return true;
			break;
		case MANAGE_ROLES:
			for(Permissions p : role.getPermissions()) 
				if(p.hasPermission(268435456) || p.hasPermission(16) || p.hasPermission(32))
					return true;
			break;
		case MANAGE_CHANNELS:
			for(Permissions p : role.getPermissions()) 
				if(p.hasPermission(16) || p.hasPermission(32))
					return true;
			break;
		case MANAGE_SERVER:
			for(Permissions p : role.getPermissions())
				if(p.hasPermission(32))
					return true;
			break;
		case ADMINISTRATOR:
			for(Permissions p : role.getPermissions())
				if(p.hasPermission(8))
					return true;
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * Check if user has permission
	 * @param user User to check
	 * @param guild Guild to check in
	 * @param permission Permission to check for
	 * @see Permission
	 * @return True if user has permission, false if not
	 */
	public static boolean userHasPermission(IUser user, IGuild guild, Permission permission) {
		if (isOwner(user, guild) && !permission.equals(Permission.BOT_OWNER))
			return true;
		if(permission.equals(Permission.BOT_OWNER) && user.getID().equals(Bot.getInstance().getBotOwnerId()))
			return true;
		if(permission.equals(Permission.NONE))
			return true;
		for (IRole role : user.getRolesForGuild(guild)) {
			if (roleHasPermission(guild, role, permission))
				return true;
		}
		return false;
	}

	/**
	 * Check if String input is a valid integer through {@link Integer#parseInt(String)}
	 * @param input String input
	 * @return True if int, false if not
	 */
	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Check if String input is a valid double through {@link Double#parseInt(String)}
	 * @param input String input
	 * @return True if double, false if not
	 */
	public static boolean isDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Get the contents of a command, if it has arguments
	 * Returns everything except the command and its prefix (split among a space)
	 * @param msg IMessage to parse
	 * @return String. Empty if there are no arguments
	 */
	public static String getCommandContents(IMessage msg) {
		return getCommandContents(msg.getContent());
	}
	/**
	 * Get the contents of a command, if it has arguments
	 * Returns everything except the first element of a split among a space
	 * @param s String to parse
	 * @return String. Empty if there are no arguments
	 */
	public static String getCommandContents(String s) {
		return combineStringArray(removeFirstArrayEntry(s.split(" ")));
	}

	/**
	 * Get the first parameter of a command based on a space split
	 * @param msg {@link IMessage} to parse
	 * @return String of first parameter based on a space split
	 */
	public static String getParam(IMessage msg) {
		return getParam(msg.getContent());
	}
	/**
	 * Get the first parameter of a command based on a space split
	 * @param str String to parse
	 * @return String of first parameter based on a space split
	 */
	public static String getParam(String str) {
		return removeFirstArrayEntry(str.split(" "))[0];
	}

	/**
	 * Return a future instant from a string formatted #w#d#h#m
	 * @param string String to resolve from
	 * @return Instant in the future
	 */
	public static Instant resolveInstantFromString(String string) {
		Matcher matcher = Pattern.compile("\\d+|[wdhmWDHM]+").matcher(string);
		Instant now = Instant.now();
		LocalDateTime nowLDT = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
		int previous = 0;
		while(matcher.find()) {
			String s = matcher.group().toLowerCase();
			if(Util.isInteger(s)) {
				previous = Integer.parseInt(s);
				continue;
			}
			switch(s) {
			case "w":
				nowLDT = nowLDT.plus(previous, ChronoUnit.WEEKS);
				break;
			case "d":
				nowLDT = nowLDT.plus(previous, ChronoUnit.DAYS);
				break;
			case "h":
				nowLDT = nowLDT.plus(previous, ChronoUnit.HOURS);
				break;
			case "m":
				nowLDT = nowLDT.plus(previous, ChronoUnit.MINUTES);
				break;
			default:
				break;
			}
		}
		return nowLDT.atZone(ZoneId.systemDefault()).toInstant();
	}
	/**
	 * Alternative to AudioPlayer#getTotalTrackTime formatted in min:sec
	 * @param file file to check (mp3)
	 * @throws UnsupportedAudioFileException bad file type
	 * @throws IOException file not found
	 * @return min:sec
	 */
	public static String getMp3Duration(File file) throws UnsupportedAudioFileException, IOException {
		AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
		if (fileFormat instanceof TAudioFileFormat) {
			Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
			String key = "duration";
			Long microseconds = (Long) properties.get(key);
			int milli = (int) (microseconds / 1000);
			int sec = (milli / 1000) % 60;
			int min = (milli / 1000) / 60;
			if((sec+"").length() == 1)
				return min+":0"+sec;
			return min+":"+sec;
		} else {
			throw new UnsupportedAudioFileException();
		}
	}

	public static long getMp3DurationMillis(File file) {
		try {
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			if (fileFormat instanceof TAudioFileFormat) {
				Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
				String key = "duration";
				Long microseconds = (Long) properties.get(key);
				return (microseconds / 1000);
			}
		} catch(UnsupportedAudioFileException | IOException e) {
			return 60 * 15 * 1000;
		}
		return 60 * 15 * 1000;
	}

	/**
	 * Convert milliseconds to mm:ss format
	 * @param milli int of milliseconds (not expecting Long values)
	 * @return min:sec
	 */
	public static String formatTime(int milli) {
		int sec = (milli / 1000) % 60;
		int min = (milli / 1000) / 60;
		if((sec+"").length() == 1)
			return min+":0"+sec;
		return min+":"+sec;
	}

}
