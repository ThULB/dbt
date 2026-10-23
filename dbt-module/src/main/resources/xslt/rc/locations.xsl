<?xml version="1.0" encoding="UTF-8"?>

<!-- XSL to display data of slot locations -->

<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  exclude-result-prefixes="#all"
>

  <xsl:include href="resource:xslt/MyCoReLayout.xsl" />
  <xsl:include href="resource:xslt/classificationBrowser.xsl" />

  <xsl:variable name="PageID" select="'select-rclocation'" />

  <xsl:variable name="PageTitle" select="mcri18n:translate('component.rc.locationSelectDisplay')" />
  
  <xsl:template match="locations[@classID]">
    <xsl:call-template name="mcrClassificationBrowser">
      <xsl:with-param name="classification" select="@classID" />
      <xsl:with-param name="category" select="@categID" />
      <xsl:with-param name="style" select="'locationSubselect'" />
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="locations[group]">
    <ul>
      <xsl:apply-templates select="location" />
    </ul>
  </xsl:template>
</xsl:stylesheet>
