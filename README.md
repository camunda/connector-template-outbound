> A Connector template for new C8 outbound connector
>
> To use this template update the following resources to match the name of your connector:
>
> * [README](./README.md) (title, description)
> * [POM](./pom.xml) (artifact name, id, description)
> * [Connector](src/main/java/io/camunda/example/MyConnector.java) (rename, implement, update
    `OutboundConnector` annotation)
> * [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider) (
    adapt to your connector class)
> * [Element Template](./element-templates/my-connector.json) (will be generated during build)
>
> about [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
>
> Check out the [Connectors SDK](https://github.com/camunda/connectors)

# Connector Template

Camunda Outbound Connector Template

This repository provides a template for creating a Camunda Outbound Connector using the Connector SDK. 
The example is using the annotations-based approach that allows to define multiple operations within a single Connector.

## Operations API - `OutboundConnectorProvider`

Example implementation: [`io.camunda.example.MyConnector`](src/main/java/io/camunda/example/MyConnector.java).

This example leverages the `@Operation` annotation to define multiple operations within a single Connector class. 
The example defines two operations - `echo` and `addTwoNumbers` - each represented by a method annotated with `@Operation`.
Each operation method accepts an input parameter annotated with `@Variable` (or `@Header`) and returns an output object.

The runtime uses the `OutboundConnectorProvider` interface to discover and instantiate the Connector. If you rename your
Connector class, make sure to update the corresponding entry in the `META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider` file.

Another example implementation for an annotations-based Connector is the [CSV Connector](https://github.com/camunda/connectors/blob/main/connectors/csv/src/main/java/io/camunda/connector/csv/CsvConnector.java).

## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- A fat JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK
  artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing
  the Connector.

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and
the [Connector Runtime](https://github.com/camunda/connectors).
This helps to avoid classpath conflicts when the Connector is executed.


For example, without shading, you might encounter errors like:
```
java.lang.NoSuchMethodError: com.fasterxml.jackson.databind.ObjectMapper.setserializationInclusion(Lcom/fasterxml/jackson/annotation/JsonInclude$Include;)Lcom/fasterxml/jackson/databind/ObjectMapper;
```
This occurs when your connector and the runtime use different versions of the same library (e.g., Jackson).

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html)
provides more details on relocations.

## API

### Input

| Name    | Description      | Example           | Notes                                                                      |
|---------|------------------|-------------------|----------------------------------------------------------------------------|
| user    | Mock username    | `alice`           | Has no effect on the function call outcome.                                |
| token   | Mock token value | `my-secret-token` | Has no effect on the function call outcome.                                |
| message | Mock message     | `Hello World`     | Echoed back in the output. If starts with 'fail', an error will be thrown. |

### Output

```json
{
  "result":{
    "myProperty":"Message received: ..."
  }
}
```

### Error codes

| Code | Description                                |
|------|--------------------------------------------|
| FAIL | Message starts with 'fail' (ignoring case) |

## Test locally

Run unit tests

```bash
mvn clean verify
```

## Testing
### Unit and Integration Tests

You can run the unit and integration tests by executing the following Maven command:
```bash
mvn clean verify
```

### Local environment

#### Prerequisites
You will need the following tools installed on your machine:
1. Camunda Modeler, which is available in two variants:
    - [Desktop Modeler](https://camunda.com/download/modeler/) for a local installation.
    - [Web Modeler](https://modeler.camunda.io/) for an online experience.

2. [Docker](https://www.docker.com/products/docker-desktop), which is required to run the Camunda platform.

#### Setting Up the Camunda platform

The Connectors Runtime requires a running Camunda platform to interact with. To set up a local Camunda environment, follow these steps:

1. Clone the [Camunda distributions repository](https://github.com/camunda/camunda-distributions) from GitHub and navigate to the Camunda 8.8 docker-compose directory:

```shell
git clone git@github.com:camunda/camunda-distributions.git
cd cd docker-compose/versions/camunda-8.8
```

**Note:** This template is compatible with Camunda 8.8. Using other versions may lead to compatibility issues.

Either comment out the connectors service, or use the `--scale` flag to exclude it:

```shell
docker compose -f docker-compose-core.yaml up --scale connectors=0
```

#### Configure the Desktop Modeler and Use Your Connector

Add the `element-templates/my-connector.json` to your Modeler configuration as per
the [Element Templates documentation](https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/).

#### Using Your Connector
Then, to use your connector in a local Camunda environment, follow these steps:

1. Run the [`io.camunda.example.LocalConnectorRuntime`](src/test/java/io/camunda/example/LocalConnectorRuntime.java) to start your connector for testing purposes.
2. Open the Camunda Desktop Modeler and create a new BPMN diagram.
3. Design a process that incorporates your newly created connector.
4. Deploy the process to your local Camunda platform.
5. Verify that the process is running smoothly by accessing Camunda Operate at [localhost:8088/operate](http://localhost:8088/operate). Username and password are both `demo`.

### SaaS environment

#### Creating an API Client

The Connectors Runtime (LocalConnectorRuntime) requires connection details to interact with your Camunda SaaS cluster. To set this up, follow these steps:

1. Navigate to Camunda [SaaS](https://console.camunda.io).
2. Create a cluster using the latest version available.
3. Select your cluster, then go to the `API` section and click `Create new Client`.
4. Ensure the `zeebe` checkbox is selected, then click `Create`.
5. Copy the configuration details displayed under the `Spring Boot` tab.
6. Paste the copied configuration into your `application.properties` file within your project.

#### Using Your Connector

1. Start your connector by executing `io.camunda.example.LocalConnectorRuntime` in your development
   environment.
2. Access the Web Modeler and create a new project.
3. Click on `Create new`, then select `Upload files`. Upload the connector template from the repository you have.
4. After uploading, **publish the connector template** by clicking the Publish button.
5. In the same folder, create a new BPMN diagram.
6. Design and start a process that incorporates your new connector.

## Element Template

The element template for this sample connector is generated automatically based on the connector
input class using
the [Element Template Generator](https://github.com/camunda/connectors/tree/main/element-template-generator/core).

The generation is embedded in the Maven build and can be triggered by running `mvn clean package`.

The generated element template can be found
in [element-templates/my-connector.json](./element-templates/my-connector.json).
