<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
  xmlns:mcrurl="http://www.mycore.de/xslt/url" exclude-result-prefixes="#all"
>

  <xsl:include href="resource:xslt/MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle" select="mcri18n:translate('component.rc.slot.entry.file.title.add')" />

  <xsl:variable name="entry" select="mcrurl:get-param($RequestURL, 'entry')" />
  <xsl:variable name="slotId" select="mcrurl:get-param($RequestURL, 'slotId')" />
  <xsl:variable name="afterId" select="mcrurl:get-param($RequestURL, 'afterId')" />

  <xsl:template match="entry-file">
    <link type="text/css" href="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/css/fileinput.min.css" rel="stylesheet" />
    <form action="{$ServletsBaseURL}RCUploadServlet" method="post" enctype="multipart/form-data" class="form-horizontal" role="form">
      <input type="hidden" name="action" value="upload" />
      <input type="hidden" name="entry" value="{$entry}" />
      <input type="hidden" name="slotId" value="{$slotId}" />
      <input type="hidden" name="afterId" value="{$afterId}" />

      <div class="card my-2">
        <h5 class="card-header">
          <xsl:value-of select="mcri18n:translate('component.rc.slot.entry.file.title.add')" />
        </h5>
        <div class="card-body">
          <div class="mb-3 row">
            <label for="file" class="col-md-3 control-label text-md-end">
              <xsl:value-of select="mcri18n:translate('component.rc.slot.entry.file')" />
            </label>
            <div class="col-md-9">
              <input type="file" class="form-control" name="file" id="file" data-browse-label="{mcri18n:translate('component.rc.slot.entry.file.browse')}"
                data-remove-label="{mcri18n:translate('component.rc.slot.entry.file.remove')}"
                data-msg-selected="{mcri18n:translate('component.rc.slot.entry.file.msgSelected')}"
                data-msg-loading="{mcri18n:translate('component.rc.slot.entry.file.msgLoading')}"
                data-msg-progress="{mcri18n:translate('component.rc.slot.entry.file.msgProgress')}" />
            </div>
          </div>
          <div class="mb-3 row">
            <div class="offset-md-3 col-md-9">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="copyrighted" name="copyrighted" value="true" />
                <label class="form-check-label" for="copyrighted">
                  <xsl:value-of select="mcri18n:translate('component.rc.slot.entry.file.copyrighted')" />
                </label>
              </div>
            </div>
          </div>
          <div class="mb-3 row">
            <label for="comment" class="col-md-3 control-label text-md-end">
              <xsl:value-of select="mcri18n:translate('component.rc.slot.entry.file.description')" />
            </label>
            <div class="col-md-9">
              <textarea rows="3" id="comment" class="form-control input-md" name="comment" />
            </div>
          </div>
        </div>
        <div class="card-footer d-flex justify-content-end align-items-start">
          <button type="submit" class="btn btn-primary btn-md" name="upload">
            <xsl:value-of select="mcri18n:translate('component.rc.slot.entry.file.upload')" />
          </button>
          <span>
            <xsl:text> &#160;</xsl:text>
          </span>
          <button type="submit" class="btn btn-outline-secondary btn-md" name="cancel">
            <xsl:value-of select="mcri18n:translate('button.cancel')" />
          </button>
        </div>
      </div>
    </form>
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/js/fileinput.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/themes/fas/theme.min.js" />
    <script type="text/javascript">
      <xsl:value-of select="concat('var servletsBaseURL = &quot;', $ServletsBaseURL, '&quot;;')" />
    </script>
    <script type="text/javascript" src="{$WebApplicationBaseURL}content/rc/js/upload.min.js" />
  </xsl:template>

</xsl:stylesheet>
