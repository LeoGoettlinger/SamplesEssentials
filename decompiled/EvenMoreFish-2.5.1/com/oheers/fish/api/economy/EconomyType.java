package com.oheers.fish.api.economy;

import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EconomyType extends RegistryItem {
   String getIdentifier();

   @NonNull
   @Override
   default String getKey() {
      return this.getIdentifier();
   }

   void load();

   double getMultiplier();

   boolean deposit(@NonNull OfflinePlayer var1, double var2, boolean var4);

   boolean withdraw(@NonNull OfflinePlayer var1, double var2, boolean var4);

   boolean has(@NonNull OfflinePlayer var1, double var2);

   double get(@NonNull OfflinePlayer var1);

   double prepareValue(double var1, boolean var3);

   @Nullable
   Component formatWorth(double var1, boolean var3);

   boolean isAvailable();

   default boolean register() {
      return EMFRegistry.ECONOMY_TYPE.register(this);
   }
}
