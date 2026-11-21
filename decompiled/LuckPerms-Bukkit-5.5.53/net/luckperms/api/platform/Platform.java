package net.luckperms.api.platform;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface Platform {
   @NonNull
   Platform.Type getType();

   @NonNull
   @Unmodifiable
   Set<UUID> getUniqueConnections();

   @NonNull
   @Unmodifiable
   Collection<String> getKnownPermissions();

   @NonNull
   Instant getStartTime();

   public static enum Type {
      BUKKIT("Bukkit"),
      BUNGEECORD("BungeeCord"),
      SPONGE("Sponge"),
      NUKKIT("Nukkit"),
      VELOCITY("Velocity"),
      FABRIC("Fabric"),
      NEOFORGE("NeoForge"),
      FORGE("Forge"),
      STANDALONE("Standalone"),
      HYTALE("Hytale");

      private final String friendlyName;

      private Type(String friendlyName) {
         this.friendlyName = friendlyName;
      }

      @NonNull
      public String getFriendlyName() {
         return this.friendlyName;
      }
   }
}
