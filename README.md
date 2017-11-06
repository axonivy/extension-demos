# Custom Tomcat Valve
Demonstrates how to write your own Valve for the embedded Tomcat webserver of Axon.ivy.
A valve can be used to implement your custom user authentication scenario.

## Axon.ivy integration
This demo is a good expample to show how custom code can extend the Axon.ivy core.
It comes as OSGi bundle: means that the `META-INF/MANIFEST.MF` strictly steers the integration into the existing class realm of Axon.ivy:
- bundle-id
- require-bundle (set dependencies to the ivy.core)
- buddy-policy (define which bundle of the ivy.core can consume classes of my custom bundle)
