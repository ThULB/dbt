<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY html-output SYSTEM "xsl/xsl-output-html.fragment">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="i18n">

  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:param name="PageTitle" select="i18n:translate('component.session-listing.pageTitle')" />

  <!-- URL's -->
  <xsl:variable name="sessionListing.baseURL">
    <xsl:value-of select="concat($ServletsBaseURL,'MCRSessionListingServlet')" />
  </xsl:variable>
  <xsl:variable name="sessionListing.listSessionsURL">
    <xsl:value-of select="concat($sessionListing.baseURL,'?sessionListing.mode=listSessions')" />
  </xsl:variable>

  <!-- default sort -->
  <xsl:param name="sessionListing.sort" select="'userRealName'" />

  <!-- handle tabs reqs -->
  <xsl:param name="sessionListing.toc.pageSize" select="20" />
  <xsl:param name="sessionListing.toc.pos" select="1" />
  <xsl:variable name="sessionListing.toc.pos.verif">
    <xsl:call-template name="get.sessionListing.toc.pos.verif" />
  </xsl:variable>

  <!-- ============================================================================================================= -->

  <xsl:template match="/sessionListing">
    <xsl:choose>
      <xsl:when test="$direction = 'rtl' ">
        <table class="generalTableRtL" width="100%">
          <xsl:call-template name="sessionListing.summary" />
        </table>
      </xsl:when>
      <xsl:otherwise>
        <table class="generalTableLtR" width="100%">
          <xsl:call-template name="sessionListing.summary" />
        </table>
      </xsl:otherwise>
    </xsl:choose>
    <br />
    <xsl:call-template name="sessionListing.grouping" />
    <div class="table-responsive">
      <xsl:choose>
        <xsl:when test="$direction = 'rtl' ">
          <table class="table table-striped sessionTableRtL" width="100%">
            <xsl:call-template name="sessionListing.table.head" />
            <xsl:call-template name="sessionListing.table.body" />
          </table>
        </xsl:when>
        <xsl:otherwise>
          <table class="table table-striped sessionTableLtR" width="100%">
            <xsl:call-template name="sessionListing.table.head" />
            <xsl:call-template name="sessionListing.table.body" />
          </table>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.summary">
    <tr>
      <td colspan="7">
        <b>
          <xsl:value-of
            select="concat(i18n:translate('component.session-listing.userTotal'),' ',count(session),', ',i18n:translate('component.session-listing.userLoggedIn'),' ',count(session/login[text()!='gast']))" />
        </b>
      </td>
    </tr>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.table.head">
    <xsl:variable name="sortURL">
      <xsl:value-of select="concat($sessionListing.listSessionsURL,'&amp;XSL.sessionListing.sort.SESSION')" />
    </xsl:variable>

    <tr>
      <td>#</td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.name')" />
        <a href="{$sortURL}=userRealName" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.login')" />
        <a href="{$sortURL}=login" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.ip')" />
        <a href="{$sortURL}=ip" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.firstAccess')" />
        <a href="{$sortURL}=createTime" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.lastAccess')" />
        <a href="{$sortURL}=lastAccessTime" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
      <td class="sessionListingTableHead">
        <xsl:value-of select="i18n:translate('component.session-listing.loginSince')" />
        <a href="{$sortURL}=loginTime" alt="sortieren" title="sortieren" class="pull-right">
          <span class="glyphicon glyphicon-sort" />
        </a>
      </td>
    </tr>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.table.body">
    <xsl:for-each select="session">
      <xsl:sort select="*[name()=$sessionListing.sort]" order="ascending" />
      <xsl:call-template name="sessionListing.printSession" />
    </xsl:for-each>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.printSession">
    <xsl:variable name="rowLayout">
      <xsl:choose>
        <xsl:when test="position() mod 2 = 0">
          <xsl:value-of select="'listingSessionRowEven'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'listingSessionRowOdd'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="(position() &gt;= $sessionListing.toc.pos.verif) and ($sessionListing.toc.pos.verif+$sessionListing.toc.pageSize&gt;position())">
      <tr class="{$rowLayout}">
        <td class="listingSessionData">
          <xsl:value-of select="position()" />
        </td>
        <td class="listingSessionData">
          <b>
            <xsl:apply-templates select="userRealName" />
          </b>
        </td>
        <td class="listingSessionData">
          <xsl:apply-templates select="login" />
        </td>
        <td class="listingSessionData">
          <xsl:if test="string-length(ip) &gt; 0">
            <xsl:apply-templates select="ip" />
            <br />
          </xsl:if>
          <xsl:apply-templates select="hostname" />
        </td>
        <td class="listingSessionData">
          <xsl:apply-templates select="createTime" />
        </td>
        <td class="listingSessionData">
          <xsl:apply-templates select="lastAccessTime" />
        </td>
        <td class="listingSessionData">
          <xsl:apply-templates select="loginTime" />
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template match="login">
    <xsl:copy-of select="text()" />
  </xsl:template>
  <!-- ============================================================================================================= -->

  <xsl:template match="ip">
    <xsl:copy-of select="text()" />
  </xsl:template>
  <!-- ============================================================================================================= -->

  <xsl:template match="hostname">
    <xsl:copy-of select="text()" />
  </xsl:template>
  <!-- ============================================================================================================= -->

  <xsl:template match="userRealName">
    <xsl:copy-of select="text()" />
  </xsl:template>
  <!-- ============================================================================================================= -->

  <xsl:template match="createTime">
    <xsl:call-template name="sessionListing.formatLongTime">
      <xsl:with-param name="time" select="text()" />
    </xsl:call-template>
  </xsl:template>
  <!-- ============================================================================================================= -->

  <xsl:template match="lastAccessTime">
    <xsl:call-template name="sessionListing.formatLongTime">
      <xsl:with-param name="time" select="text()" />
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template match="loginTime">
    <xsl:call-template name="sessionListing.formatLongTime">
      <xsl:with-param name="time" select="text()" />
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.formatLongTime">
    <xsl:param name="time" />
    <xsl:variable name="isoTime">
      <xsl:value-of xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" select="mcrxml:getISODate($time, 'long' )" />
    </xsl:variable>
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="string-length(normalize-space($isoTime))=4">
          <xsl:value-of select="i18n:translate('component.session-listing.metaData.dateYear')" />
        </xsl:when>
        <xsl:when test="string-length(normalize-space($isoTime))=7">
          <xsl:value-of select="i18n:translate('component.session-listing.metaData.dateYearMonth')" />
        </xsl:when>
        <xsl:when test="string-length(normalize-space($isoTime))=10">
          <xsl:value-of select="i18n:translate('component.session-listing.metaData.dateYearMonthDay')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="i18n:translate('component.session-listing.metaData.dateTime')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="formatISODate">
      <xsl:with-param name="date" select="$isoTime" />
      <xsl:with-param name="format" select="$format" />
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.grouping">
    <form xmlns:encoder="xalan://java.net.URLEncoder" xmlns:xalan="http://xml.apache.org/xalan" action="{$sessionListing.baseURL}" accept-charset="UTF-8">
      <input type="hidden" name="sessionListing.mode" value="listSessions" />
      <xsl:choose>
        <xsl:when test="$direction = 'rtl' ">
          <table class="sessionListingGroupingRtL">
            <tr>
              <td>
                <b>
                  <xsl:value-of select="i18n:translate('component.session-listing.sizeOfTable')" />
                </b>
                <input name="XSL.sessionListing.toc.pageSize.SESSION" value="{$sessionListing.toc.pageSize}" size="3" />
                <xsl:value-of select="i18n:translate('component.session-listing.rowsPerPage')" />
                <xsl:call-template name="sessionListing.grouping.chooseHitPage">
                  <xsl:with-param name="children" select="." />
                </xsl:call-template>
              </td>
            </tr>
          </table>
        </xsl:when>
        <xsl:otherwise>
          <table class="sessionListingGroupingLtR">
            <tr>
              <td>
                <b>
                  <xsl:value-of select="i18n:translate('component.session-listing.sizeOfTable')" />
                </b>
                <input name="XSL.sessionListing.toc.pageSize.SESSION" value="{$sessionListing.toc.pageSize}" size="3" />
                <xsl:value-of select="i18n:translate('component.session-listing.rowsPerPage')" />
                <xsl:call-template name="sessionListing.grouping.chooseHitPage">
                  <xsl:with-param name="children" select="." />
                </xsl:call-template>
              </td>
            </tr>
          </table>
        </xsl:otherwise>
      </xsl:choose>
    </form>
  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="sessionListing.grouping.chooseHitPage">
    <xsl:param name="children" />

    <xsl:variable name="numberOfChildren">
      <xsl:value-of select="count(xalan:nodeset($children)/session)" />
    </xsl:variable>
    <xsl:variable name="numberOfHitPages">
      <xsl:value-of select="ceiling(number($numberOfChildren) div number($sessionListing.toc.pageSize))" />
    </xsl:variable>
    <xsl:if test="number($numberOfChildren)&gt;number($sessionListing.toc.pageSize)">
      <b>
        <xsl:value-of select="i18n:translate('component.session-listing.choosePage')" />
      </b>
      <xsl:for-each select="xalan:nodeset($children)/session[number($numberOfHitPages)&gt;=position()]">
        <xsl:variable name="jumpToPos">
          <xsl:value-of select="(position()*number($sessionListing.toc.pageSize))-number($sessionListing.toc.pageSize)" />
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="number($jumpToPos)+1=number($sessionListing.toc.pos)">
            <xsl:value-of select="concat(' [',position(),'] ')" />
          </xsl:when>
          <xsl:otherwise>
            <a href="{concat($sessionListing.listSessionsURL,'&amp;XSL.sessionListing.toc.pos.SESSION=',$jumpToPos+1)}" alt="{i18n:translate('component.session-listing.goToPage')}{concat(' ',position())}"
              title="{i18n:translate('component.session-listing.goToPage')} {concat(' ',position())}">
              <xsl:value-of select="concat(' ',position(),' ')" />
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>

  </xsl:template>

  <!-- ============================================================================================================= -->

  <xsl:template name="get.sessionListing.toc.pos.verif">
    <xsl:choose>
      <xsl:when test="$sessionListing.toc.pageSize&gt;count(/sessionListing/session)">
        <xsl:value-of select="1" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$sessionListing.toc.pos" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================================================= -->

</xsl:stylesheet>