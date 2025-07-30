<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="xsl xalan i18n acl mcrxml"
>

  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slot.entry.file.title.add')" />

  <xsl:variable name="entry">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'entry'" />
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="slotId">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'slotId'" />
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="afterId">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'afterId'" />
    </xsl:call-template>
  </xsl:variable>

  <xsl:template match="entry-file">
    <link type="text/css" href="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/css/fileinput.min.css" rel="stylesheet" />
    <form action="{$ServletsBaseURL}RCUploadServlet" method="post" enctype="multipart/form-data" class="form-horizontal" role="form">
      <input type="hidden" name="action" value="upload" />
      <input type="hidden" name="entry" value="{$entry}" />
      <input type="hidden" name="slotId" value="{$slotId}" />
      <input type="hidden" name="afterId" value="{$afterId}" />

      <div class="card my-2">
        <h5 class="card-header">
          <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.title.add')" />
        </h5>
        <div class="card-body">
          <div class="mb-3 row">
            <label for="file" class="col-md-3 control-label text-md-end">
              <xsl:value-of select="i18n:translate('component.rc.slot.entry.file')" />
            </label>
            <div class="col-md-9">
              <input type="file" class="form-control" name="file" id="file" data-browse-label="{i18n:translate('component.rc.slot.entry.file.browse')}"
                data-remove-label="{i18n:translate('component.rc.slot.entry.file.remove')}"
                data-msg-selected="{i18n:translate('component.rc.slot.entry.file.msgSelected')}"
                data-msg-loading="{i18n:translate('component.rc.slot.entry.file.msgLoading')}"
                data-msg-progress="{i18n:translate('component.rc.slot.entry.file.msgProgress')}" />
            </div>
          </div>
          <div class="mb-3 row">
            <div class="offset-md-3 col-md-9">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="copyrighted" name="copyrighted" value="true" />
                <label class="form-check-label" for="copyrighted">
                  <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.copyrighted')" />
                </label>
              </div>
            </div>
          </div>
          <div class="mb-3 row">
            <label for="comment" class="col-md-3 control-label text-md-end">
              <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.description')" />
            </label>
            <div class="col-md-9">
              <textarea rows="3" id="comment" class="form-control input-md" name="comment" />
            </div>
          </div>
        </div>
        <div class="card-footer d-flex justify-content-end align-items-start">
          <button type="submit" class="btn btn-primary btn-md" name="upload">
            <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.upload')" />
          </button>
          <span>
            <xsl:text disable-output-escaping="yes"> &amp;nbsp;</xsl:text>
          </span>
          <button type="submit" class="btn btn-default btn-md" name="cancel">
            <xsl:value-of select="i18n:translate('button.cancel')" />
          </button>
        </div>
      </div>
    </form>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/js/fileinput.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/themes/fas/theme.min.js" />
    <script type="text/javascript">
      <xsl:value-of select="concat('var servletsBaseURL = &quot;', $ServletsBaseURL, '&quot;;')" disable-output-escaping="yes" />
    </script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}content/rc/js/upload.min.js" />
  </xsl:template>

</xsl:stylesheet>