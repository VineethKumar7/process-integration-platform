package dev.vineeth.pip.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("autoApprover")
public class AutoApprover implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(AutoApprover.class);

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("decision", "AUTO_APPROVED");
        execution.setVariable("decidedBy", "system");
        log.info("Auto-approved request {}", execution.getVariable("requestId"));
    }
}
