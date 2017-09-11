/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.passthru.transport.test;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This test case verifies that the MIME boundaries are correctly set in a scenario that the payload is built before
 * it is sent to an endpoint. Related public jira: https://wso2.org/jira/browse/ESBJAVA-5092
 * The fix tested by this is a regression issue introduced by fixing https://wso2.org/jira/browse/ESBJAVA-4631
 */
public class ESBJAVA5092MTOMMIMEBoundaryTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mtom/ESBJAVA5092.xml");
    }

    @Test(groups = { "wso2.esb" }, description = "Test MIME boundary correctness", enabled = true)
    public void disableMtomBeforeSendMediatorTest() throws Exception {
        String proxyHttpUrl = getProxyServiceURLHttp("MTOMMIMEBoundaryTestProxy");

        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        Map<String, String> headers = new HashMap<>();
        headers.put("SOAPAction", "urn:mediate");
        String body = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "  <soapenv:Body>"
                + "    <echo:echoString xmlns:echo=\"http://echo.services.core.carbon.wso2.org\">Test Message</echo:echoString>"
                + "  </soapenv:Body>" + "</soapenv:Envelope>";

        HttpResponse httpResponse = simpleHttpClient.doPost(proxyHttpUrl, headers, body, "text/xml; charset=UTF-8");

        // If the payload received by the backend had correct MIME boundaries it would have processed the payload
        // correctly and sent 200 OK response
        Assert.assertEquals(
                "Actual MIME Boundaries may not have been matched with the boundaries set in " + "Content-Type header",
                200, httpResponse.getStatusLine().getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    private void destroy() throws Exception {
        super.cleanup();
    }
}
