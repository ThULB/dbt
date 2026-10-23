<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- $Revision$ $Date$ -->
<!-- ======================================================================= -->
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all"
>

  <xsl:template match="/iktlist">
    <options>
      <xsl:apply-templates />
    </options>
  </xsl:template>

  <xsl:template match="ikt">
    <option value="{@key}">
      <xsl:value-of select="text()" />
    </option>
  </xsl:template>
</xsl:stylesheet>