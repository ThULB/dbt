<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dbtslot="https://www.db-thueringen.de/xslt/slot" xmlns:dbtmedia="https://www.db-thueringen.de/xslt/media"
  xmlns:dbtutils="https://www.db-thueringen.de/xslt/utils" xmlns:dbtversion="https://www.db-thueringen.de/xslt/version"
  exclude-result-prefixes="#all"
>

  <xsl:function name="dbtslot:is-active" as="xs:boolean">
    <xsl:param name="slotId" />
    <xsl:sequence
      select="if (string($slotId) = '') then false()
        else document(concat('callJava:de.urmel_dl.dbt.rc.persistency.SlotManager:isActive:', encode-for-uri($slotId)))/string = 'true'" />
  </xsl:function>

  <xsl:function name="dbtslot:is-owner" as="xs:boolean">
    <xsl:param name="objId" />
    <xsl:sequence
      select="if (string($objId) = '') then false()
        else document(concat('callJava:de.urmel_dl.dbt.rc.persistency.SlotManager:isOwner:', encode-for-uri($objId)))/string = 'true'" />
  </xsl:function>

  <xsl:function name="dbtslot:is-streaming-supported" as="xs:boolean">
    <xsl:param name="slotId" />
    <xsl:param name="entryId" />
    <xsl:sequence
      select="if (string($slotId) = '' or string($entryId) = '') then false()
        else document(concat('callJava:de.urmel_dl.dbt.rc.persistency.SlotManager:isStreamingSupported:', encode-for-uri($slotId), ':',
        encode-for-uri($entryId)))/string = 'true'" />
  </xsl:function>

  <xsl:function name="dbtmedia:has-media-files" as="xs:boolean">
    <xsl:param name="id" />
    <xsl:sequence
      select="if (string($id) = '') then false()
        else document(concat('callJava:de.urmel_dl.dbt.media.MediaService:hasMediaFiles:', encode-for-uri($id)))/string = 'true'" />
  </xsl:function>

  <xsl:function name="dbtmedia:has-smil-file" as="xs:boolean">
    <xsl:param name="id" />
    <xsl:sequence
      select="if (string($id) = '') then false()
        else document(concat('callJava:de.urmel_dl.dbt.media.MediaService:hasSMILFile:', encode-for-uri($id)))/string = 'true'" />
  </xsl:function>

  <xsl:function name="dbtmedia:build-internal-id" as="xs:string">
    <xsl:param name="input" />
    <xsl:sequence
      select="string(document(concat('callJava:de.urmel_dl.dbt.media.MediaService:buildInternalId:', encode-for-uri($input)))/string)" />
  </xsl:function>

  <xsl:function name="dbtutils:url-decode" as="xs:string">
    <xsl:param name="input" />
    <xsl:sequence
      select="if (string($input) = '') then ''
        else string(document(concat('callJava:java.net.URLDecoder:decode:', encode-for-uri($input), ':UTF-8'))/string)" />
  </xsl:function>

  <xsl:function name="dbtutils:get-supported-main-file" as="xs:string">
    <xsl:param name="derivateId" />
    <xsl:sequence
      select="if (string($derivateId) = '') then ''
        else string(document(concat('callJava:org.mycore.iview2.services.MCRIView2Tools:getSupportedMainFile:', encode-for-uri($derivateId)))/string)" />
  </xsl:function>

  <xsl:function name="dbtutils:encode-uri-path" as="xs:string">
    <xsl:param name="path" />
    <xsl:sequence
      select="if (string($path) = '') then ''
        else string(document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:encodeURIPath:', encode-for-uri($path)))/string)" />
  </xsl:function>

  <xsl:function name="dbtversion:version" as="xs:string">
    <xsl:sequence select="string(document('callJava:de.urmel_dl.dbt.common.DBTVersion:getVersion')/string)" />
  </xsl:function>

  <xsl:function name="dbtversion:abbrev" as="xs:string">
    <xsl:sequence select="string(document('callJava:de.urmel_dl.dbt.common.DBTVersion:getAbbRev')/string)" />
  </xsl:function>

  <xsl:function name="dbtversion:mir-complete-version" as="xs:string">
    <xsl:sequence select="string(document('callJava:org.mycore.mir.common.MIRCoreVersion:getCompleteVersion')/string)" />
  </xsl:function>

</xsl:stylesheet>
