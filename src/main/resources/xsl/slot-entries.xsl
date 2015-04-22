<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <!-- include custom templates for supported objecttypes -->
  <xsl:include href="xslInclude:objectTypes" />

  <xsl:include href="pica-record-isbd.xsl" />

  <xsl:param name="Mode" select="'view'" />

  <xsl:variable name="slotId" select="/slot/@id" />
  
   <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/XMLPRS=N/PPN?PPN=')" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />

  <xsl:template name="groupEntries">
    <xsl:param name="entries" />
    <xsl:param name="position" select="1" />
    <xsl:param name="hlPos" select="1" />
    <xsl:param name="hlIndex" select="1" />

    <xsl:variable name="numHLs" select="count($entries/headline)" />

    <xsl:if test="$hlIndex = 1 and $hlPos &gt; 1">
      <group>
        <xsl:for-each select="$entries">
          <xsl:if test="position() &lt; $hlPos">
            <xsl:copy-of select="." />
          </xsl:if>
        </xsl:for-each>
      </group>
    </xsl:if>
    <group>
      <xsl:for-each select="$entries">
        <xsl:if test="position() &gt;= $hlPos and $hlIndex &gt;= count(preceding-sibling::*/headline) and not(position() &gt; $hlPos and headline)">
          <xsl:copy-of select="." />
        </xsl:if>
      </xsl:for-each>
    </group>
  </xsl:template>

  <!-- grouped slot entries -->
  <xsl:variable name="groupedEntries">
    <xsl:variable name="entries" select="//entry" />

    <entries>
      <xsl:for-each select="$entries">
        <xsl:if test="headline">
          <xsl:call-template name="groupEntries">
            <xsl:with-param name="entries" select="$entries" />
            <xsl:with-param name="hlPos" select="position()" />
            <xsl:with-param name="hlIndex" select="count(preceding-sibling::*/headline) + 1" />
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>
    </entries>
  </xsl:variable>

  <!-- ==== MAIN LAYOUT =================================================== -->

  <xsl:template match="entries">
    <xsl:apply-templates select="xalan:nodeset($groupedEntries)" mode="toc" />
    <xsl:apply-templates select="xalan:nodeset($groupedEntries)//group" />
  </xsl:template>

  <xsl:template match="entries" mode="toc">
    <div class="slot-toc">
      <h2>
        <xsl:value-of select="i18n:translate('component.rc.slot.toc')" />
      </h2>
      <ul>
        <xsl:for-each select="group/entry/headline">
          <li>
            <a href="{concat('#', ../@id)}">
              <xsl:value-of select="." />
            </a>
          </li>
        </xsl:for-each>
      </ul>
    </div>
  </xsl:template>

  <xsl:template match="group">
    <div class="slot-section">
      <xsl:choose>
        <xsl:when test="$Mode = 'edit'">
          <xsl:apply-templates select="entry" mode="edit" />
          <xsl:call-template name="addNewEntry">
            <xsl:with-param name="lastEntry" select=".//entry[last()]/@id" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="entry" mode="view" />
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:variable name="entryTypes" select="document('slot:entryTypes')/entry-types" />

  <xsl:template name="addNewEntry">
    <xsl:param name="lastEntry" select="''" />

    <div class="new-entry-actions">
      <b>
        <xsl:value-of select="i18n:translate('component.rc.slot.entry.add')" />
      </b>
      <xsl:for-each select="xalan:nodeset($entryTypes)//entry-type">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="concat($WebApplicationBaseURL, 'content/rc/edit-entry-', ./@name, '.xed?slotId=', $slotId, '&amp;afterId=', $lastEntry)" />
            <xsl:if test="@name = 'opcrecord'">
              <xsl:value-of select="concat('&amp;catalogId=', $catalogId)" />
            </xsl:if>
          </xsl:attribute>
          <xsl:value-of select="i18n:translate(i18n/@single)" />
        </a>
        <xsl:if test="position() != last()">
          <xsl:text> | </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="entry" mode="view">
    <xsl:apply-templates select="headline|text|webLink|mcrobject|opcrecord" mode="view" />
  </xsl:template>

  <xsl:template match="entry" mode="edit">
    <xsl:apply-templates select="headline|text|webLink|mcrobject|opcrecord" mode="edit" />
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|opcrecord" mode="view">
    <div class="entry-{name()}">
      <xsl:apply-templates select="." />
    </div>
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|opcrecord" mode="edit">
    <div class="entry-{name()}">
      <xsl:apply-templates select="." mode="editButtons" />
      <xsl:apply-templates select="." />
    </div>
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|opcrecord" mode="editButtons">
    <div class="entry-buttons">
      <div class="btn-group">
        <a href="{$WebApplicationBaseURL}content/rc/edit-entry-{local-name(.)}.xed?slotId={$slotId}&amp;entryId={../@id}">
          <span class="glyphicon glyphicon-pencil" />
        </a>
        <a href="{$WebApplicationBaseURL}content/rc/edit-entry-{local-name(.)}.xed?slotId={$slotId}&amp;entryId={../@id}&amp;action=delete">
          <span class="glyphicon glyphicon-trash" />
        </a>
      </div>
    </div>
  </xsl:template>
  
  <!-- ==== ENTRIES ======================================================= -->
  
  <!-- HeadlineEntry -->
  <xsl:template match="headline">
    <a name="{../@id}" />
    <h2 id="{../@id}">
      <xsl:value-of select="." />
    </h2>
  </xsl:template>
  
  <!-- TextEntry -->
  <xsl:template match="text">
    <xsl:choose>
      <xsl:when test="@format = 'plain'">
        <pre id="{../@id}" class="pre-scrollable">
          <code>
            <xsl:value-of select="." />
          </code>
        </pre>
      </xsl:when>
      <xsl:when test="@format = 'preformatted'">
        <pre id="{../@id}" class="pre-scrollable">
          <xsl:value-of select="." />
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <p id="{../@id}">
          <xsl:value-of select="." disable-output-escaping="yes" />
        </p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- WebLinkEntry -->
  <xsl:template match="webLink">
    <a id="{../@id}" href="{@url}">
      <xsl:choose>
        <xsl:when test="string-length(.) &gt; 0">
          <xsl:value-of select="." />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@url" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
    <br />
  </xsl:template>
  
  <!-- MCRObjectEntry -->
  <xsl:template match="mcrobject">
      <!-- simpler, call mode title -->
    <xsl:apply-templates select="document(concat('mcrobject:', @id))/*" mode="basketContent" />
    <xsl:if test="string-length(.) &gt; 0">
      <span class="comment">
        <xsl:value-of select="." />
      </span>
    </xsl:if>
  </xsl:template>
  
  <!-- OPCRecordEntry -->
  <xsl:template match="opcrecord">
    <xsl:apply-templates select="pica:record" mode="isbd" />
    <xsl:if test="string-length(comment) &gt; 0">
      <span class="comment">
        <xsl:value-of select="comment" />
      </span>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
