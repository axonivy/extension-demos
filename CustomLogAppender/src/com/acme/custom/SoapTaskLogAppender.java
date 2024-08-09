package com.acme.custom;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.ITask;

@Plugin(name = "SoapTaskLogAppender", category = "Core", elementType = "appender", printObject = true)
public final class SoapTaskLogAppender extends AbstractAppender {

  private final String categoryName;

  protected SoapTaskLogAppender(
          final String name,
          final String categoryName,
          final Filter filter,
          final Layout<? extends Serializable> layout,
          final boolean ignoreExceptions,
          final Property[] properties) {
      super(name, filter, layout, ignoreExceptions, properties);
      this.categoryName = categoryName;
  }

  public String getCategoryName() {
    return categoryName;
  }

  @Override
  public void append(LogEvent event) {
    String formattedMessage = new String(getLayout().toByteArray(event));
    if (formattedMessage.contains("*** SOAP Request ***")) {
      Pattern pattern = Pattern.compile("task=(\\d+)");
      Matcher matcher = pattern.matcher(formattedMessage);
      if (matcher.find()) {
        String taskString = matcher.group(1);
        Long taskId = Long.parseLong(taskString);
        ITask task = Ivy.wf().findTask(taskId);
        task.createNote(Ivy.session(), formattedMessage);
        task.setDescription(formattedMessage);
        Ivy.log().info("MAO LOG: " + formattedMessage);
      }
    }
  }

  @Override
  public void stop() {
    super.stop();
  }

  @PluginFactory
  public static SoapTaskLogAppender createAppender(
          @PluginAttribute("name") @Required String name,
          @PluginAttribute("categoryName") String categoryName,
          @PluginElement("Layout") Layout<? extends Serializable> layout,
          @PluginElement("Filter") Filter filter,
          @PluginElement("Properties") Property[] properties) {
    if (name == null) {
      LOGGER.error("No name provided for SoapTaskLogAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new SoapTaskLogAppender(name, categoryName, filter, layout, true, properties);
  }
}
