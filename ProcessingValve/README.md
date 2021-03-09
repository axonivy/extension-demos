# Custom Tomcat Valve
Demonstrates how to write your own Valve for the embedded Tomcat webserver of Axon Ivy.

A valve can be used to implement your custom user authentication scenario. See the [Engine Guide](https://developer.axonivy.com/doc/latest/EngineGuideHtml/configuration.html#configuration-tomcat-context-xml).

You can configure any third party valve or even your own implementation of a valve. A custom valve implementation can only be loaded by the Axon Ivy Engine if its classes are accessible by the engines OSGi environment. This can be reached as follows:

1. The valve JAR must be implemented as OSGi bundle. This means that the standard JAR manifest (`META-INF/MANIFEST.MF`) must also contain headers to declare the id, name and version of this bundle. These headers will be set automatically if the bundle is created within the Axon Ivy Designer via `File > New > Other... > Plug-in Project`
2. Your bundle must require the bundle with the id `ch.ivyteam.tomcat` of the Axon Ivy Engine.
3. Your bundle must register itself as buddy of the tomcat bundle by setting the `MANIFEST.MF header: Eclipse-RegisterBuddy: ch.ivyteam.tomcat`
4. The package that contains the custom valve must be exported in the MANIFEST.
5. Export the bundle as `JAR: Menu Export > Deployable Plug-ins and Fragments`.
6. The custom bundle must be copied into the `dropins` directory of the Axon Ivy Engine.

Troubleshooting: If your valve is not working as expected, consult the dropins/README.html.

Sample `META-INF/MANIFEST.MF`:

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: ProcessingValve
Bundle-SymbolicName: com.acme.ProcessingValve;singleton:=true
Bundle-Version: 1.0.0.qualifier
Require-Bundle: ch.ivyteam.tomcat
Eclipse-RegisterBuddy: ch.ivyteam.tomcat
Export-Package: com.acme.valve
```
