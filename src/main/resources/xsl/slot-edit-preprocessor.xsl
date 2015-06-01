<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />

  <xsl:variable name="objectId" select="document(concat('slot:slotId=', /slot/@id, '&amp;objectId'))/mcrobject" />

  <xsl:variable name="accessKeys" select="document(concat('accesskeys:', $objectId))/accesskeys" />

  <xsl:template match="/slot">
    <slot>
      <xsl:copy-of select="@*|*" />
      <xsl:copy-of select="$accessKeys" />
    </slot>
  </xsl:template>
</xsl:stylesheet>