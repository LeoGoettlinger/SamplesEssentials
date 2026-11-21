package net.ess3.provider.providers;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import net.ess3.provider.AbstractChatEvent;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public abstract class PaperChatListenerProvider implements Listener {
   private final boolean formatParsing;
   private final LegacyComponentSerializer serializer;
   private final Map<AsyncChatEvent, PaperChatEvent> eventMap = new IdentityHashMap<>();

   public PaperChatListenerProvider() {
      this(true);
   }

   public PaperChatListenerProvider(final boolean formatParsing) {
      this.formatParsing = formatParsing;
      this.serializer = LegacyComponentSerializer.builder()
         .flattener(ComponentFlattener.basic())
         .extractUrls(AbstractChatEvent.URL_PATTERN)
         .useUnusualXRepeatedCharacterHexFormat()
         .hexColors()
         .build();
   }

   public void onChatLowest(final AbstractChatEvent event) {
   }

   public void onChatNormal(final AbstractChatEvent event) {
   }

   public void onChatHighest(final AbstractChatEvent event) {
   }

   public void onChatMonitor(final AbstractChatEvent event) {
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public final void onLowest(final AsyncChatEvent event) {
      this.onChatLowest(this.wrap(event));
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public final void onNormal(final AsyncChatEvent event) {
      this.onChatNormal(this.wrap(event));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void onHighest(final AsyncChatEvent event) {
      PaperChatEvent paperChatEvent = this.wrap(event);
      this.onChatHighest(paperChatEvent);
      if (!event.isCancelled()) {
         if (this.formatParsing) {
            TextComponent format = this.serializer.deserialize(paperChatEvent.getFormat());
            TextComponent eventMessage = this.serializer.deserialize(paperChatEvent.getMessage());
            event.renderer(
               ChatRenderer.viewerUnaware(
                  (player, displayName, message) -> format.replaceText(
                     builder -> builder.match("%(\\d)\\$s")
                        .replacement((index, match) -> (ComponentLike)(index.group(1).equals("1") ? displayName : eventMessage))
                  )
               )
            );
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onMonitor(final AsyncChatEvent event) {
      this.onChatMonitor(this.wrap(event));
      this.eventMap.remove(event);
   }

   private PaperChatEvent wrap(final AsyncChatEvent event) {
      PaperChatEvent paperChatEvent = this.eventMap.get(event);
      if (paperChatEvent != null) {
         return paperChatEvent;
      } else {
         paperChatEvent = new PaperChatEvent(event, this.serializer);
         this.eventMap.put(event, paperChatEvent);
         return paperChatEvent;
      }
   }
}
