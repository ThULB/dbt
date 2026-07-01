<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dbt="https://www.db-thueringen.de"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsi xlink">
    <xsl:output media-type="text/xml" indent="yes" method="xml"/>
    <xsl:variable name="uboClass"
                  select="document('https://raw.githubusercontent.com/ThULB/ThUniBib/develop/weimar/src/setup/classifications/ORIGIN.xml')"/>

    <xsl:template match='@*|node()'>
        <xsl:param name="prefix"/>

        <!-- default template: just copy -->
        <xsl:copy>
            <xsl:if test="@ID">
                <xsl:attribute name="ID">
                    <xsl:value-of select="concat($prefix, @ID)"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="@*[local-name()!='ID']|node()">
                <xsl:with-param name="prefix" select="$prefix"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*[local-name()='xsi']"/>
    <xsl:template match="@*[local-name()='xlink']"/>

    <xsl:template match="*[ancestor-or-self::*[@ID='3']]" priority="2">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="label"/>
            <xsl:variable name="thisCategory" select="."/>
            <xsl:variable name="orgUnit" select="dbt:findUBOCategory(.)"/>
            <xsl:choose>
                <xsl:when test="not($orgUnit)"/>
                <xsl:when test="$orgUnit[local-name() = 'fail']">
                    <xsl:comment>
                        <xsl:value-of select="concat(@ID, ' not found')"/>
                    </xsl:comment>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$orgUnit/label[@xml:lang != 'de']"/>
                </xsl:otherwise>
            </xsl:choose>
            <!--xsl:copy-of select="$orgUnit" /-->
            <xsl:copy-of select="uri"/>
            <xsl:apply-templates select="category"/>
            <xsl:if test="$orgUnit and not($orgUnit[local-name() = 'fail'])">
                <xsl:for-each select="$orgUnit/category">
                    <xsl:variable name="thisLabel" select="label[lang('de')]/normalize-unicode(@text)"/>
                    <xsl:choose>
                        <xsl:when
                                test="$thisCategory/category/label[lang('de')]/lower-case(normalize-unicode(@text)) = lower-case($thisLabel)">
                            <xsl:comment>skipping category
                                <xsl:value-of select="@ID"/>
                            </xsl:comment>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:comment>adding category
                                <xsl:value-of select="@ID"/>
                            </xsl:comment>
                            <xsl:apply-templates select=".">
                                <xsl:with-param name="prefix" select="concat($thisCategory/@ID,'.')"/>
                            </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="dbt:findUBOCategory" as="item()*">
        <xsl:param name="currentNode" as="element()"/>
        <xsl:for-each select="$currentNode/ancestor-or-self::category[ancestor::category[@ID='3']]">
            <xsl:sort order="ascending"/>
            <xsl:variable name="label.de" select="label[lang('de')]/normalize-unicode(@text)"/>
            <xsl:variable name="depth" select="position()"/>
            <xsl:variable name="orgUnit"
                          select="$uboClass//category[label/lower-case(normalize-unicode(@text)) = lower-case($label.de) and count(ancestor-or-self::category) = $depth]"/>
        <xsl:message>
            label-de: <xsl:value-of select="$label.de" />
            depth: <xsl:value-of select="$depth" />
            candidates: <xsl:value-of select="count($uboClass//category[label/lower-case(normalize-unicode(@text)) = lower-case($label.de)])" />
            depth2: <xsl:value-of select="count($uboClass//category[label/lower-case(normalize-unicode(@text)) = lower-case($label.de)]/ancestor-or-self::category)" />
        </xsl:message>
            <xsl:choose>
                <xsl:when test="position() != last() and not($orgUnit)">
                    <fail/>
                </xsl:when>
                <xsl:when test="position() != last() and $orgUnit">
                    <!-- right on plan -->
                </xsl:when>
                <xsl:when test="$orgUnit">
                    <xsl:sequence select="$orgUnit"/>
                </xsl:when>
                <xsl:otherwise>
                    <fail/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>

</xsl:stylesheet>
