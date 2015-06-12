<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- $Revision$ $Date$ -->
<!-- ======================================================================= -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n"
>

  <xsl:include href="coreFunctions.xsl" />

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