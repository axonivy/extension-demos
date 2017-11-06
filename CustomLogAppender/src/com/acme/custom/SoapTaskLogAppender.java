package com.acme.custom;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.ITask;

public class SoapTaskLogAppender extends AppenderSkeleton {
	
	
	private String categoryName;
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		//if(event.getLoggerName().compareTo(categoryName) == 0) {
			String formattedMessage = this.layout.format(event);
			Ivy.log().info("GOT MESSAGE: " + formattedMessage);
			
			if(formattedMessage.contains("*** SOAP Request ***")) {
				Pattern pattern = Pattern.compile("task=(\\d+)");
			    Matcher matcher = pattern.matcher(formattedMessage);
			    
		
			    if(matcher.find()) {
			    	String taskString = matcher.group(0);
			    	taskString = taskString.replace("task=", "");
			    	Long taskId = Long.parseLong(taskString);
			    	ITask task = Ivy.wf().findTask(taskId);
			    	task.createNote(Ivy.session(), formattedMessage);
			    	task.setDescription(formattedMessage);
			    	Ivy.log().info("MAO LOG: "+formattedMessage);
			    }
			}
		//}
	}
}
