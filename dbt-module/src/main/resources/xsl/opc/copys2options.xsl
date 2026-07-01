<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd">

  <xsl:param name="ppn" />

  <xsl:template match="/catalog">
    <copys>
      <xsl:apply-templates select="document(concat('opc:catalogId=', text(), '&amp;record=', $ppn))" />
    </copys>
  </xsl:template>

  <xsl:template match="pica:record">
    <xsl:apply-templates select="pica:field[@tag = '203@']" />
  </xsl:template>

  <xsl:template match="pica:field[@tag = '203@']">
    <xsl:variable name="occurrence" select="@occurrence" />
    <option value="{pica:subfield[@code='0']}">
      <xsl:value-of select="pica:subfield[@code='0']" />
      <xsl:text> - </xsl:text>
      <xsl:apply-templates select="../pica:field[@tag = '209A' and @occurrence = $occurrence]" />
    </option>
  </xsl:template>

  <xsl:template match="pica:field[@tag = '209A']">
    <xsl:value-of select="pica:subfield[@code='a']" />
  </xsl:template>

</xsl:stylesheet>