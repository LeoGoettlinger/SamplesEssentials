package de.oliver.cloud.brigadier.argument;

import de.oliver.cloud.CommandManager;
import de.oliver.cloud.brigadier.CloudBrigadierManager;

public interface BrigadierMappingContributor {
   <C, S> void contribute(CommandManager<C> manager, CloudBrigadierManager<C, S> brigadierManager);
}
