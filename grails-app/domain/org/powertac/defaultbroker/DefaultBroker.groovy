package org.powertac.defaultbroker

import org.powertac.common.Broker
import org.powertac.common.PluginConfig
import org.powertac.common.TariffSpecification
import org.powertac.common.Rate
import org.powertac.common.enumerations.PowerType
import org.powertac.common.Tariff
/**
 * Created by IntelliJ IDEA.
 * User: flath
 * Date: 26.04.11
 * Time: 08:32
 * To change this template use File | Settings | File Templates.
 */
class DefaultBroker
{
    def tariffMarketService
    /** Public config data */
    PluginConfig config
    /** Internal Broker for market interaction */
    Broker broker
    static constraints = {
        config(nullable: false)
        broker(nullable: false)
    }

    /**Publishing of Default Tariffs for Consumption and Generation*/
    def publishDefaultTariffs()
    {
        /* Default Consumption Tariff */
        TariffSpecification defaultConsumptionTariffSpecification = new TariffSpecification
        (broker: broker, powerType: PowerType.CONSUMPTION)
        Rate defaultConsumptionRate = new Rate(value: getDefaultConsumptionRate())
        defaultConsumptionTariffSpecification.addToRates(defaultConsumptionRate)
        defaultConsumptionTariffSpecification.save()


        /* Default Production Tariff */
        TariffSpecification defaultProductionTariffSpecification = new TariffSpecification
        (broker: broker, powerType: PowerType.PRODUCTION)
        Rate defaultProductionRate = new Rate(value: getProductionRate())
        defaultProductionTariffSpecification.addToRates(defaultProductionRate)
        defaultProductionTariffSpecification.save()

        def defaultConsumptionTariff = new Tariff(tariffSpec: defaultConsumptionTariffSpecification, broker: broker)
        defaultConsumptionTariff.init()

        def defaultProductionTariff = new Tariff(tariffSpec: defaultProductionTariffSpecification, broker: broker)
        defaultProductionTariff.init()

        broker.addToTariffs(defaultConsumptionTariff)
        broker.addToTariffs(defaultProductionTariff)
        tariffMarketService.processTariff(defaultConsumptionTariffSpecification)
        tariffMarketService.processTariff(defaultProductionTariffSpecification)
    }
    private Number getDefaultConsumptionRate()
    {
        BigDecimal rate = 0.0
        if (config == null) {
            log.error("cannot find configuration")
        }
        else {
            rate = config.configuration['consumptionRate'].toBigDecimal()
        }
        return rate
    }
    private Number getProductionRate()
    {
        BigDecimal rate = 0.0
        if (config == null) {
            log.error("cannot find configuration")
        }
        else {
            rate = config.configuration['productionRate'].toBigDecimal()
        }
        return rate
    }
}
