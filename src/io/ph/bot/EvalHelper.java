package io.ph.bot;

import java.util.List;

import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class EvalHelper {
	public static String getG(List<IGuild> list) {
		StringBuilder sb = new StringBuilder();
		for(IGuild g : list) {
			sb.append(g.getID() + " | " + g.getName() + "\n");
		}
		return sb.toString();
	}
	
	public static String getU(List<IUser> list) {
		StringBuilder sb = new StringBuilder();
		for(IUser g : list) {
			sb.append(g.getID() + " | " + g.getName() + "\n");
		}
		return sb.toString();
	}
	
	public static String get(@SuppressWarnings("rawtypes") List<? extends sx.blah.discord.handle.obj.IDiscordObject> list) {
		StringBuilder sb = new StringBuilder();
		for(IDiscordObject<?> g : list) {
			sb.append(g.getID() + "\n");
		}
		return sb.toString();
	}
	
}
