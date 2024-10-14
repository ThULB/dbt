<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:include href="coreFunctions.xsl" />
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

    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="string-length(validTo) &gt; 0">
          <xsl:value-of select="validTo" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>now</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="period" select="document(concat('period:areacode=0&amp;date=', $date, '&amp;fq=true'))" />

    <xsl:for-each select="lecturers/lecturer">
      <to>
        <xsl:value-of select="concat(@name, ' &lt;', @email, '&gt;')" />
      </to>
    </xsl:for-each>
    <subject>
      <xsl:value-of select="concat('ESA ', @id, ': Ablauf Ihres Semesterapparats')" />
    </subject>
    <body>
      <strong>Sehr geehrte/r Benutzer/in,</strong>
      <br />
      <p>folgender Semesterapparat wird ablaufen:</p>

      <dl>
        <dt>Titel</dt>
        <dd>
          <xsl:value-of select="title" />
        </dd>
        <dt>Standort</dt>
        <dd>
          <xsl:apply-templates select="@id" mode="rcLocationText" />
        </dd>
        <dt>Semester</dt>
        <dd>
          <xsl:value-of select="$period//label[lang($CurrentLang)]/@description" />
        </dd>
        <dt>Gültig bis</dt>
        <dd>
          <xsl:value-of select="validTo" />
        </dd>
      </dl>

      <p>
        Benutzen Sie nachfolgenden Link, um uns mitzuteilen, was mit Ihrem Semesterapparat geschehen soll.
        <br />
        <xsl:variable name="sLink" select="concat($WebApplicationBaseURL, 'content/rc/slot.xed?action=status&amp;slotId=', @id)" />
        <a href="{$sLink}">
          <xsl:value-of select="$sLink" />
        </a>
      </p>
    </body>
  </xsl:template>
</xsl:stylesheet>