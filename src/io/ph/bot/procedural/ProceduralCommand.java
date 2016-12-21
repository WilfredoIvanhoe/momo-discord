package io.ph.bot.procedural;

import java.awt.Color;
import java.util.ArrayList;

import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Superclass for procedural commands
 * For usage, see {@link io.ph.bot.commands.moderation.Strawpoll}
 * @author Paul
 *
 */
public abstract class ProceduralCommand implements ProceduralInterface {
	
	// This message is the one that started it all
	private IMessage starter;
	// Title of all embeds
	private String title;
	private int currentStep;
	private ArrayList<Object> responses = new ArrayList<Object>();
	
	/**
	 * Generate a procedural command and set the starting message
	 * @param msg Message that started it all
	 */
	public ProceduralCommand(IMessage msg) {
		currentStep = 0;
		this.starter = msg;
	}
	/**
	 * Send a (templated) message to the original channel
	 * @param description Description to be included
	 */
	public void sendMessage(String description) {
		EmbedBuilder em = new EmbedBuilder();
		em.withColor(Color.MAGENTA);
		em.withTitle(this.title);
		em.withDesc(description);
		em.withFooterText("Type \"exit\" to quit");
		MessageUtils.sendMessage(this.starter.getChannel(), em.build());
	}
	
	/**
	 * Step through the options
	 * @param msg {@link IMessage} that triggered this
	 */
	public void step(IMessage msg) {
		if(msg.getContent().equalsIgnoreCase("exit")) {
			exit();
			return;
		}
		if(msg.getContent().equalsIgnoreCase(this.getBreakOut()) 
				&& getTypes()[getCurrentStep()].equals(StepType.REPEATER)) {
			incrementStep();
			if(getCurrentStep() >= getSteps().length) {
				finish();
				return;
			}
		}
		switch(getTypes()[getCurrentStep()]) {
		case STRING:
			addResponse(msg.getContent());
			break;
		case INTEGER:
			if(Util.isInteger(msg.getContent())) {
				addResponse(Integer.parseInt(msg.getContent()));
			} else {
				sendMessage("Error: Not a valid numerical input");
				return;
			}
			break;
		case DOUBLE:
			if(Util.isDouble(msg.getContent())) {
				addResponse(Double.parseDouble(msg.getContent()));
			} else {
				sendMessage("Error: Not a valid decimal input");
				return;
			}
			break;
		case REPEATER:
			addResponse(msg.getContent());
			sendMessage(getSteps()[getCurrentStep()] + " (to finish, respond with \"" + getBreakOut() + "\")");
			return;
		case YES_NO:
			String s = msg.getContent();
			if(s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes")) {
				addResponse(true);
			} else if(s.equalsIgnoreCase("n") || s.equalsIgnoreCase("no")) {
				addResponse(false);
			} else {
				sendMessage("Invalid yes/no answer: Please use \"y\" or \"n\"");
				return;
			}
			break;
		}
		if(getCurrentStep() + 1 >= getSteps().length) {
			finish();
		} else {
			incrementStep();
			sendMessage(getSteps()[getCurrentStep()]);
		}
	}
	
	/**
	 * Finish this command, handle the responses
	 */
	public abstract void finish();
	
	/**
	 * Exit stepping through and remove from listener
	 */
	public void exit() {
		ProceduralListener.getInstance().removeListener(this.starter.getAuthor());
	}
	public void addResponse(Object o) {
		this.responses.add(o);
	}
	public ArrayList<Object> getResponses() {
		return this.responses;
	}
	public int getCurrentStep() {
		return currentStep;
	}
	public void incrementStep() {
		currentStep++;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
