<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="xlink"
>

  <xsl:import href="xslImport:solr-document:slot-solr.xsl" />

  <xsl:template match="mycoreobject[contains(@ID,'_rcslot_')]">
    <xsl:apply-imports />
    <xsl:apply-templates select="metadata/def.rcSlotContainer/rcSlotContainer/slot" />
  </xsl:template>

  <xsl:template match="slot">
    <xsl:apply-templates select="@*|*" mode="slot" />
  </xsl:template>

  <xsl:template match="@id" mode="slot">
    <field name="slotId">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="title" mode="slot">
    <field name="{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="lecturer" mode="slot">
    <field name="{name()}">
      <xsl:value-of select="@name" />
    </field>
  </xsl:template>

  <xsl:template match="slot/@*[not(contains('id', name()))]" mode="slot">
    <field name="{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

</xsl:stylesheet>