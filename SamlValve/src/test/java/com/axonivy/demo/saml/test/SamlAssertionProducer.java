package com.axonivy.demo.saml.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.xml.security.Init;
import org.joda.time.DateTime;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.saml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

public class SamlAssertionProducer
{
  private final String keyStoreLocation;
  private final char[] storePassword;
  private final String alias;
  
  public SamlAssertionProducer(String keyStoreLocation, char[] storePassword, String alias)
  {
    this.keyStoreLocation = keyStoreLocation;
    this.storePassword = storePassword;
    this.alias = alias;
  }

  public String createSAMLResponse(String subjectId, DateTime authenticationTime,
          HashMap<String, List<String>> attributes, String issuer, int samlAssertionDays) throws Exception
  {
    Init.init();
    Signature responseSignature = createSignature();
    Signature assertionSignature = createSignature();
    Status status = createStatus();
    Issuer responseIssuer = null;
    Issuer assertionIssuer = null;
    Subject subject = null;
    AttributeStatement attributeStatement = null;

    if (issuer != null)
    {
      responseIssuer = createIssuer(issuer);
      assertionIssuer = createIssuer(issuer);
    }

    if (subjectId != null)
    {
      subject = createSubject(subjectId, samlAssertionDays);
    }

    if (attributes != null && attributes.size() != 0)
    {
      attributeStatement = createAttributeStatement(attributes);
    }

    AuthnStatement authnStatement = createAuthnStatement(authenticationTime);

    Assertion assertion = createAssertion(new DateTime(), subject, assertionIssuer, authnStatement,
            attributeStatement);
    assertion.setSignature(assertionSignature);

    Response response = createResponse(new DateTime(), responseIssuer, status, assertion);
    response.setSignature(responseSignature);

    ResponseMarshaller marshaller = new ResponseMarshaller();
    Element element = marshaller.marshall(response);
    Signer.signObject(assertionSignature);
    Signer.signObject(responseSignature);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    XMLHelper.writeNode(element, baos);

    return new String(Base64.getEncoder().encode(baos.toByteArray()), StandardCharsets.UTF_8);
  }

  private Response createResponse(DateTime issueDate, Issuer issuer, Status status, Assertion assertion)
  {
    ResponseBuilder responseBuilder = new ResponseBuilder();
    Response response = responseBuilder.buildObject();
    response.setID(UUID.randomUUID().toString());
    response.setIssueInstant(issueDate);
    response.setVersion(SAMLVersion.VERSION_20);
    response.setIssuer(issuer);
    response.setStatus(status);
    response.getAssertions().add(assertion);
    return response;
  }

  private Assertion createAssertion(DateTime issueDate, Subject subject, Issuer issuer,
          AuthnStatement authnStatement,
          AttributeStatement attributeStatement)
  {
    AssertionBuilder assertionBuilder = new AssertionBuilder();
    Assertion assertion = assertionBuilder.buildObject();
    assertion.setID(UUID.randomUUID().toString());
    assertion.setIssueInstant(issueDate);
    assertion.setSubject(subject);
    assertion.setIssuer(issuer);

    if (authnStatement != null)
      assertion.getAuthnStatements().add(authnStatement);

    if (attributeStatement != null)
      assertion.getAttributeStatements().add(attributeStatement);

    return assertion;
  }

  private Issuer createIssuer(String issuerName)
  {
    // create Issuer object
    IssuerBuilder issuerBuilder = new IssuerBuilder();
    Issuer issuer = issuerBuilder.buildObject();
    issuer.setValue(issuerName);
    return issuer;
  }

  private Subject createSubject(String subjectId, int samlAssertionDays)
  {
    DateTime currentDate = new DateTime();
    currentDate = currentDate.plusDays(samlAssertionDays);

    // create name element
    NameIDBuilder nameIdBuilder = new NameIDBuilder();
    NameID nameId = nameIdBuilder.buildObject();
    nameId.setValue(subjectId);
    nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");

    SubjectConfirmationDataBuilder dataBuilder = new SubjectConfirmationDataBuilder();
    SubjectConfirmationData subjectConfirmationData = dataBuilder.buildObject();
    subjectConfirmationData.setNotOnOrAfter(currentDate);

    SubjectConfirmationBuilder subjectConfirmationBuilder = new SubjectConfirmationBuilder();
    SubjectConfirmation subjectConfirmation = subjectConfirmationBuilder.buildObject();
    subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
    subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

    // create subject element
    SubjectBuilder subjectBuilder = new SubjectBuilder();
    Subject subject = subjectBuilder.buildObject();
    subject.setNameID(nameId);
    subject.getSubjectConfirmations().add(subjectConfirmation);

    return subject;
  }

  private AuthnStatement createAuthnStatement(DateTime issueDate)
  {
    // create authcontextclassref object
    AuthnContextClassRefBuilder classRefBuilder = new AuthnContextClassRefBuilder();
    AuthnContextClassRef classRef = classRefBuilder.buildObject();
    classRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

    // create authcontext object
    AuthnContextBuilder authContextBuilder = new AuthnContextBuilder();
    AuthnContext authnContext = authContextBuilder.buildObject();
    authnContext.setAuthnContextClassRef(classRef);

    // create authenticationstatement object
    AuthnStatementBuilder authStatementBuilder = new AuthnStatementBuilder();
    AuthnStatement authnStatement = authStatementBuilder.buildObject();
    authnStatement.setAuthnInstant(issueDate);
    authnStatement.setAuthnContext(authnContext);

    return authnStatement;
  }

  private AttributeStatement createAttributeStatement(HashMap<String, List<String>> attributes)
  {
    // create authenticationstatement object
    AttributeStatementBuilder attributeStatementBuilder = new AttributeStatementBuilder();
    AttributeStatement attributeStatement = attributeStatementBuilder.buildObject();

    AttributeBuilder attributeBuilder = new AttributeBuilder();
    if (attributes != null)
    {
      for (Map.Entry<String, List<String>> entry : attributes.entrySet())
      {
        Attribute attribute = attributeBuilder.buildObject();
        attribute.setName(entry.getKey());

        for (String value : entry.getValue())
        {
          XSStringBuilder stringBuilder = new XSStringBuilder();
          XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                  XSString.TYPE_NAME);
          attributeValue.setValue(value);
          attribute.getAttributeValues().add(attributeValue);
        }

        attributeStatement.getAttributes().add(attribute);
      }
    }

    return attributeStatement;
  }

  private Status createStatus()
  {
    StatusCodeBuilder statusCodeBuilder = new StatusCodeBuilder();
    StatusCode statusCode = statusCodeBuilder.buildObject();
    statusCode.setValue(StatusCode.SUCCESS);

    StatusBuilder statusBuilder = new StatusBuilder();
    Status status = statusBuilder.buildObject();
    status.setStatusCode(statusCode);

    return status;
  }

  private Signature createSignature() throws Exception
  {
    if (keyStoreLocation != null)
    {
      SignatureBuilder builder = new SignatureBuilder();
      Signature signature = builder.buildObject();
      signature.setSigningCredential(getSigningCredential());
      signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
      signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

      return signature;
    }
    return null;
  }

  private Credential getSigningCredential() throws Exception
  {
    KeyStore ks = getStore();
    BasicX509Credential credential = new BasicX509Credential((X509Certificate) ks.getCertificate(this.alias));
    credential.setPrivateKey((PrivateKey) ks.getKey(alias, storePassword));
    return credential;
  }

  public KeyStore getStore() throws Exception
  {
    KeyStore ks = null;
    try (InputStream in = new FileInputStream(this.keyStoreLocation))
    {
      ks = KeyStore.getInstance("PKCS12");
      ks.load(in, storePassword);
      return ks;
    }
  }
}
