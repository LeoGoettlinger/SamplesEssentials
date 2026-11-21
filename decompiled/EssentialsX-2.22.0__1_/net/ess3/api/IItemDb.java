package net.ess3.api;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface IItemDb extends com.earth2me.essentials.api.IItemDb {
   void registerResolver(Plugin plugin, String name, IItemDb.ItemResolver resolver) throws Exception;

   void unregisterResolver(Plugin plugin, String name) throws Exception;

   boolean isResolverPresent(Plugin plugin, String name);

   Map<PluginKey, IItemDb.ItemResolver> getResolvers();

   Map<PluginKey, IItemDb.ItemResolver> getResolvers(Plugin plugin);

   IItemDb.ItemResolver getResolver(Plugin plugin, String name);

   ItemStack get(String name, boolean useResolvers) throws Exception;

   @Deprecated
   String serialize(ItemStack itemStack, boolean useResolvers);

   @FunctionalInterface
   public interface ItemResolver extends Function<String, ItemStack> {
      ItemStack apply(String name);

      default Collection<String> getNames() {
         return null;
      }

      default String serialize(final ItemStack stack) {
         return null;
      }
   }
}
