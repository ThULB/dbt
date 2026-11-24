<?xml version="1.0" encoding="UTF-8"?>

<!-- ====================================================================== -->
<!-- $Revision$ $Date$ -->
<!-- ====================================================================== -->

<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrurl="http://www.mycore.de/xslt/url" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dbtutils="https://www.db-thueringen.de/xslt/utils" exclude-result-prefixes="#all"
>

  <xsl:variable name="headerCols">
    <xsl:apply-templates mode="dataTableHeader" select="." />
  </xsl:variable>

  <xsl:variable name="defaultNumPerPage" select="10" />
  <xsl:variable name="defaultSortBy">
    <xsl:value-of
      select="$headerCols/col[(position() = 1) and (string-length(@sortOrder) &gt; 0)]/@sortBy|$headerCols/th[(position() = 1) and (string-length(@sortOrder) &gt; 0)]/@sortBy" />
  </xsl:variable>
  <xsl:variable name="defaultSortOrder">
    <xsl:value-of select="$headerCols/col[1]/@sortOrder|$headerCols/th[1]/@sortOrder" />
  </xsl:variable>

  <xsl:variable name="SortBy" select="(dbtutils:url-param('SortBy')[. != ''], $defaultSortBy)[1]" />
  <xsl:variable name="SortOrder" select="(dbtutils:url-param('SortOrder')[. != ''], $defaultSortOrder)[1]" />
  <xsl:variable name="SortType" select="
    let $sortType := dbtutils:url-param('SortType'),
        $type := string-join($headerCols/col[1]/@sortType|$headerCols/th[1]/@sortType, ' ')
    return if ($sortType != '') then $sortType
           else if ($type != '') then $type
           else 'text'
  " />
  <xsl:variable name="numPerPage" select="xs:untypedAtomic((dbtutils:url-param('numPerPage')[. != ''], $defaultNumPerPage)[1])" />
  <xsl:variable name="Filter" select="dbtutils:url-param('Filter')" />
  <xsl:variable name="Page" select="xs:untypedAtomic((dbtutils:url-param('Page')[. != ''], 1)[1])" />

  <xsl:variable name="dataTableNumPerPageList">
    <xsl:text>10,25,50,100</xsl:text>
  </xsl:variable>

  <xsl:function name="dbtutils:url-param" as="xs:string">
    <xsl:param name="par" as="xs:string" />
    <xsl:sequence select="dbtutils:url-decode(mcrurl:get-param($RequestURL, $par))" />
  </xsl:function>

  <xsl:template mode="dataTable" match="*">
    <xsl:param name="id" select="'dataTable'" />
    <xsl:param name="i18nprefix" select="'dataTable'" />
    <xsl:param name="disableFilter" select="false()" />

    <!-- get total records from root element or count -->
    <xsl:variable name="nativeLimit" select="string-length(@total) &gt; 0" />
    <xsl:variable name="total">
      <xsl:choose>
        <xsl:when test="string-length(@total) &gt; 0">
          <xsl:value-of select="number(@total)" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="count(./*)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="pages">
      <xsl:variable name="p">
        <xsl:value-of select="ceiling($total div number($numPerPage))" />
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="($p * number($numPerPage)) &lt; $total">
          <xsl:value-of select="$p + 1" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$p" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="end">
      <xsl:choose>
        <xsl:when test="(number($Page) * number($numPerPage)) &gt; $total">
          <xsl:value-of select="$total" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="(number($Page) * number($numPerPage))" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="start">
      <xsl:choose>
        <xsl:when test="$total = 0">
          <xsl:value-of select="0" />
        </xsl:when>
        <xsl:when test="number($Page) &gt; $pages">
          <xsl:value-of select="1" />
        </xsl:when>
        <xsl:when test="$end = 0">
          <xsl:value-of select="$end" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="((number($Page) - 1) * number($numPerPage)) + 1" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <div id="{$id}_wrapper" class="datatable card">
      <div class="card-header">
        <form id="{$id}_form" class="d-flex align-items-center d-flex justify-content-between" role="form">
          <!-- build hidden values -->
          <xsl:call-template name="dataTableFormValues" />

          <xsl:choose>
            <xsl:when test="$disableFilter = false()">
            <!-- entries filter -->
              <div class="mb-3 flex-fill m-0" id="{$id}_filter">
                <label class="d-inline-flex justify-content-start flex-row w-100 align-items-center">
                  <span class="fa fa-filter me-1" aria-hidden="true" />
                  <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.filter'))" />
                  <input class="form-control ms-2" type="search" name="Filter">
                    <xsl:attribute name="value">
                      <xsl:if test="string-length($Filter) &gt; 0">
                        <xsl:value-of select="$Filter" />
                      </xsl:if>
                    </xsl:attribute>
                  </input>
                </label>
              </div>
            </xsl:when>
            <xsl:otherwise>
              <div />
            </xsl:otherwise>
          </xsl:choose>
          
          <!-- numPerPage selector -->
          <div id="{$id}_length" class="mb-3 d-none d-md-inline-flex m-0">
            <select class="form-select form-select me-2" name="numPerPage" size="1" onchange="this.form.submit()">
              <xsl:for-each select="tokenize($dataTableNumPerPageList, ',')">
                <option value="{.}">
                  <xsl:if test="$numPerPage = .">
                    <xsl:attribute name="selected"><xsl:text>selected</xsl:text></xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="." />
                </option>
              </xsl:for-each>
            </select>
            <label class="d-inline">
              <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.lengthMenu'))" />
              <noscript>
                <input class="btn" type="submit" name="Ok" value="Ok" />
              </noscript>
            </label>
          </div>
        </form>
      </div>
      <!-- build DataTable -->
      <div class="card-body table-responsive p-0">
        <table class="table table-striped table-hover my-0" id="{$id}">
          <xsl:call-template name="dataTableHeader" />
          <xsl:choose>
            <xsl:when test="number($end) = 0">
              <tr class="odd" align="center">
                <td colspan="{$dataTableHeaderColCount}">
                  <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.noItemFound'))" />
                </td>
              </tr>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="dataTableBody">
                <xsl:with-param name="nativeLimit" select="$nativeLimit" />
                <xsl:with-param name="start" select="$start" />
                <xsl:with-param name="end" select="$end" />
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between">
        <div>
          <span id="{$id}_info">
            <xsl:if test="$pages &gt; 1">
              <xsl:attribute name="class">d-none d-md-inline-block pt-2 pb-2</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="mcri18n:translate-with-params(concat($i18nprefix, '.filterInfo'), ($start, $end, $total))" />
          </span>
        </div>
        <xsl:if test="$pages &gt; 1">
          <xsl:call-template name="dataTablePaginate">
            <xsl:with-param name="id" select="$id" />
            <xsl:with-param name="i18nprefix" select="$i18nprefix" />
            <xsl:with-param name="pages" select="$pages" />
          </xsl:call-template>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="dataTableHeader">
    <thead>
      <tr>
        <xsl:for-each select="$headerCols/col|$headerCols/th">
          <xsl:variable name="sortOrderAfter">
            <xsl:choose>
              <xsl:when test="($SortBy = @sortBy) and ($SortOrder = 'asc')">
                <xsl:text>desc</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>asc</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:variable name="iconClass">
            <xsl:choose>
              <xsl:when test="($SortBy = @sortBy) and ($SortOrder = 'asc')">
                <xsl:text>fas fa-sort-up</xsl:text>
              </xsl:when>
              <xsl:when test="($SortBy = @sortBy) and ($SortOrder = 'desc')">
                <xsl:text>fas fa-sort-down</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>fas fa-sort</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <th>
            <xsl:copy-of select="@*[not(contains('sortBy|sortOrder|sortType', name()))]" />
            <xsl:choose>
              <xsl:when test="string-length(@sortBy) &gt; 0">
                <a>
                  <xsl:attribute name="href">
                    <xsl:call-template name="dataTableQuerystring">
                      <xsl:with-param name="sortBy" select="@sortBy" />
                      <xsl:with-param name="sortOrder" select="$sortOrderAfter" />
                      <xsl:with-param name="sortType">
                        <xsl:choose>
                          <xsl:when test="@sortType">
                            <xsl:value-of select="@sortType" />
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:text>text</xsl:text>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:with-param>
                    </xsl:call-template>
                  </xsl:attribute>
                  <i class="{$iconClass} me-2 sort-icon" />
                  <xsl:value-of select="text()" />
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="text()" />
              </xsl:otherwise>
            </xsl:choose>
          </th>
        </xsl:for-each>
      </tr>
    </thead>
  </xsl:template>

  <xsl:template name="dataTableBody">
    <xsl:param name="nativeLimit" select="false" />
    <xsl:param name="start" />
    <xsl:param name="end" />

    <xsl:variable name="sortedCol" select="$dataTableSortedCol" />

    <tbody>
      <xsl:variable name="sortOrder">
        <xsl:choose>
          <xsl:when test="$SortOrder = 'desc'">
            <xsl:text>descending</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>ascending</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:for-each select="./*">
        <xsl:sort
          select="(*[name() = $SortBy]|@*[name() = $SortBy]|text()[$SortBy = 'text()']|descendant-or-self::*[name() = $SortBy]|descendant-or-self::*//@*[name() = $SortBy])[1]"
          order="{$sortOrder}" data-type="{$SortType}" />

        <xsl:if test="((position() &gt;= $start) and (position() &lt;= $end)) or ($nativeLimit)">
          <xsl:variable name="trClass">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:text>even</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>odd</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="drow">
            <xsl:apply-templates mode="dataTableRow" select="." />
          </xsl:variable>

          <xsl:variable name="row">
            <xsl:choose>
              <xsl:when test="count($drow/row|$drow/tr) &gt; 0">
                <xsl:copy-of select="$drow" />
              </xsl:when>
              <xsl:otherwise>
                <row>
                  <xsl:copy-of select="$drow" />
                </row>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:for-each select="$row/row|$row/tr">
            <tr class="{$trClass}">
              <!-- extra css class for row -->
              <xsl:if test="./class">
                <xsl:attribute name="class">
                  <xsl:value-of select="concat($trClass, ' ', ./class)" />
                </xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="@*" />
              <xsl:for-each select="./col|./td">
                <xsl:variable name="tdClass">
                  <xsl:if test="number($sortedCol) = position()">
                    <xsl:text>sorting_1</xsl:text>
                  </xsl:if>
                </xsl:variable>
                <td>
                  <xsl:if test="string-length($tdClass) &gt; 0">
                    <xsl:attribute name="class">
                    <xsl:value-of select="$tdClass" />
                  </xsl:attribute>
                  </xsl:if>
                  <xsl:apply-templates select="@*|node()" />
                </td>
              </xsl:for-each>
            </tr>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
    </tbody>
  </xsl:template>

  <xsl:template name="dataTablePaginate">
    <xsl:param name="id" select="'dataTable'" />
    <xsl:param name="i18nprefix" select="'dataTable'" />
    <xsl:param name="pages" />

    <ul id="{$id}_paginate" class="pagination mb-0">
      <li class="page-item">
        <xsl:choose>
          <xsl:when test="number($Page) &gt; 1">
            <a class="page-link" tabindex="0" id="{$id}_first">
              <xsl:attribute name="href">
                  <xsl:call-template name="dataTableQuerystring">
                    <xsl:with-param name="page" select="1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text>«</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.first'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">page-item disabled</xsl:attribute>
            <a class="page-link" tabindex="0" id="{$id}_first">
              <xsl:text>«</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.first'))" />
              </span>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </li>
      <li class="page-item">
        <xsl:choose>
          <xsl:when test="number($Page) &gt; 1">
            <a class="page-link" tabindex="0" id="{$id}_previous">
              <xsl:attribute name="href">
                  <xsl:call-template name="dataTableQuerystring">
                    <xsl:with-param name="page" select="$Page - 1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text>‹</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.previous'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">page-item disabled</xsl:attribute>
            <a class="page-link" tabindex="0" id="{$id}_previous">
              <xsl:text>‹</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.previous'))" />
              </span>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </li>

      <!-- variable can be used to change numbers of Page entries to display -->
      <xsl:variable name="paginateMaxEntries" select="5" />
      <xsl:variable name="paginateBackCount" select="ceiling(($paginateMaxEntries - 1) div 2)" />
      <xsl:variable name="paginatePrevCount" select="$paginateMaxEntries - ceiling($paginateMaxEntries div 2)" />

      <xsl:variable name="paginateStart">
        <xsl:choose>
          <xsl:when test="((number($Page) - $paginateBackCount) &lt;= 1) or ($pages &lt; $paginateMaxEntries)">
            <xsl:value-of select="1" />
          </xsl:when>
          <xsl:when test="(number($Page) + $paginatePrevCount) &gt; $pages">
            <xsl:value-of select="number($Page) - $paginateBackCount - ((number($Page) + $paginatePrevCount) - $pages)" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="number($Page) - $paginateBackCount" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="paginateEnd">
        <xsl:choose>
          <xsl:when test="((number($Page) + $paginatePrevCount) &gt;= $pages) or ($pages &lt; $paginateMaxEntries)">
            <xsl:value-of select="$pages" />
          </xsl:when>
          <xsl:when test="(number($Page) + $paginatePrevCount) &lt; $paginateMaxEntries">
            <xsl:value-of select="$paginateMaxEntries" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="number($Page) + $paginatePrevCount" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:call-template name="dataTablePaginateEntries">
        <xsl:with-param name="pages" select="$pages" />
        <xsl:with-param name="paginateStart" select="$paginateStart" />
        <xsl:with-param name="paginateEnd" select="$paginateEnd" />
      </xsl:call-template>

      <li class="page-item">
        <xsl:choose>
          <xsl:when test="number($Page) &lt; $pages">
            <a class="page-link" tabindex="0" id="{$id}_next">
              <xsl:attribute name="href">
                  <xsl:call-template name="dataTableQuerystring">
                    <xsl:with-param name="page" select="$Page + 1" />
                  </xsl:call-template>
                </xsl:attribute>
              <xsl:text>›</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.next'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">page-item disabled</xsl:attribute>
            <a class="page-link" tabindex="0" id="{$id}_next">
              <xsl:text>›</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.next'))" />
              </span>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </li>
      <li class="page-item">
        <xsl:choose>
          <xsl:when test="number($Page) &lt; $pages">
            <a class="page-link" tabindex="0" id="{$id}_last">
              <xsl:attribute name="href">
                <xsl:call-template name="dataTableQuerystring">
                  <xsl:with-param name="page" select="$pages" />
                </xsl:call-template>
              </xsl:attribute>
              <xsl:text>»</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.last'))" />
              </span>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">page-item disabled</xsl:attribute>
            <a class="page-link" tabindex="0" id="{$id}_last">
              <xsl:text>»</xsl:text>
              <span class="visually-hidden">
                <xsl:value-of select="mcri18n:translate(concat($i18nprefix, '.last'))" />
              </span>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </li>
    </ul>
  </xsl:template>

  <xsl:template name="dataTablePaginateEntries">
    <xsl:param name="pages" />
    <xsl:param name="paginateStart" />
    <xsl:param name="paginateEnd" />

    <xsl:if test="$paginateStart = number($Page)">
      <li class="page-item active">
        <a class="page-link" tabindex="0">
          <xsl:value-of select="$paginateStart" />
        </a>
      </li>
    </xsl:if>
    <xsl:if test="$paginateStart != number($Page)">
      <li class="page-item">
        <a class="page-link" tabindex="0">
          <xsl:attribute name="href">
            <xsl:call-template name="dataTableQuerystring">
              <xsl:with-param name="page" select="$paginateStart" />
            </xsl:call-template>
          </xsl:attribute>
          <xsl:value-of select="$paginateStart" />
        </a>
      </li>
    </xsl:if>

    <xsl:if test="number($paginateStart) &lt; number($paginateEnd)">
      <xsl:call-template name="dataTablePaginateEntries">
        <xsl:with-param name="pages" select="$pages" />
        <xsl:with-param name="paginateStart" select="$paginateStart + 1" />
        <xsl:with-param name="paginateEnd" select="$paginateEnd" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- Helper Variables/Functions -->

  <xsl:variable name="dataTableHeaderColCount">
    <xsl:value-of select="count($headerCols/col|$headerCols/th)" />
  </xsl:variable>

  <xsl:variable name="dataTableSortedCol">
    <xsl:variable name="pos">
      <xsl:for-each select="$headerCols/col|$headerCols/th">
        <xsl:if test="$SortBy = @sortBy">
          <xsl:value-of select="position()" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$pos">
        <xsl:value-of select="$pos" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>0</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template name="dataTableQuerystring">
    <xsl:param name="sortBy" select="$SortBy" />
    <xsl:param name="sortOrder" select="$SortOrder" />
    <xsl:param name="sortType" select="$SortType" />
    <xsl:param name="perPage" select="$numPerPage" />
    <xsl:param name="page" select="$Page" />
    <xsl:param name="filter" select="$Filter" />

    <xsl:variable name="url1" select="mcrurl:set-param($RequestURL, 'SortBy', $sortBy)" />
    <xsl:variable name="url2" select="mcrurl:set-param($url1, 'SortOrder', $sortOrder)" />
    <xsl:variable name="url3" select="mcrurl:set-param($url2, 'SortType', $sortType)" />
    <xsl:variable name="url4" select="mcrurl:set-param($url3, 'numPerPage', $perPage)" />
    <xsl:variable name="url5" select="mcrurl:set-param($url4, 'Page', $page)" />
    <xsl:variable name="url6" select="mcrurl:set-param($url5, 'Filter', $filter)" />

    <xsl:value-of select="$url6" />
  </xsl:template>

  <xsl:template name="dataTableFormValues">
    <xsl:param name="sortBy" select="$SortBy" />
    <xsl:param name="sortOrder" select="$SortOrder" />
    <xsl:param name="sortType" select="$SortType" />
    <xsl:param name="page" select="1" />

    <xsl:variable name="queryString">
      <xsl:choose>
        <xsl:when test="contains($RequestURL,'?')">
          <xsl:value-of select="substring-after($RequestURL,'?')" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$RequestURL" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:for-each select="tokenize($queryString, '&amp;')[. != '']">
      <xsl:variable name="name" select="substring-before(., '=')" />
      <xsl:variable name="value" select="substring-after(., '=')" />

      <xsl:if test="not(contains('SortBy|SortOrder|SortType|Page|Filter|numPerPage', $name))">
        <input type="hidden" name="{$name}" value="{$value}" />
      </xsl:if>
    </xsl:for-each>

    <input type="hidden" name="SortBy" value="{$sortBy}" />
    <input type="hidden" name="SortOrder" value="{$sortOrder}" />
    <input type="hidden" name="SortType" value="{$sortType}" />
    <input type="hidden" name="Page" value="{$page}" />
  </xsl:template>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>