package dev.vineeth.pip.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("requestValidator")
public class RequestValidator implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    @Override
    public void execute(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        Object requestId = execution.getVariable("requestId");
        if (amount == null) {
            throw new IllegalStateException(
                "Missing 'amount' for request " + requestId + " — see test case TC-VAL-001"
            );
        }
        if (requestId == null) {
            throw new IllegalStateException("Missing 'requestId' — see test case TC-VAL-002");
        }
        log.info("Validated request {} (amount={})", requestId, amount);
        execution.setVariable("validated", true);
    }
}
