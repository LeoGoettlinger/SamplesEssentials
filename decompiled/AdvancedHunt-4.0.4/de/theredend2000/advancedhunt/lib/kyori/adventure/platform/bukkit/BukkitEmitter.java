package de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit;

import de.theredend2000.advancedhunt.lib.kyori.adventure.sound.Sound;
import org.bukkit.entity.Entity;

final class BukkitEmitter implements Sound.Emitter {
   final Entity entity;

   BukkitEmitter(final Entity entity) {
      this.entity = entity;
   }
}
