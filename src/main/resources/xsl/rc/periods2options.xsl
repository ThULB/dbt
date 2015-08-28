<?xml version="1.0" encoding="UTF-8"?>
<!-- ======================================================================= -->
<!-- $Revision$ $Date$ -->
<!-- ======================================================================= -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n"
>

  <xsl:include href="coreFunctions.xsl" />

  <xsl:template match="/">
    <options>
      <xsl:apply-templates />
    </options>
  </xsl:template>

  <xsl:template match="period">
    <option value="{@to}">
      <xsl:if test="@setable = 'false'">
        <xsl:attribute name="class">
          <xsl:text>not-setable</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:value-of select="concat(label[lang($CurrentLang)]/@description, ' (', @from, '-', @to, ')')" />
    </option>
  </xsl:template>
</xsl:stylesheet>