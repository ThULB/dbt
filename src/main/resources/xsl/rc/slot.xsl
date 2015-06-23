<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:acl="xalan://org.urmel.dbt.rc.persistency.SlotManager" xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="acl encoder i18n mcrxsl xlink"
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

  <xsl:variable name="date">
    <xsl:choose>
      <xsl:when test="string-length(/slot/validTo) &gt; 0">
        <xsl:value-of select="/slot/validTo" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>now</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="period" select="document(concat('period:areacode=0&amp;date=', $date, '&amp;fq=true'))" />

  <xsl:template match="/slot">
    <xsl:apply-templates mode="slotHead" select="." />
    <div id="slot-body">
      <xsl:choose>
        <xsl:when test="mcrxsl:isCurrentUserGuestUser()">
          <xsl:variable name="loginURL" select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_access')" />
            <xsl:text> </xsl:text>
            <a id="loginURL" href="{$loginURL}">
              <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />
            </a>
          </div>
        </xsl:when>
        <xsl:when test="not($readPermission)">
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_accesskey')" />
            <p>
              <a href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={encoder:encode(string($RequestURL))}">
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
    <xsl:if test="$effectiveMode = 'edit'">
      <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/jquery/plugins/jquery-sortable-min.js" />
      <script type="text/javascript">
        <xsl:value-of select="concat('var servletsBaseURL = &quot;', $ServletsBaseURL, '&quot;;')" disable-output-escaping="yes" />
        <xsl:value-of select="concat('var slotId = &quot;', $slotId, '&quot;;')" disable-output-escaping="yes" />
        <![CDATA[
          var oldEntryIndex;
          var slotEntries = $("div.slot-section").sortable({
            containerSelector: 'div.slot-section',
            itemSelector: 'div[class|="entry"][class!="entry-headline"][class!="entry-buttons"][class!="entry-infoline"]',
            exclude: 'div.entry-headline',
            handle: 'span.entry-mover',
            placeholder: '<div class="entry-placeholder" />',
            pullPlaceholder: true,
            
            // set item relative to cursor position
            onDragStart: function ($item, container, _super) {
              var offset = $item.offset(),
              pointer = container.rootGroup.pointer,
              placeholder = container.rootGroup.placeholder;
          
              oldEntryIndex = $item.index();

              adjustment = {
                left: pointer.left - offset.left,
                top: pointer.top - offset.top
              }
              
              placeholder.height($item.height());
          
              _super($item, container)
            },
            
            onDrag: function ($item, position) {
              $item.css({
                left: position.left - adjustment.left,
                top: position.top - adjustment.top
              })
            },
            
            // persists new order of entries
            serialize: function (parent, children, isContainer) {
              var obj = [];
              isContainer && obj.push($('div:first', parent).attr("id"));
              return isContainer ? obj.concat(children) : parent.attr("id");
            },
            
            onDrop: function ($item, container, _super) {
              if (oldEntryIndex != $item.index()) {
                var data = slotEntries.sortable("serialize").get();
                $.post(servletsBaseURL + "RCSlotServlet", { 'action': 'order', 'slotId': slotId, 'items': data.join() });
              }
              _super($item, container);
            }
          });
        ]]>
      </script>
    </xsl:if>
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
                  <a role="menuitem" tabindex="-1" href="{$WebApplicationBaseURL}content/rc/edit-slot.xed?slotId={@id}&amp;url={encoder:encode(string($RequestURL))}">
                    <xsl:value-of select="i18n:translate('component.rc.slot.edit')" />
                  </a>
                </li>
                <xsl:if test="$hasAdminPermission or $isOwner">
                  <li role="presentation">
                    <a role="menuitem" tabindex="-1"
                      href="{$WebApplicationBaseURL}content/rc/edit-accesskeys.xed?slotId={@id}&amp;url={encoder:encode(string($RequestURL))}"
                    >
                      <xsl:value-of select="i18n:translate('component.rc.slot.edit.accesskeys')" />
                    </a>
                  </li>
                </xsl:if>
                <li class="divider" />
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
              <xsl:if test="not($hasAdminPermission) and not($writePermission)">
                <li role="presentation">
                  <a role="menuitem" tabindex="-1"
                    href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={encoder:encode(string($RequestURL))}"
                  >
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
        <xsl:text> - </xsl:text>
        <xsl:value-of select="$period//label[lang($CurrentLang)]/@description" />
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>