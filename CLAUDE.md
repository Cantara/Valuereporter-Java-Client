# Valuereporter-java-Client

## Purpose
Client library responsible for collecting observations and activities from Java applications and forwarding them to the Valuereporter server. Provides a programmatic API for applications that want to report usage data without using the javaagent approach.

## Tech Stack
- Language: Java 8+
- Framework: None (pure library)
- Build: Maven
- Key dependencies: SLF4J

## Architecture
Lightweight client library that provides an API for manually reporting method calls and activities to the Valuereporter server. Alternative to the Valuereporter-Agent for cases where bytecode instrumentation is not suitable. Handles batching, buffering, and HTTP transport of observations.

## Key Entry Points
- Client API classes in `src/main/java/`
- `pom.xml` - Maven coordinates: `org.valuereporter:valuereporter-java-client`

## Development
```bash
# Build
mvn clean install

# Test
mvn test
```

## Domain Context
Production observability client. Provides programmatic reporting of application usage to Valuereporter, complementing the Valuereporter-Agent for scenarios requiring explicit instrumentation.
