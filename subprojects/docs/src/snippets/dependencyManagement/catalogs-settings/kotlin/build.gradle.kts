/*
 * Copyright 2020 the original author or authors.
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

// tag::use_plugin[]
plugins {
    `java-library`
    checkstyle
    alias(libs.plugins.jmh)
}
// end::use_plugin[]

// tag::simple_dependency_use[]
dependencies {
    implementation(libs.groovy.core)
}
// end::simple_dependency_use[]

// tag::use_version[]
checkstyle {
    // will use the version declared in the catalog
    toolVersion = libs.versions.checkstyle.get()
}
// end::use_version[]

// tag::use_catalog[]
dependencies {
    implementation(libs.groovy.core)
    implementation(libs.groovy.json)
    implementation(libs.groovy.nio)
}
// end::use_catalog[]

// tag::use_catalog_equiv[]
dependencies {
    implementation("org.codehaus.groovy:groovy:3.0.5")
    implementation("org.codehaus.groovy:groovy-json:3.0.5")
    implementation("org.codehaus.groovy:groovy-nio:3.0.5")
}
// end::use_catalog_equiv[]

// tag::use_dependency_bundle[]
dependencies {
    implementation(libs.bundles.groovy)
}
// end::use_dependency_bundle[]

// tag::programmatic_access[]
val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    versionCatalog.findDependency("groovy-json").ifPresent {
        implementation(it)
    }
}
// end::programmatic_access[]

