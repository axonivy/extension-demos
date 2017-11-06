# Custom Log Appender
Demonstrates how you could write a Log Appender that can be enabled to be used by Axon.ivy. trough the `configurations/log4jconfig.xml` file.

## Integration
- The custom log appender must contribute the Axon.ivy core bundle `commons.lib`, that contains Log4j and its related libraries. Therefore this bundle registers itself as buddy of `commons.lib`. See [META-INF/MANIFEST.MF](https://github.com/ivy-samples/tomcatValve/blob/master/CustomLogAppender/META-INF/MANIFEST.MF) `Eclipse-RegisterBuddy: commons.lib`

## Installation
- build this JAR via `mvn clean verify`. The jar is then create as `target/com.acme.CustomLogAppender-XYZ-SNAPSHOT.jar`.
- copy the JAR into the `dropins` directory of an Axon.ivy Engine or Designer
- enable the appender in the log4jconfig.xml by adding a custom appender
```xml
  <appender name="MyWsAppender" class="com.acme.custom.SoapTaskLogAppender">
    <param name="Threshold" value="DEBUG"/>
    <layout class="ch.ivyteam.log.layout.IvyLog4jLayout">
      <param name="DateFormat" value="HH:mm:ss.SSS"/>
      <param name="ContextPrinting" value="false"/>
      <param name="FixedCategoryLength" value="20"/>
      <param name="ThreadPrinting" value="false"/>
    </layout>
  </appender>
  
  <root> 
    <level value ="WARN" /> 
    <appender-ref ref="FileLog"/> 
    <appender-ref ref="ConsoleAppender"/> 
    <appender-ref ref="MyWsAppender"/>
  </root>
```
