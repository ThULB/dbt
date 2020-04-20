<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="xslImport:solr-document:dbt-solr.xsl"/>

  <xsl:template match="mycoreobject[contains(@ID,'_mods_')]">
    <xsl:apply-imports/>
    <xsl:apply-templates select="service/servflags/servflag[@type='dbt-dini']" mode="dbt"/>
  </xsl:template>

  <xsl:template match="servflag[@type='dbt-dini']" mode="dbt">
    <field name="dbt.dini">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

</xsl:stylesheet>
