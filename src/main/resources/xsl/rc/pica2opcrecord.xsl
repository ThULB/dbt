<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- $Revision$ $Date$ -->
<!-- ======================================================================= -->
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl xsi xlink pica"
>

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />

  <xsl:template match="/">
    <entry>
      <opcrecord>
        <display>
          <xsl:apply-templates select="pica:record" mode="isbd" />
        </display>
        <xsl:copy-of select="pica:record" />
      </opcrecord>
    </entry>
  </xsl:template>
</xsl:stylesheet>