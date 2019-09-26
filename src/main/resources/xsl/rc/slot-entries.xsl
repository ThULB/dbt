<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
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
    <div class="d-xl-flex flex-xl-row-reverse">
      <xsl:apply-templates select="xalan:nodeset($groupedEntries)" mode="toc" />
      <div class="flex-xl-column flex-grow-1 mw-xl-75 minw-0">
        <xsl:apply-templates select="xalan:nodeset($groupedEntries)//group" />
      </div>
    </div>
  </xsl:template>

  <xsl:template match="entries" mode="toc">
    <xsl:if test="count(group/entry/headline) &gt; 0">
      <div class="flex-xl-column mb-2 ml-xl-2 minw-25">
        <div class="slot-toc card">
          <h5 class="card-header">
            <xsl:value-of select="i18n:translate('component.rc.slot.toc')" />
          </h5>
          <div class="list-group list-group-flush" id="slot-toc">
            <xsl:for-each select="group/entry/headline">
              <a class="list-group-item list-group-item-action" href="{concat('#', ../@id)}">
                <xsl:value-of select="." />
              </a>
            </xsl:for-each>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="group">
    <div class="slot-section card mb-2">
      <xsl:choose>
        <xsl:when test="$effectiveMode = 'edit'">
          <xsl:apply-templates select="entry/headline" mode="edit" />
          <div class="card-body p-0">
            <xsl:apply-templates select="entry" mode="edit" />
          </div>
          <div class="card-footer">
            <xsl:call-template name="addNewEntry">
              <xsl:with-param name="lastEntry" select=".//entry[last()]/@id" />
            </xsl:call-template>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="entry/headline" mode="view" />
          <div class="card-body p-0">
            <xsl:apply-templates select="entry" mode="view" />
          </div>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:variable name="entryTypes" select="document('slot:entryTypes')/entry-types" />

  <xsl:template name="addNewEntry">
    <xsl:param name="lastEntry" select="''" />

    <div class="new-entry-actions d-flex flex-column flex-md-row justify-content-between">
      <b>
        <xsl:value-of select="i18n:translate('component.rc.slot.entry.add')" />
      </b>
      <div>
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
    </div>
  </xsl:template>

  <xsl:template match="entry" mode="view">
    <div class="media">
      <xsl:apply-templates select="text|webLink|mcrobject|file|opcrecord" mode="view" />
    </div>
  </xsl:template>

  <xsl:template match="entry" mode="edit">
    <div class="media">
      <xsl:apply-templates select="text|webLink|mcrobject|file|opcrecord" mode="edit" />
    </div>
  </xsl:template>

  <xsl:template match="headline" mode="view">
    <div id="{../@id}">
      <xsl:attribute name="class">
        <xsl:value-of select="concat('entry-', name(), ' card-header ')" />
        <xsl:apply-templates select="." mode="extraClasses" />
      </xsl:attribute>
      <xsl:apply-templates select="." />
    </div>
  </xsl:template>

  <xsl:template match="text|webLink|mcrobject|file|opcrecord" mode="view">
    <div>
      <xsl:attribute name="class">
        <xsl:value-of select="concat('entry-', name(), ' media-body mw-100 p-2 ')" />
        <xsl:apply-templates select="." mode="extraClasses" />
      </xsl:attribute>
      <xsl:apply-templates select="." />
    </div>
  </xsl:template>

  <xsl:template match="headline" mode="edit">
    <div id="{../@id}">
      <xsl:attribute name="class">
        <xsl:value-of select="concat('entry-', name(), ' card-header pr-2 ')" />
        <xsl:apply-templates select="." mode="extraClasses" />
      </xsl:attribute>
      <xsl:apply-templates select="." mode="extraAttributes" />
      <div class="d-flex justify-content-between">
        <xsl:apply-templates select="." />
        <xsl:apply-templates select="." mode="editButtons" />
      </div>
      <xsl:apply-templates select="." mode="infoLine" />
    </div>
  </xsl:template>

  <xsl:template match="text|webLink|mcrobject|file|opcrecord" mode="edit">
    <div id="{../@id}">
      <xsl:attribute name="class">
        <xsl:value-of select="concat('entry-', name(), ' media-body mw-100 p-2 ')" />
        <xsl:apply-templates select="." mode="extraClasses" />
      </xsl:attribute>
      <xsl:apply-templates select="." mode="extraAttributes" />
      <div class="d-flex justify-content-between">
        <xsl:apply-templates select="." />
        <xsl:apply-templates select="." mode="editButtons" />
      </div>
      <xsl:apply-templates select="." mode="infoLine" />
    </div>
  </xsl:template>

  <xsl:template match="*" mode="extraClasses">
  </xsl:template>

  <xsl:template match="*" mode="extraAttributes">
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="editButtons">
    <div class="ml-2 entry-buttons">
      <div class="btn-group" role="group">
        <a class="btn btn-primary" href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}"
          title="{i18n:translate('component.rc.slot.entry.edit')}"
        >
          <i class="fas fa-pencil-alt"></i>
        </a>
        <a class="btn btn-danger"
          href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}&amp;action=delete"
          title="{i18n:translate('component.rc.slot.entry.delete')}"
        >
          <i class="far fa-trash-alt"></i>
        </a>
        <button class="btn btn-info entry-mover" title="{i18n:translate('component.rc.slot.entry.move')}">
          <i class="fas fa-arrows-alt"></i>
        </button>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="headline|text|webLink|mcrobject|file|opcrecord" mode="infoLine">
    <xsl:if test="$hasAdminPermission">
      <small class="entry-infoline text-muted">
        <xsl:value-of
          select="i18n:translate('component.rc.slot.entry.infoLine', concat(../@id, ';', ../date[@type='created'], ';', ../date[@type='modified']))"
          disable-output-escaping="yes" />
      </small>
    </xsl:if>
  </xsl:template>
  
  <!-- ==== ENTRIES ======================================================= -->
  
  <!-- HeadlineEntry -->
  <xsl:template match="headline">
    <h5 class="my-0">
      <xsl:value-of select="." />
    </h5>
  </xsl:template>
  
  <!-- TextEntry -->
  <xsl:template match="text">
    <xsl:choose>
      <xsl:when test="@format = 'plain'">
        <p class="mb-0 text-justify">
          <xsl:value-of select="." />
        </p>
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
    <div class="d-flex flex-row flex-fill">
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
    </div>
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
    <div class="d-flex flex-row flex-fill justify-content-between">
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
      <span class="text-nowrap text-muted">
        <xsl:call-template name="formatFileSize">
          <xsl:with-param name="size" select="@size" />
        </xsl:call-template>
      </span>
    </div>
  </xsl:template>
  
  <!-- OPCRecordEntry -->

  <xsl:template match="opcrecord" mode="editButtons">
    <div class="ml-2 entry-buttons">
      <div class="btn-group">
        <a class="btn btn-primary" href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}"
          title="{i18n:translate('component.rc.slot.entry.edit')}"
        >
          <i class="fas fa-pencil-alt"></i>
        </a>
        <xsl:if test="$hasAdminPermission or (string-length(@deleted) = 0) or (@deleted != 'true')">
          <a class="btn btn-danger"
            href="{$WebApplicationBaseURL}content/rc/entry.xed?entry={local-name(.)}&amp;slotId={$slotId}&amp;entryId={../@id}&amp;action=delete"
            title="{i18n:translate('component.rc.slot.entry.delete')}"
          >
            <i class="far fa-trash-alt"></i>
          </a>
        </xsl:if>
        <button class="btn btn-info entry-mover" title="{i18n:translate('component.rc.slot.entry.move')}">
          <i class="fas fa-arrows-alt"></i>
        </button>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="opcrecord" mode="extraClasses">
    <xsl:if test="$writePermission or (($onlineOnly = 'false') and (string-length(@epn) &gt; 0)) or ($onlineOnly = 'true')">
      <xsl:if test="$writePermission and ($onlineOnly = 'false') and (string-length(@epn) = 0)">
        <xsl:text>border-left border-warning</xsl:text>
      </xsl:if>
      <xsl:if test="$writePermission and ($onlineOnly = 'false') and (@deleted = 'true')">
        <xsl:text>border-left border-danger</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="opcrecord">
    <xsl:if test="$writePermission or (($onlineOnly = 'false') and (string-length(@epn) &gt; 0)) or ($onlineOnly = 'true')">
      <div>
        <xsl:apply-templates select="pica:record" mode="isbd" />
        <xsl:if test="string-length(comment) &gt; 0">
          <span class="comment">
            <xsl:value-of select="comment" />
          </span>
        </xsl:if>
      </div>
<!--       <xsl:if test="$writePermission and ($onlineOnly = 'false') and (string-length(@epn) = 0)"> -->
<!--         <span class="label label-warning"> -->
<!--           <xsl:value-of select="i18n:translate('component.rc.slot.entry.opcrecord.release_required')" /> -->
<!--         </span> -->
<!--       </xsl:if> -->
<!--       <xsl:if test="$writePermission and ($onlineOnly = 'false') and (@deleted = 'true')"> -->
<!--         <span class="label label-danger"> -->
<!--           <xsl:value-of select="i18n:translate('component.rc.slot.entry.opcrecord.deletion_mark')" /> -->
<!--         </span> -->
<!--       </xsl:if> -->
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
