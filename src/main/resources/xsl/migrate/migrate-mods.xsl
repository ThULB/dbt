<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:mcrld="xalan://org.mycore.common.MCRLanguageDetector" xmlns:migutils="xalan://org.urmel.dbt.migration.MigrationUtils"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="xsl xlink mods mcrld migutils xalan"
>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:template match="mods:abstract">
    <xsl:choose>
      <xsl:when test="contains(text(), '/Derivate-')">
        <xsl:variable name="derId" select="number(substring-before(substring-after(text(), '/Derivate-'), '/'))" />
        <xsl:variable name="file" select="substring-before(substring-after(substring-after(text(), '/Derivate-'), $derId), '&quot;')" />
        <xsl:choose>
          <xsl:when test="translate(substring($file, string-length($file) - 3),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') = '.txt'">
            <xsl:variable name="text" select="migutils:getContentOfFile(concat('mir_derivate_', $derId, $file))" />
            <xsl:choose>
              <xsl:when test="string-length($text) &gt; 0">
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

  <xsl:template match="mods:relatedItem">
    <!--
     - extract relatedItem's for processing from single file
     - cat miless_mods_00017936.xml|grep 'mods:identifier type="local"'|grep -o '[0-9]*'| awk {'printf("xslt mir_mods_%08d with file ~/migrate-mods.xsl\n",$1)'} 
     -->
    <xsl:if test="not(contains('constituent|succeeding', @type))">
      <xsl:copy>
        <xsl:apply-templates select="@*" />
        <xsl:choose>
          <xsl:when test="mods:identifier[@type='local']">
            <xsl:attribute name="xlink:href">
            <xsl:value-of select="concat('mir_mods_', format-number(number(mods:identifier[@type='local']/text()), '00000000'))" />
          </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
    
	<!-- standard copy template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>