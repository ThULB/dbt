<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xsl pica"
>
  <xsl:output method="xml" indent="yes" encoding="UTF-8" />
  
  <!-- http://www.oclc.org/contacts/libraries.en.html -->
  <!-- http://sigel.staatsbibliothek-berlin.de/suche/ -->
  <xsl:param name="RecordIdSource" select="'DE-601'" />
  <xsl:param name="RecordIdPrefix" select="'ppn:'" />

  <!-- ======================================= -->
  <!-- | ROOT -->
  <!-- ======================================= -->

  <xsl:template match="/">
    <mods version="3.6" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
      <xsl:apply-templates />
    </mods>
  </xsl:template>

  <!-- ======================================= -->
  <!-- | The Record -->
  <!-- ======================================= -->
  <xsl:template match="pica:record">
    <xsl:param name="isParent" select="true()" />

    <xsl:apply-templates select="." mode="recordInfo" />

    <xsl:apply-templates select="." mode="typeOfResource" />
    <xsl:apply-templates select="." mode="genre" />

    <xsl:apply-templates select="." mode="title" />
    <xsl:apply-templates select="." mode="personal" />
    <xsl:apply-templates select="." mode="corporate" />

    <xsl:apply-templates select="." mode="language" />
    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="physicalDescription" />

    <xsl:apply-templates select="." mode="relatedItem">
      <xsl:with-param name="isParent" select="$isParent" />
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="abstract" />
    <xsl:apply-templates select="." mode="note" />
    <xsl:apply-templates select="." mode="subject" />
    <xsl:apply-templates select="." mode="classification" />

    <xsl:apply-templates select="." mode="identifier" />
  </xsl:template>

  <!-- ======================================= -->
  <!-- | matching templates -->
  <!-- ======================================= -->

  <xsl:template match="pica:record " mode="recordInfo">
    <recordInfo>
      <recordIdentifier source="{$RecordIdSource}">
        <xsl:value-of select="concat($RecordIdPrefix, pica:field[@tag='003@']/pica:subfield[@code='0'])" />
      </recordIdentifier>
    </recordInfo>
  </xsl:template>

  <xsl:template match="pica:record " mode="typeOfResource">
    <typeOfResource>
      <xsl:choose>
        <xsl:when test="pica:field[@tag='010E']/pica:subfield[@code='e'] = 'rda'">
          <xsl:for-each select="pica:field[@tag='002C']">
            <xsl:choose>
              <xsl:when test="contains('crd|cri|crm|crt|crn|crf', pica:subfield[@code='b'])">
                <xsl:text>cartographic</xsl:text>
              </xsl:when>
              <xsl:when test="contains('cod|cop', pica:subfield[@code='b'])">
                <xsl:text>software, multimedia</xsl:text>
              </xsl:when>
              <xsl:when test="contains('ntv|tcn|tct|txt', pica:subfield[@code='b'])">
                <xsl:text>text</xsl:text>
              </xsl:when>
              <xsl:when test="contains('ntm|tcm', pica:subfield[@code='b'])">
                <xsl:text>notated music</xsl:text>
              </xsl:when>
              <xsl:when test="contains('prm', pica:subfield[@code='b'])">
                <xsl:text>sound recording-musical</xsl:text>
              </xsl:when>
              <xsl:when test="contains('snd|spw', pica:subfield[@code='b'])">
                <xsl:text>sound recording-nonmusical</xsl:text>
              </xsl:when>
              <xsl:when test="contains('sti|tci', pica:subfield[@code='b'])">
                <xsl:text>still image</xsl:text>
              </xsl:when>
              <xsl:when test="contains('tdm|tdi', pica:subfield[@code='b'])">
                <xsl:text>moving image</xsl:text>
              </xsl:when>
              <xsl:when test="contains('tcf|tdf', pica:subfield[@code='b'])">
                <xsl:text>three dimensional object</xsl:text>
              </xsl:when>
              <xsl:when test="contains('xxx|zzz', pica:subfield[@code='b'])">
                <xsl:text>mixed material</xsl:text>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="pica:field[@tag='002@']">
            <xsl:choose>
              <xsl:when test="contains('A|C|H|O', substring(pica:subfield[@code='0'],1,1))">
                <xsl:text>text</xsl:text>
              </xsl:when>
              <xsl:when test="substring(pica:subfield[@code='0'],1,1)='K'">
                <xsl:text>cartographic</xsl:text>
              </xsl:when>
              <xsl:when test="contains('S|G', substring(pica:subfield[@code='0'],1,1))">
                <xsl:text>software, multimedia</xsl:text>
              </xsl:when>
              <xsl:when test="substring(pica:subfield[@code='0'],1,1)='M'">
                <xsl:text>notated music</xsl:text>
              </xsl:when>
              <xsl:when test="pica:subfield[@code='b']='B'">
                <xsl:text>moving image</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>mixed material</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </typeOfResource>
  </xsl:template>

  <xsl:template match="pica:record " mode="genre">
    <xsl:variable name="rda" select="pica:field[@tag='010E']/pica:subfield[@code='e'] = 'rda'" />
    <xsl:if test="$rda">
      <!-- http://www.loc.gov/standards/valuelist/rdacontent.html -->
      <genre authority="rdacontent">
        <xsl:for-each select="pica:field[@tag='002C']">
          <xsl:attribute name="type"><xsl:value-of select="pica:subfield[@code='b']" /></xsl:attribute>
          <xsl:attribute name="displayLabel"><xsl:value-of select="pica:subfield[@code='a']" /></xsl:attribute>
          <xsl:choose>
            <xsl:when test="pica:subfield[@code='b'] = 'crd'">
              <xsl:text>cartographic dataset</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'cri'">
              <xsl:text>cartographic image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'crm'">
              <xsl:text>cartographic moving image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'crt'">
              <xsl:text>cartographic tactile three-dimensional form</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'crf'">
              <xsl:text>cartographic three-dimensional form</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'cod'">
              <xsl:text>computer dataset</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'cop'">
              <xsl:text>computer program</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'ntv'">
              <xsl:text>notated movement</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'ntm'">
              <xsl:text>notated music</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'prm'">
              <xsl:text>performed music</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'snd'">
              <xsl:text>sounds</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'spw'">
              <xsl:text>spoken word</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'sti'">
              <xsl:text>still image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tci'">
              <xsl:text>tactile image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tcm'">
              <xsl:text>tactile notated music</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tcn'">
              <xsl:text>tactile notated movement</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tct'">
              <xsl:text>tactile text</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tcf'">
              <xsl:text>tactile three-dimensional form</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'txt'">
              <xsl:text>text</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tdf'">
              <xsl:text>three-dimensional form</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tdm'">
              <xsl:text>three-dimensional moving image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'tdi'">
              <xsl:text>two-dimensional moving image</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'xxx'">
              <xsl:text>other</xsl:text>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='b'] = 'zzz'">
              <xsl:text>unspecified</xsl:text>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </genre>
    </xsl:if>
  </xsl:template>

  <!-- Language -->
  <xsl:template match="pica:record " mode="language">
    <xsl:for-each select="pica:field[@tag='010@']"> <!-- 1500 Language -->
      <xsl:for-each select="pica:subfield[@code='a']">
        <language>
          <languageTerm type="code" authority="iso639-2b">
            <xsl:value-of select="." />
          </languageTerm>
        </language>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <!-- originInfo -->
  <xsl:template match="pica:record" mode="originInfo">
    <originInfo eventType="publication">
      <xsl:apply-templates select="." mode="publisherAndPlace" />
      <xsl:apply-templates select="." mode="dates" />
      <xsl:apply-templates select="." mode="issuance" />
      <xsl:apply-templates select="." mode="frequency" />
    </originInfo>

    <xsl:if test="starts-with(pica:field[@tag='002@']/pica:subfield[@code='0'], 'O')">
      <originInfo eventType="online_publication"> <!-- 4031 -->
        <xsl:apply-templates select="." mode="place">
          <xsl:with-param name="type" select="'online'" />
        </xsl:apply-templates>
        <xsl:apply-templates select="." mode="dates">
          <xsl:with-param name="type" select="'online'" />
        </xsl:apply-templates>
      </originInfo>
    </xsl:if>

  </xsl:template>
    
  <!-- Publisher and Place -->
  <xsl:template match="pica:record" mode="publisherAndPlace">
    <xsl:param name="type" select="''" />

    <xsl:choose>
      <xsl:when test="$type = 'online'">
        <xsl:if test="pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='p']">  <!-- 4030 Ort, Verlag -->
          <place>
            <placeTerm type="text">
              <xsl:value-of select="pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='p']" />
            </placeTerm>
          </place>
        </xsl:if>
        <xsl:if test="pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='n']">  <!-- 4030 Ort, Verlag -->
          <publisher>
            <xsl:value-of select="./pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='n']" />
          </publisher>
        </xsl:if>
        <edition>[Electronic ed.]</edition>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="pica:field[@tag='033A']">
          <xsl:for-each select="pica:subfield[@code='p']">
            <place>
              <placeTerm type="text">
                <xsl:value-of select="." />
              </placeTerm>
            </place>
          </xsl:for-each>
          <xsl:if test="pica:subfield[@code='n']">  <!-- 4030 Ort, Verlag -->
            <publisher>
              <xsl:value-of select="pica:subfield[@code='n']" />
            </publisher>
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="pica:field[@tag='019@']/pica:subfield[@code='a']">
          <place>
            <placeTerm type="code" authority="iso3166">
              <xsl:value-of select="." />
            </placeTerm>
          </place>
        </xsl:for-each>
    
        <!-- normierte Orte -->
        <xsl:for-each select="pica:field[@tag='033B' and @occurrence='03']/pica:subfield[@code='p']">
          <place supplied="yes">
            <placeTerm lang="ger" type="text">
              <xsl:value-of select="." />
            </placeTerm>
          </place>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Dates -->
  <xsl:template match="pica:record" mode="dates">
    <xsl:param name="type" select="''" />

    <xsl:choose>
      <xsl:when test="$type = 'online'">
        <xsl:for-each select="pica:field[@tag='011B']">   <!-- 1109 -->
          <xsl:choose>
            <xsl:when test="pica:subfield[@code='b']">
              <dateCaptured encoding="iso8601" point="start">
                <xsl:value-of select="pica:subfield[@code='a']" />
              </dateCaptured>
              <dateCaptured encoding="iso8601" point="end">
                <xsl:value-of select="pica:subfield[@code='b']" />
              </dateCaptured>
            </xsl:when>
            <xsl:otherwise>
              <dateCaptured encoding="iso8601">
                <xsl:value-of select="pica:subfield[@code='a']" />
              </dateCaptured>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="pica:field[@tag='011@']">
          <xsl:choose>
            <xsl:when test="pica:subfield[@code='b']">
              <dateIssued>
                <xsl:value-of select="concat(pica:subfield[@code='a'], '-', pica:subfield[@code='b'])" />
              </dateIssued>
              <dateIssued keyDate="yes" encoding="iso8601" point="start">
                <xsl:value-of select="pica:subfield[@code='a']" />
              </dateIssued>
              <dateIssued encoding="iso8601" point="end">
                <xsl:value-of select="pica:subfield[@code='b']" />
              </dateIssued>
            </xsl:when>
            <xsl:when test="contains(pica:subfield[@code='n'], '-') and string-length(substring-after(pica:subfield[@code='n'], '-')) = 0">
              <dateIssued>
                <xsl:value-of select="pica:subfield[@code='n']" />
              </dateIssued>
              <dateIssued encoding="marc" point="start">
                <xsl:value-of select="substring-before(pica:subfield[@code='n'], '-')" />
              </dateIssued>
              <dateIssued encoding="marc" point="end">9999</dateIssued>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="contains(pica:subfield[@code='a'], 'X')">
                  <dateCreated keyDate="yes" encoding="iso8601" point="start">
                    <xsl:value-of select="translate(pica:subfield[@code='a'], 'X','0')" />
                  </dateCreated>
                  <dateCreated encoding="iso8601" point="end">
                    <xsl:value-of select="translate(pica:subfield[@code='a'], 'X', '9')" />
                  </dateCreated>
                  <dateCreated qualifier="approximate">
                    <xsl:value-of select="pica:subfield[@code='a']"></xsl:value-of>
                    <xsl:if test="pica:subfield[@code='n']">
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="pica:subfield[@code='n']" />
                    </xsl:if>
                  </dateCreated>
                </xsl:when>
                <xsl:otherwise>
                  <dateIssued keyDate="yes" encoding="iso8601">
                    <xsl:value-of select="pica:subfield[@code='a']" />
                  </dateIssued>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Issuance -->
  <xsl:template match="pica:record" mode="issuance">
    <xsl:for-each select="pica:field[@tag='002@']">
      <xsl:choose>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='a'">
          <issuance>monographic</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='b'">
          <issuance>serial</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='c'">
          <issuance>multipart monograph</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='d'">
          <issuance>serial</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='f'">
          <issuance>monographic</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='F'">
          <issuance>monographic</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='j'">
          <issuance>single unit</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='s'">
          <issuance>single unit</issuance>
        </xsl:when>
        <xsl:when test="substring(pica:subfield[@code='0'],2,1)='v'">
          <issuance>monographic</issuance>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- frequency -->
  <xsl:template match="pica:record" mode="frequency">
    <xsl:for-each select="pica:field[@tag='031@']/pica:subfield[@code='a']">
      <frequency>
        <xsl:value-of select="." />
      </frequency>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pica:record" mode="physicalDescription">
    <xsl:if test="pica:field[@tag='002D' or @tag='034D' or @tag='034M' or @tag='034I' or @tag='034K']">
      <physicalDescription>
        <xsl:for-each select="pica:field[@tag='034D']/pica:subfield[@code='a']">   <!--  4060 Umfang, Seiten -->
          <extent>
            <xsl:value-of select="." />
          </extent>
        </xsl:for-each>
        <xsl:for-each select="pica:field[@tag='034M']/pica:subfield[@code='a']">   <!--  4061 Illustrationen -->
          <extent>
            <xsl:value-of select="." />
          </extent>
        </xsl:for-each>
        <xsl:for-each select="pica:field[@tag='034I']/pica:subfield[@code='a']">   <!-- 4062 Format, Größe  -->
          <extent>
            <xsl:value-of select="." />
          </extent>
        </xsl:for-each>
        <xsl:for-each select="pica:field[@tag='034K']/pica:subfield[@code='a']">   <!-- 4063 Begleitmaterial  -->
          <extent>
            <xsl:value-of select="." />
          </extent>
        </xsl:for-each>
      </physicalDescription>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pica:record" mode="abstract">
    <xsl:for-each select="pica:field[@tag='047I']">
      <xsl:if test="pica:subfield[@code='a']">
        <abstract>
          <xsl:value-of select="pica:subfield[@code='a']" />
        </abstract>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pica:record" mode="note">
    <xsl:for-each select="pica:field[@tag='009P' and @occurrence='09']">
      <note>
        <xsl:attribute name="xlink:href"><xsl:value-of select="pica:subfield[@code='a']" /></xsl:attribute>
        <xsl:value-of select="pica:subfield[@code='y']" />
      </note>
    </xsl:for-each>

    <xsl:for-each
      select="pica:field[@tag='037A' or @tag='037B' or @tag='037C' or @tag='046L' or @tag='046F' or @tag='046G' or @tag='046H' or @tag='046I' or @tag='046P']"
    ><!-- 4201, 4202, 4221, 4215, 4216, 4217, 4218 -->
      <note>
        <xsl:attribute name="type">
          <xsl:choose>
            <xsl:when test="@tag='037C'"><xsl:text>thesis</xsl:text></xsl:when>
            <xsl:when test="@tag='046L'"><xsl:text>language</xsl:text></xsl:when>
            <xsl:when test="@tag='046P'"><xsl:text>numbering</xsl:text></xsl:when>
            <xsl:otherwise><xsl:text>other</xsl:text></xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="pica:subfield[@code='b']">
          <xsl:value-of select="substring-before(pica:subfield[@code='b'], '@')" />
          <xsl:value-of select="substring-after(pica:subfield[@code='b'], '@')" />
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:if test="pica:subfield[@code='d']">
          <xsl:value-of select="pica:subfield[@code='d']" />
          <xsl:if test="pica:subfield[@code='e']">
            <xsl:text>, </xsl:text>
            <xsl:value-of select="pica:subfield[@code='e']" />
          </xsl:if>
          <xsl:if test="pica:subfield[@code='f']">
            <xsl:text> (</xsl:text>
            <xsl:value-of select="pica:subfield[@code='f']" />
            <xsl:text>)</xsl:text>
          </xsl:if>
        </xsl:if>
        <xsl:value-of select="pica:subfield[@code='a']" />
      </note>
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='047C']"><!-- 4200 -->
      <note type="titlewordindex">
        <xsl:value-of select="pica:subfield[@code='a']" />
      </note>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pica:record" mode="subject">
    <xsl:for-each select="pica:field[@tag='041A' or @tag='044K']">
      <xsl:choose>
        <xsl:when test="pica:subfield[@code='9']">
          <xsl:variable name="aboveRecord" select="document(concat('notnull:opc:record=', pica:subfield[@code='9'], '&amp;isil=', $RecordIdSource))" />
          <xsl:choose>
            <xsl:when test="$aboveRecord/pica:record">
              <subject>
                <xsl:for-each select="$aboveRecord/pica:record">
                  <xsl:choose>
                    <xsl:when test="substring(pica:field[@tag='002@'], 2, 1) = 'g'">
                      <geographic>
                        <xsl:call-template name="authorityGND">
                          <xsl:with-param name="aboveRecord" select="$aboveRecord" />
                        </xsl:call-template>
                        <xsl:value-of select="pica:field[@tag='065A']/pica:subfield[@code='a']" />
                      </geographic>
                    </xsl:when>
                    <xsl:when test="substring(pica:field[@tag='002@'], 2, 1) = 's'">
                      <topic>
                        <xsl:call-template name="authorityGND">
                          <xsl:with-param name="aboveRecord" select="$aboveRecord" />
                        </xsl:call-template>
                        <xsl:value-of select="pica:field[@tag='041A']/pica:subfield[@code='a']" />
                      </topic>
                    </xsl:when>
                  </xsl:choose>
                </xsl:for-each>
              </subject>
            </xsl:when>
            <xsl:when test="pica:subfield[@code='a']">
              <subject>
                <topic>
                  <xsl:value-of select="pica:subfield[@code='a']" />
                </topic>
              </subject>
            </xsl:when>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="pica:subfield[@code='a']">
          <subject>
            <topic>
              <xsl:value-of select="pica:subfield[@code='a']" />
            </topic>
          </subject>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pica:record" mode="classification">
    <xsl:for-each select="pica:field[@tag='045E']/pica:subfield[@code='a']"> <!-- 5050 -->
      <classification authority="sdnb">
        <xsl:value-of select="." />
      </classification>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="pica:record" mode="identifier">
    <xsl:for-each select="pica:field[@tag='003@']"> <!--  0100 -->
      <identifier type="ppn">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='004U']"> <!-- 2050 -->
      <identifier type="urn">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='004V']"> <!-- 2051 -->
      <identifier type="doi">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='004R']"> <!-- 2052 -->
      <identifier type="hdl">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='005A']">
      <identifier type="issn">
        <xsl:value-of select="concat(substring(pica:subfield[@code='0'],1,4),'-', substring(pica:subfield[@code='0'],5))" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='006X' and pica:subfield[@code='c'] = 'OCoLC']">
      <identifier type="oclc">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='006Z']"> <!--  2110 -->
      <identifier type="zdb">
        <xsl:value-of select="pica:subfield[@code='0']" />
      </identifier>
    </xsl:for-each>
    <xsl:for-each select="pica:field[@tag='007S']"><!-- 2277 -->
      <xsl:if test="starts-with(pica:subfield[@code='0'], 'RISM')">
        <identifier type="rism">
          <xsl:value-of select="normalize-space(substring-after(pica:subfield[@code='0'], 'RISM'))" />
        </identifier>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!-- Title -->
  <xsl:template match="pica:record" mode="title">
    <xsl:choose>
      <xsl:when test="substring(pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='f' or substring(pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='F' ">
        <xsl:for-each select="pica:field[@tag='036C']"><!-- 4150 -->
          <xsl:call-template name="title" />
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="substring(pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='v' and pica:field[@tag='027D']">
        <xsl:for-each select="pica:field[@tag='027D']"><!-- 3290 -->
          <xsl:call-template name="title" />
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="pica:field[@tag='021A']"> <!--  4000 -->
          <xsl:call-template name="title" />
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Persons -->
  <xsl:template match="pica:record" mode="personal">
    <xsl:for-each select="pica:field[@tag='028A' or @tag='028B']"> <!-- 300x -->
      <xsl:call-template name="personal">
        <xsl:with-param name="marcrelatorCode">
          <xsl:text>aut</xsl:text>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='028C' or @tag='028D' or @tag='028E'or @tag='028F'or @tag='028G'or @tag='028H'or @tag='028L'or @tag='028M']"> <!-- 300x -->
      <xsl:call-template name="personal" />
    </xsl:for-each>
  </xsl:template>
  
  <!-- Corporate -->
  <xsl:template match="pica:record" mode="corporate">
    <xsl:for-each select="pica:field[@tag='029A' or @tag='029F' or @tag='029G' or @tag='029E']"> <!-- 310X -->
      <xsl:call-template name="corporate" />
    </xsl:for-each>
  </xsl:template>

  <!-- releatedItem -->
  <xsl:template match="pica:record" mode="relatedItem">
    <xsl:param name="isParent" select="true()" />
    
    <!-- preceding -->
    <xsl:for-each select="pica:field[@tag='039E']"> <!-- 4244 -->
      <relatedItem type="preceding">
        <xsl:attribute name="displayLabel"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
        <xsl:if test="pica:subfield[@code='a' or @code='t']">
          <titleInfo>
            <title>
              <xsl:choose>
                <xsl:when test="pica:subfield[@code='t']">
                  <xsl:value-of select="pica:subfield[@code='t']" />
                </xsl:when>
                <xsl:when test="pica:subfield[@code='a']">
                  <xsl:value-of select="pica:subfield[@code='a']" />
                </xsl:when>
              </xsl:choose>
            </title>
          </titleInfo>
        </xsl:if>
      </relatedItem>
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='036D' or @tag='039B']"> <!-- 4160, 4241  übergeordnetes Werk-->
      <xsl:call-template name="hostOrSeries">
        <xsl:with-param name="type" select="'host'" />
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='036F']"> <!-- 4180  Schriftenreihe-->
      <xsl:call-template name="hostOrSeries">
        <xsl:with-param name="type" select="'series'" />
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='039Q']"> <!-- 4262 -->
      <xsl:call-template name="isReferencedBy" />
    </xsl:for-each>

    <xsl:for-each select="pica:field[@tag='039P']"> <!-- 4261  RezensiertesWerk-->
      <xsl:if test="$isParent">
        <xsl:call-template name="reviewOf" />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!-- ======================================= -->
  <!-- | named templates -->
  <!-- ======================================= -->
  
  <!-- Title -->
  <xsl:template name="title">
    <titleInfo>
      <xsl:if test="pica:subfield[@code='a']">
        <xsl:variable name="mainTitle" select="pica:subfield[@code='a']" />
        <xsl:choose>
          <xsl:when test="contains($mainTitle, '@')">
            <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
            <xsl:choose>
              <xsl:when test="string-length($nonSort) &lt; 9">
                <!-- Workaround for title with @ as first char -->
                <xsl:if test="string-length($nonSort) &gt; 0">
                  <nonSort>
                    <xsl:value-of select="$nonSort" />
                  </nonSort>
                </xsl:if>
                <title>
                  <xsl:value-of select="substring-after($mainTitle, '@')" />
                </title>
              </xsl:when>
              <xsl:otherwise>
                <title>
                  <xsl:value-of select="$mainTitle" />
                </title>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <title>
              <xsl:value-of select="$mainTitle" />
            </title>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='d']">
        <subTitle>
          <xsl:value-of select="pica:subfield[@code='d']" />
        </subTitle>
      </xsl:if>
      
      <!--  nur in fingierten Titel 036C / 4150 -->
      <xsl:if test="pica:subfield[@code='y']">
        <subTitle>
          <xsl:value-of select="pica:subfield[@code='y']" />
        </subTitle>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='l']">
        <partNumber>
          <xsl:value-of select="pica:subfield[@code='l']" />
        </partNumber>
      </xsl:if>

      <xsl:if test="pica:field[@tag='027D']">
        <partNumber>
          <xsl:value-of select="../pica:field[@tag='036F']/pica:subfield[@code='l']" />
        </partNumber>
      </xsl:if>

      <xsl:if test="pica:field[@tag='036C'] and ../pica:field[@tag='021A']">
        <partName>
          <xsl:value-of select="../pica:field[@tag='021A']/pica:subfield[@code='a']" />
        </partName>
      </xsl:if>
    </titleInfo>
    <xsl:if test="pica:subfield[@code='h']">
      <note type="creator_info">
        <xsl:value-of select="pica:subfield[@code='h']" />
      </note>
    </xsl:if>
  </xsl:template>

  <xsl:template name="identifier">
    <xsl:param name="ppn" />

    <xsl:if test="$ppn">
      <!-- read source record -->
      <xsl:variable name="aboveRecord" select="document(concat('notnull:opc:record=', $ppn, '&amp;isil=', $RecordIdSource))" />

      <xsl:for-each select="$aboveRecord//pica:field[@tag='007K']">
        <nameIdentifier type="{pica:subfield[@code='a']}">
          <xsl:value-of select="pica:subfield[@code='0']" />
        </nameIdentifier>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  <!-- Persons -->
  <xsl:template name="personal">
    <xsl:param name="marcrelatorCode" select="''" />

    <name type="personal">
      <xsl:if test="pica:subfield[@code='a']">
        <namePart type="family">
          <xsl:value-of select="pica:subfield[@code='a']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='d']">
        <namePart type="given">
          <xsl:value-of select="pica:subfield[@code='d']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='P']">
        <namePart>
          <xsl:value-of select="pica:subfield[@code='P']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='n']">
        <namePart type="termsOfAddress">
          <xsl:value-of select="pica:subfield[@code='n']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='l']">
        <namePart type="termsOfAddress">
          <xsl:value-of select="pica:subfield[@code='l']" />
        </namePart>
      </xsl:if>

      <xsl:if test="pica:subfield[@code='E'] or pica:subfield[@code='F']">
        <namePart type="date">
          <xsl:value-of select="pica:subfield[@code='E']" />
          <xsl:text>-</xsl:text>
          <xsl:value-of select="pica:subfield[@code='F']" />
        </namePart>
      </xsl:if>
      <xsl:choose>
        <!-- RDA -->
        <xsl:when test="pica:subfield[@code='4']">
          <role>
            <roleTerm type="code" authority="marcrelator">
              <xsl:value-of select="pica:subfield[@code='4']" />
            </roleTerm>
          </role>
          <xsl:if test="pica:subfield[@code='B']">
            <role>
              <roleTerm type="text">
                <xsl:value-of select="pica:subfield[@code='B']" />
              </roleTerm>
            </role>
          </xsl:if>
        </xsl:when>
        <xsl:when test="pica:subfield[@code='B']">
          <role>
            <roleTerm type="code" authority="marcrelator">
              <xsl:choose>
                <!-- RAK WB §185, 2 -->
                <xsl:when test="pica:subfield[@code='B']='Bearb.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Begr.'">
                  <xsl:text>org</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Ill.'">
                  <xsl:text>ill</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Komp.'">
                  <xsl:text>cmp</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Mitarb.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Red.'">
                  <xsl:text>red</xsl:text>
                </xsl:when>
                <!-- GBV Katalogisierungsrichtlinie -->
                <xsl:when test="pica:subfield[@code='B']='Adressat'">
                  <xsl:text>rcp</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='angebl. Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='mutmaßl. Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Komm.'">
                  <xsl:text>ann</xsl:text>
                </xsl:when><!-- Kommentator = annotator -->
                <xsl:when test="pica:subfield[@code='B']='Stecher'">
                  <xsl:text>egr</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='angebl. Übers.'">
                  <xsl:text>trl</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='mutmaßl. Übers.'">
                  <xsl:text>trl</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='angebl. Verf.'">
                  <xsl:text>dub</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='mutmaßl. Verf.'">
                  <xsl:text>dub</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Verstorb.'">
                  <xsl:text>oth</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Zeichner'">
                  <xsl:text>drm</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Präses'">
                  <xsl:text>pra</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Resp.'">
                  <xsl:text>rsp</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Widmungsempfänger'">
                  <xsl:text>dto</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Zensor'">
                  <xsl:text>cns</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Beiträger'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Beiträger k.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Beiträger m.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="pica:subfield[@code='B']='Interpr.'">
                  <xsl:text>prf</xsl:text>
                </xsl:when> <!-- Interpret = Performer-->
                <xsl:otherwise>
                  <xsl:text>oth</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </roleTerm>
            <roleTerm type="text" authority="gbv">
              <xsl:text>[</xsl:text>
              <xsl:value-of select="pica:subfield[@code='B']" />
              <xsl:text>]</xsl:text>
            </roleTerm>
          </role>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="string-length($marcrelatorCode) &gt; 0">
              <role>
                <roleTerm type="code" authority="marcrelator">
                  <xsl:value-of select="$marcrelatorCode" />
                </roleTerm>
              </role>
            </xsl:when>
            <xsl:otherwise>
              <role>
                <roleTerm type="code" authority="marcrelator">oth</roleTerm>
              </role>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="pica:subfield[@code='9']">
        <xsl:call-template name="identifier">
          <xsl:with-param name="ppn" select="pica:subfield[@code='9']" />
        </xsl:call-template>
      </xsl:if>
    </name>
  </xsl:template>
  
  <!-- Corporate -->
  <xsl:template name="corporate">
    <xsl:param name="marcrelatorCode" select="''" />

    <name type="corporate">
      <xsl:if test="pica:subfield[@code='a']|pica:subfield[@code='8']">
        <xsl:variable name="mainTitle" select="pica:subfield[@code='a']|pica:subfield[@code='8']" />
        <xsl:choose>
          <xsl:when test="contains($mainTitle, '@')">
            <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
            <xsl:choose>
              <xsl:when test="string-length($nonSort) &lt; 9">
                <namePart>
                  <xsl:value-of select="normalize-space(substring-before($mainTitle, '@'))" />
                  <xsl:value-of select="normalize-space(substring-after($mainTitle, '@'))" />
                </namePart>
              </xsl:when>
              <xsl:otherwise>
                <namePart>
                  <xsl:value-of select="$mainTitle" />
                </namePart>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="contains($mainTitle,' / ')">
            <displayForm>
              <xsl:value-of select="$mainTitle" />
            </displayForm>
            <namePart>
              <xsl:value-of select="normalize-space(substring-before($mainTitle, ' / '))" />
            </namePart>
            <namePart>
              <xsl:value-of select="normalize-space(substring-after($mainTitle, ' / '))" />
            </namePart>
          </xsl:when>
          <xsl:otherwise>
            <namePart>
              <xsl:value-of select="$mainTitle" />
            </namePart>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='b']">
        <namePart>
          <xsl:value-of select="pica:subfield[@code='b']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='d']">
        <namePart type="date">
          <xsl:value-of select="pica:subfield[@code='d']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='g']"> <!--  non-normative type "place" -->
        <namePart>
          <xsl:value-of select="pica:subfield[@code='g']" />
        </namePart>
      </xsl:if>
      <xsl:if test="pica:subfield[@code='c']"> <!--  non-normative type "place" -->
        <namePart>
          <xsl:value-of select="pica:subfield[@code='c']" />
        </namePart>
      </xsl:if>

      <xsl:if test="pica:subfield[@code='B']">
        <role>
          <roleTerm type="text" authority="gbv">
            <xsl:value-of select="pica:subfield[@code='B']" />
          </roleTerm>
        </role>
      </xsl:if>

      <xsl:if test="pica:subfield[@code='9']">
        <xsl:call-template name="identifier">
          <xsl:with-param name="ppn" select="pica:subfield[@code='9']" />
        </xsl:call-template>
      </xsl:if>
    </name>
  </xsl:template>

  <xsl:template name="hostOrSeries">
    <xsl:param name="type" />

    <relatedItem>
      <xsl:attribute name="type"><xsl:value-of select="$type" /></xsl:attribute>
      <xsl:if test="pica:subfield[@code='c']">
        <xsl:attribute name="displayLabel"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="pica:subfield[@code='9']">
          <xsl:variable name="aboveRecord" select="document(concat('notnull:opc:record=', pica:subfield[@code='9'], '&amp;isil=', $RecordIdSource))" />
          <xsl:apply-templates select="$aboveRecord/pica:record">
            <xsl:with-param name="isParent" select="false()" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="title" />
        </xsl:otherwise>
      </xsl:choose>

      <part>
        <!-- article -->
        <xsl:if test="pica:subfield[@code='x']">
          <xsl:attribute name="order"><xsl:value-of select="substring(pica:subfield[@code='x'],1,4)" /></xsl:attribute>
        </xsl:if>

        <xsl:if test="pica:subfield[@code='X']">
          <xsl:attribute name="order">
            <xsl:choose>
              <xsl:when test="contains(pica:subfield[@code='X'], ',')">
                <xsl:value-of select="substring-before(substring-before(pica:subfield[@code='X'], '.'), ',')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="substring-before(pica:subfield[@code='X'], '.')" />
              </xsl:otherwise>
            </xsl:choose>   
          </xsl:attribute>
        </xsl:if>
        <!-- ToDo:  type attribute: issue, volume, chapter, .... -->
        <xsl:if test="pica:subfield[@code='l']">
          <detail type="volume">
            <number>
              <xsl:value-of select="pica:subfield[@code='l']" />
            </number>
          </detail>
        </xsl:if>

        <!-- article -->
        <xsl:for-each select="../pica:field[@tag='031A']"> <!-- 4070 -->
          <!-- Volume -->
          <xsl:if test="pica:subfield[@code='d']">
            <detail type="volume">
              <number>
                <xsl:value-of select="pica:subfield[@code='d']" />
              </number>
            </detail>
          </xsl:if>
          <!-- Issue -->
          <xsl:if test="pica:subfield[@code='e']">
            <detail type="issue">
              <number>
                <xsl:value-of select="pica:subfield[@code='e']" />
              </number>
            </detail>
          </xsl:if>
                 
          <!-- Pages -->
          <xsl:if test="pica:subfield[@code='h' or @code='g']">
            <extent unit="pages">
              <xsl:if test="pica:subfield[@code='g']">
                <total>
                  <xsl:value-of select="pica:subfield[@code='g']" />
                </total>
              </xsl:if>
              <xsl:if test="pica:subfield[@code='h']">
                <xsl:if test="not (contains(pica:subfield[@code='h'], ','))">
                  <xsl:if test="not (contains(pica:subfield[@code='h'], '-'))">
                    <start>
                      <xsl:value-of select="pica:subfield[@code='h']" />
                    </start>
                  </xsl:if>
                  <xsl:if test="contains(pica:subfield[@code='h'], '-')">
                    <start>
                      <xsl:value-of select="substring-before(pica:subfield[@code='h'], '-')" />
                    </start>
                    <end>
                      <xsl:value-of select="substring-after(pica:subfield[@code='h'], '-')" />
                    </end>
                  </xsl:if>
                </xsl:if>
                <xsl:if test="contains(pica:subfield[@code='h'], ',')">
                  <list>
                    <xsl:value-of select="pica:subfield[@code='h']" />
                  </list>
                </xsl:if>
              </xsl:if>
            </extent>
          </xsl:if>
          
          <!-- Date -->
          <xsl:if test="pica:subfield[@code='j']">
            <date encoding="iso8601">
              <xsl:value-of select="substring(pica:subfield[@code='j'],1,4)" />
            </date>
          </xsl:if>
          <xsl:if test="pica:subfield[@code='y']">
            <text type="display">
              <xsl:value-of select="substring(pica:subfield[@code='y'],1,4)" />
            </text>
          </xsl:if>

          <xsl:for-each select="../pica:field[@tag='031C']"> <!-- 4072 -->
            <text type="article series">
              <xsl:value-of select="pica:subfield[@code='a']" />
            </text>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:if test="pica:subfield[@code='x']">
          <text type="sortstring">
            <xsl:value-of select="pica:subfield[@code='x']" />
          </text>
        </xsl:if>
      </part>
    </relatedItem>
  </xsl:template>
  
  <!-- isReferencedBy -->
  <xsl:template name="isReferencedBy">
    <relatedItem type="isReferencedBy">
      <xsl:attribute name="otherType"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
      <xsl:attribute name="displayLabel"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
      <xsl:choose>
        <xsl:when test="pica:subfield[@code='9']">
          <xsl:variable name="aboveRecord" select="document(concat('notnull:opc:record=', pica:subfield[@code='9'], '&amp;isil=', $RecordIdSource))" />
          <xsl:apply-templates select="$aboveRecord/pica:record">
            <xsl:with-param name="isParent" select="false()" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <titleInfo>
            <title>
              <xsl:value-of select="pica:subfield[@code='8']" />
            </title>
          </titleInfo>
        </xsl:otherwise>
      </xsl:choose>
    </relatedItem>
  </xsl:template>
  
  <!-- reviewOf -->
  <xsl:template name="reviewOf">
    <relatedItem type="reviewOf">
      <xsl:attribute name="otherType"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
      <xsl:attribute name="displayLabel"><xsl:value-of select="pica:subfield[@code='c']" /></xsl:attribute>
      <xsl:choose>
        <xsl:when test="pica:subfield[@code='9']">
          <xsl:variable name="aboveRecord" select="document(concat('notnull:opc:record=', pica:subfield[@code='9'], '&amp;isil=', $RecordIdSource))" />
          <xsl:apply-templates select="$aboveRecord/pica:record">
            <xsl:with-param name="isParent" select="false()" />
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <titleInfo>
            <title>
              <xsl:value-of select="pica:subfield[@code='8']" />
            </title>
          </titleInfo>
        </xsl:otherwise>
      </xsl:choose>
    </relatedItem>
  </xsl:template>
  
  <!-- subject - authority -->
  <xsl:template name="authorityGND">
    <xsl:param name="aboveRecord" />

    <xsl:if test="$aboveRecord//pica:field[@tag='007K']/pica:subfield[@code='a'] = 'gnd'">
      <xsl:attribute name="authority"><xsl:value-of select="'gnd'" /></xsl:attribute>
      <xsl:attribute name="authorityURI"><xsl:value-of select="'http://d-nb.info/gnd/'" /></xsl:attribute>
      <xsl:attribute name="valueURI"><xsl:value-of select="concat('http://d-nb.info/gnd/', $aboveRecord//pica:field[@tag='007K']/pica:subfield[@code='0'])" /></xsl:attribute>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>