package com.lishid.openinv.util.setting;

import com.lishid.openinv.util.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class PlayerToggles {
   private static final Map<String, PlayerToggle> TOGGLES = new HashMap<>();
   private static final PlayerToggle ANY = add(new PlayerToggles.MemoryToggle("AnyContainer") {
      @Override
      public boolean is(@NotNull Player player) {
         return this.is(player.getUniqueId()) && (Permissions.CONTAINER_ANY.hasPermission(player) || Permissions.CONTAINER_ANY_USE.hasPermission(player));
      }
   });
   private static final PlayerToggle SILENT = add(new PlayerToggles.MemoryToggle("SilentContainer") {
      @Override
      public boolean is(@NotNull Player player) {
         return this.is(player.getUniqueId()) && (Permissions.CONTAINER_SILENT.hasPermission(player) || Permissions.CONTAINER_SILENT_USE.hasPermission(player));
      }
   });

   @NotNull
   public static PlayerToggle any() {
      return ANY;
   }

   @NotNull
   public static PlayerToggle silent() {
      return SILENT;
   }

   @Nullable
   public static PlayerToggle get(@NotNull String toggleName) {
      PlayerToggle toggle = TOGGLES.get(toggleName);
      if (toggle == null) {
         toggle = TOGGLES.get(toggleName.toLowerCase(Locale.ENGLISH));
      }

      return toggle;
   }

   @NotNull
   @UnmodifiableView
   public static Collection<PlayerToggle> get() {
      return Collections.unmodifiableCollection(TOGGLES.values());
   }

   @NotNull
   private static PlayerToggle add(@NotNull PlayerToggle toggle) {
      TOGGLES.put(toggle.getName().toLowerCase(Locale.ENGLISH), toggle);
      return toggle;
   }

   private PlayerToggles() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }

   private abstract static class MemoryToggle implements PlayerToggle {
      @NotNull
      private final Set<UUID> enabled = new HashSet<>();
      @NotNull
      private final String name;

      private MemoryToggle(@NotNull String name) {
         this.name = name;
      }

      @NotNull
      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public boolean is(@NotNull UUID uuid) {
         return this.enabled.contains(uuid);
      }

      @Override
      public boolean set(@NotNull UUID uuid, boolean enabled) {
         return enabled ? this.enabled.add(uuid) : this.enabled.remove(uuid);
      }
   }
}
