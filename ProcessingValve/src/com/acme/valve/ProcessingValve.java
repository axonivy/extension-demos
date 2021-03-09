package com.acme.valve;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import ch.ivyteam.log.Logger;

/**
 * Simple processing valve adapted so that it runs in the OSGi environment of
 * Axon Ivy.
 * 
 * http://javaevangelist.blogspot.ch/2012/12/tomcat-7-custom-valve.html
 */
public class ProcessingValve extends ValveBase
{
  private static final Logger LOGGER = Logger.getLogger(ProcessingValve.class);

  static
  {
    Configurator.setLevel(LOGGER.getName(), Level.INFO);
  }

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException
  {
    HttpServletRequest httpServletRequest = request.getRequest();
    Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
    while (headerNames.hasMoreElements())
    {
      String header = headerNames.nextElement();
      LOGGER.info("Header --> {0} Value --> {1}",
              new Object[] {header, httpServletRequest.getHeader(header)});
    }
    getNext().invoke(request, response);
  }
}
