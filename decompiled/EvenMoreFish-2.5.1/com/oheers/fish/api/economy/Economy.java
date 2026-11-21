package com.oheers.fish.api.economy;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public class Economy {
   public static Economy instance;
   private final List<EconomyType> relevantTypes;

   private Economy(@NonNull Collection<EconomyType> types) {
      this.relevantTypes = new ArrayList<>(types);
   }

   @NonNull
   public static Economy economy() {
      return new Economy(EMFRegistry.ECONOMY_TYPE.getRegistry().values());
   }

   @NonNull
   public static Economy economy(@NonNull Collection<EconomyType> types) {
      return new Economy(types);
   }

   @NonNull
   public static Economy getInstance() {
      if (instance == null) {
         instance = economy();
      }

      return instance;
   }

   @Deprecated(
      forRemoval = true
   )
   public List<EconomyType> getRegisteredEconomies() {
      return this.getEconomyTypes();
   }

   public List<EconomyType> getEconomyTypes() {
      return List.copyOf(this.relevantTypes);
   }

   public void setEconomyTypes(@NonNull Collection<EconomyType> types) {
      this.relevantTypes.clear();
      this.relevantTypes.addAll(types);
   }

   public boolean isEnabled() {
      if (this.relevantTypes.isEmpty()) {
         Logging.debug("There are no EconomyTypes currently loaded into this Economy instance.");
         return false;
      } else {
         return this.relevantTypes.stream().anyMatch(EconomyType::isAvailable);
      }
   }

   public void deposit(@NonNull OfflinePlayer player, double amount, boolean applyMultiplier) {
      this.relevantTypes.forEach(type -> type.deposit(player, amount, applyMultiplier));
   }

   public void withdraw(@NonNull OfflinePlayer player, double amount, boolean applyMultiplier) {
      this.relevantTypes.forEach(type -> type.withdraw(player, amount, applyMultiplier));
   }

   public boolean has(@NonNull OfflinePlayer player, double amount) {
      return this.relevantTypes.stream().filter(EconomyType::isAvailable).allMatch(type -> type.has(player, amount));
   }

   @NonNull
   public Optional<EconomyType> getEconomyType(@NonNull String identifier) {
      for (EconomyType type : this.relevantTypes) {
         if (type.getIdentifier().equalsIgnoreCase(identifier)) {
            return Optional.of(type);
         }
      }

      return Optional.empty();
   }

   @NonNull
   public Component getWorthFormat(double value, boolean applyMultiplier) {
      List<Component> components = this.getEconomyTypes().stream().map(type -> type.formatWorth(value, applyMultiplier)).filter(Objects::nonNull).toList();
      return Component.join(JoinConfiguration.commas(true), components);
   }

   @Deprecated(
      forRemoval = true
   )
   public boolean registerEconomyType(@NonNull EconomyType economyType) {
      boolean registered = EMFRegistry.ECONOMY_TYPE.register(economyType);
      if (registered) {
         this.relevantTypes.add(economyType);
      }

      return registered;
   }

   public boolean isEmpty() {
      return this.relevantTypes.isEmpty();
   }
}
