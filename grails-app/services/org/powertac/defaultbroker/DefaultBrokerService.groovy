package org.powertac.defaultbroker

import org.powertac.common.Broker
import org.powertac.common.PluginConfig

/**
 * Created by IntelliJ IDEA.
 * User: flath
 * Date: 22.04.11
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
class DefaultBrokerService extends Broker
{
    PluginConfig configuration
    def tariffMarketService
    static transactional = true

}
