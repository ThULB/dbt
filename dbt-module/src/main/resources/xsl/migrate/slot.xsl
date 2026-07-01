<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:output method="xml" />

  <xsl:param name="dirname" />

  <xsl:variable name="slotId" select="translate(/slot/@ID, ':', '.')" />
  
   <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />

  <xsl:template match="slot">
    <xsl:message>
      -------------------------------------------------
      Migration slot...
      -------------------------------------------------
      slotId:
      <xsl:value-of select="$slotId" />
      status
      <xsl:value-of select="@status" />

      dirname:
      <xsl:value-of select="$dirname" />
      catalogId:
      <xsl:value-of select="$catalogId" />
      -------------------------------------------------
    </xsl:message>
    <slot>
      <xsl:apply-templates select="@*" />
      <xsl:if test="not(@status)">
        <xsl:attribute name="status">
          <xsl:value-of select="'archived'" />
        </xsl:attribute>
      </xsl:if>
      <xsl:attribute name="pendingStatus">
        <xsl:text>ownerTransfer</xsl:text>
      </xsl:attribute>
      <xsl:call-template name="lecturers" />
      <xsl:apply-templates select="*" />
      <xsl:if test="count(validTo) = 0">
        <validTo>30.09.2016</validTo>
      </xsl:if>
    </slot>
  </xsl:template>

  <xsl:template match="slot/@ID">
    <xsl:attribute name="id">
      <xsl:value-of select="translate(., ':', '.')" />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="slot/@status">
    <xsl:attribute name="status">
      <xsl:choose>
        <xsl:when test="(string-length(.) = 0) or (. = 'inactive') or (. = 'validating')">
          <xsl:value-of select="'archived'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
        </xsl:otherwise>
      </xsl:choose>
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
    <xsl:variable name="email">
      <xsl:choose>
        <xsl:when test="(string-length(@email) = 0) and (string-length(@ID) &gt; 0)">
          <xsl:call-template name="getEMailFromLE">
            <xsl:with-param name="leId" select="@ID" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@email" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <contact name="{@name}" email="{$email}" />
  </xsl:template>

  <xsl:template name="lecturers">
    <lecturers>
      <xsl:for-each select="lecturer">
        <xsl:variable name="email">
          <xsl:choose>
            <xsl:when test="(string-length(@email) = 0) and (string-length(@ID) &gt; 0)">
              <xsl:call-template name="getEMailFromLE">
                <xsl:with-param name="leId" select="@ID" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@email" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <lecturer name="{@name}" email="{$email}" />
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

  <xsl:template match="milessLink">
    <mcrobject id="dbt_mods_{@documentID}">
      <xsl:value-of select="comment" />
    </mcrobject>
  </xsl:template>
  
  <!-- helper -->

  <xsl:template name="getEMailFromLE">
    <xsl:param name="leId" />

    <xsl:variable name="le" select="document(concat('notnull:file://',$dirname,'/legalentities/legalentity-', $leId,'.xml'))" />

    <xsl:value-of select="$le/legalEntity/contact[@type='office']/email" />
  </xsl:template>

  <!-- copy template -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>