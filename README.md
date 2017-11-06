# Axon.ivy integration
The demos in this repository show how custom code can extend the Axon.ivy core.

## Bundling
The Axon.ivy core JAR libraries life in an OSGi environment. Therefore contributions to these JARs must be OSGi compliant by itself. This means that extension JARs must not only contain the binary code of your classes. But as well an enriched `META-INF/MANIFEST.MF` that describes OSGi bundle meta data.

### How to turn your JAR into a bundle
To make your own JAR an OSGi bundle the following Manifest entries are important:
- bundle name and versions are strictly required
```
Bundle-ManifestVersion: 2
Bundle-SymbolicName: com.acme.MyExtension
Bundle-Version: 1.0.0.qualifier
```
- define bundles from the ivy.core that are required to compile your sources
```
Require-Bundle: ch.ivyteam.ivy.server;bundle-version="7.0.0"
```
- define which bundle of the ivy.core can consume classes of your bundle by registering your bundle as buddy
```
Eclipse-RegisterBuddy: ch.ivyteam.tomcat
```

