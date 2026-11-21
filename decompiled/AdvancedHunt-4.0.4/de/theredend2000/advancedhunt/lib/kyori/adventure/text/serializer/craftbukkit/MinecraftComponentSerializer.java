package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.craftbukkit;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class MinecraftComponentSerializer implements ComponentSerializer<Component, Component, Object> {
   private static final MinecraftComponentSerializer INSTANCE = new MinecraftComponentSerializer();
   private final de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.MinecraftComponentSerializer realSerial = de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.MinecraftComponentSerializer.get();

   public static boolean isSupported() {
      return de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit.MinecraftComponentSerializer.isSupported();
   }

   @NotNull
   public static MinecraftComponentSerializer get() {
      return INSTANCE;
   }

   @NotNull
   @Override
   public Component deserialize(@NotNull final Object input) {
      return this.realSerial.deserialize(input);
   }

   @NotNull
   @Override
   public Object serialize(@NotNull final Component component) {
      return this.realSerial.serialize(component);
   }
}
