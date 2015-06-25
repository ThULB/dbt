<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="MCR.mir-module.MailSender" />
  
  <xsl:param name="action" />
  <xsl:param name="type" />
  <xsl:param name="slotId" />
  <xsl:param name="entryId" />

  <xsl:template match="/">
    <xsl:message>
      type:
      <xsl:value-of select="$type" />
      action:
      <xsl:value-of select="$action" />
      slotId:
      <xsl:value-of select="$slotId" />
      entryId:
      <xsl:value-of select="$entryId" />
    </xsl:message>
    <email>
      <from>
        <xsl:value-of select="$MCR.mir-module.MailSender" />
      </from>
      <xsl:apply-templates select="/*" mode="email" />
    </email>
  </xsl:template>
</xsl:stylesheet>