package io.ph.bot.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import io.ph.bot.Bot;
import io.ph.bot.exception.BadUsernameException;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.exception.NoPermissionException;
import io.ph.bot.exception.UnspecifiedException;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IUser;

/**
 * Represent a Twitch.tv stream
 * 
 * Postcondition: Any TwitchObject is a valid Twitch.tv stream (i.e. the username exists)
 * @author Paul
 *
 */
public class TwitchObject {
	private String twitchUsername;
	private String guildId;
	//UserID that registered this twitch
	private String userId;
	private boolean status;

	private String streamTitle;
	private String streamingGame;
	private String streamDescription;
	private String previewImage;
	private String logoImage;

	private static String apiUrl = "https://api.twitch.tv/kraken/";

	private TwitchObject() {

	}

	public TwitchObject(String twitchUsername, String guildId, String userId) throws NoAPIKeyException, IOException, UnspecifiedException, BadUsernameException {
		if(checkTwitchUsername(twitchUsername)) {
			this.twitchUsername = twitchUsername;
			this.guildId = guildId;
			this.userId = userId;
			this.status = false;
		} else {
			throw new BadUsernameException();
		}
	}

	/**
	 * Register a twitch username with this guild
	 * @return True if registered, false if already is registered
	 * @throws SQLException Something went wrong with connection: check the stacktrace
	 */
	public boolean register() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getTwitchDatabase();
			String sql = "INSERT INTO `global_twitch` (twitch_username, guild_id, user_id, status) VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.twitchUsername.toLowerCase());
			stmt.setString(2, this.guildId);
			stmt.setString(3, this.userId);
			stmt.setInt(4, (this.status == true ? 1 : 0));
			stmt.execute();
			return true;
		} catch(SQLException e) {
			if(e.getErrorCode() == 19) {
				return false;
			}
			throw e;
		} finally {
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}

	public boolean unregister(String requesterId) throws NoPermissionException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql;
			conn = ConnectionPool.getTwitchDatabase();
			IUser u = Bot.getInstance().getBot().getGuildByID(this.guildId).getUserByID(requesterId);
			String targetUserKey = null;
			//If user isn't a mod, need to check that they made this

			sql = "SELECT user_id FROM `global_twitch` WHERE twitch_username = ? AND guild_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.twitchUsername.toLowerCase());
			stmt.setString(2, this.guildId);
			try {
				rs = stmt.executeQuery();
				rs.next();
				targetUserKey = rs.getString(1);
				if(!Util.userHasPermission(u, Bot.getInstance().getBot().getGuildByID(this.guildId),
						Permission.KICK) && !requesterId.equals(targetUserKey)) {
					throw new NoPermissionException();
				}
			} catch(SQLException e) {
				e.printStackTrace();
				return false;
			} finally {
				SQLUtils.closeQuietly(rs);
				SQLUtils.closeQuietly(stmt);
			}


			sql = "DELETE FROM `global_twitch` WHERE twitch_username = ? AND user_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.twitchUsername);
			stmt.setString(2, targetUserKey);
			stmt.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			SQLUtils.closeQuietly(rs);
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
	}
	/**
	 * Check if a twitch username is valid
	 * @param username Username to check for
	 * @return True if valid, false if not
	 * @throws NoAPIKeyException Bot owner doesn't have API key setup
	 * @throws IOException Bad URL (shouldn't happen) or error reading Twitch.tv services
	 * @throws UnspecifiedException Extra error found in Twitch.tv json 
	 * (Maybe banned? I've never seen this happen, but there's certainly room for it)
	 */
	private static boolean checkTwitchUsername(String username) throws NoAPIKeyException, IOException {
		try {
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(apiUrl + "users/" + username)).openConnection();
			conn.setRequestProperty("Client-ID", Bot.getInstance().getApiKeys().get("twitch"));
			conn.connect();
			conn.getInputStream();
		} catch(FileNotFoundException e) {
			return false;
		}
		return true;
	}

	/**
	 * Get a twitch object for primary keys username and guildID
	 * This retrieves data directly from the Twitch.tv Kraken API
	 * @param username Twitch.tv username to lookup
	 * @param guildId Guild where the request originates
	 * @return TwitchObject with valid fields filled (if offline, no fields are filled)
	 * @throws NoAPIKeyException Guild doesn't have API key set for twitch.tv
	 * @throws BadUsernameException Username not found
	 * @throws IOException Error accessing the Twitch API
	 */
	public static TwitchObject forName(String username, String guildId) throws NoAPIKeyException, BadUsernameException, IOException {
		if(!checkTwitchUsername(username))
			return null;
		TwitchObject toReturn = new TwitchObject();
		HttpsURLConnection conn = (HttpsURLConnection) (new URL(apiUrl + "streams/" + username)).openConnection();
		conn.setRequestProperty("Client-ID", Bot.getInstance().getApiKeys().get("twitch"));
		conn.connect();
		StringBuilder stb = new StringBuilder();
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			stb.append(line);
		}
		JsonObject jo = Json.parse(stb.toString()).asObject();

		toReturn.setGuildId(guildId);
		toReturn.setTwitchUsername(StringUtils.capitalize(username));
		if(jo.get("stream").isNull()) {
			// offline
			toReturn.setStatus(false);
			return toReturn;
		}
		toReturn.setTwitchUsername(StringUtils.capitalize(jo.get("stream").asObject().get("channel").asObject().get("display_name").asString()));
		toReturn.setStreamingGame(jo.get("stream").asObject().get("game").asString());
		toReturn.setStreamDescription(jo.get("stream").asObject().get("channel").asObject().get("status").asString());
		toReturn.setPreviewImage(jo.get("stream").asObject().get("preview").asObject().get("large").asString());
		if(jo.get("stream").asObject().get("channel").asObject().get("logo").isNull())
			toReturn.setLogoImage("https://static-cdn.jtvnw.net/jtv-static/404_preview-300x300.png");
		else
			toReturn.setLogoImage(jo.get("stream").asObject().get("channel").asObject()
					.get("logo").asString());
		toReturn.setStatus(true);

		return toReturn;

	}

	/**
	 * Update this object's streaming status
	 */
	private void updateStreamingStatus() {
		try {
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(apiUrl + "streams/" + this.getTwitchUsername())).openConnection();
			conn.setRequestProperty("Client-ID", Bot.getInstance().getApiKeys().get("twitch"));
			conn.connect();
			StringBuilder stb = new StringBuilder();
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				stb.append(line);
			}
			JsonObject jo = Json.parse(stb.toString()).asObject();
			if(jo.get("stream").isNull()) {
				this.setStatus(false);
			} else {
				this.setStatus(true);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String getTwitchUsername() {
		return twitchUsername;
	}

	public String getGuildId() {
		return guildId;
	}

	public String getUserId() {
		return userId;
	}

	public String getStreamTitle() {
		return streamTitle;
	}

	public String getStreamingGame() {
		return streamingGame;
	}

	public void setTwitchUsername(String twitchUsername) {
		this.twitchUsername = twitchUsername;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean getStatus() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public void setStreamTitle(String streamTitle) {
		this.streamTitle = streamTitle;
	}

	public void setStreamingGame(String streamingGame) {
		this.streamingGame = streamingGame;
	}

	public String getStreamDescription() {
		return streamDescription;
	}

	public void setStreamDescription(String streamDescription) {
		this.streamDescription = streamDescription;
	}

	public String getPreviewImage() {
		return previewImage;
	}

	public void setPreviewImage(String previewImage) {
		this.previewImage = previewImage;
	}

	public String getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(String logoImage) {
		this.logoImage = logoImage;
	}
}
