<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:variable name="newline" select="'&#xA;'" />
    
  <!-- HTML to Plain Text -->
  <xsl:template match="address|article|aside|blockquote|div|dl|fieldset|footer|h1|h2|h3|h4|h5|h6|h7|header|hgroup|hr|ol|p|pre|section|table|ul"
    mode="text"
  >
    <xsl:value-of select="$newline" />
    <xsl:apply-templates mode="text" />
  </xsl:template>

  <xsl:template match="a" mode="text">
    <xsl:choose>
      <xsl:when test="(string-length(@href) &gt;0) and (@href != text())">
        <xsl:apply-templates mode="text" />
        <xsl:value-of select="concat(' [', @href, '] ')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="br" mode="text">
    <xsl:value-of select="$newline" />
  </xsl:template>

  <xsl:template match="li" mode="text">
    <xsl:text>&#9;</xsl:text>
    <xsl:for-each select="ancestor::li">
      <xsl:text>&#9;</xsl:text>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="name(..) = 'ul'">
        <xsl:text>*</xsl:text>
      </xsl:when>
      <xsl:when test="name(..) = 'ol'">
        <xsl:if test="name(../../..) = 'ol'">
          <xsl:value-of select="concat(count(../../preceding-sibling::li) + 1, '.')" />
        </xsl:if>
        <xsl:value-of select="concat(count(preceding-sibling::li) + 1, '.')" />
      </xsl:when>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:apply-templates mode="text" />
  </xsl:template>

  <xsl:template match="dd" mode="text">
    <xsl:text>&#9;</xsl:text>
    <xsl:apply-templates mode="text" />
    <xsl:value-of select="$newline" />
  </xsl:template>

  <xsl:template match="text()" mode="text">
    <xsl:variable name="text" select="normalize-space(.)" />
    <xsl:if test="string-length($text) &gt; 0">
      <xsl:choose>
        <xsl:when
          test="contains('address|article|aside|blockquote|div|dl|fieldset|footer|h1|h2|h3|h4|h5|h6|h7|header|hgroup|hr|ol|p|pre|section|table|ul|', concat(name(..), '|')) and ((count(../node()) = 1) or ((count(preceding-sibling::*) &gt; 0) and (position() = last())))"
        >
          <xsl:value-of select="concat($text, $newline)" />
        </xsl:when>
        <xsl:when test="contains('dt|', concat(name(..), '|')) or (contains('li|', concat(name(..), '|')) and (count(../node()) = 1))">
          <xsl:value-of select="concat($text, $newline)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$text" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <!-- HTML -->
  <xsl:template match="node()" mode="html">
    <xsl:value-of select="concat('&lt;', name())" />
    <xsl:apply-templates select="@*" mode="html" />
    <xsl:choose>
      <xsl:when test="count(child::node()) &gt; 0">
        <xsl:value-of select="'&gt;'" />
        <xsl:apply-templates mode="html" />
        <xsl:value-of select="concat('&lt;/', name(), '&gt;')" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="' /&gt;'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*" mode="html">
    <xsl:value-of select="concat(' ', name(), '=&quot;', ., '&quot;')" />
  </xsl:template>

  <xsl:template match="text()" mode="html">
    <xsl:value-of select="." />
  </xsl:template>
</xsl:stylesheet>