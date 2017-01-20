package io.ph.bot.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import io.ph.bot.Bot;
import io.ph.bot.audio.MusicSource;
import io.ph.bot.audio.sources.Youtube;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandHandler;
import io.ph.bot.exception.BadCommandNameException;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.audio.AudioPlayer;

public class Guild {
	// How many songs to have buffered at one time per guild
	private static final int MUSIC_QUEUE_SIZE = 3;

	public static HashMap<String, Guild> guildMap = new HashMap<String, Guild>();
	
	private Map<String, Integer> userTimerMap = ExpiringMap.builder()
			.expiration(15, TimeUnit.SECONDS)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();
	private PropertiesConfiguration config;
	private SpecialChannels specialChannels;
	private HashMap<String, String> optIn = new HashMap<String, String>();
	private HashMap<String, Boolean> commandStatus = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> featureStatus = new HashMap<String, Boolean>();
	private ServerConfiguration guildConfig;
	private HashSet<String> joinableRoles = new HashSet<String>();
	private HistoricalSearches historicalSearches;
	private GuildMusic musicManager;
	private ChatterBotSession cleverBot;
	private String mutedRoleId;

	/**
	 * Initialize the Guild object and add it to the hashmap
	 * Note: No checks to see if the guild is already added, so initializing
	 * a guild again will overwrite
	 * @param g Guild to initialize
	 */
	public Guild(IGuild g) {
		try {
			// Read data from this file
			this.config = new PropertiesConfiguration("resources/guilds/" + g.getID() + "/GuildProperties.properties");
			this.config.setAutoSave(true);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		this.specialChannels = new SpecialChannels(config.getString("WelcomeChannelId"),
				config.getString("MusicChannelId"), config.getString("TwitchChannelId"),
				config.getString("LogChannelId"), config.getString("VoiceChannelId"));
		this.historicalSearches = new HistoricalSearches();

		String[] optInChannels = config.getStringArray("OptInChannelRoles");
		String[] optInRoles = config.getStringArray("OptInChannelIds");
		for(int i = 0; i < optInChannels.length; i++) {
			this.optIn.put(optInChannels[i], optInRoles[i]);
		}

		String[] enabledCommands = config.getStringArray("EnabledCommands");
		String[] disabledCommands = config.getStringArray("DisabledCommands");
		for(String s : enabledCommands) {
			this.commandStatus.put(s, true);
		}
		for(String s : disabledCommands) {
			this.commandStatus.put(s, false);
		}
		String[] enabledFeatures = config.getStringArray("EnabledFeatures");
		String[] disabledFeatures = config.getStringArray("DisabledFeatures");
		for(String s : enabledFeatures) {
			this.featureStatus.put(s, true);
		}
		for(String s : disabledFeatures) {
			this.featureStatus.put(s, false);
		}
		String welcomeMessage = Arrays.toString(config.getStringArray("NewUserWelcomeMessage"));
		welcomeMessage = welcomeMessage.substring(1, welcomeMessage.length() - 1);
		this.guildConfig = new ServerConfiguration(config.getString("ServerCommandPrefix"), 
				config.getInt("MessagesPerFifteenSeconds"),
				config.getInt("CommandCooldown"), 
				welcomeMessage,
				config.getBoolean("LimitToOneRole", false),
				config.getBoolean("FirstTime"),
				config.getBoolean("DisableInvites", false));
		String[] joinableRolesP = config.getStringArray("JoinableRoles");
		for(String s : joinableRolesP) {
			if(s.equals(""))
				continue;
			if(g.getRoleByID(s) == null)
				continue;
			this.joinableRoles.add(s);
		}
		this.mutedRoleId = config.getString("MutedRoleId", "");
		try {
			this.cleverBot = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT).createSession();
		} catch (Exception e) {
			this.cleverBot = null;
			e.printStackTrace();
		}
		Guild.guildMap.put(g.getID(), this);
		Bot.getInstance().getLogger().info("Guild {} initialized - {}", g.getID(), g.getName());
	}


	/**
	 * Disable a command on this guild
	 * @param s Main name of command to disable
	 * @return True if disabled, false if it was already disabled
	 * @throws BadCommandNameException Command doesn't exist
	 */
	public boolean disableCommand(String s) throws BadCommandNameException {
		return editCommand(s, false);
	}

	/**
	 * Enable a command on this guild
	 * @param s Main name of command to enable
	 * @return True if enabled, false if it was already enabled
	 * @throws BadCommandNameException Command doesn't exist
	 */
	public boolean enableCommand(String s) throws BadCommandNameException {
		return editCommand(s, true);
	}

	private boolean editCommand(String s, boolean toEnable) throws BadCommandNameException {
		s = CommandHandler.aliasToDefaultMap.get(s);
		if(!validCommandToEdit(s))
			throw new BadCommandNameException();
		if(!this.commandStatus.get(s))
			return false;
		this.commandStatus.put(s, toEnable);
		List<String> enabled = config.getList("EnabledCommands").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		List<String> disabled = config.getList("DisabledCommands").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		if(toEnable) {
			disabled.remove(s);
			enabled.add(s);
		} else {
			enabled.remove(s);
			disabled.add(s);
		}
		enabled.remove("");
		disabled.remove("");
		config.setProperty("EnabledCommands", enabled);
		config.setProperty("DisabledCommands", disabled);
		return true;
	}

	public boolean getCommandStatus(String input) {
		try {
			Command c = CommandHandler.getCommand(input);
			if(c == null)
				throw new NullPointerException();
			if(!c.getPermission().equals(Permission.NONE))
				return true;
			return this.commandStatus.get(input);
		} catch(NullPointerException e) {
			//NPE if someone uses a random command that doesn't exist or isn't listed in the enabled/disabled
			return false;
		}
	}
	public boolean validCommandToEdit(String s) {
		for(String key : commandStatus.keySet()) {
			if(s.equalsIgnoreCase(key))
				return true;
		}
		return false;
	}
	public void enableAllCommands() {
		commandStatus.replaceAll((key, value) -> true);
	}
	public void disableAllCommands() {
		commandStatus.replaceAll((key, value) -> false);
	}

	/**
	 * Disable a feature in this guild
	 * @param s name of feature to disable
	 * @return True if disabled, false if it was already disabled
	 * @throws BadCommandNameException Feature doesn't exist
	 */
	public boolean disableFeature(String s) throws BadCommandNameException {
		return editFeature(s, false);
	}

	/**
	 * Enable a feature on this guild
	 * @param s name of command to enable
	 * @return True if enabled, false if it was already enabled
	 * @throws BadCommandNameException Feature doesn't exist
	 */
	public boolean enableFeature(String s) throws BadCommandNameException {
		return editFeature(s, true);
	}
	private boolean editFeature(String s, boolean toEnable) throws BadCommandNameException {
		if(!featureStatus.containsKey(s))
			throw new BadCommandNameException();
		if(!this.featureStatus.get(s))
			return false;
		this.featureStatus.put(s, toEnable);
		List<String> enabled = config.getList("EnabledFeatures").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		List<String> disabled = config.getList("DisabledFeatures").stream()
				.map(object -> Objects.toString(object, null))
				.collect(Collectors.toList());
		if(toEnable) {
			disabled.remove(s);
			enabled.add(s);
		} else {
			enabled.remove(s);
			disabled.add(s);
		}
		enabled.remove("");
		disabled.remove("");
		config.setProperty("EnabledFeatures", enabled);
		config.setProperty("DisabledFeatures", disabled);
		return true;
	}
	/**
	 * Get status of a feature
	 * @param input Feature
	 * @return True if enabled, false if not
	 */
	public boolean getFeatureStatus(String input) {
		try {
			if(!featureStatus.containsKey(input))
				throw new NullPointerException();
			return this.featureStatus.get(input);
		} catch(NullPointerException e) {
			return false;
		}
	}

	public Map<String, Integer> getUserTimerMap() {
		return userTimerMap;
	}
 
	public HistoricalSearches getHistoricalSearches() {
		return historicalSearches;
	}

	public ChatterBotSession getCleverBot() {
		return cleverBot;
	}

	public void resetCleverBot() {
		try {
			this.cleverBot = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT).createSession();
		} catch (Exception e) {
			this.cleverBot = null;
			e.printStackTrace();
		}
	}

	public Configuration getPropertyConfig() {
		return config;
	}

	public ServerConfiguration getGuildConfig() {
		return guildConfig;
	}

	public SpecialChannels getSpecialChannels() {
		return specialChannels;
	}

	public boolean addJoinableRole(String roleId) {
		if(this.joinableRoles.add(roleId)) {
			this.config.setProperty("JoinableRoles", this.joinableRoles);
			return true;
		}
		return false;
	}
	public boolean removeJoinableRole(String roleId) {
		if(this.joinableRoles.remove(roleId)) {
			this.config.setProperty("JoinableRoles", this.joinableRoles);
			return true;
		}
		return false;
	}
	public HashSet<String> getJoinableRoles() {
		return this.joinableRoles;
	}
	public boolean isJoinableRole(String roleId) {
		return this.joinableRoles.contains(roleId) ? true : false;
	}
	public HashMap<String, Boolean> getCommandStatus() {
		return this.commandStatus;
	}
	public HashMap<String, Boolean> getFeatureStatus() {
		return this.featureStatus;
	}
	public String getMutedRoleId() {
		return mutedRoleId;
	}

	public void setMutedRoleId(String mutedRoleId) {
		this.mutedRoleId = mutedRoleId;
		this.config.setProperty("MutedRoleId", mutedRoleId);
	}

	public GuildMusic getMusicManager() {
		return musicManager;
	}

	public void initMusicManager(IGuild guild) {
		this.musicManager = new GuildMusic(guild);
	}

	public class ServerConfiguration {
		private String commandPrefix;
		private int messagesPerFifteen;
		private int commandCooldown;
		private String welcomeMessage;
		private boolean limitToOneRole;
		private boolean firstTime;
		private boolean disableInvites;

		ServerConfiguration(String commandPrefix, int messagesPerFifteen, int commandCooldown,
				String welcomeMessage, boolean limitToOneRole, boolean firstTime, boolean disableInvites) {
			this.commandPrefix = commandPrefix;
			this.messagesPerFifteen = messagesPerFifteen;
			this.commandCooldown = commandCooldown;
			this.welcomeMessage = welcomeMessage;
			this.limitToOneRole = limitToOneRole;
			this.firstTime = firstTime;
			this.disableInvites = disableInvites;
		}

		@Override
		public String toString() {
			return "ServerConfiguration [commandPrefix=" + commandPrefix + ", messagesPerFifteen=" + messagesPerFifteen
					+ ", commandCooldown=" + commandCooldown + ", welcomeMessage=" + welcomeMessage + ", firstTime="
					+ firstTime + "]";
		}

		public String getCommandPrefix() {
			return commandPrefix;
		}
		public void setCommandPrefix(String commandPrefix) {
			this.commandPrefix = commandPrefix;
			config.setProperty("ServerCommandPrefix", commandPrefix);
		}
		public int getMessagesPerFifteen() {
			return messagesPerFifteen;
		}
		public void setMessagesPerFifteen(int messagesPerFifteen) {
			this.messagesPerFifteen = messagesPerFifteen;
			config.setProperty("MessagesPerFifteenSeconds", messagesPerFifteen);
		}
		public int getCommandCooldown() {
			return commandCooldown;
		}
		public void setCommandCooldown(int commandCooldown) {
			this.commandCooldown = commandCooldown;
			config.setProperty("CommandCooldown", commandCooldown);
		}
		public String getWelcomeMessage() {
			return welcomeMessage;
		}
		public void setWelcomeMessage(String welcomeMessage) {
			this.welcomeMessage = welcomeMessage;
			config.setProperty("NewUserWelcomeMessage", welcomeMessage);
		}
		public boolean isFirstTime() {
			return firstTime;
		}
		public void setFirstTime(boolean firstTime) {
			this.firstTime = firstTime;
			config.setProperty("FirstTime", firstTime);
		}

		public boolean isLimitToOneRole() {
			return limitToOneRole;
		}

		public void setLimitToOneRole(boolean limitToOneRole) {
			this.limitToOneRole = limitToOneRole;
			config.setProperty("LimitToOneRole", limitToOneRole);
		}

		public boolean isDisableInvites() {
			return disableInvites;
		}

		public void setDisableInvites(boolean disableInvites) {
			this.disableInvites = disableInvites;
			config.setProperty("DisableInvites", disableInvites);
		}
	}

	public class HistoricalSearches {
		// Historical anime searches. Object is {title, malId}
		private Map<Integer, Object[]> historicalAnime;
		// This is used to play Themes.moe or Youtube results directly with $music
		private Map<Integer, String[]> historicalMusic;
		// This is used to do $theme #
		private Map<Integer, ArrayList<Theme>> historicalThemeSearchResults;

		HistoricalSearches() {
			this.historicalAnime = ExpiringMap.builder()
					.expiration(15, TimeUnit.MINUTES)
					.expirationPolicy(ExpirationPolicy.CREATED)
					.build();
			this.historicalMusic = ExpiringMap.builder()
					.expiration(15, TimeUnit.MINUTES)
					.expirationPolicy(ExpirationPolicy.CREATED)
					.build();
			historicalThemeSearchResults = ExpiringMap.builder()
					.expiration(15, TimeUnit.MINUTES)
					.expirationPolicy(ExpirationPolicy.CREATED)
					.build();
		}

		public Map<Integer, ArrayList<Theme>> getHistoricalThemeSearchResults() {
			return historicalThemeSearchResults;
		}

		public void addHistoricalThemeSearchResult(int i, ArrayList<Theme> a) {
			this.historicalThemeSearchResults.put(i, a);
		}

		public Map<Integer, Object[]> getHistoricalAnime() {
			return historicalAnime;
		}

		public void addHistoricalAnime(int i, Object[] o) {
			this.historicalAnime.put(i, o);
		}

		public void clearAnimeSearches() {
			this.historicalAnime.clear();
		}

		public Map<Integer, String[]> getHistoricalMusic() {
			return historicalMusic;
		}

		public void addHistoricalMusic(int i, String[] s) {
			this.historicalMusic.put(i, s);
		}

		public void clearHistoricalMusic() {
			this.historicalMusic.clear();
		}
	}
	public class SpecialChannels {
		private String welcome;
		private String music;
		private String twitch;
		private String log;
		private String voice;

		SpecialChannels(String welcome, String music, String twitch, String log, String voice) {
			this.welcome = welcome;
			this.music = music;
			this.twitch = twitch;
			this.log = log;
			this.voice = voice;
		}


		public String getWelcome() {
			return welcome;
		}

		public void setWelcome(String welcome) {
			this.welcome = welcome;
			config.setProperty("WelcomeChannelId", welcome);
		}

		public String getMusic() {
			return music;
		}

		public void setMusic(String music) {
			this.music = music;
			config.setProperty("MusicChannelId", music);
		}

		public String getLog() {
			return log;
		}

		public void setLog(String log) {
			this.log = log;
			config.setProperty("LogChannelId", log);
		}

		public String getVoice() {
			return voice;
		}

		public void setVoice(String voice) {
			this.voice = voice;
			config.setProperty("VoiceChannelId", voice);
		}

		public String getTwitch() {
			return twitch;
		}

		public void setTwitch(String twitch) {
			this.twitch = twitch;
			config.setProperty("TwitchChannelId", twitch);
		}
	}

	public class GuildMusic {
		private AudioPlayer audioPlayer;
		private int skipVotes;
		private MusicSource currentSong;

		private Set<String> skipVoters;
		private LinkedList<MusicSource> overflowQueue;
		
		private int index;

		public GuildMusic(IGuild guild) {
			this.audioPlayer = new AudioPlayer(guild);
			this.audioPlayer.setVolume(0.5f);
			this.skipVotes = 0;
			this.skipVoters = new HashSet<String>();
			this.overflowQueue = new LinkedList<MusicSource>();
			this.index = 0;
		}

		public AudioPlayer getAudioPlayer() {
			return this.audioPlayer;
		}

		/**
		 * The special method. Queue a MusicSource, which will then be buffered if the current
		 * audio playlist size is less than designated MUSIC_QUEUE_SIZE
		 * @param userId User who queued
		 * @param trackName Name of track
		 * @param url URL if applicable
		 * @throws UnsupportedAudioFileException 
		 * @throws IOException 
		 */
		public void addMusicSource(MusicSource source) {
			overflowQueue.add(source);
			if(this.audioPlayer.getPlaylistSize() < MUSIC_QUEUE_SIZE) {
				queueNext();
			}
		}
		
		/**
		 * Move a song from the overflowqueue to the AudioPlayer queue
		 */
		public synchronized void queueNext() {
			if(overflowQueue.isEmpty() || index >= overflowQueue.size())
				return;
			try {
				MusicSource source;
				if((source = overflowQueue.get(index)).isQueued()) {
					return;
				}
				index++;
				source.setQueued(true);
				if(source instanceof Youtube) {
					((Youtube) source).processVideo();
				}
				audioPlayer.queue(source.getSource());
			} catch (IOException | UnsupportedAudioFileException e) {
				e.printStackTrace();
				overflowQueue.pop();
				index--;
			}
		}
		
		/**
		 * non-functional
		 * TODO: make it functional
		 */
		public void shuffle() {
			this.audioPlayer.clear();
			Collections.shuffle(this.overflowQueue);
			this.index = 0;
			while(this.audioPlayer.getPlaylistSize() < MUSIC_QUEUE_SIZE) {
				queueNext();
			}
		}
		
		public MusicSource pollSource() {
			index--;
			return (this.currentSong = overflowQueue.poll());
		}

		public LinkedList<MusicSource> getOverflowQueue() {
			return overflowQueue;
		}

		public int getOverflowQueueSize() {
			return overflowQueue.size();
		}

		public int getQueueSize() {
			return overflowQueue.size();
		}
		
		/**
		 * Use this to test for no music at all
		 * @return True if empty (and nothing currently playing), false if not
		 */
		public boolean emptyQueue() {
			return overflowQueue.size() + this.audioPlayer.getPlaylistSize() == 0;
		}

		public int getSkipVotes() {
			return skipVotes;
		}

		public void setSkipVotes(int skipVotes) {
			this.skipVotes = skipVotes;
		}

		public Set<String> getSkipVoters() {
			return skipVoters;
		}

		public void setCurrentSong(MusicSource m) {
			this.currentSong = m;
		}

		public MusicSource getCurrentSong() {
			return this.currentSong;
		}
		public void reset() {
			this.setCurrentSong(null);
			this.setSkipVotes(0);
			this.overflowQueue.clear();
			this.skipVoters.clear();
			this.audioPlayer.clear();
			this.index = 0;
		}
	}
}
