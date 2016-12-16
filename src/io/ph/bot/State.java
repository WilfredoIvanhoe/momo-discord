package io.ph.bot;

import java.io.File;

import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class State {
	public static void changeBotStatus(String status) {	
		Status st = Status.game(status);
		Bot.getInstance().getBot().changeStatus(st);
	}
	public static void changeBotUsername(String newUser) {
		try {
			Bot.getInstance().getBot().changeUsername(newUser);
		} catch (DiscordException e) {
			Bot.getInstance().getLogger().warn("Changing username too often");
		} catch (RateLimitException e2) {
			Bot.getInstance().getLogger().warn("Rate limits!");
		}
	}
	public static void changeBotAvatar(File image) {
		try {
			Bot.getInstance().getBot().changeAvatar(Image.forFile(image));
			
		} catch (DiscordException e) {
			e.printStackTrace();
		} catch (RateLimitException e2) {
			Bot.getInstance().getLogger().warn("Changing username too often");
		}
	}
}
