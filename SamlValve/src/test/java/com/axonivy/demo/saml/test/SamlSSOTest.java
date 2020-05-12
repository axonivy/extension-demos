package com.axonivy.demo.saml.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.connector.Request;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;

import com.axonivy.demo.saml.UserAuthenticator;

public class SamlSSOTest {
  
  @BeforeAll
  static void initOpenSaml() throws InitializationException
  {
    InitializationService.initialize();
  }

  @Test
  void userAuth() throws Exception
  {
    SamlAssertionProducer prod = new SamlAssertionProducer("selfsigned.p12", "changeit".toCharArray(), "ivysaml");
    String samlToken = prod.createSAMLResponse("guest", new DateTime(), null, "MySamlIssuer", 3);
    
    UserAuthenticator userAuth = new UserAuthenticator(new MockRequest(samlToken));
    userAuth.authenticate("certificate.cer");
    assertThat(userAuth.getAssertion().getSubject().getNameID().getValue()).isEqualTo("guest");
    assertThat(userAuth.getAssertion().getIssuer().getValue()).isEqualTo("MySamlIssuer");
  }

  void ssoLogin() throws Exception
  {
    SamlAssertionProducer prod = new SamlAssertionProducer("selfsigned.p12", "changeit".toCharArray(), "zugpcarus");
    String samlToken = prod.createSAMLResponse("guest", new DateTime(), null, null, 3);
    HttpResponse httpResponse = startProcess("demo-portal/PortalExamples/164211E97C598DAA/DefaultApplicationHomePage.ivp", samlToken);
    String response = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
    System.out.println("response="+response);
    assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
  }

  private static HttpResponse startProcess(String pathToIvp, String samlToken) throws IOException
  {
    try (CloseableHttpClient client = HttpClientBuilder.create().build())
    {
      // http://zugpcarus:8080/ivy/pro/demo-portal/PortalExamples/164211E97C598DAA/DefaultApplicationHomePage.ivp
      HttpPost post = new HttpPost("http://zugpcarus:8080/ivy/pro/"+pathToIvp);
      List<NameValuePair> urlParameters = new ArrayList<>();
      urlParameters.add(new BasicNameValuePair("SAMLResponse", samlToken));
      post.setEntity(new UrlEncodedFormEntity(urlParameters));
      return client.execute(post);
    }
  }

  private static class MockRequest extends Request
  {
    private final String samlToken;

    public MockRequest(String samlToken)
    {
      super(null);
      this.samlToken = samlToken;
    }
    
    @Override
    public String getParameter(String paramName)
    {
      return samlToken;
    }
  }
}
