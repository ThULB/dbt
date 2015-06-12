<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  exclude-result-prefixes="xsl i18n"
>

  <xsl:template match="/period">
    <buttons>
      <div class="btn-group">
        <button type="button" class="btn btn-default" onclick="jQuery('#validTo').val('{@lectureEnd}');">
          <xsl:value-of select="i18n:translate('component.rc.slot.validTo.labels.lectureEnd')" />
        </button>
        <button type="button" class="btn btn-default" onclick="jQuery('#validTo').val('{@to}');">
          <xsl:value-of select="i18n:translate('component.rc.slot.validTo.labels.to')" />
        </button>
      </div>
    </buttons>
  </xsl:template>
  
</xsl:stylesheet>