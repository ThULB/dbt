<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="i18n xlink"
>
  <xsl:include href="MyCoReLayout.xsl" />
  
  <xsl:include href="slot-templates.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.slots.title')" />

  <xsl:variable name="rcLocations" select="document('classification:metadata:-1:children:RCLOC')//categories" />

  <xsl:template match="/slots">
    <script type="text/javascript" language="javascript" src="{$WebApplicationBaseURL}dbt/assets/jquery/plugins/jquery.dataTables.js" />
    <script type="text/javascript" language="javascript" src="{$WebApplicationBaseURL}dbt/assets/bootstrap/js/dataTables.bootstrap.js" />

    <script type="text/javascript">
      $(document).ready(function() {
        $('table[id="rcSlots"]').dataTable({
          "oLanguage": {
            "sSearch" : "",
            "sLengthMenu" : "<xsl:value-of select="i18n:translate('component.rc.slots.table.sLengthMenu')" />",
            "sInfo" : "<xsl:value-of select="i18n:translate('component.rc.slots.table.sInfo')" />",
            "sInfoEmpty" : "<xsl:value-of select="i18n:translate('component.rc.slots.table.sInfoEmpty')" />",
            "sInfoFiltered" : "<xsl:value-of select="i18n:translate('component.rc.slots.table.sInfoFiltered')" />",
          },
          "iDisplayLength": 20,
          "aLengthMenu": [20, 50, 100, 200],
          "aoColumns" : [
            { sWidth : "20%" },
            { sWidth : "30%" },
            { sWidth : "20%" },
            { sWidth : "auto" }
          ],
          "fnPreDrawCallback": function( oSettings ) {
            $('.dataTables_filter input').addClass('form-control input-sm');
            $('.dataTables_length select').addClass('form-control input-sm');
            $('.dataTables_filterinput').attr('placeholder', '<xsl:value-of select="i18n:translate('component.rc.slots.table.sSearch')" />');
      
            if (Math.ceil((this.fnSettings().fnRecordsDisplay()) / this.fnSettings()._iDisplayLength) > 1) {
              $('.dataTables_paginate').show();
            } else {
              $('.dataTables_paginate').hide();
            }
          }
        });
      });
    </script>

    <div class="table-responsive">
      <table class="table table-striped" id="rcSlots">
        <thead>
          <tr>
            <th class="col-md-2">
              <xsl:value-of select="i18n:translate('component.rc.slot.id')" />
            </th>
            <th class="col-md-3">
              <xsl:value-of select="i18n:translate('component.rc.slot.location')" />
            </th>
            <th class="col-md-3">
              <xsl:value-of select="i18n:translate('component.rc.slot.lecturer')" />
            </th>
            <th class="col-md-4">
              <xsl:value-of select="i18n:translate('component.rc.slot.title')" />
            </th>
          </tr>
        </thead>
        <tbody>
          <xsl:apply-templates />
        </tbody>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="slot">
    <tr>
      <td>
        <a href="{$WebApplicationBaseURL}rc/{@id}">
          <xsl:value-of select="@id" />
        </a>
        <xsl:if test="@status = 'new'">
          <span class="label label-danger pull-right">
            <xsl:value-of select="i18n:translate('component.rc.slot.new')" />
          </span>
        </xsl:if>
      </td>
      <td>
        <xsl:apply-templates select="@id" mode="rcLocation" />
      </td>
      <td>
        <xsl:for-each select="lecturers/lecturer">
          <xsl:value-of select="@name" />
          <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </td>
      <td>
        <a href="{$WebApplicationBaseURL}rc/{@id}">
          <xsl:value-of select="title" />
        </a>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>