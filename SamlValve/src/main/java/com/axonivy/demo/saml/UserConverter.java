package com.axonivy.demo.saml;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.apache.http.auth.AuthenticationException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;

import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.ISession;
import ch.ivyteam.ivy.security.IUser;

/**
 * This class handles the conversion between a user defined in a SAML Response and an Ivy user.
 */
public final class UserConverter
{
  private UserConverter() {}

  public static String getIvyUserName(Request request, Assertion assertion) throws AuthenticationException
  {
    String ivyUserName = null;
    // For simplicity we are assuming here that the user name in in the Subject.NameId field
    String samlUserName = assertion.getSubject().getNameID().getValue();
    // It is common that the user information is stored in the Assertion attributes
    // e.g. assertion.getAttributeStatements().get(0).getAttributes()
    HttpSession session = request.getSession();
    ISession ivySession = null;
    if (session != null)
    {
      ivySession = (ISession) session.getAttribute(ISession.class.getName());
      ISecurityContext securityContext = ivySession.getSecurityContext();
      IUser ivyUser = executeAsSystemUser(securityContext, () -> securityContext.findUser(samlUserName));
      if (ivyUser != null)
      {
        ivyUserName = ivyUser.getName();
        // Ivy User may needs to be updated, e.g. because of role change
        // e.g. updateIvyUser(securityContext, ivyUser, assertion);
      }
      else
      {
        ivyUserName = createIvyUser(securityContext, samlUserName, assertion, request.getLocale());
      }
    }
    return ivyUserName;
  }

  private static IUser executeAsSystemUser(ISecurityContext securityContext, Callable<IUser> callable)
          throws AuthenticationException
  {
    try
    {
      return securityContext.executeAsSystemUser(callable);
    }
    catch (Exception exc)
    {
      throw new AuthenticationException("Unable to call findUser", exc);
    }
  }

  private static String createIvyUser(ISecurityContext securityContext, String samlUserName,
          Assertion assertion,
          Locale locale) throws AuthenticationException
  {
    List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();

    // Following attribute names are just examples.
    String fullUserName = getAttribute(attributes, "fullName");
    String eMailAddress = getAttribute(attributes, "eMail");
    List<String> roles = getAttributeList(attributes, "roles");

    IUser newIvyUser = executeAsSystemUser(securityContext,
            () -> securityContext.createUser(samlUserName, fullUserName, "", locale, eMailAddress, samlUserName));
    roles.stream()
            .map(role -> securityContext.findRole(role))
            .filter(role -> role != null)
            .forEach(newIvyUser::addRole);
    return newIvyUser.getName();
  }

  private static List<String> getAttributeList(List<Attribute> attributes, String attributeName)
  {
    return attributes.stream()
            .filter(attr -> attributeName.equals(attr.getName()))
            .map(attr -> attr.getAttributeValues()).findFirst().stream()
            .flatMap(attrValues -> attrValues.stream())
            .map(attrValue -> attrValue.getDOM().getFirstChild().getNodeValue()).collect(Collectors.toList());
  }

  private static String getAttribute(List<Attribute> attributes, String attributeName)
  {
    return attributes.stream()
            .filter(attr -> attributeName.equals(attr.getName()))
            .map(attr -> attr.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue()).findFirst()
            .orElse("");
  }
}
