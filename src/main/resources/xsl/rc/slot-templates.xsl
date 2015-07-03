<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="i18n xlink"
>

  <xsl:param name="CurrentLang" />
  
  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:variable name="date">
    <xsl:choose>
      <xsl:when test="string-length(/slot/validTo) &gt; 0">
        <xsl:value-of select="/slot/validTo" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>now</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="period" select="document(concat('period:areacode=0&amp;date=', $date, '&amp;fq=true'))" />

  <xsl:template match="@id" mode="rcLocation">
    <xsl:variable name="rcLocId">
      <xsl:value-of select="substring(., 1, string-length(.) - 5)" />
    </xsl:variable>
    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />

    <xsl:for-each select="$currentLocation/ancestor-or-self::category">
      <xsl:choose>
        <xsl:when test="string-length(url/@xlink:href) &gt; 0">
          <a href="{url/@xlink:href}" target="_blank">
            <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="position() != last()">
        <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;raquo;&amp;nbsp;</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="@id" mode="rcLocationText">
    <xsl:variable name="rcLocId">
      <xsl:value-of select="substring(., 1, string-length(.) - 5)" />
    </xsl:variable>
    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />

    <xsl:for-each select="$currentLocation/ancestor-or-self::category">
      <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
      <xsl:if test="position() != last()">
        <xsl:text> / </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>