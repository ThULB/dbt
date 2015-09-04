<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:acl="xalan://org.urmel.dbt.rc.persistency.SlotManager" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="acl encoder i18n mcrxsl xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.attendees.pageTitle')" />

  <xsl:template match="/attendees">
    <table id="attendees" class="table">
      <thead>
        <tr>
          <th>
            <xsl:value-of select="i18n:translate('component.rc.attendees.name')" />
          </th>
          <th>
            <xsl:value-of select="i18n:translate('component.rc.attendees.type')" />
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="attendee[@owner='true']|attendee[@writeKey='true']">
          <xsl:sort select="@owner" />
        </xsl:apply-templates>
        <xsl:apply-templates select="attendee[@owner!='true' and @writeKey!='true']">
          <xsl:sort select="@name" />
        </xsl:apply-templates>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="attendee">
    <tr>
      <td>
        <a href="mailto:{@email}">
          <xsl:value-of select="@name" />
        </a>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="@owner = 'true'">
            <xsl:value-of select="i18n:translate('component.rc.attendees.type.owner')" />
          </xsl:when>
          <xsl:when test="@owner = 'false' and @writeKey = 'true'">
            <xsl:value-of select="i18n:translate('component.rc.attendees.type.contact')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="i18n:translate('component.rc.attendees.type.attendee')" />
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>