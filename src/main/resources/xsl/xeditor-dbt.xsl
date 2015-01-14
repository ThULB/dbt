<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xed="http://www.mycore.de/xeditor" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xsl xed xalan i18n">

  <!-- ========== Repeater buttons: <xed:repeat><xed:controls> ========== -->

  <xsl:template match="text()" mode="xed.control" priority="10">
        <!-- append insert remove up down -->
    <xsl:param name="name" /> <!-- name to submit as request parameter when button/image is clicked -->

        <!-- Choose a label for the button -->
    <xsl:variable name="symbol">
      <xsl:choose>
        <xsl:when test=".='add'">
          <xsl:value-of select="'asterisk'" />
        </xsl:when>
        <xsl:when test=".='append'">
          <xsl:value-of select="'chevron-down'" />
        </xsl:when>
        <xsl:when test=".='insert'">
          <xsl:value-of select="'plus'" />
        </xsl:when>
        <xsl:when test=".='remove'">
          <xsl:value-of select="'minus'" />
        </xsl:when>
        <xsl:when test=".='up'">
          <xsl:value-of select="'arrow-up'" />
        </xsl:when>
        <xsl:when test=".='down'">
          <xsl:value-of select="'arrow-down'" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <button type="submit" class="btn btn-default" name="{$name}">
      <span class="glyphicon glyphicon-{$symbol}"></span>
    </button>
  </xsl:template>
  
  <!-- ========== Validation error messages: <xed:validate /> ========== -->

  <xsl:template match="xed:validate[@i18n]" mode="message">
    <li>
      <xsl:copy-of select="i18n:translate(@i18n)" />
    </li>
  </xsl:template>

  <xsl:template match="xed:validate" mode="message">
    <!-- <span class="help-inline"> -->
    <li>
      <xsl:apply-templates select="node()" mode="xeditor" />
    </li>
    <!-- </span> -->
  </xsl:template>

</xsl:stylesheet>
