package com.earth2me.essentials.libs.kyori.adventure.platform.bukkit;

import com.earth2me.essentials.libs.kyori.adventure.sound.Sound;
import org.bukkit.entity.Entity;

final class BukkitEmitter implements Sound.Emitter {
   final Entity entity;

   BukkitEmitter(final Entity entity) {
      this.entity = entity;
   }
}
