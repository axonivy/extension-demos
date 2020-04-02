package com.axonivy.demo.saml;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.opensaml.saml.saml2.core.Assertion;

import ch.ivyteam.ivy.security.ISession;

/**
 * Tomcat Valve for processing and authenticating Ivy users with a posted SAML tokens for SSO login.<p>
 * This valve has to be registered in the context.xml file of the Ivy Engine / Designer.
 */
public final class IvySamlValve extends ValveBase
{

  private static final String SAML_AUTH_ERROR_MSG = "Exception while performing the SAML SSO authentication: ";
  private static final String IVY_SESSION_COOKIE_NAME = "JSESSIONID";

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException
  {
    if (!requestHasActiveUserSession(request))
    {
      try
      {
        // The request has a SAML Assertion which should be checked and used for SSO login
        UserAuthenticator auth = new UserAuthenticator(request).authenticate();
        performUserLogin(request, auth.getAssertion());
        sendRedirect(request, response, auth.getRedirectUri());
      }
      catch (AuthenticationException e)
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, SAML_AUTH_ERROR_MSG + e.getMessage());
      }
      return;
    }
    getNext().invoke(request, response);
  }

  private void performUserLogin(Request request, Assertion assertion)
          throws AuthenticationException
  {
    request.setUserPrincipal(new SamlPrincipal(UserConverter.getIvyUserName(request, assertion)));
  }

  private void sendRedirect(Request request, Response response, String redirectURI) throws IOException
  {
    Optional<Cookie> ivySessionCookie = getIvySessionCookie(request);
    if (ivySessionCookie.isPresent())
    {
      Cookie sessionCookie = new Cookie(IVY_SESSION_COOKIE_NAME, ivySessionCookie.get().getValue());
      sessionCookie.setSecure(true);
      response.addCookie(sessionCookie);
    }
    response.sendRedirect(redirectURI);
  }

  /**
   * checks if the request has an active Ivy Session
   */
  public boolean requestHasActiveUserSession(Request request)
  {
    if (!getIvySessionCookie(request).isPresent())
    {
      return false;
    }
    ISession ivySession = (ISession) request.getSession().getAttribute(ISession.class.getName());
    return !ivySession.isSessionUserUnknown();
  }

  /**
   * Returns the {@link #IVY_SESSION_COOKIE_NAME} cookie as an Optional.
   * 
   * @return An optional {@link #IVY_SESSION_COOKIE_NAME} cookie object.
   */
  private static Optional<Cookie> getIvySessionCookie(Request request)
  {
    return Arrays.stream(request.getCookies())
            .filter(c -> StringUtils.equalsIgnoreCase(c.getName(), IVY_SESSION_COOKIE_NAME) && StringUtils.isNotBlank(c.getValue()))
            .findFirst();
  }

  private static class SamlPrincipal implements Principal
  {
    private final String userName;

    private SamlPrincipal(String userName)
    {
      this.userName = userName;
    }

    @Override
    public String getName()
    {
      return userName;
    }
  }
}
