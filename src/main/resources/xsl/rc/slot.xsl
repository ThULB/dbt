<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:acl="xalan://org.urmel.dbt.rc.persistency.SlotManager" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="acl i18n mcrxsl xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />
  <xsl:include href="slot-entries.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slot.pageTitle', concat(/slot/title, ';', /slot/@id))" />

  <xsl:param name="Mode" select="'view'" />
  <xsl:variable name="effectiveMode">
    <xsl:choose>
      <xsl:when test="$Mode = 'edit' and $writePermission">
        <xsl:text>edit</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>view</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="slotId" select="/slot/@id" />
  <xsl:variable name="onlineOnly" select="/slot/@onlineOnly" />
  <xsl:variable name="objectId" select="document(concat('slot:slotId=', $slotId, '&amp;objectId'))/mcrobject" />

  <xsl:variable name="hasAdminPermission" select="acl:hasAdminPermission()" />
  <xsl:variable name="isOwner" select="acl:isOwner($objectId)" />
  <xsl:variable name="readPermission" select="acl:checkPermission($objectId, 'read')" />
  <xsl:variable name="writePermission" select="acl:checkPermission($objectId, 'writedb')" />

  <xsl:template match="/slot">
    <xsl:apply-templates mode="slotHead" select="." />
    <div id="slot-body">
      <xsl:choose>
        <xsl:when test="mcrxsl:isCurrentUserGuestUser()">
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_access')" />
          </div>
        </xsl:when>
        <xsl:when test="not($readPermission)">
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_accesskey')" />
            <p>
              <a href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={$RequestURL}">
                <xsl:value-of select="i18n:translate('component.rc.slot.enter_accesskey')" />
              </a>
            </p>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="entries" />
          <xsl:if test="count(entries) = 0 and $effectiveMode = 'edit'">
            <xsl:call-template name="addNewEntry" />
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template match="slot" mode="slotHead">
    <div id="slot-head">
      <h1>
        <xsl:value-of select="title" />
        <xsl:if test="@status = 'new'">
          <!-- TODO: New badge if needed -->
        </xsl:if>
        <xsl:if test="$readPermission">
          <div class="dropdown pull-right">
            <button class="btn btn-default btn-sm dropdown-toggle" type="button" id="rcOptionMenu" data-toggle="dropdown" aria-expanded="false">
              <span class="glyphicon glyphicon-cog" aria-hidden="true" />
              <span class="caret" />
            </button>
            <ul class="dropdown-menu" role="menu" aria-labelledby="rcOptionMenu">
              <xsl:if test="$hasAdminPermission or $writePermission">
                <li role="presentation">
                  <xsl:choose>
                    <xsl:when test="$effectiveMode = 'view'">
                      <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Mode=edit">
                        <xsl:value-of select="i18n:translate('component.rc.slot.edit')" />
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}">
                        <xsl:value-of select="i18n:translate('component.rc.slot.edit.cancel')" />
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                </li>
              </xsl:if>
              <xsl:if test="$hasAdminPermission or $isOwner">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}content/rc/edit-accesskeys.xed?slotId={@id}">
                    <xsl:value-of select="i18n:translate('component.rc.slot.edit.accesskeys')" />
                  </a>
                </li>
              </xsl:if>
              <xsl:if test="not($hasAdminPermission) and not($writePermission)">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={$RequestURL}">
                    <xsl:value-of select="i18n:translate('component.rc.slot.change_accesskey')" />
                  </a>
                </li>
              </xsl:if>
              <xsl:if test="$hasAdminPermission">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Style=xml">
                    <xsl:value-of select="i18n:translate('component.rc.slot.showXML')" />
                  </a>
                </li>
              </xsl:if>
            </ul>
          </div>
        </xsl:if>
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