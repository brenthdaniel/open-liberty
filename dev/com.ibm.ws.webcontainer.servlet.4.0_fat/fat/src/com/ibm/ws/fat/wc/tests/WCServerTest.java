/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.fat.wc.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.fat.util.LoggingTest;
import com.ibm.ws.fat.util.SharedServer;
import com.ibm.ws.fat.util.browser.WebBrowser;
import com.ibm.ws.fat.util.browser.WebResponse;
import com.ibm.ws.fat.wc.WCApplicationHelper;

import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;

/**
 * All Servlet 4.0 tests with all applicable server features enabled.
 */
public class WCServerTest extends LoggingTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(WCServerTest.class.getName());

	protected static final Map<String, String> testUrlMap = new HashMap<String, String>();

	@ClassRule
	public static SharedServer SHARED_SERVER = new SharedServer("servlet40_wcServer");

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ibm.ws.fat.util.LoggingTest#getSharedServer()
	 */
	@Override
	protected SharedServer getSharedServer() {
		// TODO Auto-generated method stub
		return SHARED_SERVER;
	}

	@BeforeClass
	public static void setUp() throws Exception {

		LOG.info("Setup : add TestServlet40 to the server if not already present.");

		WCApplicationHelper.addEarToServerDropins(SHARED_SERVER.getLibertyServer(), "TestServlet40.ear", true,
				"TestServlet40.war", true, "TestServlet40.jar", true, "testservlet40.war.servlets",
				"testservlet40.war.listeners", "testservlet40.jar.servlets");

		SHARED_SERVER.startIfNotStarted();

		LOG.info("Setup : wait for message to indicate app has started");

		SHARED_SERVER.getLibertyServer().waitForStringInLog("CWWKZ0001I.* TestServlet40", 10000);

		LOG.info("Setup : wait for message to indicate app has started");

	}

	@AfterClass
	public static void testCleanup() throws Exception {

		LOG.info("testCleanUp : stop server");

		SHARED_SERVER.getLibertyServer().stopServer(null);

	}

	protected String parseResponse(WebResponse wr, String beginText, String endText) {
		String s;
		String body = wr.getResponseBody();
		int beginTextIndex = body.indexOf(beginText);
		if (beginTextIndex < 0)
			return "begin text, " + beginText + ", not found";
		int endTextIndex = body.indexOf(endText, beginTextIndex);
		if (endTextIndex < 0)
			return "end text, " + endText + ", not found";
		s = body.substring(beginTextIndex + beginText.length(), endTextIndex);
		return s;
	}

	/**
	 * Request a simple servlet.
	 *
	 * @throws Exception
	 */
	@Test
	@Mode(TestMode.LITE)
	public void testSimpleServlet() throws Exception {

		this.verifyResponse("/TestServlet40/SimpleTestServlet", "Hello World");
	}

	/**
	 * Simple test to a servlet then read the header to ensure we are using
	 * Servlet 4.0
	 *
	 * @throws Exception
	 *             if something goes horribly wrong
	 */
	@Test
	@Mode(TestMode.LITE)
	public void testServletHeader() throws Exception {
		WebResponse response = this.verifyResponse("/TestServlet40/MyServlet", "Hello World");

		// verify the X-Powered-By Response header
		response.verifyResponseHeaderEquals("X-Powered-By", false, "Servlet/4.0", true, false);
	}

	/**
	 * Verifies that the ServletContext.getMajorVersion() returns 4 and
	 * ServletContext.getMinorVersion() returns 0 for Servlet 4.0.
	 *
	 * @throws Exception
	 */

	@Test
	@Mode(TestMode.LITE)
	public void testServletContextMajorMinorVersion() throws Exception {
		this.verifyResponse("/TestServlet40/MyServlet?TestMajorMinorVersion=true", "majorVersion: 4");

		this.verifyResponse("/TestServlet40/MyServlet?TestMajorMinorVersion=true", "minorVersion: 0");
	}

	/**
	 * Verify that HttpServletRequest.getCookies(String name) works correctly by
	 * returning a set of cookies for a given name.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCookiesByName() throws Exception {
		WebBrowser wb = createWebBrowserForTestCase();

		// First request will set the cookies in the response object and the
		// client will store them
		this.verifyResponse(wb, "/TestServlet40/CookieFilter?cookieName=test",
				new String[] { "All the cookies", "Filtering cookies with name: test" });

		// Second request the client will send the cookies in the request object
		this.verifyResponse(wb, "/TestServlet40/CookieFilter?cookieName=test",
				new String[] { "All the cookies", "Cookie: name: test; value: hij", "Cookie: name: test; value: def",
						"Cookie: name: hello; value: John", "Cookie: name: awesome; value: thing",
						"Cookie: name: bye; value: Ally", "Cookie: name: test; value: klm",
						"Filtering cookies with name: test", "Cookie Filter: name: test; value: hij",
						"Cookie Filter: name: test; value: def", "Cookie Filter: name: test; value: klm" });
	}

	/**
	 * Test the setSessionTimeout and getSessionTimeout methods from the servlet
	 * context API.
	 *
	 * @throws Exception
	 */
	@Test
	@Mode(TestMode.LITE)
	public void testServletContextSetAndGetSessionTimeout() throws Exception {
		WebBrowser wb = createWebBrowserForTestCase();

		// First request will get a new session and will verify that the
		// getSessionTimeout method returns the correct timeout.
		this.verifyResponse(wb, "/TestServlet40/SessionTimeoutServlet?TestSessionTimeout=new",
				new String[] { "Session Timeout: 1", "Session object: # HttpSessionImpl #",
						"max inactive interval : 60", "valid session : true", "new session : true" });

		// Second request will verify that the session is still the same, so no
		// new session has been created
		this.verifyResponse(wb, "/TestServlet40/SessionTimeoutServlet?TestSessionTimeout=current",
				new String[] { "Session object: # HttpSessionImpl #", "max inactive interval : 60",
						"valid session : true", "new session : false" });

		Thread.sleep(70000); // Wait 70 seconds or 1 minute and 10 seconds

		// Third request will verify if session was invalidated after 70 seconds
		// or 1 minute and 10 seconds.
		this.verifyResponse("/TestServlet40/SessionTimeoutServlet?TestSessionTimeout=current",
				new String[] { "Session object: null", "Session Invalidated" });
	}
}