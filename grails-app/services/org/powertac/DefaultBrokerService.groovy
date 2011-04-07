package org.powertac

import org.powertac.common.Broker
import org.powertac.common.Rate
import org.powertac.common.TariffSpecification
import org.powertac.common.enumerations.PowerType

class DefaultBrokerService extends Broker {

  static transactional = true

  double consumptionRate
  double productionRate

  def DefaultBrokerService(double cr, double pr, String id, String apiKey) {
    consumptionRate = cr
    productionRate = pr
    this.local = true
    this.id = id
    this.apiKey = apiKey
  }

  def publishDefaultTariffs() {
    static transactional = true

    /* Default Consumption Tariff */
    TariffSpecification defaultConsumptionTariffSpecification = new TariffSpecification
    (brokerId: id, powerType: PowerType.CONSUMPTION)
    Rate defaultConsumptionRate = new Rate(value: consumptionRate)
    defaultConsumptionTariffSpecification.addToRates(defaultConsumptionRate)

    /* Default Production Tariff */
    TariffSpecification defaultProductionTariffSpecification = new TariffSpecification
    (brokerId: id, powerType: PowerType.PRODUCTION)
    Rate defaultProductionRate = new Rate(value: productionRate)
    defaultProductionTariffSpecification.addToRates(defaultProductionRate)

  }
}
