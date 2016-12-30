package io.ph.bot.procedural;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import sx.blah.discord.handle.obj.IMessage;

public class ProceduralListener {
	private static ProceduralListener instance;
	/**
	 * Observers are identified by the User ID & Channel ID that started
	 * Form: userID,channelID
	 */
	private Map<String, ProceduralCommand> observers = ExpiringMap.builder()
			.expiration(5, TimeUnit.MINUTES)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();

	public void addListener(IMessage msg, ProceduralCommand c) {
		observers.put(String.join(",", msg.getAuthor().getID(), msg.getChannel().getID()), c);
	}
	public void removeListener(IMessage msg) {
		observers.remove(String.join(",", msg.getAuthor().getID(), msg.getChannel().getID()));
	}

	public void update(IMessage msg) {
		if(observers.get(String.join(",", msg.getAuthor().getID(), msg.getChannel().getID())) != null)
			observers.get(String.join(",", msg.getAuthor().getID(), msg.getChannel().getID())).step(msg);
	}

	public static ProceduralListener getInstance() {
		if(instance == null)
			instance = new ProceduralListener();
		return instance;
	}
}
