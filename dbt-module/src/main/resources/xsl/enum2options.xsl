<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n" version="1.0"
>

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="i18nPrefix" select="'common'" />

  <!-- camel case SimpleClassName -->
  <xsl:variable name="enumName">
    <xsl:value-of select="translate(substring(/enum/@name, 1, 1),'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:value-of select="substring(/enum/@name, 2)" />
  </xsl:variable>

  <xsl:template match="value">
    <option value="{text()}">
      <xsl:copy-of select="@*" />
      <xsl:value-of select="i18n:translate(concat($i18nPrefix, '.', $enumName, '.', text()))" />
    </option>
  </xsl:template>

</xsl:stylesheet>