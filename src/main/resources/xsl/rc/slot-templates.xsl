<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:mgr="xalan://de.urmel_dl.dbt.rc.persistency.SlotManager" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="mgr acl encoder i18n mcrxsl xlink xalan"
>

  <xsl:param name="CurrentLang" />
  <xsl:param name="RequestURL" />

  <xsl:param name="Mode" select="'view'" />

  <xsl:variable name="slotId" select="/slot/@id" />
  <xsl:variable name="onlineOnly" select="/slot/@onlineOnly" />
  <xsl:variable name="objectId" select="document(concat('slot:slotId=', $slotId, '&amp;objectId'))/mcrobject" />

  <xsl:variable name="hasAdminPermission" select="acl:checkPermission('administrate-slot')" />
  <xsl:variable name="hasEditorPermission" select="acl:checkPermission('edit-slot')" />
  <xsl:variable name="isOwner" select="mgr:isOwner($objectId)" />
  <xsl:variable name="readPermission" select="acl:checkPermission($objectId, 'read')" />
  <xsl:variable name="writePermission" select="acl:checkPermission($objectId, 'writedb')" />

  <xsl:variable name="effectiveMode">
    <xsl:message>
      readPermission:
      <xsl:value-of select="$readPermission" />
      writePermission:
      <xsl:value-of select="$writePermission" />
      isOwner:
      <xsl:value-of select="$isOwner" />
      hasAdminPermission:
      <xsl:value-of select="$hasAdminPermission" />
      hasEditorPermission:
      <xsl:value-of select="$hasEditorPermission" />
    </xsl:message>
    <xsl:choose>
      <xsl:when test="$Mode = 'edit' and (($hasAdminPermission or $hasEditorPermission) or ($writePermission and (/slot/@status != 'archived')))">
        <xsl:text>edit</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>view</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:template match="@id" mode="rcLocation">
    <xsl:variable name="rcLocId">
      <xsl:value-of select="substring(., 1, string-length(.) - 5)" />
    </xsl:variable>
    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />

    <xsl:for-each select="$currentLocation/ancestor-or-self::category">
      <xsl:choose>
        <xsl:when test="string-length(url/@xlink:href) &gt; 0">
          <a href="{url/@xlink:href}" target="_blank">
            <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="position() != last()">
        <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;raquo;&amp;nbsp;</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="@id" mode="rcLocationText">
    <xsl:variable name="rcLocId">
      <xsl:value-of select="substring(., 1, string-length(.) - 5)" />
    </xsl:variable>
    <xsl:variable name="currentLocation" select="$rcLocations/descendant-or-self::category[@ID=$rcLocId]" />

    <xsl:for-each select="$currentLocation/ancestor-or-self::category">
      <xsl:value-of select="label[@xml:lang=$CurrentLang]/@text" />
      <xsl:if test="position() != last()">
        <xsl:text> / </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="slot" mode="slotHead">
    <div id="slot-head">
      <h1>
        <xsl:if test="contains($RequestURL, '/attendees')">
          <xsl:value-of select="i18n:translate('component.rc.attendees')" />
          <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:value-of select="title" />
        <xsl:if test="not(mcrxsl:isCurrentUserGuestUser()) and ($readPermission or $writePermission)">
          <div class="dropdown pull-right">
            <button class="btn btn-default btn-sm dropdown-toggle" type="button" id="rcOptionMenu" data-toggle="dropdown" aria-expanded="false">
              <span class="glyphicon glyphicon-cog" aria-hidden="true" />
              <span class="caret" />
            </button>
            <ul class="dropdown-menu" role="menu" aria-labelledby="rcOptionMenu">
              <xsl:if test="$writePermission and (@status = 'archived')">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1"
                    href="{$WebApplicationBaseURL}content/rc/slot.xed?action=reactivate&amp;slotId={@id}&amp;url={encoder:encode(string($RequestURL))}"
                  >
                    <xsl:value-of select="i18n:translate('component.rc.slot.reactivate')" />
                  </a>
                </li>
              </xsl:if>
              <xsl:if test="$hasAdminPermission or $hasEditorPermission or ($writePermission and (@status != 'archived'))">
                <xsl:if
                  test="(($hasAdminPermission or $hasEditorPermission) or ($writePermission and (@status != 'archived'))) and not(contains($RequestURL, '/attendees'))"
                >
                  <li role="presentation">
                    <xsl:choose>
                      <xsl:when test="$effectiveMode = 'view'">
                        <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}?XSL.Mode=edit">
                          <xsl:value-of select="i18n:translate('component.rc.slot.edit.entries')" />
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
                  <xsl:if test="not(contains($RequestURL, '/attendees'))">
                    <li class="divider" />
                    <li role="presentation">
                      <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}/attendees">
                        <xsl:value-of select="i18n:translate('component.rc.attendees')" />
                      </a>
                    </li>
                  </xsl:if>
                  <xsl:if test="contains($RequestURL, '/attendees')">
                    <li role="presentation">
                      <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}rc/{@id}">
                        <xsl:value-of select="i18n:translate('component.rc.slot.show.entries')" />
                      </a>
                    </li>
                  </xsl:if>
                  <li class="divider" />
                  <li role="presentation">
                    <a role="menuitem" tabindex="-1"
                      href="{$WebApplicationBaseURL}content/rc/edit-accesskeys.xed?slotId={@id}&amp;url={encoder:encode(string($RequestURL))}"
                    >
                      <xsl:value-of select="i18n:translate('component.rc.slot.edit.accesskeys')" />
                    </a>
                  </li>
                  <li class="divider" />
                  <li role="presentation">
                    <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}content/rc/slot.xed?slotId={@id}&amp;url={encoder:encode(string($RequestURL))}">
                      <xsl:value-of select="i18n:translate('component.rc.slot.edit')" />
                    </a>
                  </li>
                </xsl:if>
              </xsl:if>
              <xsl:if test="not($hasAdminPermission) and not($hasEditorPermission) and not($writePermission)">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1"
                    href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={encoder:encode(string($RequestURL))}"
                  >
                    <xsl:value-of select="i18n:translate('component.rc.slot.change_accesskey')" />
                  </a>
                </li>
              </xsl:if>
              <xsl:if test="$hasAdminPermission">
                <li class="divider" />
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

        <xsl:for-each select="lecturers/lecturer">
          <xsl:choose>
            <xsl:when test="not(mcrxsl:isCurrentUserGuestUser()) and $readPermission and string-length(/slot/contact/@email) = 0">
              <a href="mailto:{@email}">
                <xsl:call-template name="formatName">
                  <xsl:with-param name="name" select="@name" />
                </xsl:call-template>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatName">
                <xsl:with-param name="name" select="@name" />
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="position() != last()">
            <xsl:text>; </xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text disable-output-escaping="yes">&amp;nbsp;-&amp;nbsp;</xsl:text>
        <xsl:value-of select="i18n:translate('component.rc.slot.header', @id)" />
        <xsl:text disable-output-escaping="yes">&amp;nbsp;(</xsl:text>
        <xsl:apply-templates select="@id" mode="rcLocation" />
        <xsl:text>)</xsl:text>
        <xsl:text> - </xsl:text>
        <xsl:value-of select="$period//label[lang($CurrentLang)]/@description" />
        <xsl:if test="string-length(contact/@name) &gt; 0">
          <div class="contact">
            <strong>
              <xsl:value-of select="i18n:translate('component.rc.slot.contact')" />
              <xsl:text>: </xsl:text>
            </strong>
            <xsl:choose>
              <xsl:when test="$readPermission">
                <a href="mailto:{contact/@email}">
                  <xsl:call-template name="formatName">
                    <xsl:with-param name="name" select="contact/@name" />
                  </xsl:call-template>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="formatName">
                  <xsl:with-param name="name" select="contact/@name" />
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </div>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="formatName">
    <xsl:param name="name" />

    <xsl:choose>
      <xsl:when test="not(contains($name, ',')) and contains($name, ' ')">
        <xsl:variable name="parts">
          <xsl:call-template name="Tokenizer">
            <xsl:with-param name="string" select="$name" />
            <xsl:with-param name="delimiter" select="' '" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="last" select="xalan:nodeset($parts)/token[count(../token)]" />
        <xsl:variable name="first" select="substring-before($name, $last)" />
        <xsl:value-of select="concat($last, ', ', $first)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>