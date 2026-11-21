package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.parser;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.argument.BrigadierMappingContributor;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;

@API(
   status = API.Status.INTERNAL
)
public final class ComponentBrigadierContributor implements BrigadierMappingContributor {
   @Override
   public <C, S> void contribute(final CommandManager<C> manager, final CloudBrigadierManager<C, S> brigadierManager) {
      brigadierManager.registerMapping(new TypeToken<ComponentParser<C>>() {}, builder -> builder.cloudSuggestions().to(argument -> {
         switch (argument.stringMode()) {
            case QUOTED:
               return StringArgumentType.string();
            case GREEDY:
            case GREEDY_FLAG_YIELDING:
               return StringArgumentType.greedyString();
            default:
               return StringArgumentType.word();
         }
      }));
   }
}
