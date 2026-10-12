<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle" select="'Podcasts'" />

  <xsl:template match="/podcast">
    <head>
      <meta name="podcast-base-path" content="{$WebApplicationBaseURL}podcast/" />
      <link rel="stylesheet" href="{$WebApplicationBaseURL}dbt/assets/podcast/podcast.css" />
    </head>

    <!--
      The module mounts on #main_content > .container. Vue therefore replaces
      everything generated inside that container, while the MIR header, footer,
      and outer page shell remain server-rendered.
    -->
    <script type="module" src="{$WebApplicationBaseURL}dbt/assets/podcast/podcast.js"></script>
  </xsl:template>
</xsl:stylesheet>
