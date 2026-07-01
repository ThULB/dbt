<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/faq">
    <site title="FAQ">
      <script>
        var shiftWindow = function() { scrollBy(0, -110) };
        if (location.hash) shiftWindow();
        window.addEventListener("hashchange", shiftWindow);
      </script>
      <h1>Digitale Bibliothek Thüringen (DBT) – FAQ</h1>
      <div id="faqCategHead">
        <xsl:for-each select="category">
          <div class="faqCategHeadElm">
            <h2>
              <a href="#{categoryTitle/@href}">
                <xsl:value-of select="categoryTitle" />
              </a>
            </h2>
            <xsl:for-each select="entry">
              <p>
                <a href="#{@href}">
                  <xsl:value-of select="question" />
                </a>
              </p>
            </xsl:for-each>
          </div>
        </xsl:for-each>
      </div>
      <div id="faqCategBody">
        <xsl:for-each select="category">
          <div class="faqCategBodyElm">
            <h2 id="{categoryTitle/@href}">
              <b>
                <xsl:value-of select="categoryTitle" />
              </b>
            </h2>
            <xsl:for-each select="entry">
              <div class="faqCategBodyElmEntry">
                <h3 id="{@href}">
                  <xsl:value-of select="question" />
                </h3>
                <xsl:copy-of select="answer/node()" />
              </div>
            </xsl:for-each>
          </div>
        </xsl:for-each>
      </div>
    </site>
  </xsl:template>
</xsl:stylesheet>
