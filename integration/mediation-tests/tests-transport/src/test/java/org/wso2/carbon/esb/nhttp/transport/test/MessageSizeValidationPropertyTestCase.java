/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.nhttp.transport.test;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This class verifies the functionality of dropping messages when the message size passes a given threshold, in
 * nhttp transport.
 *
 * Public Jira: https://wso2.org/jira/browse/ESBJAVA-4989
 */
public class MessageSizeValidationPropertyTestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private SimpleHttpClient httpClient;
    //payload size below 185 bytes which is the threshold set in nhttp.properties
    private String smallPayload = "<ns3:orders xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
            " xmlns:ns3=\"http://www.wso2.com/xml/ns/wso2/order/2.0\">\n" +
            "    <ns3:order id=\"00000000000000000000\">\n" +
            "    </ns3:order>\n" +
            "</ns3:orders>";
    //payload size 2160 bytes > 185 bytes which is the threshold set in nhttp.properties
    private String largePayload = "<ns3:orders xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
            " xmlns:ns3=\"http://www.wso2.com/xml/ns/wso2/order/2.0\">\n" +
            "    <ns3:order id=\"00000000000000000001\">\n" +
            "        <ns3:order-header>\n" +
            "            <ns3:order-process-info>\n" +
            "                <ns3:status>EXPORTED</ns3:status>\n" +
            "                <ns3:creation-date>2016-03-09T12:30:59.806+11:00</ns3:creation-date>\n" +
            "            </ns3:order-process-info>\n" +
            "            <order-info>\n" +
            "                <document-no>00000001</document-no>\n" +
            "                <store>SYDNEY</store>\n" +
            "                <locale>en_AU</locale>\n" +
            "                <currency>AUD</currency>\n" +
            "                <cost-center>xusKAAqA4_cAAAFTsp1zRAaL</cost-center>\n" +
            "            </order-info>\n" +
            "            <ns3:customer>\n" +
            "                <ns3:customer-id>ACME Inc</ns3:customer-id>\n" +
            "                <ns3:account>educking@test.de</ns3:account>\n" +
            "                <ns3:customer-type-id>MEMBER</ns3:customer-type-id>\n" +
            "                <ns3:calculation-type>net</ns3:calculation-type>\n" +
            "                <ns3:first-name>Emil</ns3:first-name>\n" +
            "                <ns3:last-name>Ducking</ns3:last-name>\n" +
            "                <ns3:company-name1>ACME Inc</ns3:company-name1>\n" +
            "                <ns3:user-id>educking</ns3:user-id>\n" +
            "                <ns3:taxation-id>986d35608155</ns3:taxation-id>\n" +
            "            </ns3:customer>\n" +
            "            <ns3:invoice-to-address>\n" +
            "                <ns3:contact>\n" +
            "                    <ns3:first-name>Bashir</ns3:first-name>\n" +
            "                    <ns3:last-name>Ahmad</ns3:last-name>\n" +
            "                    <ns3:phone-home>257896321</ns3:phone-home>\n" +
            "                    <ns3:mailing-address>\n" +
            "                        <ns3:line1>Main Street 12</ns3:line1>\n" +
            "                        <ns3:city>Melborne</ns3:city>\n" +
            "                        <ns3:country>Australia</ns3:country>\n" +
            "                        <ns3:country-code>AU</ns3:country-code>\n" +
            "                        <ns3:postal-code>3000</ns3:postal-code>\n" +
            "                        <ns3:state>VIC</ns3:state>\n" +
            "                    </ns3:mailing-address>\n" +
            "                    <ns3:company-name1>ACME Inc</ns3:company-name1>\n" +
            "                </ns3:contact>\n" +
            "            </ns3:invoice-to-address>\n" +
            "        </ns3:order-header>\n" +
            "    </ns3:order>\n" +
            "</ns3:orders>";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        serverConfigurationManager =
                new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyConfiguration(Paths.get(getESBResourceLocation(), "nhttp", "transport",
                "messagesizevalidation", "nhttp.properties").toFile());
        super.init();
        verifyAPIExistence("MessageSizeValidationPropertyTestAPI");
        httpClient = new SimpleHttpClient();
    }

    @Test(groups = "wso2.esb", description = "Tests whether a message of a size below the threshold specified at nhttp"
            + ".properties passes")
    public void testSendMessageBelowThreshold() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/xml;charset=UTF-8");
        HttpResponse response = httpClient
                .doPost(getApiInvocationURL("echoapi/respond"), headers, smallPayload,
                        "application/xml;charset=UTF-8");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                "Server returned unexpected HTTP reason phrase");
    }

    @Test(groups = "wso2.esb", description = "Tests whether a messages is dropped when message size " +
            "exceeds a given threshold specified at nhttp.properties")
    public void testSendMessageOverThreshold() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/xml;charset=UTF-8");
        HttpResponse response = httpClient
                .doPost(getApiInvocationURL("echoapi/respond"), headers, largePayload,
                        "application/xml;charset=UTF-8");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 413,
                "Server returned unexpected HTTP response code.");
        Assert.assertEquals(response.getStatusLine().getReasonPhrase(), "Request Too Long",
                "Server returned unexpected HTTP response reason phrase");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
    }

}
