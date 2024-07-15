<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="Referer" />

  <xsl:template match="/navigation//label">
  </xsl:template>

  <xsl:template match="/navigation//menu[@id and (group[item] or item)]">
    <xsl:param name="class" select="'nav-item'" />
    <xsl:param name="linkClass" select="'nav-link'" />
    <xsl:param name="dropdownClass" select="''" />
    <xsl:param name="showIcon" select="string-length(icon) &gt; 0" />
    <xsl:param name="active" select="descendant-or-self::item[@href = $browserAddress]" />

    <xsl:variable name="menuId" select="generate-id(.)" />
    <li class="{$class} dropdown ">
      <xsl:if test="$active">
        <xsl:attribute name="class">
          <xsl:value-of select="concat($class,' dropdown active')" />
        </xsl:attribute>
      </xsl:if>
      <a id="{$menuId}" class="{$linkClass} dropdown-toggle" data-toggle="dropdown" href="#">
        <xsl:choose>
          <xsl:when test="$showIcon and string-length(icon) &gt; 0">
            <i class="{icon}" aria-hidden="true"></i>
            <span class="d-inline d-sm-none d-lg-inline">
              <xsl:apply-templates select="." mode="linkText" />
            </span>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="linkText" />
          </xsl:otherwise>
        </xsl:choose>
      </a>
      <div class="dropdown-menu {$dropdownClass}" role="menu" aria-labelledby="{$menuId}">
        <xsl:apply-templates select="item|group">
          <xsl:with-param name="class" select="'dropdown-item'" />
        </xsl:apply-templates>
      </div>
    </li>
  </xsl:template>

  <xsl:template match="/navigation//group[@id and item]">
    <xsl:param name="rootNode" select="." />
    <xsl:if test="name(preceding-sibling::*[1])='item'">
      <div role="presentation" class="dropdown-divider" />
    </xsl:if>
    <xsl:if test="label">
      <div class="dropdown-header">
        <xsl:apply-templates select="." mode="linkText" />
      </div>
    </xsl:if>
    <xsl:apply-templates select="item">
      <xsl:with-param name="class" select="'dropdown-item'" />
    </xsl:apply-templates>
    <xsl:if test="position() != last()">
      <div role="presentation" class="dropdown-divider" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="/navigation//item[@href]">
    <xsl:param name="class" select="'dropdown-item'" />
    <xsl:param name="showIcon" select="string-length(icon) &gt; 0" />
    <xsl:param name="active" select="descendant-or-self::item[@href = $browserAddress ]" />
    <xsl:param name="url">
      <xsl:choose>
        <!-- item @type is "intern" -> add the web application path before the link -->
        <xsl:when test=" starts-with(@href,'http:') or starts-with(@href,'https:') or starts-with(@href,'mailto:') or starts-with(@href,'ftp:')">
          <xsl:value-of select="@href" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="UrlAddSession">
            <xsl:with-param name="url" select="concat($WebApplicationBaseURL,substring-after(@href,'/'))" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="string-length($url) &gt; 0">
        <a href="{$url}">
          <xsl:attribute name="class">
            <xsl:value-of select="$class" />
            <xsl:if test="$active">
              <xsl:if test="string-length($class) &gt; 0">
                <xsl:text> </xsl:text>
              </xsl:if>
              <xsl:value-of select="'active'" />
            </xsl:if>
          </xsl:attribute>

          <xsl:choose>
            <xsl:when test="$showIcon and string-length(icon) &gt; 0">
              <i class="{icon}" aria-hidden="true"></i>
              <span class="d-none d-lg-inline">
                <xsl:apply-templates select="." mode="linkText" />
              </span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="." mode="linkText" />
            </xsl:otherwise>
          </xsl:choose>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>
          <xsl:apply-templates select="." mode="linkText" />
        </xsl:comment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/navigation//*[label]" mode="linkText">
    <xsl:choose>
      <xsl:when test="label[lang($CurrentLang)] != ''">
        <xsl:value-of select="label[lang($CurrentLang)]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="label[lang($DefaultLang)]" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ************************************************************ -->
  <!-- * Breadcrumb Path -->
  <!-- ************************************************************ -->

  <xsl:template name="navigation.breadcrumbPath">
    <xsl:param name="navigation" />

    <xsl:variable name="referAddress">
      <xsl:variable name="URLParam">
        <xsl:call-template name="UrlGetParam">
          <xsl:with-param name="url" select="$Referer" />
          <xsl:with-param name="par" select="'url'" />
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="address">
        <xsl:choose>
          <xsl:when test="string-length($URLParam) &gt; 0">
            <xsl:value-of select="concat('/', substring-after(($URLParam), $WebApplicationBaseURL))" disable-output-escaping="yes" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('/', substring-after($Referer, $WebApplicationBaseURL))" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="contains($address, '?')">
          <xsl:value-of select="substring-before($address, '?')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$address" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="currentAddress">
      <xsl:variable name="address" select="concat('/', substring-after($RequestURL, $WebApplicationBaseURL))" />
      <xsl:choose>
        <xsl:when test="contains($address, '?')">
          <xsl:value-of select="substring-before($address, '?')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$address" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="referItem" select="$navigation/descendant-or-self::item[starts-with(@href, $referAddress)]" />
    <xsl:variable name="prevItem"
      select="$navigation/descendant-or-self::item[starts-with($currentAddress, @href)]|$navigation/descendant-or-self::item[starts-with($referAddress, @href)]" />
    <xsl:variable name="currentItem" select="$navigation/descendant-or-self::item[starts-with(@href, $currentAddress)]" />

    <!--
    <xsl:message>
      RequestURL:
      <xsl:value-of select="$RequestURL" />
      Referer:
      <xsl:value-of select="$Referer" />
      currentAddress:
      <xsl:value-of select="$currentAddress" />
      referAddress:
      <xsl:value-of select="$referAddress" />
      referItem:
      <xsl:value-of select="$referItem" />
      prevItem:
      <xsl:value-of select="$prevItem" />
      currentItem:
      <xsl:value-of select="$currentItem" />
    </xsl:message>
    -->

    <xsl:if
      test="$currentAddress != $navigation/@hrefStartingPage and (count($referItem/ancestor-or-self::*[name() != 'group']) != 0 or count($prevItem/ancestor-or-self::*[name() != 'group']) != 0 or count($currentItem/ancestor-or-self::*[name() != 'group']) != 0)"
    >
      <ol class="breadcrumb">
        <xsl:choose>
          <xsl:when test="count($currentItem/ancestor-or-self::*[name() != 'group']) != 0">
            <xsl:apply-templates select="$currentItem/ancestor-or-self::*[name() != 'group']" mode="breadcrumbItem">
              <xsl:with-param name="navigation" select="$navigation" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="count($prevItem/ancestor-or-self::*[name() != 'group']) != 0">
            <xsl:apply-templates select="$prevItem/ancestor-or-self::*[name() != 'group']" mode="breadcrumbItem">
              <xsl:with-param name="navigation" select="$navigation" />
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="count($referItem) != 0">
            <xsl:apply-templates select="$referItem[1]" mode="breadcrumbItem">
              <xsl:with-param name="navigation" select="$navigation" />
            </xsl:apply-templates>
          </xsl:when>
        </xsl:choose>
        <xsl:if test="count($prevItem/ancestor-or-self::*[name() != 'group']) != 0 and count($currentItem/ancestor-or-self::*[name() != 'group']) = 0">
          <xsl:variable name="activeItem">
            <item href="{concat('/', substring-after($RequestURL, $WebApplicationBaseURL))}">
              <label xml:lang="{$CurrentLang}">
                <xsl:call-template name="ShortenText">
                  <xsl:with-param name="text" select="$PageTitle" />
                  <xsl:with-param name="length" select="22" />
                </xsl:call-template>
              </label>
            </item>
          </xsl:variable>
          <xsl:apply-templates select="$activeItem" mode="breadcrumbItem">
            <xsl:with-param name="navigation" select="$navigation" />
          </xsl:apply-templates>
        </xsl:if>
      </ol>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="breadcrumbItem">
    <xsl:param name="navigation" />

    <xsl:variable name="href">
      <xsl:variable name="url">
        <xsl:choose>
          <xsl:when test="name(.) = 'navigation'">
            <xsl:value-of select="@hrefStartingPage" />
          </xsl:when>
          <xsl:when test="string-length(@href) &gt; 0">
            <xsl:value-of select="@href" />
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length($url) != 0 and starts-with($url, '/')">
          <xsl:value-of select="concat($WebApplicationBaseURL, substring-after($url, '/'))" />
        </xsl:when>
        <xsl:when test="string-length($url) != 0">
          <xsl:value-of select="concat($WebApplicationBaseURL, $url)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'#'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:choose>
      <xsl:when
        test="(concat($WebApplicationBaseURL, substring-after(@href,'/')) = $RequestURL) or (concat($WebApplicationBaseURL, @href) = $RequestURL)"
      >
        <li class="breadcrumb-item active">
          <xsl:value-of select="./label[lang($CurrentLang)]" />
        </li>
      </xsl:when>
      <xsl:when test="name(.) = 'menu'">
        <xsl:apply-templates select=".">
          <xsl:with-param name="class" select="'breadcrumb-item'" />
          <xsl:with-param name="linkClass" select="''" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <li class="breadcrumb-item">
          <a href="{$href}">
            <xsl:choose>
              <xsl:when test="name(.) = 'navigation'">
                <xsl:variable name="hrefStartingPage" select="@hrefStartingPage" />
                <xsl:value-of select="$navigation//item[@href=$hrefStartingPage]/label[lang($CurrentLang)]" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="./label[lang($CurrentLang)]" />
              </xsl:otherwise>
            </xsl:choose>
          </a>
        </li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
