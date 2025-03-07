/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.smoketests

import org.gradle.integtests.fixtures.UnsupportedWithConfigurationCache

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@UnsupportedWithConfigurationCache(
    because = "The Gretty plugin does not support configuration caching"
)
class GrettySmokeTest extends AbstractPluginValidatingSmokeTest {
    def 'run with jetty'() {
        given:
        useSample('gretty-example')
        buildFile << """
            plugins {
                id "war"
                id "org.gretty" version "${TestedVersions.gretty}"
            }

            ${jcenterRepository()}

            dependencies {
                implementation group: 'log4j', name: 'log4j', version: '1.2.15', ext: 'jar'
            }

            gretty {
                contextPath = 'quickstart'

                httpPort = 0
                integrationTestTask = 'checkContainerUp'
                servletContainer = 'jetty9.4'
                logDir = '${testProjectDir.absolutePath}/jetty-logs'
                logFileName = project.name
            }

            task checkContainerUp {
                doLast {
                    def jettyLog = new File("\${gretty.logDir}/\${gretty.logFileName}.log").text
                    def httpPortMatcher = (jettyLog =~ /.* started and listening on port (\\d+)/)
                    def parsedHttpPort = httpPortMatcher[0][1]
                    URL url = new URL("http://localhost:\$parsedHttpPort/quickstart")
                    assert url.text.contains('hello Gradle')
                }
            }
        """

        when:
        def result = runner('checkContainerUp').build()

        then:
        result.task(':checkContainerUp').outcome == SUCCESS
    }

    @Override
    Map<String, Versions> getPluginsToValidate() {
        [
            'org.gretty': Versions.of(TestedVersions.gretty)
        ]
    }
}
