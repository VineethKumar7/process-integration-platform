package dev.vineeth.pip.legacy;

import dev.vineeth.pip.api.ApprovalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LegacyXmlBridgeTest {

    private LegacyXmlBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new LegacyXmlBridge();
        ReflectionTestUtils.setField(bridge, "stylesheet",
            new ClassPathResource("xslt/legacy-to-json.xsl"));
    }

    @Test
    @DisplayName("TC-XSLT-100: legacy XML → canonical request with summed amounts")
    void parsesLegacyXmlAndSumsLines() throws Exception {
        String xml = new String(
            new ClassPathResource("legacy-sample.xml").getInputStream().readAllBytes(),
            StandardCharsets.UTF_8
        );

        ApprovalRequest req = bridge.toApprovalRequest(xml);

        assertEquals("REQ-9001", req.requestId());
        assertEquals("vineeth", req.requester());
        // Two line totals: 1499.50 + 500.00
        assertEquals(0, new BigDecimal("1999.50").compareTo(req.amount()),
            "Amount must equal the sum of <Line><Total/> values");
        assertEquals("MGR-23", req.managerId());
        assertEquals("Server replacement", req.reason());
    }

    @Test
    @DisplayName("TC-XSLT-101: malformed input throws with reference to test case")
    void rejectsMalformedXml() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> bridge.toApprovalRequest("<not-approval/>")
        );
        assertTrue(ex.getMessage().contains("TC-XSLT-001"),
            "error message should reference the canonical test-case ID");
    }
}
