# Custom Log Appender
Demonstrates how to write a log appender that can be enabled for use by Axon Ivy via the `configurations/log4j2.xml' file. 
The appender attaches itself to the desired logs. 
To see the output, you need an error log in the project, which the appender can attach. The error log should be in the process in which you want the appender to append to the log. The log should look like this: ```Ivy.log().error("your log info"); ``` 

## Integration
- The custom log appender must contribute the Axon Ivy core bundle `ch.ivyteam.lib.logging`, 
  which includes Log4j2 and its related libraries. Therefore this bundle 
  registers itself as buddy of `ch.ivyteam.lib.logging`. See [META-INF/MANIFEST.MF]
  (https://github.com/ivy-samples/tomcatValve/blob/master/CustomLogAppender/META-INF/MANIFEST.MF) 
  `Eclipse-RegisterBuddy: ch.ivyteam.lib.logging`

## Installation
- build this JAR using `mvn clean verify`. The jar will then be created as `target/com.acme.CustomLogAppender-XYZ-SNAPSHOT.jar`.
- copy the JAR into the `dropins` directory of an Axon Ivy Engine or Designer.
- enable the appender in the log4j2.xml by adding a custom appender.
```xml
  <Appenders>
    <SoapTaskLogAppender name="MyWsAppender">
    </SoapTaskLogAppender>
  </Appenders>

  <Loggers>
    <Root level="warn" includeLocation="false">
      <AppenderRef ref="IvyLog" />
      <AppenderRef ref="ConsoleLog" />
      <AppenderRef ref="MyWsAppender" />
    </Root>
  </Loggers>
```
