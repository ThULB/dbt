<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3">

<xsl:template match="node()| @*">
    <xsl:copy>
        <xsl:apply-templates select="node()| @*"/>
    </xsl:copy>
</xsl:template>


<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'VerfasserIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'aut' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'AkademischeR BetreuerIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'ths' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'FotografIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'pht' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'GutachterIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'rev' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'GefeierteR' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'hnr' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'HerausgeberIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'edt' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'IllustratorIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'ill' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'KünstlerIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'art' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'SponsorIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'spn' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'ÜbersetzerIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'trl' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'VeranstalterIn' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'orm' ]]]"/>

<xsl:template match="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:role[mods:roleTerm[@type = 'text' and text()= 'ZusammenstellendeR' ]
                    and ../mods:role[mods:roleTerm[@type= 'code' and @authority= 'marcrelator' and text()= 'col' ]]]"/>
</xsl:stylesheet>
