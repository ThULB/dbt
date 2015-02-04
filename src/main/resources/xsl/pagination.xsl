<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n"
>
  <xsl:template name="paginate">
    <xsl:param name="id" select="'pagination'" />
    <xsl:param name="i18nprefix" select="'pagination'" />
    <xsl:param name="extraStyles" select="''" />
    <xsl:param name="pages" />

    <ul id="{$id}_paginate" class="pagination pagination-sm {$extraStyles}">
      <li>
        <xsl:choose>
          <xsl:when test="number($Page) &gt; 1">
            <a tabindex="0" id="{$id}_first">
              <xsl:attribute name="href">
                  <xsl:call-template name="paginateQuerystring">
                    <xsl:with-param name="page" select="1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text disable-output-escaping="yes">&amp;laquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.first'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">disabled</xsl:attribute>
            <span>
              <xsl:text disable-output-escaping="yes">&amp;laquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.first'))" />
              </span>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </li>
      <li>
        <xsl:choose>
          <xsl:when test="number($Page) &gt; 1">
            <a tabindex="0" id="{$id}_previous">
              <xsl:attribute name="href">
                  <xsl:call-template name="paginateQuerystring">
                    <xsl:with-param name="page" select="$Page - 1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text disable-output-escaping="yes">&amp;lsaquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.previous'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">disabled</xsl:attribute>
            <span>
              <xsl:text disable-output-escaping="yes">&amp;lsaquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.previous'))" />
              </span>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </li>

      <!-- variable can be used to change numbers of Page entries to display -->
      <xsl:variable name="paginateMaxEntries" select="5" />
      <xsl:variable name="paginateBackCount" select="ceiling($paginateMaxEntries div 2)" />
      <xsl:variable name="paginatePrevCount" select="$paginateMaxEntries - ceiling($paginateMaxEntries div 2)" />

      <xsl:variable name="paginateStart">
        <xsl:choose>
          <xsl:when test="($Page - $paginateBackCount) &lt;= 1">
            <xsl:value-of select="1" />
          </xsl:when>
          <xsl:when test="($Page + $paginatePrevCount) &gt; $pages">
            <xsl:value-of select="$Page - $paginateBackCount - (($Page + $paginatePrevCount) - $pages)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$Page - $paginateBackCount" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="paginateEnd">
        <xsl:choose>
          <xsl:when test="($Page + $paginatePrevCount + 1) &gt;= $pages">
            <xsl:value-of select="$pages" />
          </xsl:when>
          <xsl:when test="($Page + $paginatePrevCount) &lt; $paginateMaxEntries">
            <xsl:value-of select="$paginateMaxEntries" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$Page + $paginatePrevCount" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:call-template name="paginateEntries">
        <xsl:with-param name="pages" select="$pages" />
        <xsl:with-param name="paginateStart" select="$paginateStart" />
        <xsl:with-param name="paginateEnd" select="$paginateEnd" />
      </xsl:call-template>

      <li>
        <xsl:choose>
          <xsl:when test="number($Page) &lt; $pages">
            <a tabindex="0" id="{$id}_next">
              <xsl:attribute name="href">
                  <xsl:call-template name="paginateQuerystring">
                    <xsl:with-param name="page" select="$Page + 1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text disable-output-escaping="yes">&amp;rsaquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.next'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">disabled</xsl:attribute>
            <span>
              <xsl:text disable-output-escaping="yes">&amp;rsaquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.next'))" />
              </span>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </li>
      <li>
        <xsl:choose>
          <xsl:when test="number($Page) &lt; $pages">
            <a tabindex="0" id="{$id}_last">
              <xsl:attribute name="href">
                <xsl:call-template name="paginateQuerystring">
                  <xsl:with-param name="page" select="$pages" />
                </xsl:call-template>
              </xsl:attribute>
              <xsl:text disable-output-escaping="yes">&amp;raquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.last'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">disabled</xsl:attribute>
            <span>
              <xsl:text disable-output-escaping="yes">&amp;raquo;</xsl:text>
              <span class="sr-only">
                <xsl:value-of select="i18n:translate(concat($i18nprefix, '.last'))" />
              </span>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </li>
    </ul>
  </xsl:template>

  <xsl:template name="paginateEntries">
    <xsl:param name="pages" />
    <xsl:param name="paginateStart" />
    <xsl:param name="paginateEnd" />

    <xsl:if test="$paginateStart = number($Page)">
      <li class="active">
        <span>
          <xsl:value-of select="$paginateStart" />
        </span>
      </li>
    </xsl:if>
    <xsl:if test="$paginateStart != number($Page)">
      <li>
        <a tabindex="0">
          <xsl:attribute name="href">
            <xsl:call-template name="paginateQuerystring">
              <xsl:with-param name="page" select="$paginateStart" />
            </xsl:call-template>
          </xsl:attribute>
          <xsl:value-of select="$paginateStart" />
        </a>
      </li>
    </xsl:if>

    <xsl:if test="$paginateStart &lt; $paginateEnd">
      <xsl:call-template name="paginateEntries">
        <xsl:with-param name="pages" select="$pages" />
        <xsl:with-param name="paginateStart" select="$paginateStart + 1" />
        <xsl:with-param name="paginateEnd" select="$paginateEnd" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="paginateQuerystring">
    <xsl:param name="page" select="1" />

    <xsl:call-template name="UrlSetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'XSL.Page'" />
      <xsl:with-param name="value" select="$page" />
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>