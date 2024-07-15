<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" doctype-system="about:legacy-compat" indent="yes" omit-xml-declaration="yes" media-type="text/html" version="5"
    encoding="UTF-8" />

  <xsl:strip-space elements="*" />

  <xsl:param name="WebApplicationBaseURL" />

  <xsl:variable name="mediaId" select="/media/@id" />

  <xsl:template match="/">
    <html>
      <head>
        <base target="_blank" />
        <link rel="stylesheet" href="{$WebApplicationBaseURL}dbt/assets/media/embed.min.css" />
      </head>
      <body>
        <video id="dbt-player-{$mediaId}" class="video-js" controls="" preload="metadata">
          <p class="vjs-no-js">
            To view this video please enable JavaScript, and consider upgrading to a web browser that
            <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>
          </p>
        </video>

        <script src="{$WebApplicationBaseURL}dbt/assets/media/embed.min.js"/>
        <script type="text/javascript">
          &lt;xsl:value-of select="concat('embeddedPlayer(&quot;', $WebApplicationBaseURL, '&quot;, &quot;dbt-player-', $mediaId, '&quot;);')"
            disable-output-escaping="yes" />
        </script>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>