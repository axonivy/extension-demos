# Ivy SAML 2.0 Valve
Demonstrates how to write your own Valve to implement SSO using a sent SAML Token.

For a basic introduction into SSO with SAML follow this link: https://www.secureauth.com/blog/introduction-to-saml

See https://developer.axonivy.com/doc/8.0/engine-guide/configuration/files/context-xml.html and https://developer.axonivy.com/doc/8.0/engine-guide/integration/web-application-firewall.html for more information.

Note that a custom valve implementation can only be loaded by the Axon.ivy Engine if its classes are accessible by the engines OSGi environment.
See the [ProcessingValve Tomcat example](https://github.com/ivy-samples/ivy-extension-demos/tree/master/ProcessingValve) for general information how to create a custom valve.

Troubleshooting: If your valve is not working as expected, consult the dropins/README.html.

Also see `META-INF/MANIFEST.MF`.
