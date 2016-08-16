<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="xsl xsi xlink mods i18n pica"
>

  <xsl:include href="MyCoReLayout.xsl" />
  <xsl:include href="pagination.xsl" />
  <xsl:include href="pica-record-isbd.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.opc.result.pageTitle')" />

  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />

  <!-- OPC vars -->
  <xsl:variable name="catalogId" select="/pica:result/@catalogId" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/XMLPRS=N/PPN?PPN=')" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />
  
  <!-- RC slot params -->
  <xsl:param name="slotId" />
  <xsl:param name="afterId" />

  <!-- Pagination -->
  <xsl:param name="Page" select="1" />
  <xsl:param name="numPerPage" select="10" />

  <xsl:variable name="start" select="(number($Page) - 1) * number($numPerPage) + 1" />
  <xsl:variable name="end" select="number($Page) * number($numPerPage)" />

  <xsl:template match="/pica:result">
    <xsl:variable name="needsPagination">
      <xsl:choose>
        <xsl:when test="count(./pica:record) &gt; number($numPerPage)">
          <xsl:value-of select="'true'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'false'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="hCols">
      <xsl:choose>
        <xsl:when test="$needsPagination = 'true'">
          <xsl:value-of select="6" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="12" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <hgroup>
      <div class="col-xs-12 col-sm-{$hCols} col-md-{$hCols}">
        <h1>
          <xsl:value-of select="i18n:translate('component.opc.result.pageTitle')" />
        </h1>
        <h2>
          <xsl:value-of select="i18n:translate('component.opc.result.head.hits', concat($Page, ';', count(./pica:record)))" />
        </h2>
      </div>
      <xsl:if test="$needsPagination = 'true'">
        <div class="col-xs-12 col-sm-6 col-md-6">
          <xsl:call-template name="paginate">
            <xsl:with-param name="extraStyles" select="'pull-right'" />
            <xsl:with-param name="pages" select="ceiling(count(./pica:record) div number($numPerPage))" />
          </xsl:call-template>
        </div>
      </xsl:if>
    </hgroup>
    <section class="col-xs-12 col-sm-12 col-md-12">
      <xsl:variable name="colwidth">
        <xsl:choose>
          <xsl:when test="string-length($slotId) &gt; 0">
            <xsl:value-of select="10" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="12" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:for-each select="./pica:record">
        <xsl:if test="(position() &gt;= $start) and (position() &lt;= $end)">
          <xsl:variable name="fullRecord" select="document(concat('opc:catalogId=', $catalogId, '&amp;record=', @ppn))" />
          <article class="search-result row">
            <div class="col-xs-{$colwidth} col-sm-{$colwidth} col-md-{$colwidth}">
              <xsl:apply-templates select="$fullRecord" mode="isbd" />
            </div>
            <xsl:variable name="matCode" select="$fullRecord//pica:field[@tag='002@']/pica:subfield[@code='0']" />
            <xsl:if test="(string-length($slotId) &gt; 0) and not(starts-with($matCode, 'As') or starts-with($matCode, 'Ab'))">
              <div class="col-xs-{12 - $colwidth} col-sm-{12 - $colwidth} col-md-{12 - $colwidth} text-center">
                <a class="btn btn-default">
                  <xsl:attribute name="href">
                    <xsl:value-of
                    select="concat($WebApplicationBaseURL, 'content/rc/entry.xed?entry=opcrecord&amp;slotId=', $slotId, '&amp;afterId=', $afterId, '&amp;catalogId=', $catalogId, '&amp;ppn=', @ppn)" />
                  </xsl:attribute>
                  <xsl:value-of select="i18n:translate('button.add')" />
                </a>
              </div>
            </xsl:if>
          </article>
        </xsl:if>
      </xsl:for-each>
    </section>
  </xsl:template>

  <xsl:template match="pica:record" mode="cover">
    <xsl:for-each select="pica:field[@tag='009P' and pica:subfield[@code='q' and contains(text(), 'cover')]][1]">
      <img class="cover" src="{pica:subfield[@code='a']}" />
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>