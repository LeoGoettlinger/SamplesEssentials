package net.thenextlvl.protect.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.flag.AreaFlagChangeEvent;
import net.thenextlvl.protect.area.event.flag.AreaFlagResetEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaEnterEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaLeaveEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaTransitionEvent;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class AreaListener implements Listener {
   private final ProtectPlugin plugin;
   private static final MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.resolver(new TagResolver[]{TagResolver.standard()})).build();

   public AreaListener(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerAreaEnter(PlayerAreaEnterEvent event) {
      this.plugin.failed(event.getPlayer(), event, event.getArea(), "area.failed.enter");
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerAreaLeave(PlayerAreaLeaveEvent event) {
      this.plugin.failed(event.getPlayer(), event, event.getArea(), "area.failed.leave");
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void greetings(PlayerAreaEnterEvent event) {
      String message = event.getArea().getFlag(this.plugin.flags.greetings);
      if (message != null) {
         event.getPlayer().sendMessage(this.deserialize(message, event));
      }

      String actionbar = event.getArea().getFlag(this.plugin.flags.greetingsActionbar);
      if (actionbar != null) {
         event.getPlayer().sendActionBar(this.deserialize(actionbar, event));
      }

      Title title = this.parseTitle(event.getArea().getFlag(this.plugin.flags.greetingsTitle), event);
      if (title != null) {
         event.getPlayer().showTitle(title);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void farewell(PlayerAreaLeaveEvent event) {
      String message = event.getArea().getFlag(this.plugin.flags.farewell);
      if (message != null) {
         event.getPlayer().sendMessage(this.deserialize(message, event));
      }

      String actionbar = event.getArea().getFlag(this.plugin.flags.farewellActionbar);
      if (actionbar != null) {
         event.getPlayer().sendActionBar(this.deserialize(actionbar, event));
      }

      Title title = this.parseTitle(event.getArea().getFlag(this.plugin.flags.farewellTitle), event);
      if (title != null) {
         event.getPlayer().showTitle(title);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerAreaTransition(PlayerAreaTransitionEvent event) {
      Boolean collides = event.getArea().getFlag(this.plugin.flags.collisions);
      this.plugin.collisionController().setCollidable(event.getPlayer(), collides);
      WeatherType weather = event.getArea().getFlag(this.plugin.flags.weather);
      if (weather != null) {
         event.getPlayer().setPlayerWeather(weather);
      } else if (event.getPrevious().hasFlag(this.plugin.flags.weather)) {
         event.getPlayer().resetPlayerWeather();
      }

      Long time = event.getArea().getFlag(this.plugin.flags.time);
      if (time != null) {
         event.getPlayer().setPlayerTime(time, false);
      } else if (event.getPrevious().hasFlag(this.plugin.flags.time)) {
         event.getPlayer().resetPlayerTime();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAreaFlagChange(AreaFlagChangeEvent<?> event) {
      if (event.getFlag().equals(this.plugin.flags.weather)) {
         WeatherType weather = (WeatherType)event.getNewState();
         event.getArea().getHighestPlayers().forEach(player -> {
            if (weather != null) {
               player.setPlayerWeather(weather);
            } else {
               player.resetPlayerWeather();
            }
         });
      } else if (event.getFlag().equals(this.plugin.flags.time)) {
         Long time = (Long)event.getNewState();
         event.getArea().getHighestPlayers().forEach(player -> {
            if (time != null) {
               player.setPlayerTime(time, false);
            } else {
               player.resetPlayerTime();
            }
         });
      } else if (event.getFlag().equals(this.plugin.flags.collisions)) {
         boolean collides = (Boolean)event.getNewState();
         event.getArea().getHighestPlayers().forEach(player -> this.plugin.collisionController().setCollidable(player, collides));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAreaFlagReset(AreaFlagResetEvent<?> event) {
      if (event.getFlag().equals(this.plugin.flags.weather)) {
         event.getArea().getHighestPlayers().forEach(Player::resetPlayerWeather);
      } else if (event.getFlag().equals(this.plugin.flags.time)) {
         event.getArea().getHighestPlayers().forEach(Player::resetPlayerTime);
      }
   }

   private Component deserialize(String text, PlayerAreaEvent event) {
      return miniMessage.deserialize(
         text, new TagResolver[]{Placeholder.component("player", event.getPlayer().name()), Placeholder.parsed("area", event.getArea().getName())}
      );
   }

   @Nullable
   private Title parseTitle(@Nullable String text, PlayerAreaEvent event) {
      if (text == null) {
         return null;
      } else {
         String[] split = text.split("\\\\n|<newline>|<br>", 2);
         Component title = this.deserialize(split[0], event);
         Component subtitle = (Component)(split.length == 2 ? this.deserialize(split[1], event) : Component.empty());
         return Title.title(title, subtitle);
      }
   }
}
