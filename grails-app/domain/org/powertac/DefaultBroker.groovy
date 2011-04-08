package org.powertac

import org.powertac.common.Broker
import org.powertac.common.Rate
import org.powertac.common.TariffSpecification
import org.powertac.common.enumerations.PowerType
import org.powertac.common.msg.SimStart

class DefaultBroker extends Broker {

  double consumptionRate
  double productionRate

  DefaultBroker() {
    this.local = true

    consumptionRate = 1.0
    productionRate = 1.0
  }

  def publishDefaultTariffs() {

    /* Default Consumption Tariff */
    TariffSpecification defaultConsumptionTariffSpecification = new TariffSpecification
    (broker: this, powerType: PowerType.CONSUMPTION)
    Rate defaultConsumptionRate = new Rate(value: consumptionRate)
    defaultConsumptionTariffSpecification.addToRates(defaultConsumptionRate)

    /* Default Production Tariff */
    TariffSpecification defaultProductionTariffSpecification = new TariffSpecification
    (broker: this, powerType: PowerType.PRODUCTION)
    Rate defaultProductionRate = new Rate(value: productionRate)
    defaultProductionTariffSpecification.addToRates(defaultProductionRate)

  }

  @Override
  void receiveMessage(Object object) {
    // Publish tariffs on simulation start
    if (object instanceof SimStart) {
      publishDefaultTariffs()
    }
  }


}
