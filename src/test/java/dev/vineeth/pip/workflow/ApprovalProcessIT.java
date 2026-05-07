package dev.vineeth.pip.workflow;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test of the {@code ApprovalProcess} BPMN.
 *
 * Two scenarios:
 *   TC-PROC-200 — auto-approval path (amount ≤ 5000)
 *   TC-PROC-201 — manager-review path (amount &gt; 5000)
 *
 * The process instance is started via {@link RuntimeService} directly so we
 * can hold the {@link ProcessInstance} reference for {@code assertThat(pi)}
 * — that overload works for both live and ended instances (camunda-bpm-assert
 * pulls history when the instance has finished).
 */
@SpringBootTest
class ApprovalProcessIT {

    @Autowired private RuntimeService runtimeService;
    @Autowired private TaskService taskService;
    @Autowired private HistoryService historyService;

    @Test
    @DisplayName("TC-PROC-200: amount=100 → auto-approved + ended")
    void autoApprovesSmallRequests() {
        String requestId = "REQ-AUTO-" + UUID.randomUUID();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
            "ApprovalProcess", requestId, vars(requestId, new BigDecimal("100")));

        assertThat(pi)
            .hasPassed("Task_ValidateRequest", "Task_AutoApprove",
                       "Task_NotifyUpstream", "EndEvent_Done")
            .isEnded();

        Object decision = historyService.createHistoricVariableInstanceQuery()
            .processInstanceIdIn(pi.getProcessInstanceId())
            .variableName("decision")
            .singleResult()
            .getValue();
        assertEquals("AUTO_APPROVED", decision,
            "Auto-approval branch must persist decision=AUTO_APPROVED");
    }

    @Test
    @DisplayName("TC-PROC-201: amount=20000 → user task waits + completes to end")
    void routesLargeAmountsThroughManager() {
        String requestId = "REQ-MGR-" + UUID.randomUUID();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
            "ApprovalProcess", requestId, vars(requestId, new BigDecimal("20000")));

        assertThat(pi)
            .isWaitingAt("Task_ManagerReview")
            .task().hasName("Manager review");

        Task t = taskService.createTaskQuery()
            .processInstanceId(pi.getProcessInstanceId())
            .singleResult();
        taskService.complete(t.getId());

        assertThat(pi)
            .hasPassed("Task_ManagerReview", "Task_NotifyUpstream", "EndEvent_Done")
            .isEnded();
    }

    private static Map<String, Object> vars(String requestId, BigDecimal amount) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("requestId", requestId);
        vars.put("requester", "vineeth");
        vars.put("amount",    amount);
        vars.put("managerId", "MGR-1");
        vars.put("reason",    "test");
        return vars;
    }
}
