<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="copynodes.xsl" />

  <xsl:variable name="currentUser" select="document('user:current')/user" />

  <xsl:template match="/slot">
    <slot>
      <xsl:copy-of select="@*" />
      <lecturers>
        <lecturer name="{$currentUser/realName}" email="{$currentUser/eMail}" />
      </lecturers>
      <xsl:copy-of select="*[not(contains('lecturers|accesskeys', name()))]" />
    </slot>
  </xsl:template>
</xsl:stylesheet>