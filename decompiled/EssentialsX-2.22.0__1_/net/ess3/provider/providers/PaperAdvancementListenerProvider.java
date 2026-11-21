package net.ess3.provider.providers;

import io.papermc.paper.advancement.AdvancementDisplay;
import io.papermc.paper.text.PaperComponents;
import net.ess3.provider.AbstractAchievementEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.metadata.MetadataValue;

public class PaperAdvancementListenerProvider implements Listener {
   private final ComponentSerializer<Component, TextComponent, String> serializer;

   public PaperAdvancementListenerProvider() {
      ComponentSerializer<Component, TextComponent, String> yeOldSerializer;
      try {
         yeOldSerializer = PaperComponents.plainTextSerializer();
      } catch (NoSuchMethodError var3) {
         yeOldSerializer = PaperComponents.plainSerializer();
      }

      this.serializer = yeOldSerializer;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAdvancement(final PlayerAdvancementDoneEvent event) {
      AdvancementDisplay display = event.getAdvancement().getDisplay();
      if (display != null && display.doesAnnounceToChat()) {
         if (this.isVanished(event)) {
            event.message(null);
            return;
         }

         Bukkit.getPluginManager().callEvent(new AbstractAchievementEvent(event.getPlayer(), (String)this.serializer.serialize(display.title())));
      }
   }

   private boolean isVanished(final PlayerAdvancementDoneEvent event) {
      for (MetadataValue val : event.getPlayer().getMetadata("vanished")) {
         if (val.asBoolean()) {
            return true;
         }
      }

      return false;
   }
}
