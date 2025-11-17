> A Connector template for new C8 outbound connector
>
> To use this template update the following resources to match the name of your connector:
>
> * [README](./README.md) (title, description)
> * [Element Template](./element-templates/template-connector.json)
> * [POM](./pom.xml) (artifact name, id, description)
> * [Connector Function](src/main/java/io/camunda/example/classic/MyConnectorFunction.java) (rename, implement, update
    `OutboundConnector` annotation)
> * [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorFunction) (
    rename)
>
>
> about [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
>
> Check out the [Connectors SDK](https://github.com/camunda/connectors)

# Connector Template

Camunda Outbound Connector Template

This template project illustrates two different approaches to implement a Camunda Outbound Connector.
Both implementations are supported by the Connector SDK and can be used as a foundation for building your own
Connector. You can choose the approach that best fits your needs:

## Classic API - `OutboundConnectorFunction`

Example implementation: [`io.camunda.example.classic.MyConnectorFunction`](src/main/java/io/camunda/example/classic/MyConnectorFunction.java).

This approach utilizes the traditional `OutboundConnectorFunction` interface from the Connector SDK.

## Operations API - `OutboundConnectorProvider`

Example implementation: [`io.camunda.example.operations.MyConnectorProvider`](src/main/java/io/camunda/example/operations/MyConnectorProvider.java).

Another example implementation from the main Connectors repository can be found [here](https://github.com/camunda/connectors/blob/23e577ead64c2a6b478b05d2ea3100ca6846e70a/connectors/csv/src/main/java/io/camunda/connector/csv/CsvConnector.java).

This approach leverages the newer Operations API by implementing the `OutboundConnectorProvider` interface.
The benefit of this approach is that it allows you to define multiple operations within a single Connector
without the need to explicitly handle the routing of operations within a connector yourself.

The element template generator tool also supports both approaches and will generate the appropriate
element templates based on the implementation you choose.

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

| Name     | Description      | Example           | Notes                                                                      |
|----------|------------------|-------------------|----------------------------------------------------------------------------|
| username | Mock username    | `alice`           | Has no effect on the function call outcome.                                |
| token    | Mock token value | `my-secret-token` | Has no effect on the function call outcome.                                |
| message  | Mock message     | `Hello World`     | Echoed back in the output. If starts with 'fail', an error will be thrown. |

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

Add the `element-templates/template-connector-message-start-event.json` to your Modeler configuration as per
the [Element Templates documentation](https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/).

#### Using Your Connector
Then, to use your connector in a local Camunda environment, follow these steps:

1. Run `io.camunda.connector.inbound.LocalConnectorRuntime` to start your connector.
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

1. Start your connector by executing `io.camunda.connector.inbound.LocalConnectorRuntime` in your development
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
in [element-templates/template-connector.json](./element-templates/template-connector.json).
