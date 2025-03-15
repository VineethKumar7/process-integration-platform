package dev.vineeth.pip.api;

import dev.vineeth.pip.service.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody ApprovalRequest req) {
        String processId = approvalService.submit(req);
        return ResponseEntity.accepted().body(Map.of(
            "processInstanceId", processId,
            "requestId", req.requestId(),
            "status", "STARTED"
        ));
    }

    @PostMapping(value = "/legacy", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Map<String, Object>> submitLegacyXml(@RequestBody String xml) {
        String processId = approvalService.submitLegacyXml(xml);
        return ResponseEntity.accepted().body(Map.of(
            "processInstanceId", processId,
            "status", "STARTED_FROM_LEGACY_XML"
        ));
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String processInstanceId) {
        return ResponseEntity.ok(approvalService.status(processInstanceId));
    }
}
