/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.testing.testng

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.testing.fixture.TestNGCoverage
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Unroll

class TestNGUpToDateCheckIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        executer.noExtraLogging()
        file('src/test/java/SomeTest.java') << '''
            public class SomeTest {
                @org.testng.annotations.Test
                public void pass() {}
            }
        '''.stripIndent()
        file('suite.xml') << '''
            <!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
            <suite name="MySuite">
              <test name="MyTest">
                <classes>
                  <class name="SomeTest" />
                </classes>
              </test>
            </suite>
        '''.stripIndent()
    }

    @Unroll
    @Issue('https://github.com/gradle/gradle/issues/4924')
    def 'test task is up-to-date when #property is changed because it should not impact output'() {
        given:
        TestNGCoverage.enableTestNG(buildFile)

        when:
        succeeds ':test'

        then:
        executedAndNotSkipped ':test'

        when:
        buildScript """
            apply plugin: "java"
            ${mavenCentralRepository()}
            testing {
                suites {
                    test {
                        useTestNG('${TestNGCoverage.NEWEST}')
                        targets {
                            all {
                                testTask.configure {
                                    options {
                                        $property $modification
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """

        and:
        succeeds ':test'

        then:
        skipped ':test'

        where:
        property              | modification
        'suiteName'           | '= "Honeymoon Suite"'
        'testName'            | '= "Turing completeness"'
        'parallel'            | '= "methods"'
        'threadCount'         | '= 2'
        'listeners'           | '= ["org.testng.reporters.FailedReporter"]'
        'useDefaultListeners' | '= true'
        //'configFailurePolicy' | '= "continue"' // See https://github.com/gradle/gradle/issues/10507; API removed in TestNG v7
        'preserveOrder'       | '= true'
        'groupByInstances'    | '= true'
    }

    @Ignore("SLG Temporarily ignore this test until we can figure out how ordering between test framework selection and option configuration works")
    @Unroll
    @Issue('https://github.com/gradle/gradle/issues/4924')
    def "re-executes test when #property is changed"() {
        given:
        TestNGCoverage.enableTestNG(buildFile)

        when:
        succeeds ':test'

        then:
        executedAndNotSkipped ':test'

        when:
        buildScript """
            apply plugin: "java"
            ${mavenCentralRepository()}
            testing {
                suites {
                    test {
                        useTestNG('${TestNGCoverage.NEWEST}')
                        targets {
                            all {
                                testTask.configure {
                                    options {
                                        $property $modification
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """

        and:
        succeeds ':test'

        then:
        executedAndNotSkipped ':test'

        where:
        property              | modification
        'excludeGroups'       | '= ["some group"]'
        'includeGroups'       | '= ["some group"]'
        'outputDirectory'     | '= file("$buildDir/my-out")'
        'suiteXmlFiles'       | '= [file("suite.xml")]'
        'suiteXmlBuilder()'   | '''
                                .suite(name: 'MySuite') {
                                    test(name: 'MyTest') {
                                        classes([:]) {
                                            'class'(name: 'SomeTest')
                                        }
                                    }
                                }
                                '''
    }

}
