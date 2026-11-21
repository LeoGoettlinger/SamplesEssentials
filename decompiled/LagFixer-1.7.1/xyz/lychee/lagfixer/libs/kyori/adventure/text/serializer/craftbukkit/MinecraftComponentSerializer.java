package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.craftbukkit;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.ComponentSerializer;

@Deprecated
public final class MinecraftComponentSerializer implements ComponentSerializer<Component, Component, Object> {
   private static final MinecraftComponentSerializer INSTANCE = new MinecraftComponentSerializer();
   private final xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.MinecraftComponentSerializer realSerial = xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.MinecraftComponentSerializer.get();

   public static boolean isSupported() {
      return xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.MinecraftComponentSerializer.isSupported();
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
