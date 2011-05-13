package org.powertac.defaultbroker

import org.powertac.common.interfaces.InitializationService
import org.powertac.common.Competition
import org.powertac.common.PluginConfig
import org.powertac.common.Broker

class DefaultBrokerInitializationService implements InitializationService
{

  static transactional = true

  def defaultBrokerService
  def springSecurityService

  void setDefaults() 
  {
    DefaultBroker defaultBroker = new DefaultBroker()
    PluginConfig config = new PluginConfig(roleName:'defaultBroker', name: 'defaultBroker',
        configuration: [consumptionRate: '0.0', productionRate: '0.0'])
    config.save()
    defaultBroker.config = config
    defaultBroker.broker = Broker.findByUsername('defaultBroker') ?: new Broker(
        username: 'defaultBroker', local: true,
        password: springSecurityService.encodePassword('password'),
        enabled: true)
    defaultBroker.broker.save()
    defaultBroker.save()
  }

  String initialize(Competition competition, List<String> completedInits)
  {
    if (!completedInits.find{'TariffMarket' == it}) {
      return null
    }
    defaultBrokerService.init()
    return 'DefaultBroker'
  }

}
