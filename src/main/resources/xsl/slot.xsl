<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="i18n xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />
  <xsl:include href="slot-entries.xsl" />

  <xsl:param name="Mode" select="'view'" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slot.pageTitle', concat(/slot/title, ';', /slot/@id))" />

  <xsl:template match="/slot">
    <xsl:apply-templates mode="slotHead" select="." />
    <div id="slot-body">
      <xsl:apply-templates select="entries" />
      <xsl:if test="count(entries) = 0 and $Mode = 'edit'">
        <xsl:call-template name="addNewEntry" />
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="slot" mode="slotHead">
    <div id="slot-head">
      <h1>
        <xsl:value-of select="title" />
        <xsl:if test="@status = 'new'">
          <!-- TODO: New badge if needed -->
        </xsl:if>
        <div class="dropdown pull-right">
          <button class="btn btn-default btn-sm dropdown-toggle" type="button" id="rcOptionMenu" data-toggle="dropdown" aria-expanded="false">
            <span class="glyphicon glyphicon-cog" aria-hidden="true" />
            <span class="caret" />
          </button>
          <ul class="dropdown-menu" role="menu" aria-labelledby="rcOptionMenu">
            <li role="presentation">
              <xsl:choose>
                <xsl:when test="$Mode = 'view'">
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Mode=edit">
                    <xsl:value-of select="i18n:translate('component.rc.slot.edit')" />
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Mode=view">
                    <xsl:value-of select="i18n:translate('component.rc.slot.edit.cancel')" />
                  </a>
                </xsl:otherwise>
              </xsl:choose>
            </li>
            <li role="presentation">
              <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Style=xml">
                <xsl:value-of select="i18n:translate('component.rc.slot.showXML')" />
              </a>
            </li>
          </ul>
        </div>
      </h1>
      <div class="info">
        <xsl:for-each select="lecturers/lecturer">
          <xsl:value-of select="@name" />
          <xsl:if test="position() != last()">
            <xsl:text>; </xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text disable-output-escaping="yes">&amp;nbsp;-&amp;nbsp;</xsl:text>
        <xsl:value-of select="i18n:translate('component.rc.slot.header', @id)" />
        <xsl:text disable-output-escaping="yes">&amp;nbsp;(</xsl:text>
        <xsl:apply-templates select="@id" mode="rcLocation" />
        <xsl:text>)</xsl:text>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>