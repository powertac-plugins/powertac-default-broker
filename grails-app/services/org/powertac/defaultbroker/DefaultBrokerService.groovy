/*
 * Copyright (c) 2011 by the original author or authors.
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

/**
 * The default broker entity provides standard legacy tariffs which set
 * the baseline competitiveness. The idea is that customers always need
 * a tariff to fall back to.
 * @author Christoph Flath
 */

package org.powertac.defaultbroker

import org.powertac.common.enumerations.PowerType
import org.joda.time.Duration
import org.joda.time.Instant
import org.powertac.common.TariffSpecification
import org.powertac.common.TimeService
import org.powertac.common.Rate
import org.joda.time.*
import org.powertac.common.interfaces.TariffMarket
import org.powertac.common.Broker
import org.powertac.common.PluginConfig

class DefaultBrokerService {
    static transactional = true

    void init()
    {
        def defaultBrokerList = DefaultBroker.list()
        defaultBrokerList*.publishDefaultTariffs()
    }
}
