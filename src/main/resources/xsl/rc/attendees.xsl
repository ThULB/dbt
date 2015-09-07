<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:acl="xalan://org.urmel.dbt.rc.persistency.SlotManager" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="acl encoder i18n mcrxsl xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.attendees')" />

  <xsl:variable name="slot" select="document(concat('slot:slotId=', /attendees/@slotId))" />
  <xsl:variable name="slotId" select="/attendees/@slotId" />

  <xsl:variable name="hasAdminPermission" select="acl:hasAdminPermission()" />

  <xsl:template match="/attendees">
    <xsl:apply-templates mode="slotHead" select="$slot" />
    <h2>
      <xsl:value-of select="i18n:translate('component.rc.attendees')" />
    </h2>
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
        <xsl:choose>
          <xsl:when test="count(attendee) &gt; 0">
            <xsl:apply-templates select="attendee[@owner='true']|attendee[@writeKey='true']">
              <xsl:sort select="@owner" />
            </xsl:apply-templates>
            <xsl:apply-templates select="attendee[@owner!='true' and @writeKey!='true']">
              <xsl:sort select="substring-after(@name, ' ')" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <tr>
              <td colspan="2" class="text-center">
                <strong>
                  <xsl:value-of select="i18n:translate('component.rc.attendees.emptyList')" />
                </strong>
              </td>
            </tr>
          </xsl:otherwise>
        </xsl:choose>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="attendee">
    <tr>
      <td>
        <xsl:choose>
          <xsl:when test="string-length(@email) &gt; 0">
            <a href="mailto:{@email}">
              <xsl:value-of select="@name" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$hasAdminPermission">
          <xsl:value-of select="concat(' (',@uid, ')')" />
        </xsl:if>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="@owner = 'true'">
            <strong>
              <xsl:value-of select="i18n:translate('component.rc.attendees.type.owner')" />
            </strong>
          </xsl:when>
          <xsl:when test="@owner = 'false' and @writeKey = 'true'">
            <strong>
              <xsl:value-of select="i18n:translate('component.rc.attendees.type.contact')" />
            </strong>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="i18n:translate('component.rc.attendees.type.attendee')" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$hasAdminPermission and @owner = 'false' and @readKey = 'false' and @writeKey = 'false'">
          <xsl:text>&#160;</xsl:text>
          <span class="glyphicon glyphicon-exclamation-sign text-danger" data-toggle="tooltip" data-placement="right"
            title="{i18n:translate('component.rc.attendees.invalidKey')}" />
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>