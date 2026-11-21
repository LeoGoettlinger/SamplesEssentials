package com.fastasyncworldedit.core.util.progress;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

public class ChatProgressTracker extends DefaultProgressTracker {
   public ChatProgressTracker(Player player) {
      super(player);
      this.setInterval(this.getDelay() / 50);
   }

   @Override
   public void sendTile(Component title, Component sub) {
      this.getPlayer().print(TextComponent.builder().append(title).append("\n").append(sub).build());
   }
}
