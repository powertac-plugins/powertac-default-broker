/*
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.powertac

import org.powertac.common.Broker
import org.powertac.common.Rate
import org.powertac.common.TariffSpecification
import org.powertac.common.enumerations.PowerType
import org.powertac.common.msg.SimStart
import org.powertac.common.interfaces.TariffMarket
import org.powertac.common.PluginConfig
/**
 * The default broker represents the legacy power provider. It provides
 * basic tariffs for consumption and production as specified in the
 * competition configuration
 * @author Christoph Flath, KIT
 */
class DefaultBroker extends Broker {
  PluginConfig configuration
  // JEC - this class should be a Service, not a Domain type. I have added
  // transients notation to temporarily bypass the problem...

  TariffMarket tariffMarketService
  
  static transients = ['tariffMarketService']

  // JEC -- this is not the correct way to initialize either a service
  // or a domain type.
  DefaultBroker() {
    this.local = true
  }

  def publishDefaultTariffs(TariffMarket tms) {

    /* Default Consumption Tariff */
    TariffSpecification defaultConsumptionTariffSpecification = new TariffSpecification
    (broker: this, powerType: PowerType.CONSUMPTION)
    Rate defaultConsumptionRate = new Rate(value: getConsumptionRate())
    defaultConsumptionTariffSpecification.addToRates(defaultConsumptionRate)
    tms.processTariff(defaultConsumptionTariffSpecification)

    /* Default Production Tariff */
    TariffSpecification defaultProductionTariffSpecification = new TariffSpecification
    (broker: this, powerType: PowerType.PRODUCTION)
    Rate defaultProductionRate = new Rate(value: getProductionRate())
    defaultProductionTariffSpecification.addToRates(defaultProductionRate)
    tms.processTariff(defaultProductionTariffSpecification)
  }

  @Override
  void receiveMessage(Object object) 
  {
    // Publish tariffs on simulation start
    if (object instanceof SimStart) {
      publishDefaultTariffs()
    }
    }
  // set default tariffs according to configuration entries
  private Number getProductionRate()
  {
    BigDecimal rate = 0.0
    if (configuration == null) {
      log.error("cannot find configuration")
    }
    else {
      rate = configuration.configuration['defaultProductionRate'].toBigDecimal()
    }
    return rate
  }
     private Number getConsumptionRate()
  {
    BigDecimal rate = 0.0
    if (configuration == null) {
      log.error("cannot find configuration")
    }
    else {
      rate = configuration.configuration['defaultConsumptionRate'].toBigDecimal()
    }
    return rate
  }
  }