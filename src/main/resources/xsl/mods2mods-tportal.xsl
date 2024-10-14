<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions" xmlns:str="http://exslt.org/strings" xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
  exclude-result-prefixes="iview2 str mcr">
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:include href="mods2record.xsl" />
  <xsl:include href="mods2mods.xsl" />
  <xsl:template match="mycoreobject" mode="metadata">
    <mets:mets
      xsi:schemaLocation="http://www.loc.gov/METS/ http://www.loc.gov/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
      <mets:dmdSec ID="dmd_{@ID}">
        <mets:mdWrap MDTYPE="MODS">
          <mets:xmlData>
            <xsl:apply-templates mode="mods2mods" />
          </mets:xmlData>
        </mets:mdWrap>
      </mets:dmdSec>
      <xsl:apply-templates select="structure/derobjects" />
    </mets:mets>
  </xsl:template>

  <xsl:template match="derobjects">
    <mets:fileSec>
      <mets:fileGrp USE="DEFAULT">
        <xsl:apply-templates />
      </mets:fileGrp>
    </mets:fileSec>
  </xsl:template>

  <xsl:template match="derobject">
    <xsl:variable name="derivate" select="document(concat('mcrobject:',@xlink:href))" />
    <xsl:if test="not($derivate/mycorederivate/derivate/@display='false')">
      <xsl:variable name="iviewFile" select="iview2:getSupportedMainFile(@xlink:href)" />
      <xsl:choose>
        <xsl:when test="string-length($iviewFile) &gt; 0">
          <!-- Thumbnail from mycore-iview -->
          <mets:file ID="@xlink:href" MIMETYPE="image/jpeg">
            <mets:FLocat LOCTYPE="URL"
              xlink:href="{concat($WebApplicationBaseURL,'servlets/MCRTileCombineServlet/THUMBNAIL/',@xlink:href,'/',$iviewFile)}" />
          </mets:file>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="maindoc" select="$derivate/mycorederivate/derivate/internals/internal/@maindoc" />
          <xsl:if test="translate(str:tokenize($maindoc,'.')[position()=last()],'PDF','pdf') = 'pdf'">
            <xsl:variable name="filePath" select="concat(@xlink:href,'/',mcr:encodeURIPath($maindoc))" />
            <mets:file ID="{@xlink:href}" MIMETYPE="image/jpeg">
              <mets:FLocat LOCTYPE="URL" xlink:href="{$WebApplicationBaseURL}img/pdfthumb/{$filePath}?centerThumb=no" />
            </mets:file>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>