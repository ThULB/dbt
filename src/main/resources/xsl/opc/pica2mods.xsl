<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="pica"
>
  <xsl:output method="xml" indent="yes" />
  
  <!-- include custom templates for matching pica fields to classification entries -->
  <xsl:include href="xslInclude:pica2class" />

  <!-- http://www.oclc.org/contacts/libraries.en.html -->
  <!-- http://sigel.staatsbibliothek-berlin.de/suche/ -->
  <xsl:param name="RecordIdSource" select="'DEUBI'" />
  <xsl:param name="RecordIdPrefix" select="'dbt/ppn:'" />

  <xsl:param name="PURLPrefix" select="''" />

  <xsl:template match="pica:record">
    <mods:mods xmlns:mods="http://www.loc.gov/mods/v3" version="3.5">
      <mods:recordInfo>
        <mods:recordIdentifier source="{$RecordIdSource}">
          <xsl:value-of select="concat($RecordIdPrefix, ./pica:field[@tag='003@']/pica:subfield[@code='0'])" />
        </mods:recordIdentifier>
      </mods:recordInfo>
      <xsl:for-each select="./pica:field[@tag='009P' and @occurrence='03']"> <!-- 4083 (kein eigenes Feld) -->
        <xsl:if test="contains(./pica:subfield[@code='a'], '//purl.')">
          <mods:identifier type="purl">
            <xsl:value-of select="./pica:subfield[@code='a']" />
          </mods:identifier>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='003@']"> <!--  0100 -->
        <mods:identifier type="PPN">
          <xsl:value-of select="./pica:subfield[@code='0']" />
        </mods:identifier>
      </xsl:for-each>
      <xsl:for-each select="./pica:field[@tag='004U']"> <!-- 2050 -->
        <mods:identifier type="urn">
          <xsl:value-of select="./pica:subfield[@code='0']" />
        </mods:identifier>
      </xsl:for-each>
      <xsl:for-each select="./pica:field[@tag='004V']"> <!-- 2051 -->
        <mods:identifier type="doi">
          <xsl:value-of select="./pica:subfield[@code='0']" />
        </mods:identifier>
      </xsl:for-each>
      <xsl:for-each select="./pica:field[@tag='004R']"> <!-- 2052 -->
        <mods:identifier type="hdl">
          <xsl:value-of select="./pica:subfield[@code='0']" />
        </mods:identifier>
      </xsl:for-each>
      <xsl:for-each select="./pica:field[@tag='006Z']"> <!--  2110 -->
        <mods:identifier type="zdb">
          <xsl:value-of select="./pica:subfield[@code='0']" />
        </mods:identifier>
      </xsl:for-each>
      <xsl:for-each select="./pica:field[@tag='007S']"><!-- 2277 -->
        <xsl:if test="starts-with(./pica:subfield[@code='0'], 'RISM')">
          <mods:identifier type="rism">
            <xsl:value-of select="normalize-space(substring-after(./pica:subfield[@code='0'], 'RISM'))" />
          </mods:identifier>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='028A' or @tag='028B']"> <!-- 300x -->
        <xsl:call-template name="PersonalName">
          <xsl:with-param name="marcrelatorCode">
            <xsl:text>aut</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='028C' or @tag='028D' or @tag='028E'or @tag='028F'or @tag='028G'or @tag='028H'or @tag='028L'or @tag='028M']"> <!-- 300x -->
        <xsl:call-template name="PersonalName" />
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='029A' or @tag='029F' or @tag='029G' or @tag='029E']"> <!-- 310X -->
        <xsl:call-template name="CorporateName">
        </xsl:call-template>
      </xsl:for-each>

      <xsl:choose>
        <xsl:when
          test="substring(./pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='f' or substring(./pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='F' "
        >
          <xsl:for-each select="./pica:field[@tag='036C']"><!-- 4150 -->
            <xsl:call-template name="Title" />
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="substring(./pica:field[@tag='002@']/pica:subfield[@code='0'],2,1)='v' and ./pica:field[@tag='027D']">
          <xsl:for-each select="./pica:field[@tag='027D']"><!-- 3290 -->
            <xsl:call-template name="Title" />
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="./pica:field[@tag='021A']"> <!--  4000 -->
            <xsl:call-template name="Title" />
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
      
      <!--  Titel fingiert, wenn kein Titel in 4000 -->

      <xsl:for-each select="./pica:field[@tag='010@']"> <!-- 1500 Language -->
      <!-- weiter Unterfelder für Orginaltext / Zwischenübersetzung nicht abbildbar -->
        <xsl:for-each select="./pica:subfield[@code='a']">
          <mods:language>
            <mods:languageTerm type="code" authority="iso639-2b">
              <xsl:value-of select="." />
            </mods:languageTerm>
          </mods:language>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='039B']"> <!-- 4241  übergeordnetes Werk-->
        <xsl:call-template name="ArticleParent" />
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='036D']"> <!-- 4160  übergeordnetes Werk-->
        <xsl:call-template name="HostOrSeries">
          <xsl:with-param name="type">
            <xsl:text>host</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='036F']"> <!-- 4180  Schriftenreihe-->
        <xsl:call-template name="HostOrSeries">
          <xsl:with-param name="type">
            <xsl:text>series</xsl:text>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='039P']"> <!-- 4261  RezensiertesWerk-->
        <xsl:call-template name="Review" />
      </xsl:for-each>

      <!--033J =  4033 Druckernormadaten, aber kein Ort angegeben (müsste aus GND gelesen werden)
      MODS unterstützt keine authorityURIs für Verlage
      deshalb 033A verwenden -->

      <!-- check use of eventtype attribute -->
      <mods:originInfo eventType="creation">
        <xsl:for-each select="./pica:field[@tag='033A']">
          <xsl:if test="./pica:subfield[@code='n']">  <!-- 4030 Ort, Verlag -->
            <mods:publisher>
              <xsl:value-of select="./pica:subfield[@code='n']" />
            </mods:publisher>
          </xsl:if>
          <xsl:for-each select="./pica:subfield[@code='p']">
            <mods:place>
              <mods:placeTerm type="text">
                <xsl:value-of select="." />
              </mods:placeTerm>
            </mods:place>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="./pica:field[@tag='011@']">
          <xsl:choose>
            <xsl:when test="./pica:subfield[@code='b']">
              <mods:dateIssued keyDate="yes" encoding="iso8601" point="start">
                <xsl:value-of select="./pica:subfield[@code='a']" />
              </mods:dateIssued>
              <mods:dateIssued encoding="iso8601" point="end">
                <xsl:value-of select="./pica:subfield[@code='b']" />
              </mods:dateIssued>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="contains(./pica:subfield[@code='a'], 'X')">
                  <mods:dateCreated keyDate="yes" encoding="iso8601" point="start">
                    <xsl:value-of select="translate(./pica:subfield[@code='a'], 'X','0')" />
                  </mods:dateCreated>
                  <mods:dateCreated encoding="iso8601" point="end">
                    <xsl:value-of select="translate(./pica:subfield[@code='a'], 'X', '9')" />
                  </mods:dateCreated>
                  <mods:dateCreated qualifier="approximate">
                    <xsl:value-of select="./pica:subfield[@code='a']"></xsl:value-of>
                    <xsl:if test="./pica:subfield[@code='n']">
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="./pica:subfield[@code='n']" />
                    </xsl:if>
                  </mods:dateCreated>
                </xsl:when>
                <xsl:otherwise>
                  <mods:dateIssued keyDate="yes" encoding="iso8601">
                    <xsl:value-of select="./pica:subfield[@code='a']" />
                  </mods:dateIssued>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="./pica:field[@tag='032@']"> <!-- 4020 Ausgabe-->
          <xsl:choose>
            <xsl:when test="./pica:subfield[@code='c']">
              <mods:edition>
                <xsl:value-of select="./pica:subfield[@code='a']" />
                <xsl:text>/</xsl:text>
                <xsl:value-of select="./pica:subfield[@code='c']" />
              </mods:edition>
            </xsl:when>
            <xsl:otherwise>
              <mods:edition>
                <xsl:value-of select="./pica:subfield[@code='a']" />
              </mods:edition>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>

        <xsl:for-each select="./pica:field[@tag='002@']">
          <xsl:choose>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='a'">
              <mods:issuance>monographic</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='b'">
              <mods:issuance>serial</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='c'">
              <mods:issuance>multipart monograph</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='d'">
              <mods:issuance>serial</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='f'">
              <mods:issuance>monographic</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='F'">
              <mods:issuance>monographic</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='j'">
              <mods:issuance>single unit</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='s'">
              <mods:issuance>single unit</mods:issuance>
            </xsl:when>
            <xsl:when test="substring(./pica:subfield[@code='0'],2,1)='v'">
              <mods:issuance>monographic</mods:issuance>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="./pica:field[@tag='031@']/pica:subfield[@code='a']">
          <mods:frequency>
            <xsl:value-of select="." />
          </mods:frequency>
        </xsl:for-each>

        <!-- normierte Orte -->
        <xsl:for-each select="./pica:field[@tag='033B' and @occurrence='03']/pica:subfield[@code='p']">
          <mods:place supplied="yes">
            <mods:placeTerm lang="ger" type="text">
              <xsl:value-of select="." />
            </mods:placeTerm>
          </mods:place>
        </xsl:for-each>

      </mods:originInfo>
      
      <!--033J =  4033 Druckernormadaten -->
      <xsl:for-each select="./pica:field[@tag='033J']">
        <mods:note type="publisher_authority">
          <xsl:attribute name="xlink:href">http://d-nb.info/<xsl:value-of select="./pica:subfield[@code='0']" /></xsl:attribute>
          <xsl:value-of select="./pica:subfield[@code='a']"></xsl:value-of>
          <xsl:if test="./pica:subfield[@code='d']">
            <xsl:text>, </xsl:text>
            <xsl:value-of select="./pica:subfield[@code='d']"></xsl:value-of>
          </xsl:if>
        </mods:note>
      </xsl:for-each>

      <!-- TODO check MAT code -->
      <xsl:if test="starts-with(./pica:field[@tag='002@']/pica:subfield[@code='0'], 'O')">
        <mods:originInfo eventType="online_publication"> <!-- 4031 -->
          <xsl:if test="./pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='n']">  <!-- 4030 Ort, Verlag -->
            <mods:publisher>
              <xsl:value-of select="./pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='n']" />
            </mods:publisher>
          </xsl:if>
          <xsl:if test="./pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='p']">  <!-- 4030 Ort, Verlag -->
            <mods:place>
              <mods:placeTerm type="text">
                <xsl:value-of select="./pica:field[@tag='033B' and @occurrence='01']/pica:subfield[@code='p']" />
              </mods:placeTerm>
            </mods:place>
          </xsl:if>
          <mods:edition>[Electronic ed.]</mods:edition>

          <xsl:for-each select="./pica:field[@tag='011B']">   <!-- 1109 -->
            <xsl:choose>
              <xsl:when test="./pica:subfield[@code='b']">
                <mods:dateCaptured encoding="iso8601" point="start">
                  <xsl:value-of select="./pica:subfield[@code='a']" />
                </mods:dateCaptured>
                <mods:dateCaptured encoding="iso8601" point="end">
                  <xsl:value-of select="./pica:subfield[@code='b']" />
                </mods:dateCaptured>
              </xsl:when>
              <xsl:otherwise>
                <mods:dateCaptured encoding="iso8601">
                  <xsl:value-of select="./pica:subfield[@code='a']" />
                </mods:dateCaptured>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>

        </mods:originInfo>
      </xsl:if>

      <xsl:for-each select="./pica:field[@tag='009A']"> <!-- 4065 Besitznachweis der Vorlage-->
        <mods:location>
          <xsl:if test="./pica:subfield[@code='c']">
            <xsl:choose>
              <xsl:when test="./pica:subfield[@code='c']='UB Rostock'">
                <mods:physicalLocation type="current" authorityURI="http://d-nb.info/gnd/" valueURI="http://d-nb.info/gnd/25968-8">Universitätsbibliothek Rostock
                </mods:physicalLocation>
              </xsl:when>
              <xsl:otherwise>
                <mods:physicalLocation type="current">
                  <xsl:value-of select="./pica:subfield[@code='c']" />
                </mods:physicalLocation>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
          <xsl:if test="./pica:subfield[@code='a']">
            <mods:shelfLocator>
              <xsl:value-of select="./pica:subfield[@code='a']" />
            </mods:shelfLocator>
          </xsl:if>
        </mods:location>
      </xsl:for-each>

      <xsl:variable name="digitalOrigin">
        <xsl:for-each select="./pica:field[@tag='037H']/pica:subfield[@code='a']">   <!-- 4238 Technische Angaben zum elektr. Dokument  -->
          <xsl:if test="contains(., 'Digitalisierungsvorlage: Original')">
            <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
          </xsl:if>
          <xsl:if test="contains(., 'Digitalisierungsvorlage: Mikrofilm')">
            <mods:digitalOrigin>digitized microfilm</mods:digitalOrigin>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="$digitalOrigin or ./pica:field[@tag='034D'  or @tag='034M' or @tag='034I' or @tag='034K']">
        <mods:physicalDescription>
          <xsl:for-each select="./pica:field[@tag='034D']/pica:subfield[@code='a']">   <!--  4060 Umfang, Seiten -->
            <mods:extent>
              <xsl:value-of select="." />
            </mods:extent>
          </xsl:for-each>
          <xsl:for-each select="./pica:field[@tag='034M']/pica:subfield[@code='a']">   <!--  4061 Illustrationen -->
            <mods:extent>
              <xsl:value-of select="." />
            </mods:extent>
          </xsl:for-each>
          <xsl:for-each select="./pica:field[@tag='034I']/pica:subfield[@code='a']">   <!-- 4062 Format, Größe  -->
            <mods:extent>
              <xsl:value-of select="." />
            </mods:extent>
          </xsl:for-each>
          <xsl:for-each select="./pica:field[@tag='034K']/pica:subfield[@code='a']">   <!-- 4063 Begleitmaterial  -->
            <mods:extent>
              <xsl:value-of select="." />
            </mods:extent>
          </xsl:for-each>
          <xsl:copy-of select="$digitalOrigin" />
        </mods:physicalDescription>
      </xsl:if>


      <xsl:for-each select="./pica:field[@tag='009P' and contains(./pica:subfield[@code='a'], 'rosdok')][1]">
        <mods:location>
          <mods:physicalLocation type="online" authorityURI="http://d-nb.info/gnd/" valueURI="http://d-nb.info/gnd/25968-8">Universitätsbibliothek Rostock
          </mods:physicalLocation>
          <mods:url usage="primary" access="object in context">
            <xsl:value-of select="./pica:subfield[@code='a']" />
          </mods:url>
        </mods:location>
      </xsl:for-each>



      <xsl:for-each select="./pica:field[@tag='044S']"> <!-- 5570 Gattungsbegriffe AAD -->
        <mods:genre authority="aadgenres">
          <xsl:value-of select="./pica:subfield[@code='a']" />
        </mods:genre>
      </xsl:for-each>

      <xsl:call-template name="Class_DocType" />

      <xsl:for-each select="./pica:field[@tag='009P' and @occurrence='09']">
        <mods:note>
          <xsl:attribute name="xlink:href"><xsl:value-of select="./pica:subfield[@code='a']" /></xsl:attribute>
          <xsl:value-of select="./pica:subfield[@code='y']" />
        </mods:note>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='007S']"><!-- 2277 -->
        <xsl:if
          test="not(starts-with(./pica:subfield[@code='0'], 'VD 16')) and not(starts-with(./pica:subfield[@code='0'], 'VD16')) and not(starts-with(./pica:subfield[@code='0'], 'VD17')) and not(starts-with(./pica:subfield[@code='0'], 'RISM')) and not(./pica:subfield[@code='S']='e')"
        >
          <mods:note type="bibliographic_reference">
            <xsl:value-of select="./pica:subfield[@code='0']" />
          </mods:note>
        </xsl:if>
      </xsl:for-each>


      <xsl:for-each select="./pica:field[@tag='037A' or @tag='037B' or @tag='046L' or @tag='046F' or @tag='046G' or @tag='046H' or @tag='046I']"><!-- 4201, 4202, 4221, 4215, 4216, 4217, 4218 -->
        <mods:note type="other">
          <xsl:value-of select="./pica:subfield[@code='a']" />
        </mods:note>
      </xsl:for-each>

      <xsl:for-each select="./pica:field[@tag='047C']"><!-- 4200 -->
        <mods:note type="titlewordindex">
          <xsl:value-of select="./pica:subfield[@code='a']" />
        </mods:note>
      </xsl:for-each>
      
	<!-- 
      <mods:extension displayLabel="picaxml">
        <xsl:copy-of select="." />
      </mods:extension>
	-->
    </mods:mods>
  </xsl:template>
  <xsl:template name="HostOrSeries">
    <xsl:param name="type" />
    <mods:relatedItem>
      <!--  ToDo teilweise redundant mit title template -->
      <xsl:attribute name="type"><xsl:value-of select="$type" /></xsl:attribute>
      <mods:titleInfo>
        <xsl:if test="./pica:subfield[@code='a']">
          <xsl:variable name="mainTitle" select="./pica:subfield[@code='a']" />
          <xsl:choose>
            <xsl:when test="contains($mainTitle, '@')">
              <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
              <xsl:choose>
                <xsl:when test="string-length(nonSort) &lt; 9">
                  <mods:nonSort>
                    <xsl:value-of select="$nonSort" />
                  </mods:nonSort>
                  <mods:title>
                    <xsl:value-of select="substring-after($mainTitle, '@')" />
                  </mods:title>
                </xsl:when>
                <xsl:otherwise>
                  <mods:title>
                    <xsl:value-of select="$mainTitle" />
                  </mods:title>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <mods:title>
                <xsl:value-of select="$mainTitle" />
              </mods:title>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </mods:titleInfo>
      <xsl:if test="./pica:subfield[@code='9']">
        <xsl:if test="not($type = 'series')">
          <mods:recordInfo>
            <mods:recordIdentifier source="{$RecordIdSource}">
              <xsl:value-of select="concat($RecordIdPrefix, pica:subfield[@code='0'])" />
            </mods:recordIdentifier>
          </mods:recordInfo>
          <xsl:if test="string-length($PURLPrefix) &gt; 0">
            <mods:identifier type="purl">
              <xsl:value-of select="concat($PURLPrefix, ./pica:subfield[@code='9'])" />
            </mods:identifier>
          </xsl:if>
        </xsl:if>
        <xsl:if test="$type = 'series'">
          <mods:identifier type="gvk:ppn">
            <xsl:value-of select="./pica:subfield[@code='9']" />
          </mods:identifier>
        </xsl:if>
      </xsl:if>

      <mods:part>
        <xsl:if test="./pica:subfield[@code='X']">
          <xsl:attribute name="order">
            <xsl:choose>
              <xsl:when test="contains(./pica:subfield[@code='X'], ',')">
                <xsl:value-of select="substring-before(substring-before(./pica:subfield[@code='X'], '.'), ',')" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="substring-before(./pica:subfield[@code='X'], '.')" />
              </xsl:otherwise>
            </xsl:choose>   
          </xsl:attribute>
        </xsl:if>
        <!-- ToDo:  type attribute: issue, volume, chapter, .... -->
        <xsl:if test="./pica:subfield[@code='l']">
          <mods:detail type="volume">
            <mods:number>
              <xsl:value-of select="./pica:subfield[@code='l']" />
            </mods:number>
          </mods:detail>
        </xsl:if>
        <xsl:if test="./pica:subfield[@code='x']">
          <mods:text type="sortstring">
            <xsl:value-of select="./pica:subfield[@code='x']" />
          </mods:text>
        </xsl:if>
      </mods:part>

    </mods:relatedItem>
  </xsl:template>

  <xsl:template name="ArticleParent">
    <mods:relatedItem>
      <!--  ToDo teilweise redundant mit title template -->
      <xsl:attribute name="type">host</xsl:attribute>
      <xsl:attribute name="displayLabel">appears_in</xsl:attribute>
      <mods:titleInfo>
        <xsl:if test="./pica:subfield[@code='a']">
          <xsl:variable name="mainTitle" select="./pica:subfield[@code='a']" />
          <xsl:choose>
            <xsl:when test="contains($mainTitle, '@')">
              <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
              <xsl:choose>
                <xsl:when test="string-length(nonSort) &lt; 9">
                  <mods:nonSort>
                    <xsl:value-of select="$nonSort" />
                  </mods:nonSort>
                  <mods:title>
                    <xsl:value-of select="substring-after($mainTitle, '@')" />
                  </mods:title>
                </xsl:when>
                <xsl:otherwise>
                  <mods:title>
                    <xsl:value-of select="$mainTitle" />
                  </mods:title>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <mods:title>
                <xsl:value-of select="$mainTitle" />
              </mods:title>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
        <xsl:if test="./pica:subfield[@code='d']">
          <mods:subTitle>
            <xsl:value-of select="./pica:subfield[@code='d']" />
          </mods:subTitle>
        </xsl:if>


      </mods:titleInfo>

      <mods:part>
        <xsl:if test="./pica:subfield[@code='x']">
          <xsl:attribute name="order">
                    <xsl:value-of select="substring(./pica:subfield[@code='x'],1,4)" />   
                 </xsl:attribute>
        </xsl:if>
        <xsl:if test="./pica:subfield[@code='x']">
          <mods:text type="sortstring">
            <xsl:value-of select="./pica:subfield[@code='x']" />
          </mods:text>
        </xsl:if>
        <xsl:for-each select="./../pica:field[@tag='031A']"> <!-- 4070 -->
                 <!-- Volume -->
          <xsl:if test="./pica:subfield[@code='d']">
            <mods:detail type="volume">
              <mods:number>
                <xsl:value-of select="./pica:subfield[@code='d']" />
              </mods:number>
            </mods:detail>
          </xsl:if>
                 <!-- Issue -->
          <xsl:if test="./pica:subfield[@code='e']">
            <mods:detail type="issue">
              <mods:number>
                <xsl:value-of select="./pica:subfield[@code='e']" />
              </mods:number>
            </mods:detail>
          </xsl:if>
                 
                 
                 
                  <!-- Seitenzahlen zu Pica to MODS -->
          <xsl:if test="./pica:subfield[@code='h' or @code='g']">
            <mods:extent unit="page">
              <xsl:if test="./pica:subfield[@code='g']">
                <mods:total>
                  <xsl:value-of select="./pica:subfield[@code='g']" />
                </mods:total>
              </xsl:if>
              <xsl:if test="./pica:subfield[@code='h']">
                <xsl:if test="not (contains(./pica:subfield[@code='h'], ','))">
                  <xsl:if test="not (contains(./pica:subfield[@code='h'], '-'))">
                    <mods:start>
                      <xsl:value-of select="./pica:subfield[@code='h']" />
                    </mods:start>
                  </xsl:if>
                  <xsl:if test="contains(./pica:subfield[@code='h'], '-')">
                    <mods:start>
                      <xsl:value-of select="substring-before(./pica:subfield[@code='h'], '-')" />
                    </mods:start>
                    <mods:end>
                      <xsl:value-of select="substring-after(./pica:subfield[@code='h'], '-')" />
                    </mods:end>
                  </xsl:if>
                </xsl:if>
                <xsl:if test="contains(./pica:subfield[@code='h'], ',')">
                  <mods:list>
                    <xsl:value-of select="./pica:subfield[@code='h']" />
                  </mods:list>
                </xsl:if>
              </xsl:if>
            </mods:extent>
          </xsl:if>
                 <!-- Date -->
          <xsl:if test="./pica:subfield[@code='j']">
            <mods:date encoding="iso8601">
              <xsl:value-of select="substring(./pica:subfield[@code='j'],1,4)" />
            </mods:date>
          </xsl:if>
          <xsl:if test="./pica:subfield[@code='y']">
            <mods:text type="display">
              <xsl:value-of select="substring(./pica:subfield[@code='y'],1,4)" />
            </mods:text>
          </xsl:if>

          <xsl:for-each select="./../pica:field[@tag='031C']"> <!-- 4072 -->
            <mods:text type="article series">
              <xsl:value-of select="./pica:subfield[@code='a']" />
            </mods:text>
          </xsl:for-each>
        </xsl:for-each>
      </mods:part>

    </mods:relatedItem>
  </xsl:template>

  <xsl:template name="Review">
    <mods:relatedItem type="reviewOf">
      <mods:titleInfo>
        <xsl:if test="./pica:subfield[@code='a']">
          <xsl:variable name="mainTitle" select="./pica:subfield[@code='a']" />
          <xsl:choose>
            <xsl:when test="contains($mainTitle, '@')">
              <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
              <xsl:choose>
                <xsl:when test="string-length(nonSort) &lt; 9">
                  <mods:nonSort>
                    <xsl:value-of select="$nonSort" />
                  </mods:nonSort>
                  <mods:title>
                    <xsl:value-of select="substring-after($mainTitle, '@')" />
                  </mods:title>
                </xsl:when>
                <xsl:otherwise>
                  <mods:title>
                    <xsl:value-of select="$mainTitle" />
                  </mods:title>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <mods:title>
                <xsl:value-of select="$mainTitle" />
              </mods:title>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </mods:titleInfo>
      <mods:identifier type="PPN">
        <xsl:value-of select="./pica:subfield[@code='9']" />
      </mods:identifier>

    </mods:relatedItem>
  </xsl:template>

  <xsl:template name="Title">
    <mods:titleInfo>
      <xsl:if test="./pica:subfield[@code='a']">
        <xsl:variable name="mainTitle" select="./pica:subfield[@code='a']" />
        <xsl:choose>
          <xsl:when test="contains($mainTitle, '@')">
            <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
            <xsl:choose>
              <xsl:when test="string-length(nonSort) &lt; 9">
                <mods:nonSort>
                  <xsl:value-of select="$nonSort" />
                </mods:nonSort>
                <mods:title>
                  <xsl:value-of select="substring-after($mainTitle, '@')" />
                </mods:title>
              </xsl:when>
              <xsl:otherwise>
                <mods:title>
                  <xsl:value-of select="$mainTitle" />
                </mods:title>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <mods:title>
              <xsl:value-of select="$mainTitle" />
            </mods:title>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='d']">
        <mods:subTitle>
          <xsl:value-of select="./pica:subfield[@code='d']" />
        </mods:subTitle>
      </xsl:if>
      
      <!--  nur in fingierten Titel 036C / 4150 -->
      <xsl:if test="./pica:subfield[@code='y']">
        <mods:subTitle>
          <xsl:value-of select="./pica:subfield[@code='y']" />
        </mods:subTitle>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='l']">
        <mods:partNumber>
          <xsl:value-of select="./pica:subfield[@code='l']" />
        </mods:partNumber>
      </xsl:if>

      <xsl:if test="@tag='027D'">
        <mods:partNumber>
          <xsl:value-of select="./../pica:field[@tag='036F']/pica:subfield[@code='l']" />
        </mods:partNumber>
      </xsl:if>

      <xsl:if test="@tag='036C' and ./../pica:field[@tag='021A']">
        <mods:partName>
          <xsl:value-of select="./../pica:field[@tag='021A']/pica:subfield[@code='a']" />
        </mods:partName>
      </xsl:if>
    </mods:titleInfo>
    <xsl:if test="./pica:subfield[@code='h']">
      <mods:note type="creator_info">
        <xsl:value-of select="./pica:subfield[@code='h']" />
      </mods:note>
    </xsl:if>
  </xsl:template>

  <xsl:template name="PersonalName">
    <xsl:param name="marcrelatorCode" />
    <mods:name type="personal">
      <xsl:if test="./pica:subfield[@code='0']">
        <xsl:attribute name="authority">gnd</xsl:attribute>
        <xsl:attribute name="authorityURI">http://d-nb.info/gnd/</xsl:attribute>
        <xsl:attribute name="valueURI">http://d-nb.info/<xsl:value-of select="./pica:subfield[@code='0']" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='a']">
        <mods:namePart type="family">
          <xsl:value-of select="./pica:subfield[@code='a']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='d']">
        <mods:namePart type="given">
          <xsl:value-of select="./pica:subfield[@code='d']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='P']">
        <mods:namePart>
          <xsl:value-of select="./pica:subfield[@code='P']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='n']">
        <mods:namePart type="termsOfAddress">
          <xsl:value-of select="./pica:subfield[@code='n']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='l']">
        <mods:namePart type="termsOfAddress">
          <xsl:value-of select="./pica:subfield[@code='l']" />
        </mods:namePart>
      </xsl:if>

      <xsl:if test="./pica:subfield[@code='E'] or ./pica:subfield[@code='F']">
        <mods:namePart type="date">
          <xsl:value-of select="./pica:subfield[@code='E']" />
          <xsl:text>-</xsl:text>
          <xsl:value-of select="./pica:subfield[@code='F']" />
        </mods:namePart>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="./pica:subfield[@code='B']">
          <mods:role>
            <mods:roleTerm type="code" authority="marcrelator">
              <xsl:choose> 
                <!-- RAK WB §185, 2 -->
                <xsl:when test="./pica:subfield[@code='B']='Bearb.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Begr.'">
                  <xsl:text>org</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Ill.'">
                  <xsl:text>ill</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Komp.'">
                  <xsl:text>cmp</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Mitarb.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Red.'">
                  <xsl:text>red</xsl:text>
                </xsl:when>
                <!-- GBV Katalogisierungsrichtlinie -->
                <xsl:when test="./pica:subfield[@code='B']='Adressat'">
                  <xsl:text>rcp</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='angebl. Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='mutmaßl. Hrsg.'">
                  <xsl:text>edt</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Komm.'">
                  <xsl:text>ann</xsl:text>
                </xsl:when><!-- Kommentator = annotator -->
                <xsl:when test="./pica:subfield[@code='B']='Stecher'">
                  <xsl:text>egr</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='angebl. Übers.'">
                  <xsl:text>trl</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='mutmaßl. Übers.'">
                  <xsl:text>trl</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='angebl. Verf.'">
                  <xsl:text>dub</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='mutmaßl. Verf.'">
                  <xsl:text>dub</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Verstorb.'">
                  <xsl:text>oth</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Zeichner'">
                  <xsl:text>drm</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Präses'">
                  <xsl:text>pra</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Resp.'">
                  <xsl:text>rsp</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Widmungsempfänger'">
                  <xsl:text>dto</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Zensor'">
                  <xsl:text>cns</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Beiträger'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Beiträger k.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Beiträger m.'">
                  <xsl:text>ctb</xsl:text>
                </xsl:when>
                <xsl:when test="./pica:subfield[@code='B']='Interpr.'">
                  <xsl:text>prf</xsl:text>
                </xsl:when> <!-- Interpret = Performer-->
                <xsl:otherwise>
                  <xsl:text>oth</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </mods:roleTerm>
            <mods:roleTerm type="text" authority="gbv">
              <xsl:text>[</xsl:text>
              <xsl:value-of select="./pica:subfield[@code='B']" />
              <xsl:text>]</xsl:text>
            </mods:roleTerm>
          </mods:role>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$marcrelatorCode">
              <mods:role>
                <mods:roleTerm type="code" authority="marcrelator">
                  <xsl:value-of select="$marcrelatorCode" />
                </mods:roleTerm>
              </mods:role>
            </xsl:when>
            <xsl:otherwise>
              <mods:role>
                <mods:roleTerm type="code" authority="marcrelator">oth</mods:roleTerm>
              </mods:role>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </mods:name>
  </xsl:template>
  <xsl:template name="CorporateName">
    <xsl:param name="marcrelatorCode" />
    <mods:name type="corporate">
      <xsl:if test="./pica:subfield[@code='0']">
        <xsl:attribute name="authority">gnd</xsl:attribute>
        <xsl:attribute name="authorityURI">http://d-nb.info/gnd/</xsl:attribute>
        <xsl:attribute name="valueURI">http://d-nb.info/<xsl:value-of select="./pica:subfield[@code='0']" /></xsl:attribute>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='a']">
        <xsl:variable name="mainTitle" select="./pica:subfield[@code='a']" />
        <xsl:choose>
          <xsl:when test="contains($mainTitle, '@')">
            <xsl:variable name="nonSort" select="normalize-space(substring-before($mainTitle, '@'))" />
            <xsl:choose>
              <xsl:when test="string-length(nonSort) &lt; 9">
                <mods:namePart>
                  <xsl:value-of select="normalize-space(substring-before($mainTitle, '@'))" />
                  <xsl:value-of select="normalize-space(substring-after($mainTitle, '@'))" />
                </mods:namePart>
              </xsl:when>
              <xsl:otherwise>
                <mods:namePart>
                  <xsl:value-of select="$mainTitle" />
                </mods:namePart>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <mods:namePart>
              <xsl:value-of select="$mainTitle" />
            </mods:namePart>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='b']">
        <mods:namePart>
          <xsl:value-of select="./pica:subfield[@code='b']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='d']">
        <mods:namePart type="date">
          <xsl:value-of select="./pica:subfield[@code='d']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='g']"> <!--  non-normative type "place" -->
        <mods:namePart>
          <xsl:value-of select="./pica:subfield[@code='g']" />
        </mods:namePart>
      </xsl:if>
      <xsl:if test="./pica:subfield[@code='c']"> <!--  non-normative type "place" -->
        <mods:namePart>
          <xsl:value-of select="./pica:subfield[@code='c']" />
        </mods:namePart>
      </xsl:if>

      <xsl:if test="./pica:subfield[@code='B']">
        <mods:role>
          <mods:roleTerm type="text" authority="gbv">
            <xsl:value-of select="./pica:subfield[@code='B']" />
          </mods:roleTerm>
        </mods:role>
      </xsl:if>
    </mods:name>
  </xsl:template>

  <xsl:template name="Class_DocType">
    <!--
      Overwrite with own implementation!
      
      Example:
       
      <xsl:element name="mods:classification">
        <xsl:attribute name="authorityURI">http://rosdok.uni-rostock.de/classifications/rosdok_class_doctypes</xsl:attribute>
        <xsl:attribute name="valueURI">http://rosdok.uni-rostock.de/classifications/rosdok_class_doctypes#<xsl:value-of select="./@ID" /></xsl:attribute>
      </xsl:element>
    -->
  </xsl:template>

</xsl:stylesheet> 