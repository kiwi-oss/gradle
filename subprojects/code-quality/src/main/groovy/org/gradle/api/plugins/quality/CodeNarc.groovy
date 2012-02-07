/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.plugins.quality

//import org.gradle.api.plugins.quality.internal.ConsoleReportWriter


import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.Instantiator
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.quality.internal.CodeNarcReportsImpl
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.Reporting
import org.gradle.util.DeprecationLogger
import org.gradle.api.tasks.*

/**
 * Runs CodeNarc against some source files.
 */
class CodeNarc extends SourceTask implements VerificationTask, Reporting<CodeNarcReports> {
    /**
     * The class path containing the CodeNarc library to be used.
     */
    @InputFiles
    FileCollection codenarcClasspath

    /**
     * The CodeNarc configuration file to use.
     */
    @InputFile
    File configFile

    /**
     * The format type of the CodeNarc report.
     *
     * @deprecated Use {@code reports.<report-type>.enabled} instead.
     */
    @Deprecated
    String getReportFormat() {
        DeprecationLogger.nagUserOfReplacedProperty("CodeNarc.reportFormat", "reports.<report-type>.enabled")
        reports.firstEnabled?.name
    }

    /**
     * @deprecated Use {@code reports.<report-type>.enabled} instead.
     */
    @Deprecated
    void setReportFormat(String reportFormat) {
        DeprecationLogger.nagUserOfReplacedProperty("CodeNarc.reportFormat", "reports.<report-type>.enabled")
        reports.each {
            it.enabled == it.name == reportFormat
        }
    }

    /**
     * The file to write the report to
     *
     * @deprecated Use {@code reports.<report-type>.destination} instead.
     */
    @Deprecated
    File getReportFile() {
        DeprecationLogger.nagUserOfReplacedProperty("CodeNarc.reportFile", "reports.<report-type>.destination")
        reports.firstEnabled?.destination
    }

    /**
     * @deprecated Use {@code reports.<report-type>.destination} instead.
     */
    @Deprecated
    void setReportFile(File reportFile) {
        DeprecationLogger.nagUserOfReplacedProperty("CodeNarc.reportFile", "reports.<report-type>.destination")
        reports.firstEnabled?.destination = reportFile
    }

    @Nested
    private final CodeNarcReportsImpl reports = services.get(Instantiator).newInstance(CodeNarcReportsImpl, this)

    /**
     * Whether or not the build should break when the verifications performed by this task fail.
     */
    boolean ignoreFailures

    @TaskAction
    void run() {
        logging.captureStandardOutput(LogLevel.INFO)
        def antBuilder = services.get(IsolatedAntBuilder)
        antBuilder.withClasspath(getCodenarcClasspath()).execute {
            ant.taskdef(name: 'codenarc', classname: 'org.codenarc.ant.CodeNarcTask')
            try {
                ant.codenarc(ruleSetFiles: "file:${getConfigFile()}", maxPriority1Violations: 0, maxPriority2Violations: 0, maxPriority3Violations: 0) {
                    reports.enabled.each { Report r ->
                        report(type: r.name) {
                            option(name: 'outputFile', value: r.destination)
                        }
                    }

                    source.addToAntBuilder(ant, 'fileset', FileCollection.AntType.FileSet)
                }
            } catch (Exception e) {
                if (e.message.matches('Exceeded maximum number of priority \\d* violations.*')) {
                    if (getIgnoreFailures()) {
                        return
                    }
                    if (reports.html.enabled) {
                        throw new GradleException("CodeNarc rule violations were found. See the report at ${reports.html.destination}.", e)
                    } else {
                        throw new GradleException("CodeNarc rule violations were found.", e)
                    }
                }
                throw e
            }
        }
    }

    CodeNarcReports getReports() {
        return reports
    }

    CodeNarcReports reports(Closure closure) {
        reports.configure(closure)
    }


}
