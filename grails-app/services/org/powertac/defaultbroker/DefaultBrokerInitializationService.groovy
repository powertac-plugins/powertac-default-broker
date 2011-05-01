package org.powertac.defaultbroker

import org.powertac.common.interfaces.InitializationService
import org.powertac.common.Competition
import org.powertac.common.PluginConfig
import org.powertac.common.Broker

class DefaultBrokerInitializationService implements InitializationService {

    static transactional = true

    def defaultBrokerService

    void setDefaults() {
        DefaultBroker defaultBroker = new DefaultBroker()
        PluginConfig config = new PluginConfig(roleName:'defaultBroker', name: 'defaultBroker',
                configuration: [consumptionRate: '20.0', productionRate: '10.0'])
        config.save()
        defaultBroker.config = config
        defaultBroker.broker = new Broker(username: 'defaultBroker', local: true)
        defaultBroker.broker.save()
        defaultBroker.save()
        defaultBrokerService.init()
    }

    String initialize(Competition competition, List<String> completedInits) {
        return 'org.powertac.defaultbroker.DefaultBroker'
    }
}
