<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl xsi xlink pica"
>

  <xsl:variable name="recordURLPrefix" select="''" />

  <xsl:template match="pica:record" mode="isbd">
    <h4>
      <xsl:choose>
        <xsl:when test="string-length($recordURLPrefix) &gt; 0">
          <a href="{$recordURLPrefix}{@ppn}" target="_blank">
            <xsl:apply-templates select="." mode="isbdTitle" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="isbdTitle" />
        </xsl:otherwise>
      </xsl:choose>
    </h4>
    <p>
      <xsl:apply-templates select="." mode="isbdInfo" />
    </p>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdText">
    <xsl:apply-templates select="." mode="isbdTitle" />
    <xsl:text disable-output-escaping="yes">&#13;</xsl:text>
    <xsl:apply-templates select="." mode="isbdInfo" />
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdTitle">
    <xsl:if test="pica:field[@tag = '036C']">
      <xsl:call-template name="cleanString">
        <xsl:with-param name="str" select="pica:field[@tag = '036C']/pica:subfield[@code='a']" />
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="not(pica:field[@tag = '021A'])">
          <xsl:text> ; </xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text> / </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="cleanString">
        <xsl:with-param name="str" select="pica:field[@tag = '036C']/pica:subfield[@code='l']" />
      </xsl:call-template>
      <xsl:if test="pica:field[@tag = '021A']">
        <xsl:text> / </xsl:text>
      </xsl:if>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="pica:field[@tag = '021A'] and string-length(pica:field[@tag = '021A']/pica:subfield[@code='8']) = 0">
        <xsl:call-template name="cleanString">
          <xsl:with-param name="str" select="pica:field[@tag = '021A']/pica:subfield[@code='a']" />
        </xsl:call-template>
        <xsl:if test="pica:field[@tag = '021A']/pica:subfield[@code='d']">
          <xsl:text> : </xsl:text>
          <xsl:call-template name="cleanString">
            <xsl:with-param name="str" select="pica:field[@tag = '021A']/pica:subfield[@code='d']" />
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:when test="pica:field[@tag = '021B'] and string-length(pica:field[@tag = '021A']/pica:subfield[@code='8']) &gt; 0">
        <xsl:call-template name="cleanString">
          <xsl:with-param name="str" select="pica:field[@tag = '021B']/pica:subfield[@code='l']" />
        </xsl:call-template>
        <xsl:if test="pica:field[@tag = '021B']/pica:subfield[@code='a']">
          <xsl:text> : </xsl:text>
          <xsl:call-template name="cleanString">
            <xsl:with-param name="str" select="pica:field[@tag = '021B']/pica:subfield[@code='a']" />
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="pica:field[@tag = '021B']/pica:subfield[@code='d']">
          <xsl:text> : </xsl:text>
          <xsl:call-template name="cleanString">
            <xsl:with-param name="str" select="pica:field[@tag = '021B']/pica:subfield[@code='d']" />
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- article -->
      <xsl:when test="pica:field[@tag = '021G']/pica:subfield[@code='a']">
        <xsl:call-template name="cleanString">
          <xsl:with-param name="str" select="pica:field[@tag = '021G']/pica:subfield[@code='a']" />
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdInfo">
    <xsl:apply-templates select="." mode="isbdAuthor" />
    <xsl:apply-templates select="." mode="isbdIn" />
    <xsl:if test="not(pica:field[@tag = '027D']/pica:subfield[@code='a'])">
      <xsl:apply-templates select="." mode="isbdVolume" />
      <xsl:apply-templates select="." mode="isbdPublisher" />
      <xsl:apply-templates select="." mode="isbdYear" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdAuthor">
    <xsl:choose>
      <xsl:when test="pica:field[@tag = '028A']/pica:subfield[@code='d']">
        <xsl:text>/ </xsl:text>
        <xsl:value-of select="pica:field[@tag = '028A']/pica:subfield[@code='d']" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="pica:field[@tag = '028A']/pica:subfield[@code='a']" />
      </xsl:when>
      <xsl:when test="pica:field[@tag = '028C']/pica:subfield[@code='d']">
        <xsl:text>/ </xsl:text>
        <xsl:value-of select="pica:field[@tag = '028C']/pica:subfield[@code='d']" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="pica:field[@tag = '028C']/pica:subfield[@code='a']" />
      </xsl:when>
      <xsl:when test="pica:field[@tag = '029A']/pica:subfield[@code='8']">
        <xsl:text>/ </xsl:text>
        <xsl:value-of select="pica:field[@tag = '029A']/pica:subfield[@code='8']" />
      </xsl:when>
      <xsl:when test="pica:field[@tag = '029F']/pica:subfield[@code='8']">
        <xsl:text>/ </xsl:text>
        <xsl:value-of select="pica:field[@tag = '029F']/pica:subfield[@code='8']" />
      </xsl:when>
      <xsl:when test="pica:field[@tag = '021A']/pica:subfield[@code='h']">
        <xsl:text>/ </xsl:text>
        <xsl:value-of select="pica:field[@tag = '021A']/pica:subfield[@code='h']" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdIn">
    <xsl:if test="pica:field[@tag = '027D']/pica:subfield[@code='a']">
      <xsl:if test="pica:field[@tag = '028A'] or pica:field[@tag = '028C'] or pica:field[@tag = '021A']/pica:subfield[@code='h']">
        <xsl:text>. - </xsl:text>
      </xsl:if>
      <xsl:text>In: </xsl:text>
      <xsl:value-of select="pica:field[@tag = '027D']/pica:subfield[@code='a']" />
      <xsl:if test="pica:field[@tag = '031A']/pica:subfield[@code='d']">
        <xsl:text>, Bd. </xsl:text>
        <xsl:value-of select="pica:field[@tag = '031A']/pica:subfield[@code='d']" />
        <xsl:if test="pica:field[@tag = '031A']/pica:subfield[@code='j']">
          <xsl:text> (</xsl:text>
          <xsl:value-of select="pica:field[@tag = '031A']/pica:subfield[@code='j']" />
          <xsl:text>)</xsl:text>
        </xsl:if>
        <xsl:if test="pica:field[@tag = '031A']/pica:subfield[@code='e']">
          <xsl:text>, </xsl:text>
          <xsl:value-of select="pica:field[@tag = '031A']/pica:subfield[@code='e']" />
        </xsl:if>
        <xsl:if test="pica:field[@tag = '031A']/pica:subfield[@code='h']">
          <xsl:text>, S. </xsl:text>
          <xsl:value-of select="pica:field[@tag = '031A']/pica:subfield[@code='h']" />
        </xsl:if>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdVolume">
    <xsl:if test="pica:field[@tag = '032@']/pica:subfield[@code='a']">
      <xsl:if test="pica:field[@tag = '028A'] or pica:field[@tag = '028C'] or pica:field[@tag = '021A']/pica:subfield[@code='h']">
        <xsl:text>. - </xsl:text>
      </xsl:if>
      <xsl:value-of select="pica:field[@tag = '032@']/pica:subfield[@code='a']" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdPublisher">
    <xsl:if test="pica:field[@tag = '033A'] and not(pica:field[@tag = '036C'])">
      <xsl:if
        test="pica:field[@tag = '028A'] or pica:field[@tag = '028C'] or pica:field[@tag = '021A']/pica:subfield[@code='h'] or pica:field[@tag = '032@']/pica:subfield[@code='a']"
      >
        <xsl:if test="substring(pica:field[@tag = '032@']/pica:subfield[@code='a'], string-length(pica:field[@tag = '032@']/pica:subfield[@code='a'])) != '.'">
          <xsl:text>.</xsl:text>
        </xsl:if>
        <xsl:text> - </xsl:text>
      </xsl:if>
      <xsl:value-of select="pica:field[@tag = '033A']/pica:subfield[@code='p']" />
      <xsl:text> : </xsl:text>
      <xsl:value-of select="pica:field[@tag = '033A']/pica:subfield[@code='n']" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="pica:record" mode="isbdYear">
    <xsl:if test="pica:field[@tag = '011@']/pica:subfield[@code='a']">
      <xsl:choose>
        <xsl:when test="pica:field[@tag = '032@']/pica:subfield[@code='a'] and not(pica:field[@tag = '033A']) or pica:field[@tag = '036C']">
          <xsl:text> - </xsl:text>
        </xsl:when>
        <xsl:when test="pica:field[@tag = '032@']/pica:subfield[@code='a'] or pica:field[@tag = '033A']">
          <xsl:text>, </xsl:text>
        </xsl:when>
      </xsl:choose>
      <xsl:value-of select="pica:field[@tag = '011@']/pica:subfield[@code='a']" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="cleanString">
    <xsl:param name="str" select="''" />

    <xsl:choose>
      <xsl:when test="substring($str, 1, 1) = '@'">
        <xsl:value-of select="substring($str, 2)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$str" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>