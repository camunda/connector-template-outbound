package io.camunda.dd;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.dd.dto.ProcessRequest;
import io.camunda.dd.services.KeycloakService;
import io.camunda.dd.services.OperateService;
import io.camunda.dd.services.TaskService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "task-info",
    inputVariables = {
    		"state",
    		"processInstance",
    		"tasklistAddress",
    		"operateAddress",
    		"keycloakAddress",
    		"clientSecret",
    		"clientId"
    	},
    type = "io.camunda:task-info:1"
)
public class BaseController implements OutboundConnectorFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
 
    @Override
    public Object execute(OutboundConnectorContext context) {
        ProcessRequest connectorRequest = context.bindVariables(ProcessRequest.class);
        
        KeycloakService keycloakService = new KeycloakService(
                connectorRequest.keycloakAddress(),
                connectorRequest.clientId(),
                connectorRequest.clientSecret()
            );
        OperateService operateService = new OperateService(connectorRequest.operateAddress(), keycloakService);
        TaskService taskService = new TaskService(connectorRequest.tasklistAddress(), keycloakService);

        return getTasks(connectorRequest, taskService, operateService);
    }

    private Map<String, Object> getTasks(
    	    final ProcessRequest connectorRequest,
    	    final TaskService taskService,
    	    final OperateService operateService) {
        Long currentProcessId = connectorRequest.processInstance();
        LOGGER.info("Searching for active task for process instance: {}, {}", currentProcessId, connectorRequest.state());

        Map<String, Object> result = new HashMap<>();

        try {
            while (true) {
            	String state = connectorRequest.state() != null ? connectorRequest.state() : "CREATED";
                Map<String, Object> taskResponse = taskService.waitForTaskReady(state, currentProcessId);

                if (isValidTaskResponse(taskResponse)) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("task", taskResponse);
                    return payload;
                }

                Long childProcessId = operateService.searchProcessInstance(currentProcessId);

                if (childProcessId != null) {
                    LOGGER.info("Found subsequent process instance {}. Continuing search.", childProcessId);
                    currentProcessId = childProcessId;
                } else {
                    result.put("error", "Active task for process " + currentProcessId + " not found, and the process chain has ended.");
                    return result;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching tasks for process {}: {}", currentProcessId, e.getMessage());
            result.put("error", "Unexpected error occurred: " + e.getMessage());
            return result;
        }
    }

    private boolean isValidTaskResponse(Map<String, Object> taskResponse) {
        return taskResponse != null &&
               taskResponse.containsKey("formId") &&
               taskResponse.containsKey("processDefinitionKey") &&
               taskResponse.containsKey("id");
    }
}