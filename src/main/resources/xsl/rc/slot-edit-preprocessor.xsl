<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="CurrentLang" />

  <xsl:variable name="objectId" select="document(concat('slot:slotId=', /slot/@id, '&amp;objectId'))/mcrobject" />
  <xsl:variable name="accessKeys" select="document(concat('xslStyle:accesskeys-filter:accesskeys:', $objectId))/accesskeys" />

  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:template match="/slot">
    <slot>
      <xsl:copy-of select="@*|*" />
      <xsl:copy-of select="$accessKeys" />
      <xsl:apply-templates select="@id" mode="location" />
    </slot>
  </xsl:template>

  <xsl:template match="@id" mode="location">
    <xsl:variable name="rcLocId" select="substring(., 1, string-length(.) - 5)" />
    <xsl:variable name="rcId" select="number(substring(., string-length(.) - 3))" />

    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />
    <location id="{$rcLocId}" newId="{$rcId}">
      <label>
        <xsl:attribute name="text">
          <xsl:for-each select="$currentLocation/ancestor-or-self::category">
            <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
            <xsl:if test="position() != last()">
              <xsl:text> - </xsl:text>
          </xsl:if>
          </xsl:for-each>
        </xsl:attribute>
      </label>
    </location>
  </xsl:template>
</xsl:stylesheet>