package me.byteful.plugin.leveltools.api.trigger;

import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerContext {
   private final Player player;
   private final ItemStack item;
   private final String triggerId;
   private final TriggerSlot slot;
   private final Object source;
   private final Event originalEvent;
   private final ItemProfile itemProfile;
   private final TriggerProfile triggerProfile;

   private TriggerContext(
      @NotNull Player player,
      @NotNull ItemStack item,
      @NotNull String triggerId,
      @Nullable TriggerSlot slot,
      @Nullable Object source,
      @NotNull Event originalEvent,
      @NotNull ItemProfile itemProfile,
      @NotNull TriggerProfile triggerProfile
   ) {
      this.player = player;
      this.item = item;
      this.triggerId = triggerId;
      this.slot = slot;
      this.source = source;
      this.originalEvent = originalEvent;
      this.itemProfile = itemProfile;
      this.triggerProfile = triggerProfile;
   }

   public static TriggerContext.Builder builder() {
      return new TriggerContext.Builder();
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   @NotNull
   public ItemStack getItem() {
      return this.item;
   }

   @NotNull
   public String getTriggerId() {
      return this.triggerId;
   }

   @Nullable
   public TriggerSlot getSlot() {
      return this.slot;
   }

   @Nullable
   public Object getSource() {
      return this.source;
   }

   @Nullable
   public <T> T getSourceAs(@NotNull Class<T> type) {
      return (T)(type.isInstance(this.source) ? this.source : null);
   }

   @NotNull
   public Event getOriginalEvent() {
      return this.originalEvent;
   }

   @Nullable
   public <T extends Event> T getOriginalEventAs(@NotNull Class<T> type) {
      return (T)(type.isInstance(this.originalEvent) ? this.originalEvent : null);
   }

   @NotNull
   public ItemProfile getItemProfile() {
      return this.itemProfile;
   }

   @NotNull
   public TriggerProfile getTriggerProfile() {
      return this.triggerProfile;
   }

   public static final class Builder {
      private Player player;
      private ItemStack item;
      private String triggerId;
      private TriggerSlot slot;
      private Object source;
      private Event originalEvent;
      private ItemProfile itemProfile;
      private TriggerProfile triggerProfile;

      private Builder() {
      }

      public TriggerContext.Builder player(@NotNull Player player) {
         this.player = player;
         return this;
      }

      public TriggerContext.Builder item(@NotNull ItemStack item) {
         this.item = item;
         return this;
      }

      public TriggerContext.Builder triggerId(@NotNull String triggerId) {
         this.triggerId = TriggerIds.normalize(triggerId);
         return this;
      }

      public TriggerContext.Builder slot(@Nullable TriggerSlot slot) {
         this.slot = slot;
         return this;
      }

      public TriggerContext.Builder source(@Nullable Object source) {
         this.source = source;
         return this;
      }

      public TriggerContext.Builder originalEvent(@NotNull Event originalEvent) {
         this.originalEvent = originalEvent;
         return this;
      }

      public TriggerContext.Builder itemProfile(@NotNull ItemProfile itemProfile) {
         this.itemProfile = itemProfile;
         return this;
      }

      public TriggerContext.Builder triggerProfile(@NotNull TriggerProfile triggerProfile) {
         this.triggerProfile = triggerProfile;
         return this;
      }

      public TriggerContext build() {
         if (this.player == null) {
            throw new IllegalStateException("Player must be set");
         } else if (this.item == null) {
            throw new IllegalStateException("Item must be set");
         } else if (this.triggerId == null) {
            throw new IllegalStateException("TriggerId must be set");
         } else if (this.originalEvent == null) {
            throw new IllegalStateException("Original event must be set");
         } else if (this.itemProfile == null) {
            throw new IllegalStateException("ItemProfile must be set");
         } else if (this.triggerProfile == null) {
            throw new IllegalStateException("TriggerProfile must be set");
         } else {
            return new TriggerContext(this.player, this.item, this.triggerId, this.slot, this.source, this.originalEvent, this.itemProfile, this.triggerProfile);
         }
      }
   }
}
