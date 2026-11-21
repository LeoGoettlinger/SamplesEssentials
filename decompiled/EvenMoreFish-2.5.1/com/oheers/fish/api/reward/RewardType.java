package com.oheers.fish.api.reward;

import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class RewardType implements RegistryItem {
   public abstract void doReward(@NonNull Player var1, @NonNull String var2, @NonNull String var3, @Nullable Location var4);

   @NonNull
   public abstract String getIdentifier();

   @NonNull
   @Override
   public String getKey() {
      return this.getIdentifier();
   }

   @NonNull
   public abstract String getAuthor();

   @NonNull
   public abstract Plugin getPlugin();

   public boolean register() {
      return EMFRegistry.REWARD_TYPE.register(this);
   }

   public boolean unregister() {
      return EMFRegistry.REWARD_TYPE.unregister(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RewardType type ? this.getIdentifier().equals(type.getIdentifier()) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.getIdentifier().hashCode();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static Map<String, RewardType> getLoadedTypes() {
      return EMFRegistry.REWARD_TYPE.getRegistry();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static void unregisterAll() {
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   @Nullable
   public static RewardType get(@NonNull String identifier) {
      return EMFRegistry.REWARD_TYPE.get(identifier);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.1.0"
   )
   public static boolean unregister(@NonNull String identifier) {
      return EMFRegistry.REWARD_TYPE.unregister(identifier);
   }
}
