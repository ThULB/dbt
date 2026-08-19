<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="mods"
                version="1.0">

  <xsl:import href="xeditor2mods.xsl" />

  <!-- Podcast dates are already validated; preserve forms MIR cannot safely normalize. -->
  <xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods[mods:genre[@type='intern']/@valueURIxEditor='podcast_episode']/mods:originInfo[@eventType='publication']/mods:dateIssued[@encoding='w3cdtf']">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:value-of select="." />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
