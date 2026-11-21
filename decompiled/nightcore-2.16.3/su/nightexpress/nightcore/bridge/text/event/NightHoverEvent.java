package su.nightexpress.nightcore.bridge.text.event;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public class NightHoverEvent<V> {
   private final NightHoverEvent.Action<V> action;
   private final V value;

   @NonNull
   public static NightHoverEvent<NightComponent> showText(@NonNull NightComponent text) {
      return new NightHoverEvent<>(NightHoverEvent.Action.SHOW_TEXT, text);
   }

   @NonNull
   public static NightHoverEvent<ItemStack> showItem(@NonNull ItemStack itemStack) {
      return new NightHoverEvent<>(NightHoverEvent.Action.SHOW_ITEM, itemStack);
   }

   @NonNull
   public static <V> NightHoverEvent<V> hoverEvent(@NonNull NightHoverEvent.Action<V> action, @NonNull V value) {
      return new NightHoverEvent<>(action, value);
   }

   private NightHoverEvent(@NonNull NightHoverEvent.Action<V> action, @NonNull V value) {
      this.action = action;
      this.value = value;
   }

   @NonNull
   public NightHoverEvent.Action<V> action() {
      return this.action;
   }

   @NonNull
   public V value() {
      return this.value;
   }

   public static final class Action<V> {
      public static final NightHoverEvent.Action<NightComponent> SHOW_TEXT = new NightHoverEvent.Action<>(NightComponent.class, "show_text");
      public static final NightHoverEvent.Action<ItemStack> SHOW_ITEM = new NightHoverEvent.Action<>(ItemStack.class, "show_item");
      private final Class<V> type;
      private final String name;

      public static NightHoverEvent.Action<?>[] values() {
         return new NightHoverEvent.Action[]{SHOW_TEXT, SHOW_ITEM};
      }

      @Nullable
      public static NightHoverEvent.Action<?> byName(@NonNull String name) {
         for (NightHoverEvent.Action<?> action : values()) {
            if (name.equalsIgnoreCase(action.name())) {
               return action;
            }
         }

         return null;
      }

      Action(@NonNull Class<V> type, @NonNull String name) {
         this.type = type;
         this.name = name;
      }

      @NonNull
      public String name() {
         return this.name;
      }

      @NonNull
      public Class<V> type() {
         return this.type;
      }
   }
}
