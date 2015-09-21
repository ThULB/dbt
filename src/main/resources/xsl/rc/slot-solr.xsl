<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="xlink"
>

  <xsl:import href="xslImport:solr-document:rc/slot-solr.xsl" />

  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:template match="mycoreobject[contains(@ID,'rc_slot_')]">
    <xsl:apply-imports />
    <xsl:apply-templates select="metadata/def.rcSlotContainer/rcSlotContainer/slot" />
  </xsl:template>

  <xsl:template match="slot">
    <xsl:apply-templates select="@*|*" mode="slot" />
    <xsl:apply-templates select="@*|*" mode="sort" />
  </xsl:template>

  <xsl:template match="@id" mode="slot">
    <xsl:variable name="rcLocId">
      <xsl:value-of select="substring(., 1, string-length(.) - 5)" />
    </xsl:variable>
    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />

    <field name="slotId">
      <xsl:value-of select="." />
    </field>

    <field name="category">
      <xsl:value-of select="concat('RCLOC:', $rcLocId)" />
    </field>

    <xsl:for-each select="$currentLocation/ancestor-or-self::category">
      <xsl:for-each select="label[not(starts-with(@xml:lang, 'x-'))]">
        <field name="slot.location">
          <xsl:value-of select="@text" />
        </field>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="title" mode="slot">
    <field name="search_result_link_text">
      <xsl:value-of select="." />
    </field>
    <field name="slot.{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="lecturers" mode="sort">
    <field name="slot.lecturers">
      <xsl:for-each select="lecturer">
        <xsl:choose>
          <xsl:when test="contains(@name, ', ')">
            <xsl:value-of select="@name" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat(substring-after(@name, ' '), ', ', substring-before(@name, ' '))" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position() != last()">
          <xsl:text>; </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </field>
  </xsl:template>

  <xsl:template match="lecturer" mode="slot">
    <field name="slot.{name()}">
      <xsl:value-of select="@name" />
    </field>
  </xsl:template>

  <xsl:template match="validTo" mode="slot">
    <field name="slot.{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="slot/@*[not(contains('id', name()))]" mode="slot">
    <field name="slot.{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

</xsl:stylesheet>