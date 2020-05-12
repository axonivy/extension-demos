package com.axonivy.demo.saml;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.catalina.connector.Request;
import org.apache.http.auth.AuthenticationException;
import org.joda.time.DateTime;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.ResponseUnmarshaller;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is responsible for authenticating a user given in a SAML Response.
 */
public final class UserAuthenticator
{
  private static final String REQUEST_SAML_PARAMETER_NAME = "SAMLResponse";

  // This needs to be changed to the real path to the certificate

  private final Request request;
  private Assertion assertion;
  private Response response;

  public UserAuthenticator(Request request)
  {
    this.request = request;
  }

  /**
   * Authenticate the user set in the SAML-response POST request parameter.
   * @param certFileName 
   * 
   * @return this object
   * @throws AuthenticationException
   */
  public UserAuthenticator authenticate(String certFileName) throws AuthenticationException
  {
    this.response = unmarshallSamlResponse(decodeSamlResponse(request));
    validateSamlResponse(certFileName);
    return this;
  }

  public Assertion getAssertion()
  {
    return this.assertion;
  }

  public String getRedirectUri()
  {
    return this.response.getDestination();
  }

  /**
   * Decodes a SAML response returned by an identity provider.
   */
  private static Response unmarshallSamlResponse(String decodedResponse) throws AuthenticationException
  {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    try (InputStream is = new ByteArrayInputStream(decodedResponse.getBytes(StandardCharsets.UTF_8)))
    {
      BasicParserPool parserMgr = new BasicParserPool();
      parserMgr.setNamespaceAware(true);
      Document samlDoc = parserMgr.parse(is);
      Element samlRoot = samlDoc.getDocumentElement();

      ResponseUnmarshaller unmarsh = new ResponseUnmarshaller();
      return (Response) unmarsh.unmarshall(samlRoot);
    }
    catch (XMLParserException | IOException | UnmarshallingException e)
    {
      throw new AuthenticationException("Error unmarshalling SAML Response", e);
    }
  }

  private void validateSamlResponse(String certFileName) throws AuthenticationException
  {
    this.assertion = validateResponse(this.response);
    validateAssertion(assertion);
    validateSignature(response.getSignature(), certFileName);
    validateSignature(assertion.getSignature(), certFileName);
  }

  private static Assertion validateResponse(Response response) throws AuthenticationException
  {

    String statusCode = response.getStatus().getStatusCode().getValue();
    if (!statusCode.equals("urn:oasis:names:tc:SAML:2.0:status:Success"))
    {
      throw new AuthenticationException("Invalid status code: " + statusCode);
    }

    if (response.getAssertions().size() != 1)
    {
      throw new AuthenticationException("The response doesn't contain exactly 1 assertion");
    }

    if (response.getIssuer() == null || !response.getIssuer().getValue().equals("MySamlIssuer"))
    {
      throw new AuthenticationException("The response issuer didn't match the expected value");
    }

    // Add more Response validations here

    return response.getAssertions().get(0);
  }

  private static void validateAssertion(Assertion assertion) throws AuthenticationException
  {
    if (!assertion.getIssuer().getValue().equals("MySamlIssuer"))
    {
      throw new AuthenticationException("The assertion issuer didn't match the expected value");
    }

    if (assertion.getSubject().getNameID() == null)
    {
      throw new AuthenticationException("The NameID value is missing from the SAML response");
    }

    // Add more Assertion validations here

    enforceConditions(assertion.getConditions());
  }

  private static void validateSignature(Signature signature, String certFileName) throws AuthenticationException
  {
    try
    {
      Credential credential = new BasicX509Credential(loadCertificate(certFileName));
      SignatureValidator.validate(signature, credential);
    }
    catch (SignatureException se)
    {
      throw new AuthenticationException("Error verifying signature", se);
    }
  }

  private static void enforceConditions(Conditions conditions) throws AuthenticationException
  {
    if (conditions == null)
    {
      return;
    }

    DateTime now = DateTime.now();

    if (now.isBefore(conditions.getNotBefore()))
    {
      throw new AuthenticationException(
              "Assertion cannot be used before " + conditions.getNotBefore().toString());
    }

    if (now.isAfter(conditions.getNotOnOrAfter()))
    {
      throw new AuthenticationException(
              "Assertion cannot be used after  " + conditions.getNotOnOrAfter().toString());
    }

    // Enforce more Assertion Conditions here
  }

  private static X509Certificate loadCertificate(String filePath)
  {
    try (InputStream is = new FileInputStream(filePath))
    {
      CertificateFactory fact = CertificateFactory.getInstance("X.509");
      return (X509Certificate) fact.generateCertificate(is);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  protected static String decodeSamlResponse(Request request)
  {
    String base64SamlToken = extractBase64SamlToken(request);
    return new String(Base64.getDecoder().decode(base64SamlToken), StandardCharsets.UTF_8);
  }

  private static String extractBase64SamlToken(Request request)
  {
    return request.getParameter(REQUEST_SAML_PARAMETER_NAME);
  }

  static boolean hasSamlToken(Request request)
  {
    return request.getParameter(REQUEST_SAML_PARAMETER_NAME) != null;
  }
}
