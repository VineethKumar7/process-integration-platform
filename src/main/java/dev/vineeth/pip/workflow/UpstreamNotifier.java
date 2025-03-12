package dev.vineeth.pip.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("upstreamNotifier")
public class UpstreamNotifier implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(UpstreamNotifier.class);

    @Override
    public void execute(DelegateExecution execution) {
        // In a real deployment this would post to a partner webhook.
        // For the workshop project we just record the outcome on the process.
        Object requestId = execution.getVariable("requestId");
        Object decision = execution.getVariable("decision");
        log.info("Notified upstream system: request={} decision={}", requestId, decision);
        execution.setVariable("notified", true);
    }
}
