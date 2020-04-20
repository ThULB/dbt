package de.urmel_dl.dbt.authorization;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.common.MCRCreatorCache;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mir.authorization.MIRStrategy;

public class DBTStrategy extends MIRStrategy {
    private static Logger LOGGER = LogManager.getLogger();

    private static final String FLAG_TYPE = "dbt-dini";

    private static final String FLAG_VALUE = "exclude";

    private static final String CREATOR_ROLE = MCRConfiguration2.getString("MCR.Access.Strategy.CreatorRole")
        .orElse("submitter");

    @Override
    public boolean checkPermission(String id, String permission) {
        final boolean hasPermission = super.checkPermission(id, permission);
        return hasPermission || checkPermissionForNonDINI(id, permission);
    }

    private boolean checkPermissionForNonDINI(String id, String permission) {
        if (!MCRObjectID.isValid(id)) {
            return false;
        }
        final MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRUserInformation currentUser = MCRSessionMgr.getCurrentSession().getUserInformation();
        return currentUser.isUserInRole(CREATOR_ROLE) && isCurrentUserCreator(mcrId, currentUser)
            && isNonDINIDocument(mcrId);

    }

    private static boolean isCurrentUserCreator(MCRObjectID mcrId, MCRUserInformation currentUser) {
        try {
            String creator = MCRCreatorCache.getCreator(mcrId);
            return currentUser.getUserID().equals(creator);
        } catch (ExecutionException e) {
            LOGGER.error("Error while getting creator information.", e);
            return false;
        }
    }

    private static boolean isNonDINIDocument(MCRObjectID mcrId) {
        MCRObjectID objectID = "derivate".equals(mcrId.getTypeId())
            ? MCRMetadataManager.getObjectId(mcrId, 1, TimeUnit.DAYS)
            : mcrId;
        if (objectID == null) {
            LOGGER.error("Could not find object id to {}.", mcrId);
            return false;
        }
        final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objectID);
        return obj.getService().getFlags(FLAG_TYPE).contains(FLAG_VALUE);
    }

}
