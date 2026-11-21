package de.theredend2000.advancedhunt.lib.cloud.bukkit;

import de.theredend2000.advancedhunt.lib.cloud.CloudCapability;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.CraftBukkitReflection;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum CloudBukkitCapabilities implements CloudCapability {
   BRIGADIER(
      CraftBukkitReflection.classExists("com.mojang.brigadier.tree.CommandNode")
         && (
            CraftBukkitReflection.findOBCClass("command.BukkitCommandWrapper") != null
               || CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.CommandSourceStack")
         )
   ),
   NATIVE_BRIGADIER(
      CraftBukkitReflection.classExists("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")
         || CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.CommandSourceStack")
   ),
   COMMODORE_BRIGADIER(BRIGADIER.capable() && !NATIVE_BRIGADIER.capable() && !CraftBukkitReflection.classExists("org.bukkit.entity.Warden")),
   ASYNCHRONOUS_COMPLETION(CraftBukkitReflection.classExists("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent"));

   @API(
      status = API.Status.INTERNAL
   )
   public static final Set<CloudBukkitCapabilities> CAPABLE = Arrays.stream(values()).filter(CloudBukkitCapabilities::capable).collect(Collectors.toSet());
   private final boolean capable;

   private CloudBukkitCapabilities(final boolean capable) {
      this.capable = capable;
   }

   boolean capable() {
      return this.capable;
   }

   @NonNull
   @Override
   public String toString() {
      return this.name();
   }
}
