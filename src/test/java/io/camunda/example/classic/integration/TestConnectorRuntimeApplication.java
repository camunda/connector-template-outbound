package io.camunda.example.classic.integration;

import io.camunda.client.annotation.Deployment;
import io.camunda.connector.api.document.DocumentFactory;
import io.camunda.connector.runtime.core.document.DocumentFactoryImpl;
import io.camunda.connector.runtime.core.document.store.InMemoryDocumentStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
@ImportAutoConfiguration({
    io.camunda.connector.runtime.InboundConnectorsAutoConfiguration.class,
    io.camunda.connector.runtime.OutboundConnectorsAutoConfiguration.class,
    io.camunda.connector.runtime.WebhookConnectorAutoConfiguration.class
})
@MockitoBean("SearchQueryClient")
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class TestConnectorRuntimeApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestConnectorRuntimeApplication.class, args);
  }

  @Bean
  @Primary
  public DocumentFactory documentFactory() {
    return new DocumentFactoryImpl(InMemoryDocumentStore.INSTANCE);
  }
}