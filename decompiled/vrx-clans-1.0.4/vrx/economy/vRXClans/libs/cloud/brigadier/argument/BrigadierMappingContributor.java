package vrx.economy.vRXClans.libs.cloud.brigadier.argument;

import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.brigadier.CloudBrigadierManager;

public interface BrigadierMappingContributor {
   <C, S> void contribute(CommandManager<C> manager, CloudBrigadierManager<C, S> brigadierManager);
}
