package io.ph.bot.commands;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import io.ph.bot.jobs.WebSyncJob;
import io.ph.bot.model.Guild;
import io.ph.bot.model.Permission;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

/**
 * A centralized class that manages all commands available across servers
 * Every command class created needs to have a public, default constructor
 * as well as have the correct annotations to follow CommandSyntax
 * 
 * A command needs to override only run(IMessage msg)
 * and will follow the requirements outlined in the CommandSyntax annotation
 * @author Paul
 *
 */
public class CommandHandler {

	private static HashMap<String, Command> commandMap = new HashMap<String, Command>();

	public static HashMap<String, String> aliasToDefaultMap = new HashMap<String, String>();

	public static void initCommands() {
		Reflections reflections = new Reflections("io.ph.bot.commands");    
		Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
		PropertiesConfiguration config = null;
		List<String> globalConfigCommands = null;
		try {
			config = new PropertiesConfiguration("resources/config/DefaultGlobalSettings.properties");
			globalConfigCommands = config.getList("EnabledCommands").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		for(Class<? extends Command> c : classes) {
			if(c.isInterface())
				continue;
			Annotation[] a = c.getAnnotations();
			try {
				Command instance = (Command) Class.forName(c.getName()).newInstance();
				for(Annotation a2 : a) {
					if(a2 instanceof CommandData) {
						String defaultCmd = ((CommandData) a2).defaultSyntax();
						Permission p = ((CommandData) a2).permission();
						// Add any commands that do not exist in DefaultGlobalSettings.properties to it
						if(globalConfigCommands != null && p == Permission.NONE) {
							if(!globalConfigCommands.contains(defaultCmd)) {
								LoggerFactory.getLogger(CommandHandler.class).info("Added " + defaultCmd + " to global command list");
								globalConfigCommands.add(defaultCmd);
							}
						}
						commandMap.put(defaultCmd, instance);
						aliasToDefaultMap.put(defaultCmd, defaultCmd);
						for(String s : ((CommandData) a2).aliases()) {
							//commandMap.put(s, instance);
							aliasToDefaultMap.put(s, defaultCmd);
						}
					}
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if(config != null) {
			try {
				config.setProperty("EnabledCommands", globalConfigCommands);
				config.save();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		normalizeCommands();
		LoggerFactory.getLogger(CommandHandler.class).info("Initialized commands");
	}

	/**
	 * Entry point for command handling
	 * If the user doesn't have at least kick privileges and the command is disabled,
	 * the command is rejected without a message.
	 * @param msg Message to parse
	 */
	public static void processCommand(IMessage msg) {
		Guild g = Guild.guildMap.get(msg.getGuild().getID());
		if(msg.getContent().contains(" ")) {
			String cmd = aliasToDefaultMap.get(msg.getContent().substring(g.getGuildConfig().getCommandPrefix().length(),
					msg.getContent().indexOf(" ")));
			if(cmd == null)
				return;
			if(getCommand(cmd).hasPermissions(msg)) {
				if(g.getCommandStatus(cmd) || Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
					WebSyncJob.commandCount++;
					getCommand(cmd).executeCommand(msg);
				}
			}
		} else {
			String cmd = aliasToDefaultMap.get(msg.getContent().substring(g.getGuildConfig().getCommandPrefix().length(),
					msg.getContent().length()));
			if(cmd == null)
				return;
			if(getCommand(cmd).hasPermissions(msg)) {
				if(g.getCommandStatus(cmd) || Util.userHasPermission(msg.getAuthor(), msg.getGuild(), Permission.KICK)) {
					WebSyncJob.commandCount++;
					getCommand(cmd).executeCommand(msg);
				}
			}
		}
	}

	/**
	 * Make sure every guild has all the commands/features
	 * When a new command is created, it is automatically added to DefaultGlobalSettings.properties on init
	 * 
	 * This method will update every guild and automatically add any missing commands to their properties
	 * as well as update template.properties
	 */
	private static void normalizeCommands() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration("resources/config/DefaultGlobalSettings.properties");
			List<String> globalEnabledCommands = config.getList("EnabledCommands").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
			List<String> globalDisabledCommands = config.getList("DisabledCommands").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
			List<String> globalEnabledFeatures = config.getList("EnabledFeatures").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());
			List<String> globalDisabledFeatures = config.getList("DisabledFeatures").stream()
					.map(object -> Objects.toString(object, null))
					.collect(Collectors.toList());

			Collection<File> found = FileUtils.listFiles(new File("resources/guilds"),
					TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			found.add(new File("resources/guilds/template.properties"));
			for (File f : found) {
				if(f.getName().equals("GuildProperties.properties") || f.getName().equals("template.properties")) {
					PropertiesConfiguration config2 = new PropertiesConfiguration(f);
					List<String> enabledCommands = config2.getList("EnabledCommands").stream()
							.map(object -> Objects.toString(object, null))
							.collect(Collectors.toList());
					List<String> disabledCommands = config2.getList("DisabledCommands").stream()
							.map(object -> Objects.toString(object, null))
							.collect(Collectors.toList());
					List<String> enabledFeatures = config2.getList("EnabledFeatures").stream()
							.map(object -> Objects.toString(object, null))
							.collect(Collectors.toList());
					List<String> disabledFeatures = config2.getList("DisabledFeatures").stream()
							.map(object -> Objects.toString(object, null))
							.collect(Collectors.toList());

					List<String> fullCommands = new ArrayList<String>();
					List<String> fullFeatures = new ArrayList<String>();
					fullCommands.addAll(enabledCommands);
					fullCommands.addAll(disabledCommands);
					fullFeatures.addAll(enabledFeatures);
					fullFeatures.addAll(disabledFeatures);
					//May not be needed anymore...
					enabledCommands.remove("");
					disabledCommands.remove("");
					enabledFeatures.remove("");
					disabledFeatures.remove("");
					for(String s : globalEnabledCommands) {
						if(!fullCommands.contains(s)) {
							enabledCommands.add(s);
						}
					}
					for(String s : globalDisabledCommands) {
						if(!fullCommands.contains(s)) {
							disabledCommands.add(s);
						}
					}
					for(String s : globalEnabledFeatures) {
						if(!fullFeatures.contains(s)) {
							enabledFeatures.add(s);
						}
					}
					for(String s : globalDisabledFeatures) {
						if(!fullFeatures.contains(s)) {
							disabledFeatures.add(s);
						}
					}
					for(String s : fullCommands) {
						if(s.equals(""))
							continue;
						if(!globalEnabledCommands.contains(s) && !globalDisabledCommands.contains(s)) {
							enabledCommands.remove(s);
							disabledCommands.remove(s);
						}
					}
					for(String s : fullFeatures) {
						if(s.equals(""))
							continue;
						if(!globalEnabledFeatures.contains(s) && !globalDisabledFeatures.contains(s)) {
							enabledFeatures.remove(s);
							disabledFeatures.remove(s);
						}
					}
					config2.setProperty("EnabledCommands", enabledCommands);
					config2.setProperty("DisabledCommands", disabledCommands);
					config2.setProperty("EnabledFeatures", enabledFeatures);
					config2.setProperty("DisabledFeatures", disabledFeatures);
					config2.save();
				}
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static Command getCommand(String name) {
		return CommandHandler.commandMap.get(aliasToDefaultMap.get(name));
	}

	public static Collection<Command> getAllCommands() {
		return new ArrayList<Command>(CommandHandler.commandMap.values());
	}

}
