package org.powertac.defaultbroker

import org.powertac.common.Broker
import org.powertac.common.PluginConfig
import org.powertac.common.TariffSpecification
import org.powertac.common.Rate
import org.powertac.common.enumerations.PowerType
import org.powertac.common.Tariff
import org.joda.time.Instant
import org.powertac.common.Timeslot
import org.powertac.common.MarketPosition
import org.powertac.common.Shout
import org.powertac.common.enumerations.BuySellIndicator
import org.powertac.common.enumerations.ProductType
import org.powertac.common.TariffTransaction
import org.powertac.common.HourlyCharge
import org.powertac.common.msg.VariableRateUpdate
/**
 * Created by IntelliJ IDEA.
 * User: flath
 * Date: 26.04.11
 * Time: 08:32
 * To change this template use File | Settings | File Templates.
 */
class DefaultBroker {
    def tariffMarketService
    BigDecimal movingAverageConsumption
    BigDecimal[] referenceAverages
    int movingAverageNumberOfTimeslots
    BigDecimal runningSum
    /** Public config data  */
    PluginConfig config
    /** Internal Broker for market interaction  */
    Broker broker
    static constraints = {
        config(nullable: false)
        broker(nullable: false)
    }

    /** Publishing of Default Tariffs for Consumption and Generation */
    def publishDefaultTariffs() {
        /* Default Consumption Tariff */
        TariffSpecification defaultConsumptionTariffSpecification = new TariffSpecification (broker: broker, powerType: PowerType.CONSUMPTION)
        Rate defaultConsumptionRate = new Rate(isFixed: false, minValue: 0.05, maxValue: 0.5, noticeInterval: 0, expectedMean: 0.1)
        defaultConsumptionRate.save()
        defaultConsumptionTariffSpecification.addToRates(defaultConsumptionRate)
        log.error(defaultConsumptionTariffSpecification.save())

        /* Default Production Tariff */
        //TariffSpecification defaultProductionTariffSpecification =
        //    new TariffSpecification (broker: broker, powerType: PowerType.PRODUCTION)
        //Rate defaultProductionRate = new Rate(value: getProductionRate())
        //defaultProductionRate.save()
        //defaultProductionTariffSpecification.addToRates(defaultProductionRate)
        //defaultProductionTariffSpecification.save()

        tariffMarketService.setDefaultTariff(defaultConsumptionTariffSpecification)
        //tariffMarketService.setDefaultTariff(defaultProductionTariffSpecification)
    }

    private Number getDefaultConsumptionRate() {
        BigDecimal rate = 0.0
        if (config == null) {
            log.error("cannot find configuration")
        }
        else {
            rate = config.configuration['consumptionRate'].toBigDecimal()
        }
        return rate
    }

    private Number getProductionRate() {
        BigDecimal rate = 0.0
        if (config == null) {
            log.error("cannot find configuration")
        }
        else {
            rate = config.configuration['productionRate'].toBigDecimal()
        }
        return rate
    }
    /**
     * Generates Shouts in the market to buy available required power
     */
    void generateShouts(Random gen, Instant now, List<Timeslot> openSlots, auctionService) {
        double requiredAmount = 0.0
        double referenceAmount = 0.0
        def defaultConsumptionTariffId
        def defaultConsumptionRateId
        def vru
        def updateValue
        def previousConsumptionList
        updateValue = 0.05
        if(Timeslot.currentTimeslot().serialNumber>1)
        {
            movingAverageNumberOfTimeslots = Timeslot.currentTimeslot().serialNumber
            previousConsumptionList = TariffTransaction.findAllByBrokerAndPostedTime(broker, Timeslot.findBySerialNumber(Timeslot.currentTimeslot().serialNumber-1).startInstant)
            previousConsumptionList?.each{transaction ->
                runningSum += transaction.quantity/1000.0
            }
        }
        movingAverageConsumption = runningSum / movingAverageNumberOfTimeslots
        log.error("moving average over ${movingAverageNumberOfTimeslots} timeslots is: ${movingAverageConsumption}")
        if(Timeslot.currentTimeslot().serialNumber>24)
        {
            previousConsumptionList = TariffTransaction.findAllByBrokerAndPostedTime(broker, Timeslot.findBySerialNumber(Timeslot.currentTimeslot().serialNumber-24).startInstant)
            previousConsumptionList?.each{transaction ->
                referenceAmount += transaction.quantity/1000.0
            }
            if(referenceAverages[(Timeslot.currentTimeslot().serialNumber)%24]!= null){
            referenceAmount = (referenceAverages[(Timeslot.currentTimeslot().serialNumber)%24] + referenceAmount)/2
            }
            referenceAverages[(Timeslot.currentTimeslot().serialNumber)%24]=referenceAmount
        }


        openSlots?.each { slot ->
            if(slot.serialNumber <= 23) {
                if(slot.serialNumber <= 1) {

                    // cannot buy power because there's no usage record
                }
                else {
                    List<TariffTransaction> transactionList = TariffTransaction.findAllByBrokerAndPostedTime(broker, Timeslot.findBySerialNumber(slot.serialNumber-2).startInstant)
                    transactionList?.each{ transaction ->
                        requiredAmount += transaction.quantity/1000.0
                    }
                }
            }
            else {
                List<TariffTransaction> transactionList = TariffTransaction.findAllByBrokerAndPostedTime(broker, Timeslot.findBySerialNumber(slot.serialNumber-24).startInstant)
                transactionList?.each { transaction ->
                    requiredAmount += transaction.quantity/1000.0
                }
            }
            if (slot == openSlots.first())
            {
                movingAverageNumberOfTimeslots++
                runningSum += referenceAmount
                movingAverageConsumption = runningSum / new BigDecimal(movingAverageNumberOfTimeslots)
                if (movingAverageConsumption>0)
                {
                    updateValue += (referenceAmount / movingAverageConsumption)*0.1
                }
            }
            MarketPosition position = MarketPosition.findByBrokerAndTimeslot(broker, slot)
            if (position != null) {
                // position.overallBalance is positive if we have bought power in this slot
                requiredAmount -= position.overallBalance
            }
            if (requiredAmount > 0.0) {
                // make an offer to buy
                Shout offer =
                new Shout(broker: broker, timeslot: slot,
                        product: ProductType.Future,
                        buySellIndicator: BuySellIndicator.BUY,
                        quantity: requiredAmount,
                        limitPrice: 40.0)
                offer.save()
                auctionService?.processShout(offer)
            }
        }
        if (referenceAmount>0)
        {
            updateValue = referenceAmount / movingAverageConsumption
            if (updateValue>1)
            {
                updateValue = 0.35
            }
            else
            {
                updateValue = 0.1 + updateValue/10
            }
        }
        else
        {
            updateValue = 0.1
        }
        defaultConsumptionTariffId = tariffMarketService.getDefaultTariff(PowerType.CONSUMPTION).tariffSpec.id
        defaultConsumptionRateId = tariffMarketService.getDefaultTariff(PowerType.CONSUMPTION).tariffSpec.rates[0].id
        HourlyCharge hc = new HourlyCharge(value: updateValue, atTime: now)
        vru = new VariableRateUpdate(payload: hc, broker: broker, tariffId: defaultConsumptionTariffId, rateId: defaultConsumptionRateId)
        tariffMarketService.processTariff(vru)
        log.error("changed rate to ${updateValue}")
    }
}
