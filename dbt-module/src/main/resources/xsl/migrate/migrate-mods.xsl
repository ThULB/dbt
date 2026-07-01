<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:mcrld="xalan://org.mycore.common.MCRLanguageDetector" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:mcrdataurl="xalan://org.mycore.datamodel.common.MCRDataURL" xmlns:migutils="xalan://de.urmel_dl.dbt.migration.MigrationUtils" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  exclude-result-prefixes="xsl xlink mods mcrld mcrxml mcrdataurl migutils xalan"
>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" />

  <xsl:param name="MIR.projectid.default" />

  <xsl:variable name="ProjectId">
    <xsl:choose>
      <xsl:when test="string-length($MIR.projectid.default) &gt; 0">
        <xsl:value-of select="$MIR.projectid.default" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'dbt'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="GVK_URL" select="'http://uri.gbv.de/document/gvk:ppn:'" />

  <xsl:template match="mods:abstract[not(@altRepGroup)]">
    <xsl:variable name="text">
      <xsl:apply-templates select="." mode="processAbstract" />
    </xsl:variable>

    <xsl:if test="string-length($text) &gt; 0">
      <xsl:choose>
        <xsl:when test="mcrxml:isHtml($text)">
          <xsl:apply-templates select="." mode="buildWithAltFormat">
            <xsl:with-param name="htmlContent" select="$text" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy>
            <xsl:attribute name="xml:lang">
            <xsl:call-template name="detectLanguage">
              <xsl:with-param name="text" select="$text" />
              <xsl:with-param name="defaultLang" select="@xml:lang" />
            </xsl:call-template>
          </xsl:attribute>
            <xsl:apply-templates select="@*[name(.) != 'xml:lang']" />
            <xsl:value-of select="$text" />
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:abstract[@altRepGroup]">
    <xsl:if test="string-length(@altFormat) &gt; 0">
      <xsl:variable name="content" select="document(@altFormat)/abstract" />
      <xsl:variable name="contentEscaped">
        <xsl:copy>
          <xsl:apply-templates select="xalan:nodeset($content)/child::node()" mode="serialize" />
        </xsl:copy>
      </xsl:variable>
      <xsl:variable name="processedContent">
        <xsl:apply-templates select="xalan:nodeset($contentEscaped)" mode="processAbstract" />
      </xsl:variable>

      <xsl:if test="string-length($processedContent) &gt; 0">
        <xsl:choose>
          <xsl:when test="mcrxml:isHtml($processedContent)">
            <xsl:apply-templates select="." mode="buildWithAltFormat">
              <xsl:with-param name="htmlContent" select="$processedContent" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy>
              <xsl:attribute name="xml:lang">
                <xsl:call-template name="detectLanguage">
                  <xsl:with-param name="text" select="$processedContent" />
                  <xsl:with-param name="defaultLang" select="@xml:lang" />
                </xsl:call-template>
              </xsl:attribute>
              <xsl:apply-templates select="@*[not(contains('xml:lang|altRepGroup|altFormat|contentType', name()))]" />
              <xsl:value-of select="$processedContent" />
            </xsl:copy>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="processAbstract">
    <xsl:choose>
      <xsl:when test="contains(text(), '/Derivate-')">
        <xsl:variable name="derId" select="number(substring-before(substring-after(text(), '/Derivate-'), '/'))" />
        <xsl:variable name="file" select="mcrxml:trim(substring-before(substring-after(substring-after(text(), '/Derivate-'), $derId), '&quot;'))" />
        <xsl:choose>
          <xsl:when test="translate(substring($file, string-length($file) - 3),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') = '.txt'">
            <xsl:value-of select="migutils:getContentOfFile(concat($ProjectId, '_derivate_', format-number($derId, '00000000'), $file))" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="text()" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when
        test="starts-with(text(), '&lt;b&gt;') and contains('Abstract|Zusammenfassung', translate(substring-before(substring-after(text(), '&lt;b&gt;'), '&lt;/b&gt;'), ':', ''))"
      >
        <xsl:variable name="text">
          <xsl:variable name="tmp" select="substring-after(text(), '&lt;/b&gt;')" />
          <xsl:choose>
            <xsl:when test="starts-with($tmp, '&lt;br') and (string-length(substring-before($tmp, '/&gt;')) &lt; 5)">
              <xsl:value-of select="substring-after($tmp, '/&gt;')" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$tmp" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="mcrxml:trim($text)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="text()" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:abstract" mode="buildWithAltFormat">
    <xsl:param name="htmlContent" select="text()" />

    <xsl:variable name="cleanContent">
      <xsl:call-template name="fixBRs">
        <xsl:with-param name="text" select="$htmlContent" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="altRepGroup">
      <xsl:choose>
        <xsl:when test="string-length(@altRepGroup) &gt; 0">
          <xsl:value-of select="@altRepGroup" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="generate-id(.)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="lang">
      <xsl:call-template name="detectLanguage">
        <xsl:with-param name="text" select="$cleanContent" />
        <xsl:with-param name="defaultLang" select="@xml:lang" />
      </xsl:call-template>
    </xsl:variable>
    
    <!-- plain text -->
    <xsl:copy>
      <xsl:attribute name="xml:lang">
        <xsl:value-of select="$lang" />
      </xsl:attribute>
      <xsl:attribute name="altRepGroup">
        <xsl:value-of select="$altRepGroup" />
      </xsl:attribute>
      <xsl:apply-templates select="@*[not(contains('xml:lang|altRepGroup|altFormat|contentType', name()))]" />

      <xsl:value-of select="mcrxml:stripHtml($cleanContent)" />
    </xsl:copy>
    
    <!-- html text -->
    <xsl:copy>
      <xsl:variable name="content">
        <xsl:element name="{local-name(.)}">
          <xsl:attribute name="xml:lang">
            <xsl:value-of select="$lang" />
          </xsl:attribute>
          <xsl:apply-templates select="@*[not(contains('xml:lang|altRepGroup|altFormat|contentType', name()))]" />
          <xsl:value-of select="$cleanContent" disable-output-escaping="yes" />
        </xsl:element>
      </xsl:variable>

      <xsl:attribute name="xml:lang">
        <xsl:value-of select="$lang" />
      </xsl:attribute>
      <xsl:attribute name="altRepGroup">
        <xsl:value-of select="$altRepGroup" />
      </xsl:attribute>
      <xsl:attribute name="altFormat">
        <xsl:value-of select="mcrdataurl:build($content)" />
      </xsl:attribute>
      <xsl:attribute name="contentType">
        <xsl:text>text/xml</xsl:text>
      </xsl:attribute>
      <xsl:apply-templates select="@*[not(contains('xml:lang|altRepGroup|altFormat|contentType', name()))]" />
    </xsl:copy>
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
            <xsl:value-of select="concat($ProjectId, '_mods_', format-number(number(mods:identifier[@type='local']/text()), '00000000'))" />
          </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='ppn']">
    <xsl:variable name="ppn" select="text()" />

    <xsl:if test="count(..//mods:identifier[@type='uri' and contains(text(), concat($GVK_URL, $ppn))]) = 0">
      <xsl:copy>
        <xsl:attribute name="type">
          <xsl:text>uri</xsl:text>
        </xsl:attribute>
        <xsl:value-of select="concat($GVK_URL, $ppn)" />
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!-- detect language from text -->
  <xsl:template name="detectLanguage">
    <xsl:param name="text" />
    <xsl:param name="defaultLang" select="'de'" />

    <xsl:variable name="detectedLang" select="mcrld:detectLanguage($text)" />
    <xsl:choose>
      <xsl:when test="string-length($detectedLang) &gt; 0">
        <xsl:value-of select="$detectedLang" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$defaultLang" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- fix wrong br's <br> to <br /> -->
  <xsl:template name="fixBRs">
    <xsl:param name="text" />

    <xsl:value-of select="mcrxml:regexp($text,'&lt;br&gt;', '&lt;br /&gt;')" />
  </xsl:template>
  
  <!-- nodeset to string serializer -->
  <xsl:template match="*" mode="serialize">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()" />
    <xsl:apply-templates select="@*" mode="serialize" />
    <xsl:choose>
      <xsl:when test="node()">
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates mode="serialize" />
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()" />
        <xsl:text>&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text> /&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*" mode="serialize">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()" />
    <xsl:text>="</xsl:text>
    <xsl:value-of select="." />
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="serialize">
    <xsl:value-of select="." />
  </xsl:template>
    
	<!-- standard copy template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>