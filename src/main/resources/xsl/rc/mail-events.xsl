<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />

  <xsl:param name="MCR.mir-module.MailSender" />

  <xsl:param name="WebApplicationBaseURL" />

  <!-- Event vars -->
  <xsl:param name="action" />
  <xsl:param name="type" />
  <xsl:param name="slotId" />
  <xsl:param name="entryId" />
  
  <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle - /XMLPRS=N -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/PPN?PPN=')" />

  <xsl:variable name="newline" select="'&#xA;'" />

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
    <xsl:variable name="slot" select="document(concat('slot:slotId=', $slotId))/slot" />

    <xsl:if test="($slot/@onlineOnly = 'false') and ($action != 'update')">
      <xsl:message>
        Send Mail for:
        <xsl:value-of select="name()" />
      </xsl:message>
      <xsl:for-each select="$slot/lecturers/lecturer">
        <to>
          <xsl:value-of select="concat(@name, ' &lt;', @email, '&gt;')" />
        </to>
      </xsl:for-each>
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
        <xsl:value-of select="$newline" />
        <xsl:value-of select="$newline" />

        <xsl:variable name="ppn" select="pica:record/@ppn" />
        <xsl:variable name="record" select="document(concat('opc:catalogId=', $catalogId, '&amp;record=', $ppn))" />

        <xsl:text>Eintrag  : </xsl:text>
        <xsl:value-of select="concat($WebApplicationBaseURL, 'rc/', $slotId, '?XSL.Mode=edit#', $entryId)" />
        <xsl:value-of select="$newline" />
        <xsl:text>Katalog  : </xsl:text>
        <xsl:value-of select="concat($recordURLPrefix, $ppn)" />
        <xsl:value-of select="$newline" />
        <xsl:text>PPN      : </xsl:text>
        <xsl:value-of select="$ppn" />
        <xsl:value-of select="$newline" />

        <xsl:for-each select="$record//pica:field[@tag = '209A']">
          <xsl:value-of select="$newline" />
          <xsl:text>Standort : </xsl:text>
          <xsl:value-of select="pica:subfield[@code='f']" />
          <xsl:value-of select="$newline" />
          <xsl:text>Signatur : </xsl:text>
          <xsl:value-of select="pica:subfield[@code='a']" />
          <xsl:value-of select="$newline" />
        </xsl:for-each>
      </body>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>