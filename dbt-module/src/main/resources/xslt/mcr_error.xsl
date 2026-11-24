<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mcri18n="http://www.mycore.de/xslt/i18n" xmlns:mirstrutils="http://www.mycore.de/xslt/mirstrutils"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">
  <xsl:include href="resource:xslt/layout/mir-layout-utils.xsl"/>
  <xsl:include href="resource:xslt/MyCoReLayout.xsl"/>

  <xsl:variable name="Type" select="'document'"/>

  <xsl:variable name="PageTitle" select="mcri18n:translate-with-params('titles.pageTitle.error', concat(' ', /mcr_error/@HttpError))"/>

  <xsl:template match="/mcr_error">
    <div class="card border-bottom-0">
      <div class="card-body p-5">
        <div class="text-center">
          <h1 class="text-danger">
            <xsl:value-of select="mcri18n:translate-with-params('mir.error.headline', /mcr_error/@HttpError)"/>
          </h1>
          <h2 class="text-danger">
            <xsl:value-of select="mcri18n:translate('mir.error.subheadline')"/>
          </h2>
          <p class="lead mb-0">
            <xsl:copy-of
                select="parse-xml-fragment(mcri18n:translate-with-params(concat('mir.error.codes.', /mcr_error/@HttpError), mirstrutils:escape-xml(string(/mcr_error/@requestURI))))/node()"/>
          </p>
        </div>
      </div>
    </div>

    <div class="card">
      <h5 class="card-header" id="error">
        <a href="#"
           class="d-flex flex-row justify-content-between align-items-center"
           data-bs-toggle="collapse" data-bs-target="#error-details"
           aria-expanded="false" aria-controls="error-details">
          <xsl:value-of select="mcri18n:translate('error.stackTrace')"/>
        </a>
      </h5>
      <div id="error-details" class="collapse" aria-labelledby="error">
        <div class="card-body">
          <xsl:choose>
            <xsl:when test="@errorServlet and string-length(text()) &gt; 1 or exception">
              <xsl:if test="@errorServlet and string-length(text()) &gt; 1">
                <div class="alert alert-warning my-2" role="alert">
                  <xsl:attribute name="title">
                    <xsl:value-of select="mcri18n:translate('mir.error.message')"/>
                  </xsl:attribute>
                  <xsl:call-template name="lf2br">
                    <xsl:with-param name="string" select="text()"/>
                  </xsl:call-template>
                </div>
              </xsl:if>

              <xsl:if test="exception">
                <xsl:for-each select="exception/trace">
                  <pre class="bg-dark text-light p-1 rounded">
                    <code>
                      <xsl:value-of select="."/>
                    </code>
                  </pre>
                </xsl:for-each>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <p class="text-center">
                <xsl:value-of select="mcri18n:translate('error.noInfo')"/>
              </p>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </div>

  </xsl:template>

  <xsl:template match="/mcr_error[@HttpError = ('401', '403')]" priority="1">
    <xsl:param name="isUserGuest" select="document('userobjectrights:isCurrentUserGuestUser:')/boolean/text()='true'"/>
    <div class="alert alert-warning">
      <h1>
        <xsl:value-of select="mcri18n:translate(concat('mir.error.headline.', @HttpError))"/>
      </h1>
      <p>
        <xsl:choose>
          <xsl:when test="$isUserGuest">
            <xsl:copy-of
                select="parse-xml-fragment(mcri18n:translate-with-params(concat('mir.error.codes.', @HttpError), mirstrutils:escape-xml(string(@requestURI))))/node()"/>
            <xsl:text>&#160;</xsl:text>
            <a href="{concat($ServletsBaseURL, 'MCRLoginServlet?url=', encode-for-uri(string($RequestURL)))}">
              <xsl:value-of select="mcri18n:translate('component.user2.button.login')"/>
            </a>
          </xsl:when>
          <xsl:when test="contains(@requestURI, '/receive/')">
            <xsl:variable name="objectId" select="substring-after(@requestURI, '/receive/')"/>
            <xsl:variable name="accKP" select="document(concat('accesskeys:', $objectId))"/>
            <xsl:variable name="hasAccKP" select="count($accKP/accesskeys[@readkey|@writekey]) &gt; 0"/>
            <xsl:choose>
              <xsl:when test="$hasAccKP">
                <xsl:copy-of
                    select="parse-xml-fragment(mcri18n:translate-with-params('mir.error.accessKeyRequired', mirstrutils:escape-xml($objectId)))/node()"/>
                <xsl:text>&#160;</xsl:text>
                <a href="{concat($WebApplicationBaseURL, 'authorization/accesskey.xed',
                                                 '?objId=', $objectId, '&amp;url=',
                                                 encode-for-uri(string($RequestURL)))}">
                  <xsl:value-of select="mcri18n:translate('mir.accesskey.setOnUser')"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="parse-xml-fragment(mcri18n:translate('mir.error.blocked'))/node()"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
        </xsl:choose>
      </p>
    </div>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string" as="xs:string" />

    <xsl:for-each select="tokenize($string, '\r?\n')">
      <xsl:value-of select="." />
      <xsl:if test="position() ne last()">
        <br />
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
