<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:decoder="xalan://java.net.URLDecoder"
  exclude-result-prefixes="xalan decoder"
>

  <xsl:param name="Referer" />

  <xsl:template match="/navigation//label">
  </xsl:template>

  <xsl:template match="/navigation//menu[@id and (group[item] or item)]">
    <xsl:param name="class" select="''" />
    <xsl:param name="active" select="descendant-or-self::item[@href = $browserAddress]" />

    <xsl:variable name="menuId" select="generate-id(.)" />
    <li class="dropdown {$class}">
      <xsl:if test="$active">
        <xsl:attribute name="class">
          <xsl:value-of select="concat($class,' dropdown active')" />
        </xsl:attribute>
      </xsl:if>
      <a id="{$menuId}" class="dropdown-toggle" data-toggle="dropdown" href="#">
        <xsl:apply-templates select="." mode="linkText" />
        <span class="caret"></span>
      </a>
      <ul class="dropdown-menu" role="menu" aria-labelledby="{$menuId}">
        <xsl:apply-templates select="item|group" />
      </ul>
    </li>
  </xsl:template>

  <xsl:template match="/navigation//group[@id and item]">
    <xsl:param name="rootNode" select="." />
    <xsl:if test="name(preceding-sibling::*[1])='item'">
      <li role="presentation" class="divider" />
    </xsl:if>
    <xsl:if test="label">
      <li role="presentation" class="dropdown-header">
        <xsl:apply-templates select="." mode="linkText" />
      </li>
    </xsl:if>
    <xsl:apply-templates />
    <xsl:if test="position() != last()">
      <li role="presentation" class="divider" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="/navigation//item[@href]">
    <xsl:param name="class" select="''" />
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
        <li>
          <xsl:attribute name="class">
            <xsl:value-of select="$class" />
            <xsl:if test="$active">
              <xsl:if test="string-length($class) &gt; 0">
                <xsl:text> </xsl:text>
              </xsl:if>
              <xsl:value-of select="'active'" />
            </xsl:if>
          </xsl:attribute>
          <a href="{$url}">
            <xsl:apply-templates select="." mode="linkText" />
          </a>
        </li>
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
            <xsl:value-of select="concat('/', substring-after(decoder:decode(string($URLParam),'UTF-8'), $WebApplicationBaseURL))" />
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
          <xsl:apply-templates select="xalan:nodeset($activeItem)" mode="breadcrumbItem">
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
      <xsl:when test="(concat($WebApplicationBaseURL, substring-after(@href,'/')) = $RequestURL) or (concat($WebApplicationBaseURL, @href) = $RequestURL)">
        <li class="active">
          <xsl:value-of select="./label[lang($CurrentLang)]" />
        </li>
      </xsl:when>
      <xsl:when test="name(.) = 'menu'">
        <xsl:apply-templates select="." />
      </xsl:when>
      <xsl:otherwise>
        <li>
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