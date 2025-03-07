/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.api.tasks

import groovy.transform.SelfType
import org.gradle.integtests.fixtures.AbstractIntegrationSpec

@SelfType(AbstractIntegrationSpec)
trait UnreadableCopyDestinationFixture {
    private static final String COPY_UNREADABLE_DESTINATION_DEPRECATION = "Cannot access a file in the destination directory (see --info log for details). " +
        "Copying to a directory which contains unreadable content has been deprecated. " +
        "This will fail with an error in Gradle 8.0. " +
        "Use the method Copy.ignoreExistingContentInDestinationDir(). " +
        "Consult the upgrading guide for further information: https://docs.gradle.org/current/userguide/upgrading_version_7.html#declare_unreadable_input_output"

    void expectUnreadableCopyDestinationDeprecationWarning() {
        executer.expectDocumentedDeprecationWarning(COPY_UNREADABLE_DESTINATION_DEPRECATION)
    }
}
