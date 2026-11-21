package de.theredend2000.advancedhunt.lib.cloud.brigadier.argument;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierManager;

public interface BrigadierMappingContributor {
   <C, S> void contribute(CommandManager<C> manager, CloudBrigadierManager<C, S> brigadierManager);
}
