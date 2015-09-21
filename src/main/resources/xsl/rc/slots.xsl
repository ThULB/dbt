<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="i18n xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />

  <xsl:include href="datatable.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slots.title')" />

  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:variable name="defaultNumPerPage" select="50" />

  <xsl:template match="/slots">
    <h2>
      <xsl:value-of select="i18n:translate('component.rc.slots.title')" />
    </h2>

    <xsl:apply-templates mode="dataTable" select=".">
      <xsl:with-param name="id" select="'slots'" />
      <xsl:with-param name="disableFilter" select="false()"></xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dataTableHeader" match="slots">
    <xsl:if test="$hasAdminPermission">
      <col align="center" width="5%">
        <xsl:value-of select="i18n:translate('component.rc.slot.actions')" />
      </col>
      <col sortBy="slotId" sortOrder="asc" width="10%">
        <xsl:value-of select="i18n:translate('component.rc.slot.id')" />
      </col>
    </xsl:if>
    <col width="25%">
      <xsl:if test="not($hasAdminPermission)">
        <xsl:attribute name="sortBy">slotId</xsl:attribute>
        <xsl:attribute name="sortOrder">asc</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="i18n:translate('component.rc.slot.location')" />
    </col>
    <col sortBy="slot.validTo" width="15%">
      <xsl:value-of select="i18n:translate('component.rc.slot.period')" />
    </col>
    <col sortBy="slot.lecturers" width="15%">
      <xsl:value-of select="i18n:translate('component.rc.slot.lecturer')" />
    </col>
    <col sortBy="slot.title" width="*">
      <xsl:value-of select="i18n:translate('component.rc.slot.title')" />
    </col>
  </xsl:template>

  <xsl:template mode="dataTableRow" match="slot">
    <xsl:if test="$hasAdminPermission">
      <col align="center" valign="top">
        <div class="btn-group btn-group-xs">
          <a class="btn btn-primary" href="{$WebApplicationBaseURL}content/rc/slot.xed?slotId={@id}&amp;url={$RequestURL}" title="{i18n:translate('component.rc.slot.edit')}">
            <span class="glyphicon glyphicon-pencil" />
          </a>
          <a class="btn btn-primary" href="{$WebApplicationBaseURL}content/rc/edit-accesskeys.xed?slotId={@id}&amp;url={$RequestURL}" title="{i18n:translate('component.rc.slot.edit.accesskeys')}">
            <span class="glyphicon glyphicon-lock" />
          </a>
        </div>
      </col>
      <col align="center" valign="top">
        <a href="{$WebApplicationBaseURL}rc/{@id}">
          <xsl:value-of select="@id" />
        </a>
        <xsl:if test="@status = 'new'">
          <span class="label label-danger pull-right">
            <xsl:value-of select="i18n:translate('component.rc.slot.new')" />
          </span>
        </xsl:if>
      </col>
    </xsl:if>
    <col class="text-ellipsis" valign="top">
      <xsl:attribute name="title">
        <xsl:apply-templates select="@id" mode="rcLocationText" />
      </xsl:attribute>
      <xsl:apply-templates select="@id" mode="rcLocation" />
    </col>
    <col class="text-ellipsis" valign="top">
      <xsl:variable name="date">
        <xsl:choose>
          <xsl:when test="string-length(validTo) &gt; 0">
            <xsl:value-of select="validTo" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>now</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="period" select="document(concat('period:areacode=0&amp;date=', $date, '&amp;fq=true'))" />
      <xsl:value-of
        select="concat($period//label[lang($CurrentLang)]/@shortText, '&#160;', substring-after($period//label[lang($CurrentLang)]/@description, concat($period//label[lang($CurrentLang)]/@text, ' ')))" />
    </col>
    <col valign="top">
      <xsl:for-each select="lecturers/lecturer">
        <xsl:value-of select="@name" />
        <xsl:if test="position() != last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </col>
    <col valign="top">
      <a href="{$WebApplicationBaseURL}rc/{@id}" title="{title}">
        <xsl:value-of select="title" />
      </a>
    </col>
  </xsl:template>

</xsl:stylesheet>