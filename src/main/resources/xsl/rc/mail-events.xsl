<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />

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

  <xsl:template match="entry" mode="email">
    <xsl:apply-templates select="opcrecord" mode="email" />
  </xsl:template>
  
  <!-- Entry Templates -->
  <xsl:template match="opcrecord" mode="email">
    <xsl:message>
      Send Mail for:
      <xsl:value-of select="name()" />
    </xsl:message>
    <to>rene.adler@tu-ilmenau.de</to>
    <subject>
      <xsl:choose>
        <xsl:when test="$action = 'create'">
          <xsl:text>Neuer Katalog-Eintrag</xsl:text>
        </xsl:when>
        <xsl:when test="$action = 'update'">
          <xsl:text>Katalog-Eintrag wurde geändert</xsl:text>
        </xsl:when>
        <xsl:when test="$action = 'delete'">
          <xsl:text>Katalog-Eintrag wurde gelöscht</xsl:text>
        </xsl:when>
      </xsl:choose>
    </subject>
    <body>
      <xsl:apply-templates select="pica:record" mode="isbdText" />
    </body>
  </xsl:template>
</xsl:stylesheet>