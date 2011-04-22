import org.powertac.common.PluginConfig

class DefaultBrokerBootStrap
{
  def accountingService
  def tariffMarketService
  
  def init = { servletContext ->
    // create and configure PluginConfig instances for the service
    PluginConfig defaultBroker =
        new PluginConfig(pluginRoleName:'DefaultBroker',
          configuration: [defaultConsumptionRate: '20.0', defaultProductionRate: '10.0'])
    defaultBroker.save()
    defaultBrokerService.configuration = defaultBroker
    }
}
