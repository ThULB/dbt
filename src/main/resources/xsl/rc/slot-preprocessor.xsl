<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:variable name="currentUser" select="document('user:current')/user" />

  <xsl:template match="/slot">
    <slot>
      <lecturers>
        <lecturer name="{$currentUser/realName}" email="{$currentUser/eMail}" />
      </lecturers>
    </slot>
  </xsl:template>
</xsl:stylesheet>