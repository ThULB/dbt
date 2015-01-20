<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl pica"
>

  <xsl:output method="xml" indent="yes" />

  <xsl:include href="copynodes.xsl" />
  <xsl:include href="pica2mods.xsl" />

  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />

  <xsl:variable name="catalogId" select="/pica:result/@catalogId" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <xsl:param name="RecordIdSource" select="$catalogues/catalog[@identifier=$catalogId]/ISIL[1]/text()" />

  <xsl:param name="Page" select="1" />
  <xsl:param name="numPerPage" select="10" />

  <xsl:variable name="start" select="(number($Page) - 1) * number($numPerPage)" />
  <xsl:variable name="end" select="number($Page) * number($numPerPage)" />

  <xsl:template match="pica:result">
    <result catalogId="{@catalogId}">
      <xsl:for-each select="./pica:record">
        <xsl:if test="(position() &gt;= $start) and (position() &lt;= $end)">
          <xsl:variable name="fullRecord" select="document(concat('opc:url=', $opcURL, '&amp;db=', $opcDB, '&amp;record=', @ppn))" />
          <xsl:apply-templates select="$fullRecord" />
          <xsl:copy-of select="." />
<!--           <pica-xml> -->
<!--             <xsl:copy-of select="$fullRecord" /> -->
<!--           </pica-xml> -->
        </xsl:if>
      </xsl:for-each>
    </result>
  </xsl:template>

</xsl:stylesheet>