package github.nighter.smartspawner.spawner.gui.layout;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.gui.GuiButtonSoundData;
import github.nighter.smartspawner.api.gui.GuiLayoutType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GuiButtonInteractionService implements Listener {
   private static final long NANOS_PER_TICK = 50000000L;
   private static final long ANTI_SPAM_NANOS = 100000000L;
   private final SmartSpawner plugin;
   private final Map<UUID, Long> lastInteractions = new ConcurrentHashMap<>();
   private final Map<UUID, Map<GuiLayoutType, Map<String, Long>>> lastClicks = new ConcurrentHashMap<>();
   private final Set<String> invalidSoundWarnings = ConcurrentHashMap.newKeySet();

   public GuiButtonInteractionService(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public boolean tryUse(Player player, GuiLayoutType layoutType, GuiButton button) {
      long now = System.nanoTime();
      if (this.isAntiSpamCoolingDown(player, now)) {
         return false;
      } else {
         long cooldownTicks = button.getCooldownTicks();
         if (cooldownTicks > 0L) {
            long cooldownNanos = cooldownTicks > 184467440737L ? Long.MAX_VALUE : cooldownTicks * 50000000L;
            Long previous = this.getLastButtonClick(player, layoutType, button);
            if (previous != null && now - previous < cooldownNanos) {
               long remainingNanos = cooldownNanos - (now - previous);
               this.plugin.getMessageService().sendMessage(player, "action_not_ready", Map.of("time", this.formatRemainingTime(remainingNanos)));
               return false;
            }

            Map<GuiLayoutType, Map<String, Long>> playerClicks = this.lastClicks.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>());
            Map<String, Long> layoutClicks = playerClicks.computeIfAbsent(layoutType, ignored -> new ConcurrentHashMap<>());
            layoutClicks.put(button.getButtonType(), now);
         }

         this.lastInteractions.put(player.getUniqueId(), now);
         return true;
      }
   }

   public void playNavigateSound(Player player, GuiButton button) {
      this.playNavigateSound(player, button, "click");
   }

   public void playNavigateSound(Player player, GuiButton button, String clickType) {
      this.playSounds(player, button.getClickSounds(clickType));
   }

   public void playSuccessSound(Player player, GuiButton button) {
      this.playSuccessSound(player, button, "click");
   }

   public void playSuccessSound(Player player, GuiButton button, String clickType) {
      this.playSounds(player, button.getSuccessSounds(clickType));
   }

   public void playFailSound(Player player, GuiButton button) {
      this.playFailSound(player, button, "click");
   }

   public void playFailSound(Player player, GuiButton button, String clickType) {
      this.playSounds(player, button.getFailSounds(clickType));
   }

   public boolean tryUseAntiSpam(Player player) {
      long now = System.nanoTime();
      if (this.isAntiSpamCoolingDown(player, now)) {
         return false;
      } else {
         this.lastInteractions.put(player.getUniqueId(), now);
         return true;
      }
   }

   public void clear() {
      this.lastInteractions.clear();
      this.lastClicks.clear();
      this.invalidSoundWarnings.clear();
   }

   public void playOpenSound(Player player) {
      String openSound = this.plugin.getGuiLayoutConfig().getOpenSound();
      if (openSound != null) {
         this.playSound(player, new GuiButtonSoundData(openSound, 1.0F, 1.0F));
      }
   }

   private String formatRemainingTime(long remainingNanos) {
      long tenths = Math.max(1L, (remainingNanos + 99999999L) / 100000000L);
      return tenths % 10L == 0L ? tenths / 10L + "s" : tenths / 10L + "." + tenths % 10L + "s";
   }

   private boolean isAntiSpamCoolingDown(Player player, long now) {
      Long previous = this.lastInteractions.get(player.getUniqueId());
      return previous != null && now - previous < 100000000L;
   }

   private Long getLastButtonClick(Player player, GuiLayoutType layoutType, GuiButton button) {
      Map<GuiLayoutType, Map<String, Long>> playerClicks = this.lastClicks.get(player.getUniqueId());
      if (playerClicks == null) {
         return null;
      } else {
         Map<String, Long> layoutClicks = playerClicks.get(layoutType);
         return layoutClicks != null ? layoutClicks.get(button.getButtonType()) : null;
      }
   }

   private void playSounds(Player player, List<GuiButtonSoundData> sounds) {
      for (GuiButtonSoundData sound : sounds) {
         this.playSound(player, sound);
      }
   }

   private void playSound(Player player, GuiButtonSoundData sound) {
      try {
         player.playSound(player.getLocation(), sound.getName(), sound.getVolume(), sound.getPitch());
      } catch (Exception var4) {
         if (this.invalidSoundWarnings.add(sound.getName())) {
            this.plugin.getLogger().warning("Invalid GUI button sound '" + sound.getName() + "': " + var4.getMessage());
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.lastInteractions.remove(playerId);
      this.lastClicks.remove(playerId);
   }
}
