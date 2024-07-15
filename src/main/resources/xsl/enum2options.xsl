<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  exclude-result-prefixes=" mcri18n xsl" version="3.0"
>

  <xsl:include href="resource:xsl/copynodes.xsl" />

  <xsl:param name="i18nPrefix" select="'common'" />

  <!-- camel case SimpleClassName -->
  <xsl:variable name="enumName">
    <xsl:value-of select="translate(substring(/enum/@name, 1, 1),'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:value-of select="substring(/enum/@name, 2)" />
  </xsl:variable>

  <xsl:template match="value">
    <option value="{text()}">
      <xsl:copy-of select="@*" />
      <xsl:value-of select="mcri18n:translate(concat($i18nPrefix, '.', $enumName, '.', text()))" />
    </option>
  </xsl:template>

</xsl:stylesheet>