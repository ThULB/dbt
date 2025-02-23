<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xsl xalan i18n"
>

  <xsl:include href="MyCoReLayout.xsl" />

  <xsl:variable name="PageTitle" select="i18n:translate('component.rc.mailqueue.pageTitle')" />

  <xsl:template match="/mail-queue">
    <h2>
      <xsl:value-of select="$PageTitle" />
    </h2>

    <div ng-app="MailQueue">
      <div class="alert alert-dismissible" role="alert" ng-controller="alertCtrl" ng-show="alertObj.show">
        <xsl:attribute name="ng-class">
          <xsl:text>{'alert-danger': alertObj.type == 'error', 'alert-info': alertObj.type == 'info', 'alert-warning': alertObj.type == 'warning', 'alert-success': alertObj.type == 'success'}</xsl:text>
        </xsl:attribute>
        <button type="button" class="close" aria-label="Close" ng-click="clear()">
          <span aria-hidden="true">
            <xsl:text disable-output-escaping="yes">&amp;times;</xsl:text>
          </span>
        </button>
        <h4>{{alertObj.headline}}</h4>
        <p ng-if="!alertObj.stackTrace" ng-bind-html="alertObj.message" />
        <p ng-if="alertObj.stackTrace" data-toggle="collapse" data-target="#collapseStackTrace" aria-expanded="false" aria-controls="collapseStackTrace">
          <span class="caret" aria-hidden="true"></span>
          {{alertObj.message}}
        </p>
        <div class="collapse" id="collapseStackTrace" ng-if="alertObj.stackTrace">
          <pre class="pre-scrollable">{{alertObj.stackTrace}}</pre>
        </div>
      </div>

      <div ng-controller="queueCtrl">
        <div ng-hide="!jobs.loading">
          <span class="fas fa-sync fa-spin"></span>
          <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
          <xsl:value-of select="i18n:translate('component.rc.mailqueue.loading')" />
        </div>
        <div class="datatable card" ng-show="jobs.job &amp;&amp; jobs.job.length != 0">
          <div class="card-header">
            <form role="form" class="form-inline d-flex justify-content-between p-2">
              <span class="d-none d-md-inline-block pt-2 pb-2"></span>
              <div class="form-group m-0">
                <select size="1" name="numPerPage" ng-model="jobs.limit" class="form-control custom-select mr-2"
                  ng-options="np for np in [10, 25, 50, 100]" />
                <label class="d-inline">
                  <xsl:value-of select="i18n:translate('dataTable.lengthMenu')" />
                </label>
              </div>
            </form>
          </div>
          <div class="card-body table-responsive p-0">
            <table id="mail-jobs" class="table table-striped table-hover">
              <thead>
                <tr>
                  <th>
                    <a ng-click="setSort('id')">
                      <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.id')" />
                      <span class="ml-2 fas sort-icon">
                        <xsl:attribute name="ng-class">
                          <xsl:text>{'fa-sort':!isSort('id'),'fa-sort-up':isSort('id') &amp;&amp; !sort.reverse,'fa-sort-down':isSort('id') &amp;&amp; sort.reverse}</xsl:text>
                        </xsl:attribute>
                      </span>
                    </a>
                  </th>
                  <th>
                    <a ng-click="setSort('status')">
                      <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.status')" />
                      <span class="ml-2 fas sort-icon">
                        <xsl:attribute name="ng-class">
                      <xsl:text>{'fa-sort':!isSort('status'),'fa-sort-up':isSort('status') &amp;&amp; !sort.reverse,'fa-sort-down':isSort('status') &amp;&amp; sort.reverse}</xsl:text>
                    </xsl:attribute>
                      </span>
                    </a>
                  </th>
                  <th class="text-right">
                    <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.slotId')" />
                  </th>
                  <th class="text-right">
                    <a ng-click="setSort('date', 'added')">
                      <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.added')" />
                      <span class="ml-2 fas sort-icon">
                        <xsl:attribute name="ng-class">
                      <xsl:text>{'fa-sort':!isSort('date', 'added'),'fa-sort-up':isSort('date', 'added') &amp;&amp; !sort.reverse,'fa-sort-down':isSort('date', 'added') &amp;&amp; sort.reverse}</xsl:text>
                    </xsl:attribute>
                      </span>
                    </a>
                  </th>
                  <th class="text-right">
                    <a ng-click="setSort('date', 'start')">
                      <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.start')" />
                      <span class="ml-2 fas sort-icon">
                        <xsl:attribute name="ng-class">
                      <xsl:text>{'fa-sort':!isSort('date', 'start'),'fa-sort-up':isSort('date', 'start') &amp;&amp; !sort.reverse,'fa-sort-down':isSort('date', 'start') &amp;&amp; sort.reverse}</xsl:text>
                    </xsl:attribute>
                      </span>
                    </a>
                  </th>
                  <th class="text-right">
                    <a ng-click="setSort('date', 'finished')">
                      <xsl:value-of select="i18n:translate('component.rc.mailqueue.job.finished')" />
                      <span class="ml-2 fas sort-icon">
                        <xsl:attribute name="ng-class">
                      <xsl:text>{'fa-sort':!isSort('date', 'finished'),'fa-sort-up':isSort('date', 'finished') &amp;&amp; !sort.reverse,'fa-sort-down':isSort('date', 'finished') &amp;&amp; sort.reverse}</xsl:text>
                    </xsl:attribute>
                      </span>
                    </a>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr ng-repeat="job in jobs.job | orderBy:sortBy:sort.reverse | limitTo:jobs.limit:jobs.start track by job.id" ng-click="showMailDialog(job)">
                  <td class="text-right">{{ job.id }}</td>
                  <td>{{ 'component.rc.mailqueue.job.status.' + job.status | translate }}</td>
                  <td class="text-right">{{ slotId(job) }}</td>
                  <td class="text-right">{{ date(job, "added") | date:'dd.MM.yyyy HH:mm:ss' }}</td>
                  <td class="text-right">{{ date(job, "start") | date:'dd.MM.yyyy HH:mm:ss' }}</td>
                  <td class="text-right">{{ date(job, "finished") | date:'dd.MM.yyyy HH:mm:ss' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card-footer d-flex justify-content-between">
            <span class="d-none d-md-inline-block pt-2 pb-2">
              <xsl:text>{{ formatI18N('dataTable.filterInfo', jobs.start + 1, Math.min(jobs.start + jobs.limit, jobs.job.length), jobs.total) }}</xsl:text>
            </span>
            <ul class="pagination mb-0" ng-if="jobs.limit &lt; jobs.total">
              <li class="page-item">
                <xsl:attribute name="ng-class">
                  <xsl:text>{ disabled:paginationDisabled(jobs, '-1') }</xsl:text>
                </xsl:attribute>
                <a class="page-link" href="#" aria-label="Previous" ng-click="paginationPage(jobs, '-1')">
                  <span aria-hidden="true">
                    <xsl:text disable-output-escaping="yes">&amp;laquo;</xsl:text>
                  </span>
                </a>
              </li>
              <li class="page-item" ng-repeat="p in pagination(jobs)">
                <xsl:attribute name="ng-class">
                  <xsl:text>{ active:paginationActive(jobs, p) }</xsl:text>
                </xsl:attribute>
                <a class="page-link" href="#" ng-click="paginationPage(jobs, p)">{{p}}</a>
              </li>
              <li class="page-item">
                <xsl:attribute name="ng-class">
                  <xsl:text>{ disabled:paginationDisabled(jobs, '+1') }</xsl:text>
                </xsl:attribute>
                <a class="page-link" href="#" aria-label="Next" ng-click="paginationPage(jobs, '+1')">
                  <span aria-hidden="true">
                    <xsl:text disable-output-escaping="yes">&amp;raquo;</xsl:text>
                  </span>
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>


    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/angular/js/angular.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/angular/js/angular-translate.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/angular/js/angular-translate-loader-partial.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/assets/angular/js/angular-modal-service.min.js" />
    <script type="text/javascript" src="{$WebApplicationBaseURL}dbt/js/mail-queue.min.js" />
  </xsl:template>
</xsl:stylesheet>
