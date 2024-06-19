package de.urmel_dl.dbt.pi;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.MCRPIGenerator;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.MCRDNBURNParser;

public class DBTMapObjectIDURNGenerator extends MCRPIGenerator<MCRDNBURN> {
    //last char will be replaced by checksum
    private static final String CHECKSUM_PART = "-$";
    private static final MCRDNBURNParser DNBURN_PARSER = new MCRDNBURNParser();
    private static final String URN_NBN_DE = "urn:nbn:de:";

    public MCRDNBURN generate(MCRBase mcrObj, String additional) throws MCRPersistentIdentifierException {
        return buildURN(mcrObj.getId(), additional);
    }

    public String getNamespace(String prefix) {
        if (prefix.startsWith(URN_NBN_DE)) {
            return prefix;
        }
        return "urn:nbn:de:" + prefix;
    }

    protected MCRDNBURN buildURN(MCRObjectID mcrObjectID, String s)
        throws MCRPersistentIdentifierException {
        return Optional.ofNullable(getProperties().get("Prefix." + mcrObjectID.getBase()))
            .map(this::getNamespace)
            .map(prefix -> prefix + mcrObjectID.getNumberAsInteger() + CHECKSUM_PART)
            .flatMap(DNBURN_PARSER::parse).map(MCRDNBURN.class::cast)
            .orElseThrow(() -> new MCRPersistentIdentifierException("Prefix." + mcrObjectID.getBase() +
                " is not defined in " + getGeneratorID() + "."));
    }

}
