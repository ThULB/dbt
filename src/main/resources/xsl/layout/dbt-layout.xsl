<?xml version="1.0" encoding="utf-8"?>
  <!-- ============================================== -->
  <!-- $Revision$ $Date$ -->
  <!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:basket="xalan://org.mycore.frontend.basket.MCRBasketManager" xmlns:mcr="http://www.mycore.org/"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:actionmapping="xalan://org.mycore.wfc.actionmapping.MCRURLRetriever"
  xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:mirver="xalan://org.mycore.mir.common.MIRCoreVersion" xmlns:dbtver="xalan://de.urmel_dl.dbt.common.DBTVersion"
  xmlns:encoder="xalan://java.net.URLEncoder" exclude-result-prefixes="xalan xlink basket actionmapping mcr mcrver mirver dbtver mcrxsl i18n encoder"
>
  <!-- ************************************************************ -->
  <!-- *                  additional stylesheets                  * -->
  <!-- ************************************************************ -->
  <xsl:import href="resource:xsl/layout/mir-common-layout.xsl" />
  <xsl:include href="dbt-navigation.xsl" />

  <xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" omit-xml-declaration="yes" media-type="text/html" version="5"
    encoding="UTF-8" />
  <xsl:strip-space elements="*" />

  <!-- ************************************************************ -->
  <!-- *           Parameters for MyCoRe LayoutService            * -->
  <!-- ************************************************************ -->
  <xsl:variable name="PageTitle" select="/*/@title" />

  <!-- ************************************************************ -->
  <!-- *            Optional includes within <head />             * -->
  <!-- ************************************************************ -->

  <xsl:variable name="include.HTML.Head.CSS" />
  <xsl:variable name="include.HTML.Head.JS" />
  
  <!-- ************************************************************ -->
  <!-- *                    Main HTML Elements                    * -->
  <!-- ************************************************************ -->
  <xsl:template match="/site">
    <html lang="{$CurrentLang}" class="no-js">
      <xsl:call-template name="HTML.Head" />
      <xsl:call-template name="HTML.Body" />
    </html>
  </xsl:template>

  <xsl:template name="HTML.Head">
    <head>
      <xsl:call-template name="layout.page.title" />
      <xsl:call-template name="layout.htmlContentType" />
      <link rel="icon" href="{$WebApplicationBaseURL}favicon.ico" />
      <xsl:call-template name="layout.cssLinks" />

      <xsl:copy-of select="head/*" />
    </head>
  </xsl:template>

  <xsl:template name="HTML.Body">
    <body>
      <header>
        <xsl:call-template name="layout.head" />
      </header>
      <xsl:call-template name="layout.content" />
      <footer>
        <xsl:call-template name="layout.footer" />
      </footer>

      <div id="container-overlay" class="collapse" data-toggle="collapse" data-target="#navbar,#container-overlay" aria-expanded="false"
        aria-controls="navbar"
      ></div>
      <xsl:call-template name="layout.scripts" />
    </body>
  </xsl:template>
  
  <!-- ************************************************************ -->
  <!-- *                    Main Page Elements                    * -->
  <!-- ************************************************************ -->
    
  <!-- HTML Content Type -->

  <xsl:template name="layout.htmlContentType">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  </xsl:template>

  <!-- CSS Links -->

  <xsl:template name="layout.cssLinks">
    <link href="{$WebApplicationBaseURL}dbt/assets/waves/waves.min.css" rel="stylesheet" />
    <link href="{$WebApplicationBaseURL}rsc/sass/scss/layout.min.css" rel="stylesheet" />
    <link rel="stylesheet" type="text/css" href="{$WebApplicationBaseURL}modules/webtools/upload/css/upload-gui.css" />

    <xsl:if test="$include.HTML.Head.CSS">
      <xsl:copy-of select="$include.HTML.Head.CSS" />
    </xsl:if>
  </xsl:template>

  <!-- Load JavaScripts -->

  <xsl:template name="layout.scripts">
    <script type="text/javascript">
      <xsl:value-of select="concat('var webApplicationBaseURL = &quot;', $WebApplicationBaseURL, '&quot;;')" disable-output-escaping="yes" />
      <xsl:value-of select="concat('var currentLang = &quot;', $CurrentLang, '&quot;;')" disable-output-escaping="yes" />
      window["mycoreUploadSettings"] = {
      <xsl:value-of select="concat('webAppBaseURL: &quot;', $WebApplicationBaseURL, '&quot;')" disable-output-escaping="yes" />
      }
    </script>

    <script type="text/javascript" src="{$WebApplicationBaseURL}assets/jquery/jquery.min.js" />
    <script type="text/javascript">
      <!-- Bootstrap & Query-Ui button conflict workaround  -->
      if (jQuery.fn.button){jQuery.fn.btn = jQuery.fn.button.noConflict();}
    </script>

    <script type="text/javascript" src="{$WebApplicationBaseURL}assets/bootstrap/js/bootstrap.bundle.min.js" />

    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/waves/waves.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/js/layout.min.js" />

    <script type="text/javascript" src="{$WebApplicationBaseURL}assets/jquery/plugins/jquery-confirm/jquery.confirm.min.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}js/mir/base.min.js"></script>

    <script src="{$WebApplicationBaseURL}js/mir/session-polling.js" type="text/javascript"></script>
    <script src="{$WebApplicationBaseURL}modules/webtools/upload/js/upload-api.js"></script>
    <script src="{$WebApplicationBaseURL}modules/webtools/upload/js/upload-gui.js"></script>
    
    <!-- extra scripts from each page -->
    <xsl:apply-templates select="//script" mode="html.scripts" />

    <xsl:if test="$include.HTML.Head.JS">
      <xsl:copy-of select="$include.HTML.Head.JS" />
    </xsl:if>

    <script type="text/javascript">
      $( document ).ready(function() {
      $('.overtext').tooltip();
      $.confirm.options = {
      <xsl:value-of select="concat('title: &quot;', i18n:translate('mir.confirm.title'), '&quot;,')" />
      <xsl:value-of select="concat('confirmButton: &quot;',i18n:translate('mir.confirm.confirmButton'), '&quot;,')" />
      <xsl:value-of select="concat('cancelButton: &quot;',i18n:translate('mir.confirm.cancelButton'), '&quot;,')" />
      post: false,
      confirmButtonClass: "btn-danger",
      cancelButtonClass: "btn-default",
      dialogClass: "modal-dialog modal-lg" // Bootstrap classes for large modal
      }
      $('*[data-toggle="tooltip"]').tooltip();
      });
    </script>
    <script src="{$WebApplicationBaseURL}assets/jquery/plugins/jquery-placeholder/jquery.placeholder.min.js"></script>
    <script>
      jQuery("input[placeholder]").placeholder();
      jQuery("textarea[placeholder]").placeholder();
    </script>
  </xsl:template>

  <!-- Page Title -->

  <xsl:template name="layout.page.title">
    <title>
      <xsl:if test="$PageTitle">
        <xsl:value-of select="concat($PageTitle,' – ')" />
      </xsl:if>
      <xsl:text>Digitale Bibliothek Thüringen</xsl:text>
    </title>
  </xsl:template>

  <xsl:template name="layout.head">
    <nav class="navbar navbar-expand-md navbar-light navbar-dbt fixed-top" role="navigation">
      <div class="container">
        <a class="navbar-brand" href="{concat($WebApplicationBaseURL,substring($loaded_navigation_xml/@hrefStartingPage,2),$HttpSession)}">
          <span class="img-placeholder"></span>
        </a>
        <button type="button" class="navbar-toggler collapsed plus-sign" data-toggle="collapse" data-target="#navbar,#container-overlay"
          aria-expanded="false" aria-controls="navbar"
        >
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <div id="navbar" class="navbar-collapse collapse">
          <xsl:call-template name="layout.head.mainMenu" />
        </div>
      </div>
    </nav>

    <div class="searchbar">
      <div class="container">
        <form class="navbar-form form-inline navbar-right" role="search" action="{$WebApplicationBaseURL}servlets/solr/find">
          <div class="input-group">
            <input class="form-control" type="text" id="searchTerm" name="condQuery" placeholder="{i18n:translate('dbt.search.placeholder')}" />
            <span class="input-group-btn">
              <button type="submit" class="btn btn-default">
                <span class="fa fa-search" aria-hidden="true"></span>
              </button>
            </span>
          </div>
        </form>
      </div>
    </div>

    <noscript>
      <div class="mir-no-script alert alert-warning text-center" style="border-radius: 0;">
        <xsl:value-of select="i18n:translate('mir.noScript.text')" />
        &#160;
        <a href="http://www.enable-javascript.com/de/" target="_blank">
          <xsl:value-of select="i18n:translate('mir.noScript.link')" />
        </a>
        .
      </div>
    </noscript>
  </xsl:template>

  <xsl:template name="layout.head.login">
    <li class="nav-item">
      <a class="nav-link" href="{$ServletsBaseURL}MCRLoginServlet?url={encoder:encode(string($RequestURL),'UTF-8')}">
        <i class="fa fa-sign-in-alt" aria-hidden="true"></i>
        <span class="d-inline d-xl-inline d-sm-inline d-md-none ml-1">
          <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />
        </span>
      </a>
    </li>
  </xsl:template>

  <xsl:template name="layout.head.logout">
    <xsl:variable name="userData" select="document('user:current')/user" />
    <xsl:variable name="userRoles">
      <xsl:for-each select="$userData/groups/group">
        <xsl:value-of select="@label" />
        <xsl:if test="position() != last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="userId">
      <xsl:choose>
        <xsl:when test="contains($CurrentUser,'@')">
          <xsl:value-of select="substring-before($CurrentUser,'@')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$CurrentUser" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="realm" select="document(concat('realm:', $userData/@realm))/realm" />
    <xsl:variable name="pwdChgURL">
      <xsl:choose>
        <xsl:when test="string-length($realm/passwordChangeURL) &gt; 0">
          <xsl:value-of select="$realm/passwordChangeURL" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($WebApplicationBaseURL, 'authorization/change-password.xed?action=password')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='user']">
      <xsl:with-param name="class" select="'nav-item d-none d-md-inline'" />
      <xsl:with-param name="dropdownClass" select="'dropdown-menu-right'" />
    </xsl:apply-templates>

    <li class="nav-item dropdown">
      <a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown">
        <i class="fa fa-user" aria-hidden="true"></i>
        <span class="d-inline d-xl-inline d-sm-inline d-md-none ml-1">
          <xsl:choose>
            <xsl:when test="$userData/realName">
              <xsl:value-of select="$userData/realName" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$userId" />
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </a>
      <div class="dropdown-menu dropdown-menu-right py-0" id="user-info">
        <dl class="row m-2 pt-2">
          <xsl:if test="$userData/realName">
            <dt class="col-3 p-0">
              <xsl:value-of select="i18n:translate('component.user2.admin.userAccount')" />
            </dt>
            <dd class="col-9 p-0 my-0 text-right">
              <xsl:value-of select="$userId" />
            </dd>
          </xsl:if>
          <xsl:if test="$userData/@realm != 'local'">
            <dt class="col-3 p-0">
              <xsl:value-of select="i18n:translate('component.user2.admin.user.realm')" />
            </dt>
            <dd class="col-9 p-0 my-0 text-right">
              <xsl:value-of select="$realm/label" />
            </dd>
          </xsl:if>
          <xsl:if test="$userData/eMail">
            <dt class="col-3 p-0">
              <xsl:value-of select="i18n:translate('component.user2.admin.user.email')" />
            </dt>
            <dd class="col-9 p-0 my-0 text-right">
              <xsl:value-of select="$userData/eMail" />
            </dd>
          </xsl:if>
          <xsl:if test="string-length($userRoles) &gt; 0">
            <dt class="col-3 p-0">
              <xsl:value-of select="i18n:translate('component.user2.admin.roles')" />
            </dt>
            <dd class="col-9 p-0 my-0 text-right">
              <xsl:value-of select="$userRoles" />
            </dd>
          </xsl:if>
        </dl>

        <div class="actions mx-2">
          <xsl:if test="$userData/@locked = 'false'">
            <a href="{$WebApplicationBaseURL}authorization/change-current-user.xed?action=save">
              <xsl:value-of select="i18n:translate('component.user2.admin.changedata')" />
            </a>
            <xsl:text>&#160;-&#160;</xsl:text>
          </xsl:if>
          <xsl:if test="($userData/@locked = 'false') or ($userData/@realm != 'local')">
            <a href="{$pwdChgURL}">
              <xsl:value-of select="i18n:translate('component.user2.admin.changepw')" />
            </a>
          </xsl:if>
        </div>

        <div class="dropdown-divider mb-0"></div>
        <div class="controls clearfix p-2">
          <a href="{$ServletsBaseURL}logout" class="btn btn-primary float-right">
            <span class="btn-label">
              <xsl:value-of select="i18n:translate('component.userlogin.button.logout')" />
            </span>
          </a>
        </div>

      </div>
    </li>
  </xsl:template>

  <xsl:template name="layout.head.mainMenu">
    <ul class="navbar-nav">
      <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='search']" />
      <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='browse']" />
      <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='rc']" />
      <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='publish']" />
      <li class="nav-item d-xs-inline d-sm-inline d-md-none">
        <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='top']//item">
          <xsl:with-param name="class" select="'nav-link'" />
          <xsl:with-param name="showIcon" select="false()" />
        </xsl:apply-templates>
      </li>
      <xsl:if test="not(mcrxsl:isCurrentUserGuestUser())">
        <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='user']">
          <xsl:with-param name="class" select="'nav-item d-xs-inline d-sm-inline d-md-none'" />
          <xsl:with-param name="linkClass" select="'nav-link'" />
        </xsl:apply-templates>
      </xsl:if>
      <xsl:call-template name="layout.head.basketMenu">
        <xsl:with-param name="class" select="'d-xs-inline d-sm-inline d-md-none'" />
      </xsl:call-template>
    </ul>
    <ul class="navbar-nav navbar-right">
      <xsl:call-template name="layout.head.basketMenu">
        <xsl:with-param name="class" select="'d-none d-md-inline'" />
        <xsl:with-param name="dropdownClass" select="'dropdown-menu-right'" />
      </xsl:call-template>
      <li class="nav-item d-none d-md-inline">
        <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='top']//item">
          <xsl:with-param name="class" select="'nav-link'" />
        </xsl:apply-templates>
      </li>

      <xsl:choose>
        <xsl:when test="mcrxsl:isCurrentUserGuestUser()">
          <xsl:call-template name="layout.head.login" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="layout.head.logout" />
        </xsl:otherwise>
      </xsl:choose>
    </ul>
  </xsl:template>

  <xsl:template name="layout.head.basketMenu">
    <xsl:param name="class" select="''" />
    <xsl:param name="dropdownClass" select="''" />

    <xsl:variable name="basketType" select="'objects'" />
    <xsl:variable name="basket" select="document(concat('basket:',$basketType))/basket" />
    <xsl:variable name="entryCount" select="count($basket/entry)" />
    <li class="nav-item dropdown {$class}" id="basket-list-item">
      <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" title="{i18n:translate('basket.title.objects')}">
        <i class="fa fa-bookmark" />
        <sup>
          <xsl:value-of select="$entryCount" />
        </sup>
      </a>
      <ul class="dropdown-menu {$dropdownClass}" role="menu" aria-labelledby="dLabel">
        <li class="dropdown-item disabled">
          <a>
            <xsl:choose>
              <xsl:when test="$entryCount = 0">
                <xsl:value-of select="i18n:translate('basket.numEntries.none')" disable-output-escaping="yes" />
              </xsl:when>
              <xsl:when test="$entryCount = 1">
                <xsl:value-of select="i18n:translate('basket.numEntries.one')" disable-output-escaping="yes" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="i18n:translate('basket.numEntries.many',$entryCount)" disable-output-escaping="yes" />
              </xsl:otherwise>
            </xsl:choose>
          </a>
        </li>
        <li class="dropdown-divider" />
        <li class="dropdown-item">
          <a href="{$ServletsBaseURL}MCRBasketServlet{$HttpSession}?type={$basket/@type}&amp;action=show">
            <xsl:value-of select="i18n:translate('basket.open')" />
          </a>
        </li>
      </ul>
    </li>
  </xsl:template>

  <xsl:template name="layout.content">
    <div id="container-main">
      <a id="top"></a>
      <div class="container">
        <xsl:call-template name="navigation.breadcrumbPath">
          <xsl:with-param name="navigation" select="$loaded_navigation_xml" />
        </xsl:call-template>
        
        <!-- include Internet Explorer warning -->
        <xsl:call-template name="msie-note" />

<!--           <xsl:call-template name="action.buttons" /> -->
        <xsl:call-template name="print.writeProtectionMessage" />
        <xsl:call-template name="print.statusMessage" />

        <xsl:choose>
          <xsl:when test="$readAccess='true'">
<!--               <xsl:copy-of select="@*[name() != 'titel']|node()[not(contains('|head|breadcrumb|scripts|', concat('|', name(), '|')))]" /> -->
            <xsl:apply-templates select="@*[name != 'title']|node()" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="printNotLoggedIn" />
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.footer">
    <div class="container">
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <ul class="nav-footer">
            <xsl:for-each select="$loaded_navigation_xml/menu[@id='footer']//item">
              <li>
                <xsl:apply-templates select=".">
                  <xsl:with-param name="class" select="''" />
                </xsl:apply-templates>
              </li>
            </xsl:for-each>
          </ul>
          <p class="info mb-2">
            <xsl:value-of select="i18n:translate('dbt.copyright')" />
            <i class="fas fa-info-circle text-info ml-2 pt-1" data-toggle="tooltip" data-html="true" data-placement="top"
              title="{concat('DBT: ', dbtver:getVersion(), ' (',dbtver:getAbbRev(), ')', '&lt;br&gt;MIR: ',mirver:getCompleteVersion())}"
            ></i>
          </p>
        </div>

        <div>
          <div class="btn-group dropup float-right language-select">
            <button class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown">
              <span class="language {$CurrentLang}">
                <xsl:value-of select="i18n:translate('navigation.Language')" />
              </span>
              <span class="caret" />
            </button>
            <div class="dropdown-menu dropdown-menu-right">
              <a href="#">
                <xsl:attribute name="class">
                  <xsl:text>dropdown-item language de</xsl:text>
                  <xsl:if test="$CurrentLang = 'de'">
                    <xsl:text> active</xsl:text>
                  </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="href">
                      <xsl:call-template name="UrlSetParam">
                        <xsl:with-param name="url" select="$RequestURL" />
                        <xsl:with-param name="par" select="'lang'" />
                        <xsl:with-param name="value" select="'de'" />
                      </xsl:call-template>
                    </xsl:attribute>
                <xsl:text>Deutsch</xsl:text>
              </a>
              <a href="#">
                <xsl:attribute name="class">
                  <xsl:text>dropdown-item language en</xsl:text>
                  <xsl:if test="$CurrentLang = 'en'">
                    <xsl:text> active</xsl:text>
                  </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="href">
                      <xsl:call-template name="UrlSetParam">
                        <xsl:with-param name="url" select="$RequestURL" />
                        <xsl:with-param name="par" select="'lang'" />
                        <xsl:with-param name="value" select="'en'" />
                      </xsl:call-template>
                    </xsl:attribute>
                <xsl:text>English</xsl:text>
              </a>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-center align-items-center" id="powered_by">
        <xsl:variable name="mcr_version" select="concat('MyCoRe ',mcrver:getCompleteVersion())" />
        <a href="http://www.mycore.de">
          <img alt="powered by MyCoRe" class="img-responsive"
            src="{$WebApplicationBaseURL}mir-layout/images/mycore_logo_powered_120x30_blaue_schrift_frei.png" title="{$mcr_version}" />
        </a>
      </div>

    </div>
  </xsl:template>

  <xsl:template match="head|breadcrumb|script" />

  <xsl:template match="script" mode="html.scripts">
    <xsl:copy-of select="." />
  </xsl:template>
  
  <!-- ************************************************************ -->
  <!-- *                      Action Buttons                      * -->
  <!-- ************************************************************ -->
  <xsl:variable name="actions" />

  <xsl:template name="action.buttons">
    <xsl:variable name="actions.combined" select="xalan:nodeset($actions)/action" />

    <xsl:if test="count($actions.combined) &gt; 0">
      <div class="actionButtons btn-group btn-group-sm float-right">
        <xsl:apply-templates select="$actions.combined" mode="action.buttons" />
      </div>
      <div class="clearfix" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="action" mode="action.buttons">
    <xsl:element name="a">
      <xsl:copy-of select="@*[starts-with(name(), 'data')]" />
      <xsl:if test="@openwin='true'">
        <xsl:attribute name="target">_blank</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="string-length(@style) &gt; 0">
            <xsl:value-of select="concat('btn btn-', @style)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'btn btn-default'" />
          </xsl:otherwise>
      </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="href">
        <xsl:value-of select="@target" />
        <xsl:for-each select="param">
          <xsl:choose>
            <xsl:when test="position() = 1">
              <xsl:value-of select="'?'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'&amp;'" />
            </xsl:otherwise>
          </xsl:choose>
          <xsl:value-of select="@name" />
          <xsl:value-of select="'='" />
          <xsl:value-of select="encoder:encode(string(@value),'UTF-8')" />
        </xsl:for-each>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@type='icon'">
          <xsl:attribute name="alt">
            <xsl:value-of select="@label" />
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of select="@label" />
          </xsl:attribute>
          <span class="glyphicon glyphicon-{@icon}" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
  
  <!-- Standard Copy Template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="child::node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
