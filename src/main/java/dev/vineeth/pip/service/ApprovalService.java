package dev.vineeth.pip.service;

import dev.vineeth.pip.api.ApprovalRequest;
import dev.vineeth.pip.legacy.LegacyXmlBridge;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ApprovalService {

    private static final String PROCESS_KEY = "ApprovalProcess";

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final LegacyXmlBridge legacyXmlBridge;

    public ApprovalService(RuntimeService runtimeService,
                           HistoryService historyService,
                           LegacyXmlBridge legacyXmlBridge) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.legacyXmlBridge = legacyXmlBridge;
    }

    public String submit(ApprovalRequest req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("requestId",  req.requestId());
        vars.put("requester",  req.requester());
        vars.put("amount",     req.amount());
        vars.put("managerId",  req.managerId() == null ? "default-manager" : req.managerId());
        vars.put("reason",     req.reason());

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
            PROCESS_KEY, req.requestId(), vars
        );
        return pi.getProcessInstanceId();
    }

    public String submitLegacyXml(String legacyXml) {
        ApprovalRequest normalised = legacyXmlBridge.toApprovalRequest(legacyXml);
        return submit(normalised);
    }

    public Map<String, Object> status(String processInstanceId) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId).singleResult();
        Map<String, Object> out = new LinkedHashMap<>();
        if (hpi == null) {
            out.put("found", false);
            return out;
        }
        out.put("found", true);
        out.put("businessKey", hpi.getBusinessKey());
        out.put("startTime", hpi.getStartTime());
        out.put("endTime",   hpi.getEndTime());
        out.put("ended",     hpi.getEndTime() != null);
        out.put("durationMs", hpi.getDurationInMillis());
        return out;
    }
}
