<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:mcrld="xalan://org.mycore.common.MCRLanguageDetector" xmlns:migutils="xalan://org.urmel.dbt.migration.MigrationUtils"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="xsl xlink mods mcrld migutils xalan"
>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:template match="mods:abstract">
    <xsl:choose>
      <xsl:when test="contains(text(), '/Derivate-')">
        <xsl:variable name="derId" select="substring-before(substring-after(text(), '/Derivate-'), '/')" />
        <xsl:variable name="file" select="substring-before(substring-after(text(), concat('/Derivate-', $derId)), '&quot;')" />
        <xsl:choose>
          <xsl:when test="substring($file, string-length($file) - 3) = '.txt'">
            <xsl:variable name="text" select="migutils:getContentOfFile(concat('mir_derivate_', $derId, $file))" />
            <xsl:variable name="lang" select="mcrld:detectLanguage($text)" />
            <xsl:copy>
              <xsl:attribute name="xml:lang">
                <xsl:choose>
                  <xsl:when test="string-length($lang) &gt; 0">
                    <xsl:value-of select="$lang" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@xml:lang" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:apply-templates select="@*[name(.) != 'xml:lang']" />
              <xsl:value-of select="$text" />
            </xsl:copy>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy>
              <xsl:apply-templates select="@*" />
              <xsl:apply-templates />
            </xsl:copy>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when
        test="starts-with(text(), '&lt;b&gt;') and contains('Abstract|Zusammenfassung', translate(substring-before(substring-after(text(), '&lt;b&gt;'), '&lt;/b&gt;'), ':', ''))"
      >
        <xsl:variable name="text">
          <xsl:variable name="tmp" select="substring-after(text(), '&lt;/b&gt;')" />
          <xsl:choose>
            <xsl:when test="starts-with($tmp, '&lt;br/&gt;')">
              <xsl:value-of select="substring-after($tmp, '&lt;br/&gt;')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$tmp" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:copy>
          <xsl:apply-templates select="@*" />
          <xsl:value-of select="$text" />
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*" />
          <xsl:apply-templates />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
    
	<!-- standard copy template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>