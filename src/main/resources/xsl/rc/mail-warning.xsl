<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:include href="slot-templates.xsl" />

  <xsl:param name="MCR.RC.MailSender" />

  <xsl:param name="WebApplicationBaseURL" />

  <!-- vars -->
  <xsl:param name="warningDate" />

  <xsl:variable name="newline" select="'&#xA;'" />

  <xsl:template match="/">
    <xsl:message>
      warningDate:
      <xsl:value-of select="$warningDate" />
    </xsl:message>
    <email>
      <from>
        <xsl:value-of select="$MCR.RC.MailSender" />
      </from>
      <xsl:apply-templates select="/*" mode="email" />
    </email>
  </xsl:template>

  <xsl:template match="slot" mode="email">
    <xsl:message>
      Warning mail for slotId:
      <xsl:value-of select="@id" />
    </xsl:message>

    <xsl:for-each select="lecturers/lecturer">
      <to>
        <xsl:value-of select="concat(@name, ' &lt;', @email, '&gt;')" />
      </to>
    </xsl:for-each>
    <subject>
      <xsl:value-of select="concat('ESA ', @id, ': Ablauf Ihres Semesterapparats')" />
    </subject>
    <body>
      <xsl:text>Sehr geehrte/r Benutzer/in,</xsl:text>
      <xsl:value-of select="$newline" />
      <xsl:value-of select="$newline" />
      <xsl:text>folgender Semesterapparat wird ablaufen:</xsl:text>
      <xsl:value-of select="$newline" />
      <xsl:value-of select="$newline" />

      <xsl:text>Standort  : </xsl:text>
      <xsl:apply-templates select="@id" mode="rcLocationText" />
      <xsl:value-of select="$newline" />
      <xsl:text>Semester  : </xsl:text>
      <xsl:value-of select="$period//label[lang($CurrentLang)]/@description" />
      <xsl:value-of select="$newline" />
      <xsl:text>Gültig bis: </xsl:text>
      <xsl:value-of select="validTo" />
      <xsl:value-of select="$newline" />
      <xsl:value-of select="$newline" />
      <!-- TODO mail text -->
      <xsl:text>Benutzen Sie bitte nachfolgenden Link um uns mitzuteilen was mit Ihrem Semesterapparat geschehen soll.</xsl:text>
      <xsl:value-of select="$newline" />
      <xsl:value-of select="concat($WebApplicationBaseURL, 'content/rc/slot.xed?action=status&amp;slotId=', @id)" />
      <xsl:value-of select="$newline" />
    </body>
  </xsl:template>
</xsl:stylesheet>