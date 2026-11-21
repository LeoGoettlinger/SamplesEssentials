package com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import org.bukkit.entity.Player;

public abstract class Action<T> {
   private final T value;

   public Action(String data) {
      this.value = this.transform(data);
      if (this.value == null) {
         throw new IllegalArgumentException();
      }
   }

   public abstract T transform(String data);

   public abstract void run(Player player, HashMapContext context);

   public T value() {
      return this.value;
   }
}
