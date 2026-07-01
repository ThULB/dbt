<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:variable name="currentUser" select="document('user:current')/user" />

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="slot-templates.xsl" />

  <xsl:template match="/slot">
    <slot>
      <lecturers>
        <xsl:variable name="name">
          <xsl:call-template name="formatName">
            <xsl:with-param name="name" select="$currentUser/realName" />
          </xsl:call-template>
        </xsl:variable>
        <lecturer name="{$name}" email="{$currentUser/eMail}" />
      </lecturers>
      <xsl:copy-of select="*" />
    </slot>
  </xsl:template>
</xsl:stylesheet>