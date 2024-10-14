<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- $Revision$ $Date$ -->
<!-- ======================================================================= -->
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl xsi xlink pica"
>

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />

  <xsl:template match="/entry">
    <entry>
      <xsl:apply-templates select="@*|node()" />
    </entry>
  </xsl:template>

  <xsl:template match="opcrecord">
    <opcrecord>
      <xsl:apply-templates select="@*|node()" />
      <display>
        <xsl:apply-templates select="pica:record" mode="isbd" />
      </display>
    </opcrecord>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>