<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:acl="xalan://de.urmel_dl.dbt.rc.persistency.SlotManager" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="acl encoder i18n mcrxsl xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />

  <xsl:include href="datatable.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.attendees')" />

  <xsl:variable name="slot" select="document(concat('slot:slotId=', /attendees/@slotId))" />
  <xsl:variable name="slotId" select="/attendees/@slotId" />

  <xsl:template match="/attendees">
    <xsl:apply-templates mode="slotHead" select="$slot" />

    <xsl:apply-templates mode="dataTable" select=".">
      <xsl:with-param name="id" select="'attendees'" />
      <xsl:with-param name="disableFilter" select="true()" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dataTableHeader" match="attendees">
    <col sortBy="name">
      <xsl:value-of select="i18n:translate('component.rc.attendees.name')" />
    </col>
    <col sortBy="owner" width="20%">
      <xsl:value-of select="i18n:translate('component.rc.attendees.type')" />
    </col>
  </xsl:template>

  <xsl:template mode="dataTableRow" match="attendee">
    <col>
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
    </col>
    <col>
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
    </col>
  </xsl:template>

</xsl:stylesheet>