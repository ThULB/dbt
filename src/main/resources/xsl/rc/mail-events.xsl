<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:pica="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd" exclude-result-prefixes="xalan i18n xlink pica"
>

  <xsl:include href="resource:xsl/opc/pica-record-isbd.xsl" />

  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="slot-templates.xsl" />

  <xsl:param name="MCR.RC.MailSender" />

  <xsl:param name="WebApplicationBaseURL" />

  <!-- Event vars -->
  <xsl:param name="action" />
  <xsl:param name="type" />
  <xsl:param name="slotId" />
  <xsl:param name="entryId" />
  
  <!-- OPC vars -->
  <xsl:variable name="catalogues" select="document('resource:catalogues.xml')/catalogues" />
  <xsl:variable name="catalogId" select="document(concat('slot:slotId=',$slotId,'&amp;catalogId'))" />
  <xsl:variable name="opcURL" select="$catalogues/catalog[@identifier=$catalogId]/opc/text()" />
  <xsl:variable name="opcDB" select="$catalogues/catalog[@identifier=$catalogId]/opc/@db" />

  <!-- set XMLPRS to Y to get PICA longtitle - /XMLPRS=N -->
  <xsl:variable name="recordURLPrefix" select="concat($opcURL,'/DB=', $opcDB, '/PPN?PPN=')" />

  <xsl:variable name="newline" select="'&#xA;'" />

  <xsl:variable name="editorTo">
    <xsl:variable name="m" select="document(concat('slot:slotId=', $slotId, '&amp;mail'))/mail" />
    <xsl:choose>
      <xsl:when test="string-length($m) &gt; 0">
        <xsl:value-of select="$m" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="mp" select="document(concat('slot:slotId=', $slotId, '&amp;mail&amp;parent=true'))/mail" />
        <xsl:choose>
          <xsl:when test="string-length($mp) &gt; 0">
            <xsl:value-of select="$mp" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$MCR.RC.MailSender" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:message>
      type:
      <xsl:value-of select="$type" />
      action:
      <xsl:value-of select="$action" />
      slotId:
      <xsl:value-of select="$slotId" />
      entryId:
      <xsl:value-of select="$entryId" />
    </xsl:message>
    <email>
      <from>
        <xsl:value-of select="$MCR.RC.MailSender" />
      </from>
      <xsl:apply-templates select="/*" mode="email" />
    </email>
  </xsl:template>

  <xsl:template match="slot" mode="email">
    <xsl:choose>
      <xsl:when test="($action = 'inactivate') or ($action = 'reactivate')">
        <xsl:apply-templates select="." mode="emailActivate" />
      </xsl:when>
      <xsl:when test="$action = 'ownerTransfer'">
        <xsl:apply-templates select="." mode="emailOwnerTransfer" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="slot" mode="emailActivate">
    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="string-length(validTo) &gt; 0">
          <xsl:value-of select="validTo" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>now</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="period" select="document(concat('period:areacode=0&amp;date=', $date, '&amp;fq=true'))" />

    <xsl:variable name="numRecords">
      <xsl:choose>
        <xsl:when test="$action = 'inactivate'">
          <xsl:value-of select="count(//opcrecord[string-length(@epn) &gt; 0])" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="count(//opcrecord)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="($numRecords &gt; 0) and (@onlineOnly = 'false')">
      <xsl:message>
        Send Mail for:
        <xsl:value-of select="$action" />
        To:
        <xsl:value-of select="$editorTo" />
      </xsl:message>
      <to>
        <xsl:value-of select="$editorTo" />
      </to>
      <subject>
        <xsl:value-of select="concat('ESA ', $slotId, ': ')" />
        <xsl:choose>
          <xsl:when test="$action = 'inactivate'">
            <xsl:text>wurde inaktiviert</xsl:text>
          </xsl:when>
          <xsl:when test="$action = 'reactivate'">
            <xsl:text>wurde reaktiviert</xsl:text>
          </xsl:when>
        </xsl:choose>
      </subject>
      <body>
        <strong>Sehr geehrt(e) Mitarbeiter(in),</strong>
        <br />
        <p>
          der nachfolgende Online-Semesterapparat
          <xsl:choose>
            <xsl:when test="$action = 'inactivate'">
              <xsl:text> wurde inaktiviert</xsl:text>
            </xsl:when>
            <xsl:when test="$action = 'reactivate'">
              <xsl:text> wurde reaktiviert</xsl:text>
            </xsl:when>
          </xsl:choose>
          <xsl:text>:</xsl:text>
        </p>

        <dl>
          <dt>Titel</dt>
          <dd>
            <xsl:value-of select="title" />
          </dd>
          <dt>Standort</dt>
          <dd>
            <xsl:apply-templates select="@id" mode="rcLocationText" />
          </dd>
          <dt>Semester</dt>
          <dd>
            <xsl:value-of select="$period//label[lang($CurrentLang)]/@description" />
          </dd>
          <dt>Gültig bis</dt>
          <dd>
            <xsl:value-of select="validTo" />
          </dd>
        </dl>

        <xsl:for-each select="//entry">
          <xsl:if test="opcrecord">
            <xsl:if test="($action = 'reactivate') or (($action = 'inactivate') and (string-length(@epn) &gt; 0))">
              <xsl:apply-templates select="opcrecord" mode="output">
                <xsl:with-param name="withCopys" select="$action = 'reactivate'" />
                <xsl:with-param name="entryId" select="@id" />
              </xsl:apply-templates>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>

        <xsl:if test="contains('inactivate|reactivate', $action)">
          <div id="todo">
            Bearbeitungsschritte
            <br />
            <ul>
              <xsl:choose>
                <xsl:when test="$action = 'inactivate'">
                  <li>Buchen Sie bitte alle Exemplare in PICA/DBT zurück</li>
                </xsl:when>
                <xsl:when test="$action = 'reactivate'">
                  <li>Stellen Sie alle Exemplare in das Präsenz-Regal im Lesesaal</li>
                  <li>Schalten Sie alle Einträge frei (GBV-Kat. u. DBT)</li>
                </xsl:when>
              </xsl:choose>
            </ul>
          </div>
        </xsl:if>
      </body>
    </xsl:if>
  </xsl:template>

  <xsl:template match="slot" mode="emailOwnerTransfer">
    <xsl:variable name="objectId" select="document(concat('slot:slotId=', @id, '&amp;objectId'))/mcrobject" />
    <xsl:variable name="accessKeys" select="document(concat('accesskeys:', $objectId))/accesskeys" />

    <xsl:message>
      Send Mail for:
      <xsl:value-of select="$action" />
      MCRObjectId:
      <xsl:value-of select="$objectId" />
      AccessKeys:
      <xsl:value-of select="$accessKeys" />
    </xsl:message>

    <xsl:for-each select="lecturers/lecturer">
      <to>
        <xsl:value-of select="concat(@name, ' &lt;', @email, '&gt;')" />
      </to>
    </xsl:for-each>
    <bcc>
      <xsl:value-of select="concat('Elektronische Semesterapparate', ' &lt;', 'elektronische_semesterapparate@thulb.uni-jena.de', '&gt;')" />
    </bcc>
    <subject>
      <xsl:value-of select="concat('ESA ', $slotId, ': Eigentümerwechsel')" />
<!--       <xsl:value-of select="concat('ESA ', $slotId, ': Aufforderung zur Übernahme in die neue DBT')" /> -->
    </subject>
    <body>

      <strong>Sehr geehrte/r Benutzer/in,</strong>
      <br />
      <p>
        Für eine Zuordnung dieses Elektronischen Semesterapparates (ESA) zu Ihrer Person in der Digitalen Bibliothek Thüringen (DBT) melden Sie sich bitte
        <strong>
          <a href="{concat($WebApplicationBaseURL, 'rc/', $slotId)}">hier</a>
        </strong>
        mit Ihrer persönlichen URZ-Nutzerkennung an.
      </p>
      <p>
        Nach der Anmeldung in der DBT gelangen Sie direkt in den entsprechendem ESA und schließen mit der Eingabe
        des folgenden Zugriffsschlüssels
        <strong>
          <xsl:value-of select="$accessKeys/@writekey" />
        </strong>
        die Übernahme des Elektronischen Semesterapparates ab. Der Status des ESA nach der Übernahme in die neue DBT
        entspricht dem Status des ESA vor der
        Übernahme (aktiv / inaktiv).
      </p>
      <!-- 
      <strong>Sehr geehrte Damen und Herren,</strong>
      <br />
      <p>
        mit diesem Schreiben erhalten Sie alle erforderlichen Schritte zur Übernahme des folgenden Elektronischen Semesterapparates (ESA) in die neue Digitale
        Bibliothek (DBT):
      </p>
      <dl>
        <dt>Titel</dt>
        <dd>
          <xsl:value-of select="title" />
        </dd>
        <dt>Apparate-Inhaber</dt>
        <dd>
          <xsl:for-each select="lecturers/lecturer">
            <xsl:call-template name="formatName">
              <xsl:with-param name="name" select="@name" />
            </xsl:call-template>
            <xsl:if test="position() != last()">
              <xsl:text>; </xsl:text>
            </xsl:if>
          </xsl:for-each>
        </dd>
        <dt>Status</dt>
        <dd>
          <xsl:value-of select="@status" />
        </dd>
        <dt>Gültig bis</dt>
        <dd>
          <xsl:value-of select="validTo" />
        </dd>
      </dl>
      <p>
        Haben Sie diese E-Mail als
        <i>beauftragte Person</i>
        und nicht als Apparate-Inhaber erhalten, so leiten Sie die E-Mail bitte an den Apparate-Inhaber weiter. Nur der
        Apparate-Inhaber persönlich kann die
        nachfolgend aufgeführten Schritte zur Übernahme ausführen. Nach der Übernahme kann der Apparate-Inhaber Sie wieder
        als beauftragte Person /
        Ansprechpartner benennen und mit der Betreuung des Elektronischen Semesterapparates beauftragen.
      </p>
      <p>
        Als Apparate-Inhaber gehen Sie für die Übernahme dieses Elektronischen Semesterapparates in die neue DBT bitte wie folgt vor:
      </p>
      <ul>
        <li>
          Kopieren Sie den folgenden Zugriffsschlüssel in die Zwischenablage:
          <strong>
            <xsl:value-of select="$accessKeys/@writekey" />
          </strong>
        </li>
        <li>
          Melden Sie sich
          <strong>
            <a href="{concat($WebApplicationBaseURL, 'rc/', $slotId)}"> hier </a>
          </strong>
          wie folgt an:
          <ul>
            <li>Auswahl: Uni-Login</li>
            <li>Auswahl Organisation: Friedrich-Schiller-Universität Jena / Technische Universität Ilmenau</li>
            <li>Eingabe Ihrer persönlichen URZ-Nutzerkennung</li>
            <li>Eingabe Zugriffsschlüsse. Damit erhalten Sie die Berechtigung zur Bearbeitung dieses ESA</li>
          </ul>
        </li>
        <li>
          Abschließende Zuordnung des ESA zu Ihrer Person mittels „Eigentümerwechsel“ durchführen:
          <ul>
            <li>Eingabe Leseschlüssel (für Teilnehmer der Lehrveranstaltung)</li>
            <li>Eingabe Schreibschlüssel (für beauftragte Person / Ansprechpartner)</li>
            <li>Mit „Speichern“ wird der Elektronische Semesterapparat in die neue DBT übernommen</li>
          </ul>
        </li>
        <li>
          Ansprechpartner / beauftragte Person festlegen:
          <ul>
            <li>Wechsel in den Bearbeitungsmodus</li>
            <li>Auswahl „Semesterapparat verwalten“ (ggf. muss der ESA zuerst reaktiviert werden)</li>
            <li>Eingabe Name und E-Mail-Adresse für den Ansprechpartner / die beauftragte Person</li>
          </ul>
        </li>
      </ul>
      <br />
      <strong>Hinweise:</strong>
      <p>Der Status des ESA nach der Übernahme in die neue DBT entspricht dem Status des ESA vor der Übernahme (aktiv / inaktiv). Mit Wechsel in den
        Bearbeitungsmodus können Sie inaktive ESA jederzeit reaktivieren bzw. aktive ESA sofort bearbeiten.</p>
      <p>Die gesetzten Schreib- bzw. Leseschlüssel teilen Sie bitte ausschließlich den jeweils berechtigten Personen mit. Diese melden sich für den Zugriff auf
        den ESA mit ihrer persönlichen URZ-Nutzerkennung in der DBT mit anschließender Eingabe des Schreibschlüssels an.
        Denken Sie daran, die Links zu Ihren
        Elektronischen Semesterapparaten an allen erforderlichen Stellen zu aktualisieren.</p>
      <p>
        Bei Problemen mit der Übernahme eines Semesterapparates in die neue DBT leiten Sie diese Mail bitte mit einem kurzen Kommentar weiter an
        <a href="mailto:elektronische_semesterapparate@thulb.uni-jena.de">elektronische_semesterapparate@thulb.uni-jena.de</a>
        Informationen rund um die Elektronischen Semesterapparate finden Sie unter den neu zusammengestellten FAQ direkt auf der Seite der DBT.
      </p>
      -->
      <br />
      Mit freundlichen Grüßen
      <br />
      Ihr Team Elektronische Semesterapparate
    </body>
  </xsl:template>

  <xsl:template match="entry" mode="email">
    <xsl:apply-templates select="opcrecord" mode="email" />
  </xsl:template>
  
  <!-- Entry Templates -->
  <xsl:template match="opcrecord" mode="email">
    <xsl:variable name="slot" select="document(concat('slot:slotId=', $slotId))/slot" />
    <xsl:variable name="isActive" select="document(concat('slot:slotId=', $slotId, '&amp;isActive'))/slot" />

    <xsl:if test="($slot/@onlineOnly = 'false') and ($isActive = 'true') and ($action != 'update')">
      <xsl:message>
        Send Mail for:
        <xsl:value-of select="name()" />
        To:
        <xsl:value-of select="$editorTo" />
      </xsl:message>
      <to>
        <xsl:value-of select="$editorTo" />
      </to>
      <subject>
        <xsl:value-of select="concat('ESA ', $slotId, ': ')" />
        <xsl:choose>
          <xsl:when test="$action = 'create'">
            <xsl:text>Neuer Katalog-Eintrag</xsl:text>
          </xsl:when>
          <xsl:when test="$action = 'update'">
            <xsl:text>Katalog-Eintrag wurde geändert</xsl:text>
          </xsl:when>
          <xsl:when test="$action = 'delete'">
            <xsl:text>Katalog-Eintrag wurde gelöscht</xsl:text>
          </xsl:when>
        </xsl:choose>
      </subject>
      <body>
        <strong>Sehr geehrt(e) Mitarbeiter(in),</strong>
        <br />
        <p>
          <xsl:text>der/die DozentIn hat in seinem Online-Semesterapparat ein </xsl:text>
          <xsl:choose>
            <xsl:when test="$action = 'create'">
              <xsl:text>neuen Katalog-Eintrag hinzugefügt</xsl:text>
            </xsl:when>
            <xsl:when test="$action = 'update'">
              <xsl:text>Katalog-Eintrag geändert</xsl:text>
            </xsl:when>
            <xsl:when test="$action = 'delete'">
              <xsl:text>Katalog-Eintrag gelöscht</xsl:text>
            </xsl:when>
          </xsl:choose>
          <xsl:text>:</xsl:text>
        </p>

        <xsl:apply-templates select="." mode="output">
          <xsl:with-param name="withCopys" select="$action = 'create'" />
        </xsl:apply-templates>

        <xsl:if test="contains('create|delete', $action)">
          <div id="todo">
            Bearbeitungsschritte
            <br />
            <ul>
              <xsl:choose>
                <xsl:when test="$action = 'create'">
                  <li>Stellen Sie ein Exemplar in das Präsenz-Regal im Lesesaal</li>
                  <li>Schalten Sie diesen Eintrag frei (GBV-Kat. u. DBT)</li>
                </xsl:when>
                <xsl:when test="$action = 'delete'">
                  <li>Buchen Sie bitte das Exemplar in PICA/DBT zurück</li>
                </xsl:when>
              </xsl:choose>
            </ul>
          </div>
        </xsl:if>
      </body>
    </xsl:if>
  </xsl:template>

  <xsl:template match="opcrecord" mode="output">
    <xsl:param name="withCopys" select="true()" />
    <xsl:param name="entryId" select="$entryId" />

    <div class="pica-record">
      <xsl:apply-templates select="pica:record" mode="isbd" />
    </div>

    <xsl:variable name="ppn" select="pica:record/@ppn" />
    <xsl:variable name="record" select="document(concat('notnull:opc:catalogId=', $catalogId, '&amp;record=', $ppn, '&amp;copys=true'))" />

    <dl>
      <xsl:if test="($action != 'delete') or (@deleted = 'true')">
        <dt>Eintrag</dt>
        <dd>
          <xsl:variable name="entryLink" select="concat($WebApplicationBaseURL, 'rc/', $slotId, '?XSL.Mode=edit#', $entryId)" />
          <a href="{$entryLink}">
            <xsl:value-of select="$entryLink" />
          </a>
        </dd>
      </xsl:if>
      <dt>PPN</dt>
      <dd>
        <xsl:value-of select="$ppn" />
      </dd>
      <xsl:if test="string-length(@epn) &gt; 0">
        <dt>EPN</dt>
        <dd>
          <xsl:value-of select="@epn" />
        </dd>
      </xsl:if>
    </dl>

    <xsl:if test="string-length(@epn) &gt; 0">
      <xsl:variable name="epn" select="@epn" />
      <xsl:variable name="occurrence" select="$record//pica:field[@tag = '203@' and (pica:subfield[@code = '0'] = $epn)]/@occurrence" />
      <dl>
        <xsl:for-each select="$record//pica:field[@tag = '209A' and @occurrence = $occurrence]">
          <dt>Standort</dt>
          <dd>
            <xsl:value-of select="pica:subfield[@code='f']" />
          </dd>
          <dt>Signatur</dt>
          <dd>
            <xsl:value-of select="pica:subfield[@code='a']" />
          </dd>
        </xsl:for-each>
      </dl>
    </xsl:if>

    <xsl:if test="$withCopys and (string-length(@epn) = 0)">
      <dl>
        <xsl:for-each select="$record//pica:field[@tag = '209A']">
          <dt>Standort</dt>
          <dd>
            <xsl:value-of select="pica:subfield[@code='f']" />
          </dd>
          <dt>Signatur</dt>
          <dd>
            <xsl:value-of select="pica:subfield[@code='a']" />
          </dd>
        </xsl:for-each>
      </dl>
    </xsl:if>

    <xsl:if test="position() != last()">
      <hr />
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>