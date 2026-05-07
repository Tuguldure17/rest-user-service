package com.soa.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * SOAP Auth Service руу ValidateToken хүсэлт илгээх client.
 * JSON Service-ийн middleware нь энийг ашиглана.
 *
 * Flow: JSON Service Middleware -> SoapAuthClient -> SOAP ValidateToken -> true/false
 */
@Component
public class SoapAuthClient {

    @Value("${soap.auth.url}")
    private String soapUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Token-г SOAP сервис дээр шалгана.
     * @return ValidateResult (valid, userId)
     */
    public ValidateResult validateToken(String token) {
        String soapEnvelope = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:auth="http://example.com/auth">
              <soapenv:Header/>
              <soapenv:Body>
                <auth:ValidateTokenRequest>
                  <auth:token>%s</auth:token>
                </auth:ValidateTokenRequest>
              </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(escapeXml(token));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(soapUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return parseValidateResponse(response.body());

        } catch (Exception e) {
            System.err.println("[SoapAuthClient] ValidateToken error: " + e.getMessage());
            return new ValidateResult(false, null);
        }
    }

    // ── XML parse ────────────────────────────────────────────
    private ValidateResult parseValidateResponse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

            boolean valid = false;
            Long userId   = null;

            NodeList validNodes = doc.getElementsByTagNameNS("*", "valid");
            if (validNodes.getLength() > 0) {
                valid = Boolean.parseBoolean(validNodes.item(0).getTextContent().trim());
            }

            NodeList userIdNodes = doc.getElementsByTagNameNS("*", "userId");
            if (userIdNodes.getLength() > 0) {
                String uid = userIdNodes.item(0).getTextContent().trim();
                if (!uid.isEmpty()) userId = Long.parseLong(uid);
            }

            return new ValidateResult(valid, userId);

        } catch (Exception e) {
            System.err.println("[SoapAuthClient] Parse error: " + e.getMessage());
            return new ValidateResult(false, null);
        }
    }

    private String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    public record ValidateResult(boolean valid, Long userId) {}
}
