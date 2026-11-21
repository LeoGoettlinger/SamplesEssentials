package com.artillexstudios.axafkzone.libs.axapi.gui;

import com.artillexstudios.axafkzone.libs.axapi.nms.NMSHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SignInput {
   private static final List<SignInput> inputs = new ArrayList<>();
   private final Player player;
   private final Component[] lines;
   private final BiConsumer<Player, String[]> listener;
   private final Location location;

   public SignInput(Player player, Component[] lines, BiConsumer<Player, String[]> listener) {
      this.player = player;
      this.lines = lines;
      this.listener = listener;
      Location location = player.getLocation().clone();
      this.location = location.clone().add(0.0, 4.0, 0.0);
   }

   public void open() {
      NMSHandlers.getNmsHandler().openSignInput(this);
      inputs.add(this);
   }

   public Location getLocation() {
      return this.location;
   }

   public BiConsumer<Player, String[]> getListener() {
      return this.listener;
   }

   public Component[] getLines() {
      return this.lines;
   }

   public Player getPlayer() {
      return this.player;
   }

   public static SignInput remove(Player player) {
      SignInput signInput = null;

      for (SignInput input : inputs) {
         if (input.player.equals(player)) {
            signInput = input;
            break;
         }
      }

      if (signInput != null) {
         inputs.remove(signInput);
      }

      return signInput;
   }

   public static class Builder {
      private Component[] lines = new Component[0];
      private BiConsumer<Player, String[]> response = (a, b) -> {};

      public SignInput.Builder setLines(Component[] lines) {
         this.lines = lines;
         return this;
      }

      public SignInput.Builder setLines(List<Component> lines) {
         this.lines = lines.toArray(new Component[0]);
         return this;
      }

      public SignInput.Builder setHandler(BiConsumer<Player, String[]> response) {
         this.response = response;
         return this;
      }

      public SignInput build(Player player) {
         return new SignInput(player, this.lines, this.response);
      }
   }
}
