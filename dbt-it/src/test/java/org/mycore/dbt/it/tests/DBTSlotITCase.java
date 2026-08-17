package org.mycore.dbt.it.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.dbt.it.controller.DBTSlotController;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.controller.MIRUserController;
import org.mycore.mir.it.tests.MIRITBase;


public class DBTSlotITCase extends MIRITBase {

    private static final String LECTURER_LOGIN = "rclecturer";

    private static final String LECTURER_PASSWORD = "tugdriwsella";

    private static final String LECTURER_NAME = "Mustermann, Max";

    private static final String LECTURER_EMAIL = "max.mustermann@uni-jena.de";

    private static final String TITLE = "IT_Semesterapparat";

    private static final String READ_KEY = "it-read-key";

    private static final String WRITE_KEY = "it-write-key";

    private static final String[] LOCATION = { "ThULB Jena", "Testsemesterapparate", "Geisteswissenschaften" };

    private DBTSlotController slotController;

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTControllerFactory();
    }

    @Before
    public final void init() {
        slotController = ((DBTControllerFactory) controllerFactory).createSlotController(driver, getAPPUrlString());

        userController.logoutIfLoggedIn();
        userController.loginAs(MIRUserController.ADMIN_LOGIN, MIRUserController.ADMIN_PASSWD);
        // the user page is titled with the realName once the account has one, so the default assertion of
        // createUser - which expects the login name - has to be replaced
        userController.createUser(LECTURER_LOGIN, LECTURER_PASSWORD, LECTURER_NAME, LECTURER_EMAIL,
            () -> userController.assertUserCreated(LECTURER_NAME), "submitter");
        userController.logoutIfLoggedIn();
        userController.loginAs(LECTURER_LOGIN, LECTURER_PASSWORD);
    }

    @Test
    public void testCreateSlot() {
        slotController.openNewSlotForm();

        slotController.setTitle(TITLE);
        slotController.selectLocation(LOCATION);
        slotController.setAccessKeys(READ_KEY, WRITE_KEY);

        slotController.save();

        Assert.assertTrue("New Semesterapparat should show its initial entry!", slotController.isSlotCreated());
    }


    @After
    @Override
    public void tearDown() {
        super.tearDown();

        userController.logoutIfLoggedIn();
        userController.loginAs(MIRUserController.ADMIN_LOGIN, MIRUserController.ADMIN_PASSWD);
        userController.deleteUser(LECTURER_LOGIN);
        userController.logoutIfLoggedIn();
    }
}
