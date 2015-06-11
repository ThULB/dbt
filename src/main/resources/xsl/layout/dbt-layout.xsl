<?xml version="1.0" encoding="utf-8"?>
  <!-- ============================================== -->
  <!-- $Revision$ $Date$ -->
  <!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:basket="xalan://org.mycore.frontend.basket.MCRBasketManager" xmlns:mcr="http://www.mycore.org/" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:actionmapping="xalan://org.mycore.wfc.actionmapping.MCRURLRetriever" xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion" xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:dbtver="xalan://org.urmel.dbt.common.DBTVersion" exclude-result-prefixes="xalan xlink basket actionmapping mcr mcrver dbtver mcrxsl i18n"
>
  <!-- ************************************************************ -->
  <!-- *                  additional stylesheets                  * -->
  <!-- ************************************************************ -->
  <xsl:import href="resource:xsl/layout/mir-common-layout.xsl" />
  <xsl:include href="resource:xsl/layout/dbt-navigation.xsl" />

  <xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" omit-xml-declaration="yes" media-type="text/html" version="5" encoding="UTF-8" />
  <xsl:strip-space elements="*" />

  <!-- ************************************************************ -->
  <!-- *           Parameters for MyCoRe LayoutService            * -->
  <!-- ************************************************************ -->
  <xsl:variable name="PageTitle" select="/*/@title" />
  <xsl:variable name="fontawesome.version" select="'4.0.3'" />
  
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
      <xsl:call-template name="layout.headMicrosoft" />
      <xsl:call-template name="layout.htmlContentType" />
      <xsl:call-template name="layout.noCaching" />
      <xsl:call-template name="layout.cssLinks" />
      <xsl:call-template name="layout.scripts" />

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
    </body>
  </xsl:template>
  
  <!-- ************************************************************ -->
  <!-- *                    Main Page Elements                    * -->
  <!-- ************************************************************ -->
    
  <!-- Microsoft -->

  <xsl:template name="layout.headMicrosoft">
    <meta http-equiv="cleartype" content="on" />
    <meta content="IE=9; IE=8" http-equiv="X-UA-Compatible" />
  </xsl:template>
  
  <!-- HTML Content Type -->

  <xsl:template name="layout.htmlContentType">
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  </xsl:template>

  <!-- No Caching -->

  <xsl:template name="layout.noCaching">
    <meta http-equiv="expires" content="0" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="pragma" content="no-cache" />
  </xsl:template>

  <!-- CSS Links -->

  <xsl:template name="layout.cssLinks">
    <link rel="stylesheet" href="{$WebApplicationBaseURL}dbt/css/layout.min.css" type="text/css" />
    <link rel="stylesheet" href="{$WebApplicationBaseURL}dbt/assets/smartmenus/addons/bootstrap/jquery.smartmenus.bootstrap.css" type="text/css" />

    <xsl:if test="$include.HTML.Head.CSS">
      <xsl:copy-of select="$include.HTML.Head.CSS" />
    </xsl:if>
  </xsl:template>

  <!-- Load JavaScripts -->

  <xsl:template name="layout.scripts">
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/jquery/plugins/jquery.selection.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/smartmenus/jquery.smartmenus.min.js"></script>

    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/smartmenus/addons/bootstrap/jquery.smartmenus.bootstrap.min.js"></script>

    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/js/layout.js"></script>

    <xsl:if test="$include.HTML.Head.JS">
      <xsl:copy-of select="$include.HTML.Head.JS" />
    </xsl:if>
  </xsl:template>

  <!-- Page Title -->

  <xsl:template name="layout.page.title">
    <title>
      <xsl:if test="$PageTitle">
        <xsl:value-of select="$PageTitle" />
      </xsl:if>
      <xsl:text> - Digitale Bibliothek Thüringen</xsl:text>
    </title>
  </xsl:template>

  <xsl:template name="layout.head">
    <nav class="navbar navbar-default navbar-dbt navbar-fixed-top" role="navigation">
      <div class="login-form">
        <div class="container-fluid">
          <xsl:choose>
            <xsl:when test="mcrxsl:isCurrentUserGuestUser()">
              <xsl:call-template name="layout.head.loginForm" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="layout.head.logoutForm" />
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
      <xsl:call-template name="layout.head.mainMenu" />
    </nav>
  </xsl:template>

  <xsl:template name="layout.head.loginForm">
    <form id="loginForm" action="{$ServletsBaseURL}MCRLoginServlet" method="post" class="form-inline pull-right" role="form">
      <input type="hidden" name="action" value="login" />
      <input type="hidden" name="realm" value="local" />
      <input type="hidden" name="url" value="{$RequestURL}" />
      <div class="form-group">
        <label class="sr-only" for="loginUID">
          <xsl:value-of select="i18n:translate('dbt.login.uid.placeholder')" />
        </label>
        <input type="text" class="input-sm" id="loginUID" name="uid" placeholder="{i18n:translate('dbt.login.uid.placeholder')}" />
      </div>
      <div class="form-group">
        <label class="sr-only" for="loginPWD">
          <xsl:value-of select="i18n:translate('dbt.login.pwd.placeholder')" />
        </label>
        <input type="password" class="input-sm" id="loginPWD" name="pwd" placeholder="{i18n:translate('dbt.login.pwd.placeholder')}" />
      </div>
      <button type="submit" class="btn btn-primary" name="login">
        <xsl:value-of select="i18n:translate('component.userlogin.button.login')" />
      </button>
    </form>
  </xsl:template>

  <xsl:template name="layout.head.logoutForm">
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

    <ul class="nav nav-userinfo pull-right">
      <li class="dropdown">
        <a id="userActions" class="dropdown-toggle" data-toggle="dropdown" href="#">
          <span class="glyphicon glyphicon-tasks" />
          <span class="caret" />
        </a>
        <ul class="dropdown-menu pull-right no-padding" role="menu">
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='user']/*" />
        </ul>
      </li>
      <li class="dropdown">
        <a class="dropdown-toggle" href="#" data-toggle="dropdown">
          <xsl:choose>
            <xsl:when test="$userData/realName">
              <xsl:value-of select="$userData/realName" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$userId" />
            </xsl:otherwise>
          </xsl:choose>
          <b class="caret"></b>
        </a>
        <div class="dropdown-menu pull-right no-padding">
          <div id="user-info" class="panel">
            <div class="panel-body">
              <xsl:if test="$userData/realName">
                <strong>
                  <xsl:value-of select="i18n:translate('component.user2.admin.userAccount')" />
                </strong>
                <span class="pull-right">
                  <xsl:value-of select="$userId" />
                </span>
                <br />
              </xsl:if>
              <xsl:if test="$userData/eMail">
                <strong>
                  <xsl:value-of select="i18n:translate('component.user2.admin.user.email')" />
                </strong>
                <span class="pull-right">
                  <xsl:value-of select="$userData/eMail" />
                </span>
                <br />
              </xsl:if>
              <xsl:if test="string-length($userRoles) &gt; 0">
                <strong>
                  <xsl:value-of select="i18n:translate('component.user2.admin.roles')" />
                </strong>
                <span class="pull-right">
                  <xsl:value-of select="$userRoles" />
                </span>
                <br />
              </xsl:if>
              <a href="{$WebApplicationBaseURL}authorization/change-user.xed?action=save&amp;id=current">
                <xsl:value-of select="i18n:translate('component.user2.admin.changedata')" />
              </a>
              <xsl:text>&#160;-&#160;</xsl:text>
              <a href="{$WebApplicationBaseURL}authorization/change-password.xed?action=password">
                <xsl:value-of select="i18n:translate('component.user2.admin.changepw')" />
              </a>
            </div>
            <div class="panel-footer">
              <a href="{$ServletsBaseURL}logout" class="btn btn-primary btn-sm">
                <span class="btn-label">
                  <xsl:value-of select="i18n:translate('component.userlogin.button.logout')" />
                </span>
              </a>
            </div>
          </div>
        </div>
      </li>
    </ul>
  </xsl:template>

  <xsl:template name="layout.head.mainMenu">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="{concat($WebApplicationBaseURL,substring($loaded_navigation_xml/@hrefStartingPage,2),$HttpSession)}"></a>
      </div>
      <div class="navbar-collapse collapse navbar-ex1-collapse">
        <ul class="nav navbar-nav">
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='search']" />
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='browse']" />
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='rc']" />
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='publish']" />
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='main']" />
          <xsl:call-template name="mir.basketMenu" />
        </ul>
        <form id="searchForm" action="{$WebApplicationBaseURL}servlets/solr/find?qry={0}" class="navbar-form navbar-right visible-xs visible-md visible-lg"
          role="search"
        >
          <div class="input-group input-group-sm">
            <input class="form-control" type="text" id="searchTerm" name="qry" placeholder="{i18n:translate('dbt.search.placeholder')}" />
            <div class="input-group-btn">
              <button class="btn btn-default" type="submit" name="search">
                <span class="glyphicon glyphicon-search"></span>
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.content">
    <div id="containerBackground">
      <a name="All" />
      <div class="container">
        <div id="main">
<!--         <xsl:call-template name="layout.breadcrumbPath" /> -->
<!--           <xsl:call-template name="action.buttons" /> -->

          <xsl:call-template name="print.writeProtectionMessage" />
          <xsl:choose>
            <xsl:when test="$readAccess='true'">
              <xsl:copy-of select="*[not(name()='head')]" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="printNotLoggedIn" />
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.footer">
    <div class="container-fluid">
      <div class="pull-left">
        <ul class="nav-footer">
          <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='below']//item" />
        </ul>
        <p class="credit">
          <xsl:value-of select="i18n:translate('dbt.copyright')" />
        </p>
      </div>

      <div class="pull-right">
        <p>
          <xsl:call-template name="layout.footer.lastModified" />
        </p>
        <div class="pull-right">
          <form id="langSelect" method="get">
            <input type="hidden" name="lang" value="{$CurrentLang}" />
            <div class="btn-group dropup pull-right">
              <button class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                <span class="language {$CurrentLang}">
                  <xsl:value-of select="i18n:translate('navigation.Language')" />
                </span>
                <span class="caret"></span>
              </button>
              <ul class="dropdown-menu">
                <li>
                  <xsl:if test="$CurrentLang = 'de'">
                    <xsl:attribute name="class">active</xsl:attribute>
                  </xsl:if>
                  <a href="#" class="language de">
                    <xsl:attribute name="href">
                                            <xsl:call-template name="UrlSetParam">
                                                <xsl:with-param name="url" select="$RequestURL" />
                                                <xsl:with-param name="par" select="'lang'" />
                                                <xsl:with-param name="value" select="'de'" />
                                            </xsl:call-template>
                                        </xsl:attribute>
                    <xsl:text>Deutsch</xsl:text>
                  </a>
                </li>
                <li>
                  <xsl:if test="$CurrentLang = 'en'">
                    <xsl:attribute name="class">active</xsl:attribute>
                  </xsl:if>
                  <a href="#" class="language en">
                    <xsl:attribute name="href">
                                            <xsl:call-template name="UrlSetParam">
                                                <xsl:with-param name="url" select="$RequestURL" />
                                                <xsl:with-param name="par" select="'lang'" />
                                                <xsl:with-param name="value" select="'en'" />
                                            </xsl:call-template>
                                        </xsl:attribute>
                    <xsl:text>English</xsl:text>
                  </a>
                </li>
              </ul>
            </div>
          </form>
        </div>
      </div>
    </div>
  </xsl:template>
  
  <!-- ************************************************************ -->
    <!-- *                      Action Buttons                      * -->
    <!-- ************************************************************ -->
  <xsl:variable name="actions" />

  <xsl:template name="action.buttons">
    <xsl:variable name="actions.combined" select="xalan:nodeset($actions)/action" />

    <xsl:if test="count($actions.combined) > 0">
      <div class="actionButtons btn-group btn-group-sm pull-right">
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
                    <xsl:value-of xmlns:encoder="xalan://java.net.URLEncoder" select="encoder:encode(string(@value),'UTF-8')" />
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
          <span class="glyphicon glyphicon-{@icon}"></span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@label" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:variable name="pageLastModified" />

  <xsl:template name="layout.footer.lastModified">
    <xsl:if test="string-length($pageLastModified) &gt; 0">
      <xsl:value-of select="i18n:translate('webpage.modified')" />
      <xsl:text>: </xsl:text>
      <xsl:choose>
        <xsl:when test="$CurrentLang = 'de'">
          <xsl:value-of select="substring($pageLastModified,9,2)" />
          <xsl:text>.</xsl:text>
          <xsl:value-of select="substring($pageLastModified,6,2)" />
          <xsl:text>.</xsl:text>
          <xsl:value-of select="substring($pageLastModified,1,4)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$pageLastModified" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text> - </xsl:text>
    </xsl:if>
    <xsl:value-of select="concat('Version ',dbtver:getCompleteVersion())" />
  </xsl:template>

</xsl:stylesheet>