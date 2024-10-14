<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

  <xsl:include href="resource:xsl/email-functions.xsl" />

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/email/body">
    <xsl:choose>
      <xsl:when test="(count(child::*) != 0) and (name(child::*[1]) != 'html') and (count(..//body) = 1)">
        <body>
          <xsl:apply-templates select="child::node()" mode="text" />
        </body>
        <body type="html">
          <xsl:variable name="html">
            <style type="text/css">
                <![CDATA[
a {
  color: #008855;
}

a:link, a:visited {
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

.pica-record>h4, .pica-record>p {
  margin: 0;
}

#todo>ul {
  margin-top: 0;
}
                ]]>
            </style>
            <div id="header"
              style="padding: 10px; position: relative; display: block; height: 100px; border-bottom: 1px solid #008855; position: relative; margin-bottom: 15px; font-family: 'lucida grande', 'lucida sans unicode', arial, sans-serif;"
            >
              <a id="logo" href="{$WebApplicationBaseURL}" style="position: absolute; right: 10px; display: block;">
                <img src="{$WebApplicationBaseURL}dbt/images/logo-mail.png" style="height: 80px; display: block;" />
              </a>
            </div>
            <div id="content" style="margin: 0 10px; font-family: 'lucida grande', 'lucida sans unicode', arial, sans-serif;">
              <xsl:apply-templates select="child::node()" />
            </div>
          </xsl:variable>
          <xsl:apply-templates select="xalan:nodeset($html)" mode="html" />
        </body>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates />
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Standard Copy Template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="child::node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>