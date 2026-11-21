package com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.configuration.actions.Action;
import org.bukkit.entity.Player;

public final class SoundAction extends Action<String> {
   public SoundAction(String data) {
      super(data);
   }

   public String transform(String data) {
      return "";
   }

   @Override
   public void run(Player player, HashMapContext context) {
   }
}
