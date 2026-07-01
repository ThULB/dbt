<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl xlink mods"
>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates />
      <xsl:if test="count(mods:accessCondition[@type='restriction on access']) = 0">
        <mods:accessCondition type="restriction on access" xlink:href="http://www.mycore.org/classifications/mir_access#migration"
          xlink:type="simple" />
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
  <!-- standard copy template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>