<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <!-- include custom templates for supported objecttypes -->
  <xsl:include href="xslInclude:objectTypes" />

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />
  
   <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle - /XMLPRS=N -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/PPN?PPN=')" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />

  <xsl:template name="groupEntries">
    <xsl:param name="entries" />
    <xsl:param name="position" select="1" />
    <xsl:param name="hlPos" select="1" />
    <xsl:param name="hlIndex" select="1" />

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
      <xsl:choose>
        <xsl:when test="count($entries//headline) = 0">
          <xsl:call-template name="groupEntries">
            <xsl:with-param name="entries" select="$entries" />
            <xsl:with-param name="hlPos" select="position()" />
            <xsl:with-param name="hlIndex" select="count(preceding-sibling::*/headline) + 1" />
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$entries">
            <xsl:if test="headline">
              <xsl:call-template name="groupEntries">
                <xsl:with-param name="entries" select="$entries" />
                <xsl:with-param name="hlPos" select="position()" />
                <xsl:with-param name="hlIndex" select="count(preceding-sibling::*/headline) + 1" />
              </xsl:call-template>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </entries>
  </xsl:variable>

  <!-- ==== MAIN LAYOUT =================================================== -->

  <xsl:template match="entries">
    <xsl:apply-templates select="xalan:nodeset($groupedEntries)" mode="toc" />
    <xsl:apply-templates select="xalan:nodeset($groupedEntries)//group" />
  </xsl:template>

  <xsl:template match="entries" mode="toc">
    <xsl:if test="count(group/entry/headline) &gt; 0">
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
    </xsl:if>
  </xsl:template>

  <xsl:template match="group">
    <div class="slot-section">
      <xsl:choose>
        <xsl:when test="$effectiveMode = 'edit'">
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
            <xsl:choose>
              <xsl:when test="@name = 'file'">
                <xsl:value-of select="concat($WebApplicationBaseURL, 'content/rc/entry-file.xml')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($WebApplicationBaseURL, 'content/rc/entry.xed')" />
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="concat('?entry=', ./@name,'&amp;slotId=', $slotId, '&amp;afterId=', $lastEntry)" />
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
    <xsl:apply-templates select="headline|text|webLink|mcrobject|file|opcrecord" mode="view" />
  </xsl:template>

  <xsl:template match="entry" mode="edit">
    <xsl:apply-templates select="headline|text|webLink|mcrobject|file|opcrecord" mode="edit" />
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="view">
    <div class="entry-{name()}">
      <xsl:apply-templates select="." />
    </div>
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="edit">
    <div class="entry-{name()}" id="{../@id}">
      <xsl:apply-templates select="." mode="extraAttributes" />
      <xsl:apply-templates select="." mode="editButtons" />
      <xsl:apply-templates select="." />
      <xsl:apply-templates select="." mode="infoLine" />
    </div>
  </xsl:template>

  <xsl:template match="*" mode="extraAttributes">
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="editButtons">
    <div class="entry-buttons">
      <div class="btn-group">
        <a href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}" title="{i18n:translate('component.rc.slot.entry.edit')}">
          <span class="glyphicon glyphicon-pencil" />
        </a>
        <a href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}&amp;action=delete" title="{i18n:translate('component.rc.slot.entry.delete')}">
          <span class="glyphicon glyphicon-trash text-danger" />
        </a>
        <a href="#" title="{i18n:translate('component.rc.slot.entry.move')}">
          <span class=" entry-mover glyphicon glyphicon-screenshot " />
        </a>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="infoLine">
    <xsl:if test="$hasAdminPermission">
      <div class="entry-infoline">
        <xsl:value-of select="i18n:translate('component.rc.slot.entry.infoLine', concat(../@id, ';', ../date[@type='created'], ';', ../date[@type='modified']))"
          disable-output-escaping="yes" />
      </div>
    </xsl:if>
  </xsl:template>
  
  <!-- ==== ENTRIES ======================================================= -->
  
  <!-- HeadlineEntry -->
  <xsl:template match="headline">
    <a id="{../@id}" />
    <h2>
      <xsl:value-of select="." />
    </h2>
  </xsl:template>
  
  <!-- TextEntry -->
  <xsl:template match="text">
    <xsl:choose>
      <xsl:when test="@format = 'plain'">
        <div>
          <xsl:value-of select="." />
        </div>
      </xsl:when>
      <xsl:when test="@format = 'preformatted'">
        <pre class="pre-scrollable">
          <code>
            <xsl:value-of select="." />
          </code>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <div>
          <xsl:value-of select="." disable-output-escaping="yes" />
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text" mode="extraAttributes">
    <xsl:attribute name="format">
      <xsl:value-of select="@format" />
    </xsl:attribute>
  </xsl:template>
  
  <!-- WebLinkEntry -->
  <xsl:template match="webLink">
    <a href="{@url}" target="_blank">
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
  
  <!-- File -->
  <xsl:template match="file">
    <h4>
      <a href="{$WebApplicationBaseURL}rcentry/{$slotId}/{../@id}/{@name}">
        <xsl:choose>
          <xsl:when test="string-length(.) &gt; 0">
            <xsl:value-of select="." />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name" />
          </xsl:otherwise>
        </xsl:choose>
      </a>
      <xsl:if test="not($hasAdminPermission)">
        <small>
          <xsl:text> - </xsl:text>
          <xsl:call-template name="formatFileSize">
            <xsl:with-param name="size" select="@size" />
          </xsl:call-template>
        </small>
      </xsl:if>
    </h4>
    <xsl:if test="$hasAdminPermission">
      <p>
        <xsl:text>SHA-1: </xsl:text>
        <code>
          <xsl:value-of select="@hash" />
        </code>
        <xsl:text> - </xsl:text>
        <xsl:call-template name="formatFileSize">
          <xsl:with-param name="size" select="@size" />
        </xsl:call-template>
      </p>
    </xsl:if>
  </xsl:template>
  
  <!-- OPCRecordEntry -->

  <xsl:template match="opcrecord" mode="editButtons">
    <div class="entry-buttons">
      <div class="btn-group">
        <a href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}" title="{i18n:translate('component.rc.slot.entry.edit')}">
          <span class="glyphicon glyphicon-pencil" />
        </a>
        <xsl:if test="$hasAdminPermission or (string-length(@deleted) = 0) or (@deleted != 'true')">
          <a href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}&amp;action=delete" title="{i18n:translate('component.rc.slot.entry.delete')}">
            <span class="glyphicon glyphicon-trash text-danger" />
          </a>
        </xsl:if>
        <a href="#" title="{i18n:translate('component.rc.slot.entry.move')}">
          <span class=" entry-mover glyphicon glyphicon-screenshot " />
        </a>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="opcrecord">
    <xsl:if test="$writePermission or (($onlineOnly = 'false') and (string-length(@epn) &gt; 0)) or ($onlineOnly = 'true')">
      <xsl:apply-templates select="pica:record" mode="isbd" />
      <xsl:if test="string-length(comment) &gt; 0">
        <span class="comment">
          <xsl:value-of select="comment" />
        </span>
      </xsl:if>
      <xsl:if test="$writePermission and ($onlineOnly = 'false') and (string-length(@epn) = 0)">
        <span class="label label-warning">
          <xsl:value-of select="i18n:translate('component.rc.slot.entry.opcrecord.release_required')" />
        </span>
      </xsl:if>
      <xsl:if test="$writePermission and ($onlineOnly = 'false') and (@deleted = 'true')">
        <span class="label label-danger">
          <xsl:value-of select="i18n:translate('component.rc.slot.entry.opcrecord.deletion_mark')" />
        </span>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
