package de.theredend2000.advancedhunt.managers.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.menu.minigame.HigherLowerMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.MemoryMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.OddOneOutMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.OrderMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.ReactionMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.SliderPuzzleMinigameMenu;
import de.theredend2000.advancedhunt.menu.minigame.SpotMinigameMenu;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public class MinigameManager {
   private final Main plugin;
   private final HashMap<UUID, MinigameType> activeSessions;

   public MinigameManager(Main plugin) {
      this.plugin = plugin;
      this.activeSessions = new HashMap<>();
   }

   public void startMinigame(Player player, MinigameType type, Consumer<Boolean> onFinish) {
      if (!this.plugin.getConfig().getBoolean("hint.minigames.enabled", true)) {
         onFinish.accept(true);
      } else if (!this.isMinigameEnabled(type)) {
         onFinish.accept(true);
      } else if (!this.activeSessions.containsKey(player.getUniqueId())) {
         this.activeSessions.put(player.getUniqueId(), type);
         switch (type) {
            case REACTION:
               new ReactionMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case MEMORY:
               new MemoryMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case ORDER:
               new OrderMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case ODDONEOUT:
               new OddOneOutMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case SPOT:
               new SpotMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case HIGHER_LOWER:
               new HigherLowerMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
               break;
            case SLIDERPUZZLE:
               new SliderPuzzleMinigameMenu(player, this.plugin, success -> {
                  this.endSession(player);
                  onFinish.accept(success);
               }).open();
         }
      }
   }

   public boolean isMinigameEnabled(MinigameType type) {
      return this.plugin.getConfig().getBoolean("hint.minigames." + type.name().toLowerCase() + ".enabled", true);
   }

   public void endSession(Player player) {
      this.activeSessions.remove(player.getUniqueId());
   }

   public boolean isPlaying(Player player) {
      return this.activeSessions.containsKey(player.getUniqueId());
   }
}
