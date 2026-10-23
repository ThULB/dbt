<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcracl="http://www.mycore.de/xslt/acl" exclude-result-prefixes="#all">

  <xsl:include href="resource:xslt/default-parameters.xsl" />
  <xsl:include href="xslInclude:functions" />

  <xsl:mode on-no-match="shallow-copy" />

  <xsl:template match="/accesskeys">
    <xsl:copy>
      <xsl:copy-of select="@objId" />
      <xsl:choose>
        <xsl:when test="mcracl:check-permission(@objId, 'writedb')">
          <xsl:copy-of select="@readkey|@writekey" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="@readkey">
            <xsl:attribute name="readkey" />
          </xsl:if>
          <xsl:if test="@writekey">
            <xsl:attribute name="writekey" />
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>