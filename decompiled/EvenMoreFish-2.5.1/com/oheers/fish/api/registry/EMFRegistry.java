package com.oheers.fish.api.registry;

import com.oheers.fish.api.addons.ItemAddonRegistry;
import com.oheers.fish.api.boost.RarityBoostRegistry;
import com.oheers.fish.api.economy.EconomyTypeRegistry;
import com.oheers.fish.api.requirement.RequirementTypeRegistry;
import com.oheers.fish.api.reward.RewardTypeRegistry;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EMFRegistry<T extends RegistryItem> {
   @NonNull
   RequirementTypeRegistry REQUIREMENT_TYPE = RequirementTypeRegistry.getInstance();
   @NonNull
   RewardTypeRegistry REWARD_TYPE = RewardTypeRegistry.getInstance();
   @NonNull
   ItemAddonRegistry ITEM_ADDON = ItemAddonRegistry.getInstance();
   @NonNull
   EconomyTypeRegistry ECONOMY_TYPE = EconomyTypeRegistry.getInstance();
   @NonNull
   RarityBoostRegistry RARITY_BOOST = RarityBoostRegistry.getInstance();

   boolean isEmpty();

   void clear();

   @NonNull
   Map<String, T> getRegistry();

   @Nullable
   T get(@NonNull String var1);

   @NonNull
   T getOrDefault(@NonNull String var1, @NonNull T var2);

   boolean unregister(@NonNull String var1);

   default boolean unregister(@NonNull T value) {
      return this.unregister(value.getKey());
   }

   boolean register(@NonNull T var1, boolean var2);

   default boolean register(@NonNull T value) {
      return this.register(value, false);
   }
}
