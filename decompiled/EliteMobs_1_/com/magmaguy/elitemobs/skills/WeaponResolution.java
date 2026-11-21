package com.magmaguy.elitemobs.skills;

import org.bukkit.NamespacedKey;

public sealed interface WeaponResolution permits WeaponResolution.Resolved, WeaponResolution.NotWeapon, WeaponResolution.InvalidExplicit {
   public record InvalidExplicit(String storedValue, NamespacedKey parsedId) implements WeaponResolution {
   }

   public record NotWeapon() implements WeaponResolution {
   }

   public record Resolved(WeaponIdentity identity) implements WeaponResolution {
   }
}
