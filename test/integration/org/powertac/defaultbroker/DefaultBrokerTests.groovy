package org.powertac.defaultbroker

import grails.test.GrailsUnitTestCase
import org.powertac.defaultbroker.DefaultBroker
import org.powertac.common.PluginConfig
import org.powertac.common.Broker

/**
 * Created by IntelliJ IDEA.
 * User: flath
 * Date: 26.04.11
 * Time: 10:57
 * To change this template use File | Settings | File Templates.
 */
class DefaultBrokerTests extends GrailsUnitTestCase{
    def defaultBrokerService
    DefaultBroker defaultBroker


    protected void setUp()
    {
        super.setUp()
        DefaultBroker defaultBroker = new DefaultBroker()
        PluginConfig config = new PluginConfig(roleName:'defaultBroker', name: 'defaultBroker',
                configuration: [consumptionRate: '20.0', productionRate: '10.0'])
        defaultBroker.broker = new Broker(username: 'defaultBroker', local: true)
        if (!defaultBroker.validate()) {
            defaultBroker.errors.allErrors.each { println it.toString() }
        }
    }
    protected void tearDown()
    {
        super.tearDown()
    }
    void testDefaultBroker()
    {
        assertNotNull("created DefaultBroker", defaultBroker)
    }

    void testDefaultBrokerPublishRates()
    {
        defaultBroker.publishDefaultTariffs()
        assertTrue(20.0,defaultBroker.broker.tariffs.asList().get(0).tariffSpec.rates.get(0))
        assertTrue(10.0,defaultBroker.broker.tariffs.asList().get(1).tariffSpec.rates.get(1))
    }

}
