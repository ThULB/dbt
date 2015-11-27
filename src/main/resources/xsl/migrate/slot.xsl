<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:output method="xml" />

  <xsl:variable name="slotId" select="translate(/slot/@ID, ':', '.')" />
  
   <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle - /XMLPRS=N -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/PPN?PPN=')" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />

  <xsl:template match="slot">
    <slot>
      <xsl:apply-templates select="@*" />
      <xsl:call-template name="lecturers" />
      <xsl:apply-templates select="*" />
    </slot>
  </xsl:template>

  <xsl:template match="slot/@ID">
    <xsl:attribute name="id">
      <xsl:value-of select="translate(., ':', '.')" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="slot/@status">
    <xsl:attribute name="status">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="slot/@eOnly">
    <xsl:attribute name="onlineOnly">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="document">
    <title>
      <xsl:value-of select="@title" />
    </title>
  </xsl:template>

  <xsl:template match="contact">
    <contact name="{@name}" email="{@email}" />
  </xsl:template>

  <xsl:template name="lecturers">
    <lecturers>
      <xsl:for-each select="lecturer">
        <lecturer name="{@name}" email="{@email}" />
      </xsl:for-each>
    </lecturers>
  </xsl:template>

  <xsl:template match="entries">
    <entries>
      <xsl:apply-templates select="entry" />
    </entries>
  </xsl:template>

  <xsl:template match="entry">
    <entry id="{@ID}">
      <xsl:apply-templates select="*" />
    </entry>
  </xsl:template>

  <xsl:template match="dateTime">
    <date type="{@type}" format="{@format}">
      <xsl:value-of select="@value" />
    </date>
  </xsl:template>

  <xsl:template match="orgUnit|derivate|lecturer">
    <!-- ignore this nodes -->
  </xsl:template>

  <!-- the entries -->

  <xsl:template match="freeText">
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format = 'plaintext'">
          <xsl:text>plain</xsl:text>
        </xsl:when>
        <xsl:when test="@format = 'preformatted'">
          <xsl:text>preformatted</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>plain</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <text format="{$format}">
      <xsl:apply-templates />
    </text>
  </xsl:template>

  <xsl:template match="html">
    <text format="html">
      <xsl:apply-templates />
    </text>
  </xsl:template>

  <xsl:template match="file">
    <file copyrighted="false" name="{path}">
      <xsl:value-of select="label" />
    </file>
  </xsl:template>

  <xsl:template match="opcbook">
    <opcrecord>
      <xsl:attribute name="epn">
        <xsl:value-of select="data/@epn" />
      </xsl:attribute>
      <xsl:copy-of select="document(concat('opc:record=', data/@ppn, '&amp;catalogId=', $catalogId, '&amp;copys=false'))" />
      <xsl:apply-templates select="comment" />
    </opcrecord>
  </xsl:template>

  <xsl:template match="webLink">
    <webLink url="{url}">
      <xsl:value-of select="label" />
    </webLink>
  </xsl:template>

  <!-- copy template -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>