package com.magmaguy.elitemobs.presentation.actionbar;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public final class ActionBarCompositor implements Listener {
   private static final long KEEPALIVE_INTERVAL_TICKS = 40L;
   private static final CombatHud COMBAT_HUD = new CombatHud();
   private static final Map<UUID, ActionBarCompositor.PlayerState> playerStates = new HashMap<>();
   private static final ConcurrentLinkedQueue<ActionBarCompositor.Mutation> pendingMutations = new ConcurrentLinkedQueue<>();
   private static ActionBarCompositor listener;
   private static BukkitTask task;
   private static long currentTick;
   private static long nextSequence;

   private ActionBarCompositor() {
   }

   public static void initialize() {
      start();
   }

   public static void start() {
      if (task == null) {
         requirePrimaryThread("start");
         listener = new ActionBarCompositor();
         Bukkit.getPluginManager().registerEvents(listener, MetadataHandler.PLUGIN);
         task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, ActionBarCompositor::tick, 1L, 1L);
      }
   }

   public static void show(Player player, ActionBarCompositor.Source source, String message) {
      long duration = source.isPersistent() ? source.defaultDurationTicks() : Math.max(source.defaultDurationTicks(), readingDurationTicks(message));
      publish(player, source, message, duration, true);
   }

   public static long readingDurationTicks(String message) {
      String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(message, "")));
      int words = 0;

      for (String token : stripped.trim().split("\\s+")) {
         if (!token.isBlank()) {
            words++;
         }
      }

      return Math.min(300L, 20L + words * 8L);
   }

   public static void show(Player player, ActionBarCompositor.Source source, String message, long durationTicks) {
      if (durationTicks <= 0L) {
         throw new IllegalArgumentException("Action-bar duration must be positive");
      } else {
         publish(player, source, message, durationTicks, false);
      }
   }

   public static void clear(Player player, ActionBarCompositor.Source source) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(source, "source");
      dispatch(new ActionBarCompositor.ClearMutation(player.getUniqueId(), source));
   }

   public static void shutdown() {
      if (task != null) {
         task.cancel();
         task = null;
      }

      if (listener != null) {
         HandlerList.unregisterAll(listener);
         listener = null;
      }

      playerStates.clear();
      pendingMutations.clear();
      currentTick = 0L;
      nextSequence = 0L;
   }

   private static void publish(Player player, ActionBarCompositor.Source source, String message, long durationTicks, boolean readingTime) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(message, "message");
      dispatch(new ActionBarCompositor.ShowMutation(player.getUniqueId(), source, message, durationTicks, readingTime));
   }

   private static void dispatch(ActionBarCompositor.Mutation mutation) {
      if (Bukkit.isPrimaryThread()) {
         apply(mutation);
         if (task != null) {
            render(mutation.playerId());
         }
      } else {
         pendingMutations.add(mutation);
      }
   }

   private static void tick() {
      currentTick++;

      ActionBarCompositor.Mutation mutation;
      while ((mutation = pendingMutations.poll()) != null) {
         apply(mutation);
      }

      Iterator<UUID> iterator = playerStates.keySet().iterator();

      while (iterator.hasNext()) {
         UUID playerId = iterator.next();
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline()) {
            ActionBarCompositor.PlayerState state = playerStates.get(playerId);
            removeExpired(state);
            render(player, state);
            if (state.entries.isEmpty() && !state.hasRenderedMessage) {
               iterator.remove();
            }
         } else {
            iterator.remove();
         }
      }
   }

   private static void apply(ActionBarCompositor.Mutation mutation) {
      ActionBarCompositor.PlayerState state = playerStates.computeIfAbsent(mutation.playerId(), ignored -> new ActionBarCompositor.PlayerState());
      if (mutation instanceof ActionBarCompositor.ShowMutation show) {
         long duration = show.durationTicks;
         Player player = Bukkit.getPlayer(show.playerId());
         if (player != null && AdvancedCombatRuntime.isActive(player) && show.readingTime && !show.source.isPersistent()) {
            duration = Math.min(600L, Math.max(120L, duration * 2L));
         }

         long expiresAtTick = duration == -1L ? Long.MAX_VALUE : saturatingAdd(currentTick, duration);
         state.entries.put(show.source, new ActionBarCompositor.Entry(show.source, show.message, expiresAtTick, ++nextSequence));
      } else if (mutation instanceof ActionBarCompositor.ClearMutation clear) {
         state.entries.remove(clear.source);
      }
   }

   private static void render(UUID playerId) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && player.isOnline()) {
         ActionBarCompositor.PlayerState state = playerStates.get(playerId);
         if (state != null) {
            removeExpired(state);
            render(player, state);
            if (state.entries.isEmpty() && !state.hasRenderedMessage) {
               playerStates.remove(playerId);
            }
         }
      } else {
         playerStates.remove(playerId);
      }
   }

   private static void render(Player player, ActionBarCompositor.PlayerState state) {
      if (AdvancedCombatSystemConfig.isEnableCombatHud()
         && AdvancedCombatRuntime.isActive(player)
         && Bukkit.getPluginManager().isPluginEnabled("ResourcePackManager")
         && !BedrockChecker.isBedrock(player)) {
         CombatHud.Frame frame = COMBAT_HUD.frame(player, AdvancedCombatModule.isAbilityGestureOpen(player.getUniqueId()));
         ActionBarCompositor.Entry feedback = selectWinner(state, true);
         String feedbackText = feedback == null ? null : feedback.message;
         ActionBarCompositor.Encoding feedbackEncoding = feedback == null ? null : feedback.source.encoding;
         if (!state.hasRenderedMessage
            || state.lastEncoding != ActionBarCompositor.Encoding.COMBAT_HUD
            || !Objects.equals(feedbackText, state.lastFeedback)
            || feedbackEncoding != state.lastFeedbackEncoding
            || !frame.equals(state.lastHudFrame)
            || currentTick - state.lastSentAtTick >= 40L) {
            player.spigot()
               .sendMessage(ChatMessageType.ACTION_BAR, COMBAT_HUD.component(frame, feedbackText, feedbackEncoding == ActionBarCompositor.Encoding.LEGACY));
            state.hasRenderedMessage = true;
            state.lastMessage = frame.text();
            state.lastHudFrame = frame;
            state.lastFeedback = feedbackText;
            state.lastFeedbackEncoding = feedbackEncoding;
            state.lastEncoding = ActionBarCompositor.Encoding.COMBAT_HUD;
            state.lastSentAtTick = currentTick;
         }
      } else {
         ActionBarCompositor.Entry winner = selectWinner(state, false);
         if (winner == null) {
            if (state.hasRenderedMessage) {
               player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
               state.hasRenderedMessage = false;
               state.lastMessage = null;
               state.lastEncoding = null;
            }
         } else {
            ActionBarCompositor.Entry hint = state.entries.get(ActionBarCompositor.Source.CONTEXT_HINT);
            String suffix = hint != null && hint != winner ? " §8| §r" + hint.message : "";
            String message = winner.message + suffix;
            boolean payloadChanged = !state.hasRenderedMessage || !message.equals(state.lastMessage) || winner.source.encoding != state.lastEncoding;
            boolean keepaliveDue = currentTick - state.lastSentAtTick >= 40L;
            if (payloadChanged || keepaliveDue) {
               if (winner.source.encoding == ActionBarCompositor.Encoding.LEGACY) {
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
               } else {
                  TextComponent payload = new TextComponent(winner.message);
                  if (!suffix.isEmpty()) {
                     for (BaseComponent component : TextComponent.fromLegacyText(suffix)) {
                        payload.addExtra(component);
                     }
                  }

                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, payload);
               }

               state.hasRenderedMessage = true;
               state.lastMessage = message;
               state.lastEncoding = winner.source.encoding;
               state.lastSentAtTick = currentTick;
            }
         }
      }
   }

   private static ActionBarCompositor.Entry selectWinner(ActionBarCompositor.PlayerState state, boolean feedbackOnly) {
      ActionBarCompositor.Entry winner = null;

      for (ActionBarCompositor.Entry candidate : state.entries.values()) {
         if ((!feedbackOnly || !candidate.source.isPersistent() && candidate.source != ActionBarCompositor.Source.ABILITY_INPUT)
            && (
               winner == null
                  || candidate.source.priority > winner.source.priority
                  || candidate.source.priority == winner.source.priority && candidate.sequence > winner.sequence
            )) {
            winner = candidate;
         }
      }

      return winner;
   }

   private static void removeExpired(ActionBarCompositor.PlayerState state) {
      state.entries.values().removeIf(entry -> entry.expiresAtTick <= currentTick);
   }

   private static long saturatingAdd(long left, long right) {
      return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
   }

   private static void requirePrimaryThread(String operation) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("ActionBarCompositor." + operation + " must run on the server thread");
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      playerStates.remove(event.getPlayer().getUniqueId());
   }

   private record ClearMutation(UUID playerId, ActionBarCompositor.Source source) implements ActionBarCompositor.Mutation {
   }

   private static enum Encoding {
      LEGACY,
      RAW,
      COMBAT_HUD;
   }

   private record Entry(ActionBarCompositor.Source source, String message, long expiresAtTick, long sequence) {
   }

   private sealed interface Mutation permits ActionBarCompositor.ShowMutation, ActionBarCompositor.ClearMutation {
      UUID playerId();
   }

   private static final class PlayerState {
      private CombatHud.Frame lastHudFrame;
      private final EnumMap<ActionBarCompositor.Source, ActionBarCompositor.Entry> entries = new EnumMap<>(ActionBarCompositor.Source.class);
      private boolean hasRenderedMessage;
      private String lastMessage;
      private String lastFeedback;
      private ActionBarCompositor.Encoding lastFeedbackEncoding;
      private ActionBarCompositor.Encoding lastEncoding;
      private long lastSentAtTick;
   }

   private record ShowMutation(UUID playerId, ActionBarCompositor.Source source, String message, long durationTicks, boolean readingTime)
      implements ActionBarCompositor.Mutation {
   }

   public static enum Source {
      CRITICAL_WARNING(500, 60L, ActionBarCompositor.Encoding.LEGACY),
      LOCKOUT(500, 60L, ActionBarCompositor.Encoding.RAW),
      SCRIPT(400, 60L, ActionBarCompositor.Encoding.LEGACY),
      LUA(400, 60L, ActionBarCompositor.Encoding.LEGACY),
      ABILITY_INPUT(310, 40L, ActionBarCompositor.Encoding.LEGACY),
      AFFINITY_WARNING(350, 200L, ActionBarCompositor.Encoding.LEGACY),
      COMBAT_TRANSITION(300, 60L, ActionBarCompositor.Encoding.LEGACY),
      SKILL_FEEDBACK(300, 60L, ActionBarCompositor.Encoding.LEGACY),
      LOOT(200, 60L, ActionBarCompositor.Encoding.LEGACY),
      ECONOMY(200, 60L, ActionBarCompositor.Encoding.LEGACY),
      CLASS_HUD(100, -1L, ActionBarCompositor.Encoding.LEGACY),
      CONTEXT_HINT(0, -1L, ActionBarCompositor.Encoding.LEGACY);

      private static final long PERSISTENT = -1L;
      private final int priority;
      private final long defaultDurationTicks;
      private final ActionBarCompositor.Encoding encoding;

      private Source(int priority, long defaultDurationTicks, ActionBarCompositor.Encoding encoding) {
         this.priority = priority;
         this.defaultDurationTicks = defaultDurationTicks;
         this.encoding = encoding;
      }

      public int priority() {
         return this.priority;
      }

      public long defaultDurationTicks() {
         return this.defaultDurationTicks;
      }

      public boolean isPersistent() {
         return this.defaultDurationTicks == -1L;
      }
   }
}
