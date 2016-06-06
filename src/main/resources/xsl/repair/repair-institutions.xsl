<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3">
  <xsl:include href="copynodes.xsl" />

  <xsl:template match="mods:name[@type='corporate' and @valueURI and not(@authorityURI)]">
    <xsl:choose>
      <xsl:when test="starts-with(@valueURI, '#')">
        <xsl:variable name="mir_institutes_uri" select="document('classification:metadata:-1:children:mir_institutes')/mycoreclass/label[@xml:lang='x-uri']/@text" />
        <xsl:choose>
          <xsl:when test="$mir_institutes_uri">
	          <xsl:if test="not($mir_institutes_uri = preceding-sibling::node()/@authorityURI) and not($mir_institutes_uri = following-sibling::node()/@authorityURI)">
	            <xsl:copy>
	              <xsl:attribute name="authorityURI">
	                <xsl:value-of select="$mir_institutes_uri"/>
	              </xsl:attribute>
	              <xsl:attribute name="valueURI">
	                <xsl:value-of select="concat($mir_institutes_uri, @valueURI)" />
	              </xsl:attribute>
	              <xsl:apply-templates select="@*[name()!='valueURI']|node()" />
	            </xsl:copy>
	          </xsl:if>
          </xsl:when>
          <xsl:otherwise>
          	<xsl:copy-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:name[@type='corporate' and @valueURI and @authorityURI]">
	<xsl:variable name="mir_institutes_uri" select="document('classification:metadata:-1:children:mir_institutes')/mycoreclass/label[@xml:lang='x-uri']/@text" />
	<xsl:choose>
		<xsl:when test="$mir_institutes_uri">
			<xsl:choose>
				<xsl:when test="$mir_institutes_uri = @authorityURI">
					<xsl:copy-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="not($mir_institutes_uri = preceding-sibling::node()/@authorityURI) and not($mir_institutes_uri = following-sibling::node()/@authorityURI)">
						<xsl:copy>
			              <xsl:attribute name="authorityURI">
			                <xsl:value-of select="$mir_institutes_uri"/>
			              </xsl:attribute>
			              <xsl:attribute name="valueURI">
			                <xsl:value-of select="concat($mir_institutes_uri, '#', substring-after(@valueURI,'#'))" />
			              </xsl:attribute>
			              <xsl:apply-templates select="@*[name()!='valueURI' and name()!='authorityURI']|node()" />
			            </xsl:copy>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:otherwise>
			<xsl:copy-of select="."/>
		</xsl:otherwise>
	</xsl:choose>

  </xsl:template>

</xsl:stylesheet>