/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WireMockServerTests {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void instantiationWithEmptyFileSource() throws IOException {
        Options options = new WireMockConfiguration().dynamicPort().fileSource(new SingleRootFileSource(tempDir.getRoot()));

        WireMockServer wireMockServer = null;
        try {
            wireMockServer = new WireMockServer(options);
            wireMockServer.start();
        } finally {
            if (wireMockServer != null) {
                wireMockServer.stop();
            }
        }
    }

    @Test
    public void returnsOptionsWhenCallingGetOptions() {
        Options options = new WireMockConfiguration();
        WireMockServer wireMockServer = new WireMockServer(options);
        assertThat(wireMockServer.getOptions(), is(options));
    }

    @Test
    public void buildsQualifiedHttpUrlFromPath() {
        WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        int port = wireMockServer.port();

        assertThat(wireMockServer.url("/something"), is(String.format("http://localhost:%d/something", port)));
        assertThat(wireMockServer.url("something"), is(String.format("http://localhost:%d/something", port)));
    }

    @Test
    public void buildsQualifiedHttpsUrlFromPath() {
        WireMockServer wireMockServer = new WireMockServer(options()
            .dynamicPort()
            .dynamicHttpsPort()
        );
        wireMockServer.start();
        int port = wireMockServer.httpsPort();

        assertThat(wireMockServer.url("/something"), is(String.format("https://localhost:%d/something", port)));
        assertThat(wireMockServer.url("something"), is(String.format("https://localhost:%d/something", port)));
    }

    // https://github.com/tomakehurst/wiremock/issues/193
    @Test
    public void supportsRecordingProgrammaticallyWithoutHeaderMatching() {
        WireMockServer wireMockServer = new WireMockServer(Options.DYNAMIC_PORT, new SingleRootFileSource(tempDir.getRoot()), false, new ProxySettings("proxy.company.com", Options.DYNAMIC_PORT));
        wireMockServer.start();
        wireMockServer.enableRecordMappings(new SingleRootFileSource(tempDir.getRoot() + "/mappings"), new SingleRootFileSource(tempDir.getRoot() + "/__files"));
        wireMockServer.stubFor(get(urlEqualTo("/something")).willReturn(aResponse().withStatus(200)));

        WireMockTestClient client = new WireMockTestClient(wireMockServer.port());
        assertThat(client.get("http://localhost:" + wireMockServer.port() + "/something").statusCode(), is(200));
    }

    // https://github.com/tomakehurst/wiremock/issues/691
    @Test
    public void testNotGettingNullPointerAtSecondRequest() throws Exception {

        WireMockServer wireMockServer = new WireMockServer(Options.DYNAMIC_PORT, new SingleRootFileSource(tempDir.getRoot()), false, new ProxySettings("proxy.company.com", Options.DYNAMIC_PORT));

        wireMockServer.start();
        wireMockServer.enableRecordMappings(new SingleRootFileSource(tempDir.getRoot() + "/mappings"), new SingleRootFileSource(tempDir.getRoot() + "/__files"));

        wireMockServer.stubFor(post(urlEqualTo("/something")).willReturn(aResponse().withStatus(200)));

        WireMockTestClient client = new WireMockTestClient(wireMockServer.port());

        final String firstRequest = "{\"columns\": [{\"name\": \"x\",\"y\": 3},{\"name\": \"agreementnumber\",\"a\": 1},{\"name\": \"agreementstatus\",\"b\": 2}]}";
        final WireMockResponse firstResponse = client.post("/something", new StringEntity(firstRequest, ContentType.APPLICATION_JSON));
        assertThat(firstResponse.statusCode(), is(200));


        final String secondRequest = "{\"columns\": [{\"name\": \"agreementnumber\",\"a\": 1},{\"name\": \"utilizerstatus\",\"b\": 2}]}";
        final WireMockResponse secondResponse = client.post("/something", new StringEntity(secondRequest, ContentType.APPLICATION_JSON));
        assertThat(secondResponse.statusCode(), is(200));
    }
}
