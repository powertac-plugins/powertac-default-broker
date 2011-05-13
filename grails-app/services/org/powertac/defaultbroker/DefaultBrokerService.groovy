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
import org.powertac.common.Timeslot
import org.powertac.common.interfaces.TimeslotPhaseProcessor
import org.powertac.common.TariffTransaction

class DefaultBrokerService implements TimeslotPhaseProcessor {
  static transactional = true

  def timeService // autowire
  def randomSeedService // autowire
  def competitionControlService
  def auctionService
  def tariffMarketService

  Random randomGen = null

  void init() {
    def defaultBrokerList = DefaultBroker.list()
    defaultBrokerList*.publishDefaultTariffs()
    log.info "Publishing default tariffs"
    competitionControlService.registerTimeslotPhase(this, 1)
  }
      void activate(Instant now, int phase)
  {
    log.info "Activate"
    Random gen = ensureRandomSeed()
    List<DefaultBroker> DefaultBrokerList = DefaultBroker.list()
    List<Timeslot> openSlots = Timeslot.enabledTimeslots()
    DefaultBrokerList*.generateShouts(gen, now, openSlots, auctionService)
  }

    private Random ensureRandomSeed ()
  {
    if (randomGen == null) {
      long randomSeed = randomSeedService.nextSeed('DefaultBroker','broker','model')
      randomGen = new Random(randomSeed)
    }
    return randomGen
  }
}
