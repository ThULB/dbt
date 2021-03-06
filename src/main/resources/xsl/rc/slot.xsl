<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:mgr="xalan://de.urmel_dl.dbt.rc.persistency.SlotManager" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions" xmlns:xlink="http://www.w3.org/1999/xlink"
  exclude-result-prefixes="mgr encoder i18n mcrxsl xlink"
>

  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:include href="slot-templates.xsl" />
  <xsl:include href="slot-entries.xsl" />

  <xsl:variable name="slotId" select="/slot/@id" />
  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slot.pageTitle', concat(/slot/title, ';', $slotId))" />

  <xsl:template match="/slot">
    <xsl:apply-templates mode="slotHead" select="." />
    <div class="slot-body mt-3">
      <xsl:choose>
        <xsl:when test="mcrxsl:isCurrentUserGuestUser() and not($readPermission)">
          <xsl:variable name="loginURL"
            select="concat( $ServletsBaseURL, 'MCRLoginServlet',$HttpSession,'?url=', encoder:encode( string( $RequestURL ) ) )" />
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_access')" />
            <xsl:text> </xsl:text>
            <a id="loginURL" href="{$loginURL}">
              <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />
            </a>
          </div>
        </xsl:when>
        <xsl:when test="not($readPermission or $writePermission)">
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.no_accesskey')" />
            <p>
              <a href="{$WebApplicationBaseURL}authorization/accesskey.xed?objId={$objectId}&amp;url={encoder:encode(string($RequestURL))}">
                <xsl:value-of select="i18n:translate('component.rc.slot.enter_accesskey')" />
              </a>
            </p>
          </div>
        </xsl:when>
        <xsl:when test="@pendingStatus = 'ownerTransfer'">
          <div class="alert alert-warning" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.message.ownerTransfer')" />
            <p>
              <a href="{$WebApplicationBaseURL}content/rc/slot.xed?action=ownerTransfer&amp;slotId={@id}&amp;url={encoder:encode(string($RequestURL))}">
                <xsl:value-of select="i18n:translate('component.rc.slot.ownerTransfer')" />
              </a>
            </p>
          </div>
        </xsl:when>
        <xsl:when test="(@pendingStatus = 'free') and not($isActive)">
          <div class="alert alert-danger" role="alert">
            <xsl:value-of select="i18n:translate('component.rc.slot.message.delete')" />
          </div>
          <xsl:if test="$hasAdminPermission or $hasEditorPermission">
            <xsl:apply-templates select="entries" />
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="$writePermission and (@pendingStatus = 'validating')">
            <div class="alert alert-warning">
              <p>
                <xsl:value-of disable-output-escaping="yes" select="i18n:translate('component.rc.slot.message.validating')" />
              </p>
              <br />
              <a href="{$WebApplicationBaseURL}content/rc/slot.xed?action=reactivateComplete&amp;slotId={@id}&amp;url={encoder:encode(string($RequestURL))}">
                <xsl:value-of select="i18n:translate('component.rc.slot.reactivate.complete')" />
              </a>
            </div>
          </xsl:if>
          <xsl:apply-templates select="entries" />
          <xsl:if test="count(entries) = 0 and $effectiveMode = 'edit'">
            <xsl:call-template name="addNewEntry" />
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </div>

    <script type="text/javascript" src="{$WebApplicationBaseURL}content/rc/js/sticky-toc.min.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/media/player.min.js"></script>
    <script type="text/javascript">
      <xsl:value-of select="concat('var webappBaseURL = &quot;', $WebApplicationBaseURL, '&quot;;')" disable-output-escaping="yes" />
    </script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}content/rc/js/player.min.js"></script>

    <xsl:if test="$effectiveMode = 'edit'">
      <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/jquery/plugins/jquery-sortable-min.js" />
      <script type="text/javascript">
        $( document ).ready(function() {
        <xsl:value-of select="concat('var servletsBaseURL = &quot;', $ServletsBaseURL, '&quot;;')" disable-output-escaping="yes" />
        <xsl:value-of select="concat('var slotId = &quot;', $slotId, '&quot;;')" disable-output-escaping="yes" />
          <![CDATA[
          var oldData;
          var slotEntries = $("div.slot-section").sortable({
            group: 'slot-entries',
            containerSelector: '.slot-section',
            itemPath: ".card-body",
            itemSelector: '.media',
            handle: '.entry-mover',
            placeholderClass: 'entry-placeholder',
            placeholder: '<div class="media entry-placeholder"><div class="media-body mw-100"></div></div>',
            pullPlaceholder: true,
            nested: true,
    
            // set item relative to cursor position
            onDragStart: function ($item, container, _super) {
              var offset = $item.offset(),
                pointer = container.rootGroup.pointer,
                placeholder = container.rootGroup.placeholder;
    
              oldData = slotEntries.sortable("serialize").get().join();
    
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
              if (isContainer && parent.find(".entry-headline") && parent.find(".entry-headline").attr("id")) {
                children = [parent.find(".entry-headline").attr("id")].concat(children);
              }
    
              return isContainer ? 
                children : 
                  parent.find("div[class|='entry'][class!='entry-buttons'][class!='entry-infoline'][class!='entry-placeholder']").attr("id");
            },
    
            onDrop: function ($item, container, _super) {
              var data = slotEntries.sortable("serialize").get().join();
    
              if (oldData !== data) {
                $.post(servletsBaseURL + "RCSlotServlet", { 'action': 'order', 'slotId': slotId, 'items': data });
                $item.removeClass("dragged");
                $item.removeAttr("style");
              }
              _super($item, container);
            }
          });
          ]]>
        });
      </script>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>