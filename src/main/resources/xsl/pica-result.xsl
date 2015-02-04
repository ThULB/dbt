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

  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/CMD?ACT=SRCHA&amp;IKT=1016&amp;SRT=YOP&amp;TRM=ppn+')" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />

  <!-- Pagination -->
  <xsl:param name="Page" select="1" />
  <xsl:param name="numPerPage" select="10" />

  <xsl:variable name="start" select="(number($Page) - 1) * number($numPerPage) + 1" />
  <xsl:variable name="end" select="number($Page) * number($numPerPage)" />

  <xsl:template match="/pica:result">
    <div id="search-result">
      <div class="head clearfix">
        <span class="hits pull-left">
          <xsl:value-of select="i18n:translate('component.opc.result.head.hits', concat($Page, ';', count(./pica:record)))" />
        </span>
        <xsl:call-template name="paginate">
          <xsl:with-param name="extraStyles" select="'pull-right'" />
          <xsl:with-param name="pages" select="ceiling(count(./pica:record) div number($numPerPage))" />
        </xsl:call-template>
      </div>
      <xsl:for-each select="./pica:record">
        <xsl:if test="(position() &gt;= $start) and (position() &lt;= $end)">
          <xsl:variable name="fullRecord" select="document(concat('opc:url=', $opcURL, '&amp;db=', $opcDB, '&amp;record=', @ppn))" />
          <div class="record">
            <xsl:apply-templates select="$fullRecord" mode="isbd" />
          </div>
        </xsl:if>
      </xsl:for-each>
    </div>
  </xsl:template>

</xsl:stylesheet>