package dev.vineeth.pip.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidatorTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    @DisplayName("TC-VAL-100: missing amount → IllegalStateException referencing TC-VAL-001")
    void missingAmountIsRejected() {
        DelegateExecution exec = Mockito.mock(DelegateExecution.class);
        Mockito.when(exec.getVariable("requestId")).thenReturn("REQ-1");
        Mockito.when(exec.getVariable("amount")).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> validator.execute(exec));
        assertTrue(ex.getMessage().contains("TC-VAL-001"));
    }

    @Test
    @DisplayName("TC-VAL-101: missing requestId → IllegalStateException referencing TC-VAL-002")
    void missingRequestIdIsRejected() {
        DelegateExecution exec = Mockito.mock(DelegateExecution.class);
        Mockito.when(exec.getVariable("amount")).thenReturn(new BigDecimal("100"));
        Mockito.when(exec.getVariable("requestId")).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> validator.execute(exec));
        assertTrue(ex.getMessage().contains("TC-VAL-002"));
    }

    @Test
    @DisplayName("TC-VAL-102: valid request → process variable 'validated' set to true")
    void validRequestSetsValidatedFlag() {
        DelegateExecution exec = Mockito.mock(DelegateExecution.class);
        Mockito.when(exec.getVariable("amount")).thenReturn(new BigDecimal("100"));
        Mockito.when(exec.getVariable("requestId")).thenReturn("REQ-2");

        assertDoesNotThrow(() -> validator.execute(exec));
        Mockito.verify(exec).setVariable("validated", true);
    }
}
