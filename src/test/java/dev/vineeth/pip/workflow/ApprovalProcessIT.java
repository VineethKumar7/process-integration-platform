package dev.vineeth.pip.workflow;

import dev.vineeth.pip.api.ApprovalRequest;
import dev.vineeth.pip.service.ApprovalService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test of the ApprovalProcess BPMN.
 *
 * Two scenarios are exercised here:
 *   TC-PROC-200 — auto-approval path (amount ≤ 5000)
 *   TC-PROC-201 — manager-review path (amount &gt; 5000)
 *
 * Each scenario asserts the BPMN flow walked the expected activities and
 * ended at the same `EndEvent_Done`.  Regression descriptions for each
 * gateway are kept inline so a failing run reads like a structured bug
 * report rather than a stack trace.
 */
@SpringBootTest
class ApprovalProcessIT {

    @Autowired private ApprovalService approvalService;
    @Autowired private RuntimeService runtimeService;
    @Autowired private TaskService taskService;

    @Test
    @DisplayName("TC-PROC-200: amount=100 → auto-approved + ended")
    void autoApprovesSmallRequests() {
        String pid = approvalService.submit(new ApprovalRequest(
            "REQ-AUTO-1", "vineeth", new BigDecimal("100"), "MGR-1", "lab supplies"));

        assertThat(processInstance(pid))
            .hasPassed("Task_ValidateRequest", "Task_AutoApprove",
                       "Task_NotifyUpstream", "EndEvent_Done")
            .isEnded();

        assertEquals("AUTO_APPROVED",
            runtimeServiceVariable(pid, "decision"),
            "Auto-approval branch must persist decision=AUTO_APPROVED");
    }

    @Test
    @DisplayName("TC-PROC-201: amount=20000 → user task waits + completes to end")
    void routesLargeAmountsThroughManager() {
        String pid = approvalService.submit(new ApprovalRequest(
            "REQ-MGR-1", "vineeth", new BigDecimal("20000"), "MGR-7", "infra contract"));

        assertThat(processInstance(pid))
            .isWaitingAt("Task_ManagerReview")
            .task().hasName("Manager review");

        Task t = taskService.createTaskQuery()
            .processInstanceId(pid)
            .singleResult();
        taskService.complete(t.getId());

        assertThat(processInstance(pid))
            .hasPassed("Task_ManagerReview", "Task_NotifyUpstream", "EndEvent_Done")
            .isEnded();
    }

    private Object runtimeServiceVariable(String pid, String name) {
        return runtimeService.createHistoricVariableInstanceQuery()
            .processInstanceIdIn(pid)
            .variableName(name)
            .singleResult()
            .getValue();
    }
}
