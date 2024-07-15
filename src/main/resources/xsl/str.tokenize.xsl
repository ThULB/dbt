<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions" extension-element-prefixes="fn">

  <xsl:template name="fn:tokenize">
    <xsl:param name="string" select="''" />
    <xsl:param name="delimiters" select="' &#x9;&#xA;'" />
    <xsl:choose>
      <xsl:when test="not($string)" />
      <xsl:when test="not($delimiters)">
        <xsl:call-template name="fn:_tokenize-characters">
          <xsl:with-param name="string" select="$string" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="fn:_tokenize-delimiters">
          <xsl:with-param name="string" select="$string" />
          <xsl:with-param name="delimiters" select="$delimiters" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="fn:_tokenize-characters">
    <xsl:param name="string" />
    <xsl:if test="$string">
      <token>
        <xsl:value-of select="substring($string, 1, 1)" />
      </token>
      <xsl:call-template name="fn:_tokenize-characters">
        <xsl:with-param name="string" select="substring($string, 2)" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="fn:_tokenize-delimiters">
    <xsl:param name="string" />
    <xsl:param name="delimiters" />
    <xsl:variable name="delimiter" select="substring($delimiters, 1, 1)" />
    <xsl:choose>
      <xsl:when test="not($delimiter)">
        <token>
          <xsl:value-of select="$string" />
        </token>
      </xsl:when>
      <xsl:when test="contains($string, $delimiter)">
        <xsl:if test="not(starts-with($string, $delimiter))">
          <xsl:call-template name="fn:_tokenize-delimiters">
            <xsl:with-param name="string" select="substring-before($string, $delimiter)" />
            <xsl:with-param name="delimiters" select="substring($delimiters, 2)" />
          </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="fn:_tokenize-delimiters">
          <xsl:with-param name="string" select="substring-after($string, $delimiter)" />
          <xsl:with-param name="delimiters" select="$delimiters" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="fn:_tokenize-delimiters">
          <xsl:with-param name="string" select="$string" />
          <xsl:with-param name="delimiters" select="substring($delimiters, 2)" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>