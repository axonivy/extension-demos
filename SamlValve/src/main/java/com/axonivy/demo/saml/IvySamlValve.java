package com.axonivy.demo.saml;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
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
  private String certFileName;

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException
  {
    if (!requestHasActiveUserSession(request) && UserAuthenticator.hasSamlToken(request))
    {
      try
      {
        // The request has a SAML Assertion which should be checked and used for SSO login
        UserAuthenticator auth = new UserAuthenticator(request).authenticate(certFileName);
        performUserLogin(request, auth.getAssertion());
        response.sendRedirect(auth.getRedirectUri());
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

  /**
   * checks if the request has an active Ivy Session
   */
  private boolean requestHasActiveUserSession(Request request)
  {
    ISession ivySession = (ISession) request.getSession().getAttribute(ISession.class.getName());
    return ivySession != null && !ivySession.isSessionUserUnknown();
  }
  
  public String getCertificate()
  {
    return this.certFileName;
  }
  
  public void setCertificate(String certificateFileName)
  {
    this.certFileName = certificateFileName;
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
