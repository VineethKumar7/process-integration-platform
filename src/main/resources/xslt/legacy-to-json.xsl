<?xml version="1.0" encoding="UTF-8"?>
<!--
  Transforms a legacy partner approval document
  ───────────────────────────────────────────────
  <Approval xmlns="urn:partner:approval:v1">
      <Header>
          <Id>REQ-9001</Id>
          <Submitter ManagerCode="MGR-23">vineeth</Submitter>
      </Header>
      <Lines>
          <Line><Total>1499.50</Total></Line>
          <Line><Total>500.00</Total></Line>
      </Lines>
      <Reason>Server replacement</Reason>
  </Approval>

  …into the canonical JSON shape consumed by ApprovalService:
      { "requestId": "REQ-9001", "requester": "vineeth",
        "amount": "1999.50", "managerId": "MGR-23",
        "reason": "Server replacement" }
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="urn:partner:approval:v1"
                exclude-result-prefixes="p"
                version="3.0">

    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:template match="/p:Approval">
        <xsl:variable name="amountSum"
                      select="sum(p:Lines/p:Line/p:Total)"/>
        <xsl:text>{&#10;</xsl:text>
        <xsl:text>  "requestId": </xsl:text>
        <xsl:value-of select="concat('&quot;', normalize-space(p:Header/p:Id), '&quot;')"/>
        <xsl:text>,&#10;  "requester": </xsl:text>
        <xsl:value-of select="concat('&quot;', normalize-space(p:Header/p:Submitter), '&quot;')"/>
        <xsl:text>,&#10;  "amount": </xsl:text>
        <xsl:value-of select="concat('&quot;', format-number($amountSum, '0.00'), '&quot;')"/>
        <xsl:text>,&#10;  "managerId": </xsl:text>
        <xsl:value-of select="concat('&quot;', normalize-space(p:Header/p:Submitter/@ManagerCode), '&quot;')"/>
        <xsl:text>,&#10;  "reason": </xsl:text>
        <xsl:value-of select="concat('&quot;', normalize-space(p:Reason), '&quot;')"/>
        <xsl:text>&#10;}</xsl:text>
    </xsl:template>
</xsl:stylesheet>
