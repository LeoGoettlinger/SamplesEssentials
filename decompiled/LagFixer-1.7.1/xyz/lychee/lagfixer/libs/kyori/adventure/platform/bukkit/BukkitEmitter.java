package xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit;

import org.bukkit.entity.Entity;
import xyz.lychee.lagfixer.libs.kyori.adventure.sound.Sound;

final class BukkitEmitter implements Sound.Emitter {
   final Entity entity;

   BukkitEmitter(final Entity entity) {
      this.entity = entity;
   }
}
