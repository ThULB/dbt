<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:mcri18n="http://www.mycore.de/xslt/i18n" xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:FilenameUtils="xalan://org.apache.commons.io.FilenameUtils"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:iview2="xalan://org.mycore.iview2.frontend.MCRIView2XSLFunctions"
                xmlns:media="xalan://org.mycore.media.frontend.MCRXMLFunctions"
                xmlns:menc="xalan://de.urmel_dl.dbt.media.MediaService"
                xmlns:mcrsolr="xalan://org.mycore.solr.MCRXMLFunctions"
                xmlns:mcrsolru="xalan://org.mycore.solr.MCRSolrUtils"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="xalan mcri18n mcr media mods xlink FilenameUtils iview2 mcrxsl mcrsolr mcrsolru menc fn"
>
  <xsl:import href="xslImport:modsmeta:metadata/mir-video.js.xsl" />
  <xsl:param name="UserAgent" />

  <xsl:template match="/">
    <xsl:variable name="encDerivates">
      <xsl:for-each select="//derobject">
        <xsl:variable name="derId" select="@xlink:href" />
        <der id="{$derId}">
          <xsl:for-each select="document(concat('notnull:ifs:', @xlink:href, '/'))/mcr_directory/children/child[@type='file']">
            <xsl:variable name="internalId" select="menc:buildInternalId(concat($derId, '_', name))" />
            <xsl:variable name="hasMediaFiles" select="menc:hasMediaFiles($internalId) = 'true'" />
            <!--
            <xsl:message>
              internal Id:
              <xsl:value-of select="$internalId" />
              name:
              <xsl:value-of select="name" />
              hasMediaFiles:
              <xsl:value-of select="$hasMediaFiles" />
            </xsl:message>
            -->
            <xsl:if test="$hasMediaFiles">
              <xsl:variable name="hasSMILFile" select="menc:hasSMILFile($internalId) = 'true'" />
              <file id="{$internalId}" smil="{$hasSMILFile}">
                <xsl:copy-of select="node()" />
              </file>
            </xsl:if>
          </xsl:for-each>
        </der>
      </xsl:for-each>
    </xsl:variable>
    <!-- MIR-339 solr query if there is any "wav"/"mp3" file in this object? -->
    <xsl:variable name="solrQuery"
      select="concat('+stream_content_type:(audio/x-wav audio/mpeg audio/mp4) +returnId:',mcrsolru:escapeSearchValue(mycoreobject/@ID))" />
    <xsl:if test="(mcrsolr:getNumFound($solrQuery) &gt; 0) or (count($encDerivates/der/file) &gt; 0)">
      <xsl:variable name="completeQuery"
                    select="concat('solr:q=', fn:encode-for-uri($solrQuery), '&amp;group=true&amp;group.field=derivateID&amp;group.limit=999')"/>
      <xsl:variable name="solrResult" select="document($completeQuery)" /> <!-- [string-length(str[@name='groupValue']/text()) &gt; 0] -->
      <div id="mir-player">
        <div class="card mb-3">
        <!-- I want to make just one request, not for every derivate. So group by derivate id. -->
          <xsl:variable name="optionsFragment">
            <select id="videoChooser" class="form-control">
              <xsl:variable name="docContext" select="mycoreobject" />
              <xsl:for-each select="$solrResult/response/lst[@name='grouped']/lst[@name='derivateID']/arr[@name='groups']/lst">
                <xsl:variable name="currentDerivateID" select="str[@name='groupValue']/text()" />
                <xsl:variable name="read" select="count($docContext[key('rights', $currentDerivateID)/@read]) &gt; 0" />
                <xsl:if test="$read">
                  <optgroup label="{$currentDerivateID}">
                    <xsl:apply-templates select="result/doc" mode="resultsByDerivate" />
                  </optgroup>
                </xsl:if>
              </xsl:for-each>
              <xsl:for-each select="$encDerivates/der">
                <xsl:variable name="currentDerivateID" select="@id" />
                <xsl:variable name="read" select="count($docContext[key('rights', $currentDerivateID)/@read]) &gt; 0" />
                <xsl:if test="$read">
                  <optgroup label="{$currentDerivateID}">
                    <xsl:apply-templates select="file" mode="resultsByDerivate">
                      <xsl:with-param name="derivateId" select="$currentDerivateID" />
                    </xsl:apply-templates>
                  </optgroup>
                </xsl:if>
              </xsl:for-each>
            </select>
          </xsl:variable>
          <xsl:variable name="options" select="$optionsFragment"/>

          <xsl:variable name="playerNode">
            <div class="card-body p-0">
              <div class="embed-responsive embed-responsive-16by9 mir-player mir-preview">
                <xsl:if
                  test="(count($options//optgroup/option[string-length(@data-sources-url) &gt; 0]) &gt; 0) or (count($options//optgroup/option[contains('mp4|smil', @data-file-extension)]) &gt; 0)"
                >
                  <video id="player_video" class="video-js embed-responsive-item" controls="" preload="metadata" poster="">
                    <p class="vjs-no-js">
                      To view this video please enable JavaScript, and consider upgrading
                      to a web browser that
                      <a href="http://videojs.com/html5-video-support/">supports HTML5 video</a>
                    </p>
                  </video>
                </xsl:if>
                <xsl:if test="$options//optgroup/option[contains('mp3,wav,m4a', @data-file-extension)]">
                  <audio id="player_audio" class="video-js embed-responsive-item" controls="" preload="metadata" poster="">
                    <xsl:attribute name="data-setup">{}</xsl:attribute>
                    <p class="vjs-no-js">
                      To listen to this audio file please enable JavaScript, and consider upgrading
                      to a web browser that
                      <a href="http://caniuse.com/audio">supports HTML5 audio</a>
                    </p>
                  </audio>
                </xsl:if>
              </div>
            </div>
          </xsl:variable>

          <xsl:if test="count($options//optgroup/option) &gt; 0">
            <div class="card-header">
              <xsl:copy-of select="$optionsFragment" />
            </div>
            <xsl:variable name="playerNodes" select="$playerNode" />
            <xsl:call-template name="addPlayerScripts">
              <xsl:with-param name="generatedNodes" select="$playerNodes" />
            </xsl:call-template>
            <xsl:copy-of select="$playerNodes" />
          </xsl:if>

        </div>
      </div>
    </xsl:if>

    <xsl:apply-imports />

  </xsl:template>


  <xsl:template name="addPlayerScripts">
    <xsl:param name="generatedNodes" />
    <xsl:if test="$generatedNodes//div[contains(@class, 'mir-player')]">
      <script src="{$WebApplicationBaseURL}dbt/assets/video.js/video.min.js"></script>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}dbt/assets/videojs-share/videojs-share.css" />
      <script src="{$WebApplicationBaseURL}dbt/assets/videojs-share/videojs-share.min.js"></script>
      <script src="{$WebApplicationBaseURL}dbt/assets/media/player-small.min.js"></script>
      <script src="{$WebApplicationBaseURL}dbt/js/player.min.js"></script>
    </xsl:if>
  </xsl:template>


  <xsl:template match="doc" mode="resultsByDerivate">
    <xsl:variable name="fileIFSID" select="str[@name='id']" />
    <xsl:variable name="fileMimeType" select="str[@name='stream_content_type']" />
    <xsl:variable name="filePath" select="str[@name='filePath']/text()" />
    <xsl:variable name="fileIFSPath" select="str[@name='stream_source_info']" />
    <xsl:variable name="derivateID" select="str[@name='derivateID']" />
    <xsl:variable name="fileName" select="str[@name='fileName']" />

    <xsl:variable name="lowercaseExtension"
                  select="mcri18n:translate(FilenameUtils:getExtension($fileName), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>

    <xsl:choose>
      <xsl:when test="$fileMimeType = 'video/mp4'">
        <!-- ignore -->
      </xsl:when>
      <xsl:otherwise>
        <option data-file-extension="{$lowercaseExtension}" data-mime-type="{$fileMimeType}"
          data-src="{concat($ServletsBaseURL, 'MCRFileNodeServlet/', $derivateID, $filePath)}" data-audio="true"
          data-is-main-doc="{mcr:getMainDocName($derivateID)=substring($filePath,2)}"
        >
          <xsl:value-of select="$fileName" />
        </option>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="file" mode="resultsByDerivate">
    <xsl:param name="derivateId" />
    <xsl:variable name="internalId" select="@id" />
    <xsl:variable name="fileName" select="name" />

    <option data-audio="false" data-is-main-doc="{mcr:getMainDocName($derivateId) = $fileName}" data-source-id="{$internalId}">
      <xsl:if test="file/@smil">
        <xsl:attribute name="data-file-extension"><xsl:text>smil</xsl:text></xsl:attribute>
      </xsl:if>
      <xsl:value-of select="$fileName" />
    </option>
  </xsl:template>
</xsl:stylesheet>
