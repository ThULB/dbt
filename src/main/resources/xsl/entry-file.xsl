<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="xsl xalan i18n acl mcrxml"
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
  <xsl:variable name="invalid">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'invalid'" />
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="errorcode">
    <xsl:call-template name="UrlGetParam">
      <xsl:with-param name="url" select="$RequestURL" />
      <xsl:with-param name="par" select="'errorcode'" />
    </xsl:call-template>
  </xsl:variable>

  <xsl:template match="entry-file">
    <form action="{$ServletsBaseURL}RCSlotServlet" method="post" enctype="multipart/form-data" class="form-horizontal" role="form">
      <input type="hidden" name="action" value="upload" />
      <input type="hidden" name="entry" value="{$entry}" />
      <input type="hidden" name="slotId" value="{$slotId}" />
      <input type="hidden" name="afterId" value="{$afterId}" />

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">
            <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.title.add')" />
          </h3>
        </div>
        <div class="panel-body">
          <xsl:if test="$invalid = 'true'">
            <div class="alert alert-danger">
              <xsl:value-of select="i18n:translate(concat('component.rc.slot.entry.file.errorcode.', $errorcode))" />
            </div>
          </xsl:if>
          <div class="form-group">
            <label for="file" class="col-md-3 control-label">
              <xsl:value-of select="i18n:translate('component.rc.slot.entry.file')" />
            </label>
            <div class="col-md-9">
              <input type="file" class="form-control file" name="file" id="file" multiple="true" data-show-upload="false" data-show-caption="true"
                data-browse-label="{i18n:translate('component.rc.slot.entry.file.browse')}" data-remove-label="{i18n:translate('component.rc.slot.entry.file.remove')}"
                data-msg-selected="{i18n:translate('component.rc.slot.entry.file.msgSelected')}" data-msg-loading="{i18n:translate('component.rc.slot.entry.file.msgLoading')}"
                data-msg-progress="{i18n:translate('component.rc.slot.entry.file.msgProgress')}" />
            </div>
          </div>
          <div class="form-group">
            <div class="col-md-offset-3 col-md-9">
              <div class="checkbox">
                <label>
                  <input type="checkbox" name="copyrighted" value="true">
                    <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.copyrighted')" />
                  </input>
                </label>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label for="comment" class="col-md-3 control-label">
              <xsl:value-of select="i18n:translate('component.rc.slot.entry.file.comment')" />
            </label>
            <div class="col-md-9">
              <textarea rows="3" id="comment" class="form-control input-md" name="comment" />
            </div>
          </div>
        </div>
        <div class="panel-footer clearfix">
          <div class="pull-right">
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
      </div>
    </form>
    <link type="text/css" href="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/css/fileinput.min.css" rel="stylesheet" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap-fileinput/js/fileinput.min.js" />
  </xsl:template>

</xsl:stylesheet>