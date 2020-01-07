/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.internal.provider

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.internal.state.Managed

import static org.gradle.api.internal.provider.ValueSourceProviderFactory.Listener.ObtainedValue

class DefaultValueSourceProviderFactoryTest extends ValueSourceBasedSpec {

    def "parameters are configured eagerly"() {

        given:
        def configured = false

        when:
        def provider = createProviderOf(EchoValueSource) {
            configured = true
        }

        then:
        configured
    }

    def "listener is notified when value is obtained"() {

        given: "a listener is connected to the provider factory"
        def provider = createProviderOf(EchoValueSource) {
            it.parameters.value.set("42")
        }
        List<ObtainedValue<?, ValueSourceParameters>> obtainedValues = []
        valueSourceProviderFactory.addListener { obtainedValues.add(it) }

        when: "value is obtained for the 1st time"
        provider.get()

        then: "listener is notified"
        obtainedValues.size() == 1
        obtainedValues[0].valueSourceType == EchoValueSource
        obtainedValues[0].valueSourceParametersType == EchoValueSource.Parameters
        obtainedValues[0].valueSourceParameters.value.get() == "42"
        obtainedValues[0].value.get() == "42"

        when: "value is accessed a 2nd time"
        provider.get()

        then: "no notification is sent"
        obtainedValues.size() == 1
    }

    def "provider maps null returned from obtain to not present"() {

        given:
        def provider = createProviderOf(EchoValueSource) {
            // give no value so `getParameters().getValue().getOrNull()` returns null
        }

        expect:
        !provider.isPresent()
        provider.getOrNull() == null
    }

    def "provider assumes graph is immutable"() {

        given:
        def provider = createProviderOf(EchoValueSource) {}

        expect:
        ((Managed) provider).isImmutable()
    }

    static abstract class EchoValueSource implements ValueSource<String, Parameters> {

        interface Parameters extends ValueSourceParameters {
            Property<String> getValue()
        }

        @Override
        String obtain() {
            return getParameters().getValue().getOrNull()
        }
    }
}
