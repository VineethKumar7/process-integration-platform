package dev.vineeth.pip.legacy;

import dev.vineeth.pip.api.ApprovalRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bridges legacy partner XML into the canonical {@link ApprovalRequest} form
 * via an XSLT transformation that emits JSON. Saxon-HE is used because it
 * supports XSLT 3.0 + the json-to-xml / xml-to-json functions.
 */
@Component
public class LegacyXmlBridge {

    private final TransformerFactory factory;
    private final ObjectMapper json = new ObjectMapper();

    @Value("classpath:xslt/legacy-to-json.xsl")
    private Resource stylesheet;

    public LegacyXmlBridge() {
        // Force Saxon — explicit so the build doesn't fall back to JDK XSLT 1.0.
        this.factory = TransformerFactory.newInstance(
            "net.sf.saxon.TransformerFactoryImpl",
            getClass().getClassLoader()
        );
    }

    public ApprovalRequest toApprovalRequest(String legacyXml) {
        try {
            Source xslt = new StreamSource(stylesheet.getInputStream());
            Transformer t = factory.newTransformer(xslt);
            // Saxon HE: enable safe XSLT output configuration
            t.setOutputProperty("indent", "no");
            StringWriter out = new StringWriter();
            t.transform(new StreamSource(new StringReader(legacyXml)), new StreamResult(out));

            String body = out.toString().trim();
            if (body.isEmpty()) {
                throw new IllegalArgumentException(
                    "Legacy XML did not match the expected partner schema — see test case TC-XSLT-001"
                );
            }
            JsonNode node = json.readTree(body);
            String requestId = node.path("requestId").asText("");
            if (requestId.isBlank()) {
                throw new IllegalArgumentException(
                    "Legacy XML produced no requestId — see test case TC-XSLT-001"
                );
            }
            return new ApprovalRequest(
                requestId,
                node.path("requester").asText(""),
                new BigDecimal(node.path("amount").asText("0")),
                node.path("managerId").asText(null),
                node.path("reason").asText(null)
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Legacy XML did not transform to a valid approval request — see test case TC-XSLT-001",
                e
            );
        }
    }
}
