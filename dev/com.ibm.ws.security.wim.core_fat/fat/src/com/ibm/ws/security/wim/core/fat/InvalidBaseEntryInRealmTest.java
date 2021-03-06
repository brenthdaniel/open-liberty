/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.security.wim.core.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.security.registry.EntryNotFoundException;
import com.ibm.ws.security.registry.RegistryException;
import com.ibm.ws.security.registry.test.UserRegistryServletConnection;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;
import componenttest.topology.utils.LDAPUtils;

/**
 * This tests a configuration where the realm's only participating base entry is invalid
 * and doesn't match any known repository. The expectation is that no calls will succeed.
 */
public class InvalidBaseEntryInRealmTest {

    private static LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.security.wim.core.fat.invalidBaseEntryInRealm");
    private static final Class<?> c = InvalidBaseEntryInRealmTest.class;
    private static UserRegistryServletConnection servlet;

    @BeforeClass
    public static void setUp() throws Exception {
        // Add LDAP variables to bootstrap properties file
        LDAPUtils.addLDAPVariables(server);
        Log.info(c, "setUp", "Starting the server... (will wait for userRegistry servlet to start)");
        // install our user feature

        server.copyFileToLibertyInstallRoot("lib/features", "internalfeatures/securitylibertyinternals-1.0.mf");
        server.addInstalledAppForValidation("userRegistry");
        server.startServer(c.getName() + ".log");

        //Make sure the application has come up before proceeding
        assertNotNull("Application userRegistry does not appear to have started.",
                      server.waitForStringInLog("CWWKZ0001I:.*userRegistry"));
        assertNotNull("Security service did not report it was ready",
                      server.waitForStringInLog("CWWKS0008I"));
        assertNotNull("Server did not came up",
                      server.waitForStringInLog("CWWKF0011I"));

        Log.info(c, "setUp", "Creating servlet connection the server");
        servlet = new UserRegistryServletConnection(server.getHostname(), server.getHttpDefaultPort());

        servlet.getRealm();
        Thread.sleep(5000);
        servlet.getRealm();

    }

    @AfterClass
    public static void tearDown() throws Exception {
        Log.info(c, "tearDown", "Stopping the server...");

        try {
            server.stopServer();
        } finally {
            server.removeInstalledAppForValidation("userRegistry");
            server.deleteFileFromLibertyInstallRoot("lib/features/internalfeatures/securitylibertyinternals-1.0.mf");
        }
    }

    /**
     * Hit the test servlet to see if getRealm works.
     */
    @Test
    public void getRealm() throws Exception {
        Log.info(c, "getRealm", "Checking expected realm");
        assertEquals("defaultWIMFileBasedRealm", servlet.getRealm());
    }

    @Test
    public void checkPassword() throws Exception {
        String user = "testuser";
        String password = "testuserpwd";
        Log.info(c, "checkPassword", "No valid participating base entries...");

//        try {
        assertNull(servlet.checkPassword(user, password));
//            fail("Expected RegistryException.");
//        } catch (RegistryException re) {
//            String msg = re.getMessage();
//            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
//        }
    }

    @Test
    public void isValidUser() throws Exception {
        String user = "invalidUser";
        Log.info(c, "isValidUser", "No valid participating base entries...");

        try {
            servlet.isValidUser(user);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUsers() throws Exception {
        String user = "testuser";
        Log.info(c, "getUsers", "No valid participating base entries...");

        try {
            servlet.getUsers(user, 2);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUserDisplayName() throws Exception {
        String user = "testuser";
        Log.info(c, "getUserDisplayName", "No valid participating base entries...");

        try {
            servlet.getUserDisplayName(user);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUniqueUserId() throws Exception {
        String user = "testuser";
        Log.info(c, "getUniqueUserId", "No valid participating base entries...");

        try {
            servlet.getUniqueUserId(user);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUserSecurityName() throws Exception {
        String user = "cn=testuser,o=ibm,c-us";
        Log.info(c, "getUserSecurityName", "No valid participating base entries...");

        try {
            servlet.getUserSecurityName(user);
            fail("Expected EntryNotFoundException.");
        } catch (EntryNotFoundException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML4001E exception message. Message: " + msg, msg.contains("CWIML4001E"));
        }
    }

    @Test
    public void isValidGroup() throws Exception {
        String group = "group1";
        Log.info(c, "isValidGroup", "No valid participating base entries...");

        try {
            servlet.isValidGroup(group);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getGroups() throws Exception {
        String group = "group1";
        Log.info(c, "getGroups", "No valid participating base entries...");

//        try {
        assertNotNull(servlet.getGroups(group, 2));
//            fail("Expected RegistryException.");
//        } catch (RegistryException re) {
//            String msg = re.getMessage();
//            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
//        }
    }

    @Test
    public void getGroupDisplayName() throws Exception {
        String group = "cn=group1,o=o=ibm,c=us";
        Log.info(c, "getGroupDisplayName", "No valid participating base entries...");

        try {
            servlet.getGroupDisplayName(group);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUniqueGroupId() throws Exception {
        String group = "group1";
        Log.info(c, "getUniqueGroupId", "No valid participating base entries...");

        try {
            servlet.getUniqueGroupId(group);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getGroupSecurityName() throws Exception {
        String uniqueGroupId = "cn=group1,o=ibm,c=us";
        Log.info(c, "getGroupSecurityName", "No valid participating base entries...");

        try {
            servlet.getGroupSecurityName(uniqueGroupId);
            fail("Expected EntryNotFoundException.");
        } catch (EntryNotFoundException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getGroupsForUser() throws Exception {
        String user = "samples";
        Log.info(c, "getGroupsForUser", "No valid participating base entries...");

        try {
            servlet.getGroupsForUser(user);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }

    @Test
    public void getUniqueGroupIds() throws Exception {
        String user = "cn=user1,o=ibm,c=us";
        Log.info(c, "getUniqueGroupIds", "No valid participating base entries...");

        try {
            servlet.getGroupsForUser(user);
            fail("Expected RegistryException.");
        } catch (RegistryException re) {
            String msg = re.getMessage();
            assertTrue("Expected a CWIML0515E exception message. Message: " + msg, msg.contains("CWIML0515E"));
        }
    }
}