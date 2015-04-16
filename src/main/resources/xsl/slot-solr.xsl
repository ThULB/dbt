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
    <field name="slotId">
      <xsl:value-of select="@id" />
    </field>
    <field name="status">
      <xsl:value-of select="@status" />
    </field>
    <field name="onlineOnly">
      <xsl:value-of select="@onlineOnly" />
    </field>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="title">
    <field name="{name()}">
      <xsl:value-of select="." />
    </field>
  </xsl:template>

  <xsl:template match="lecturer">
    <field name="{name()}">
      <xsl:value-of select="@name" />
    </field>
  </xsl:template>

  <xsl:template match="entries">
    <!-- ignore entries -->
  </xsl:template>

</xsl:stylesheet>