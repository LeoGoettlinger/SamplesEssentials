package su.nightexpress.nightcore.ui.inventory.item;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.ui.inventory.action.MenuItemAction;
import su.nightexpress.nightcore.ui.inventory.condition.ItemStateCondition;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class MenuItem {
   private final ItemType type;
   private final Map<String, ItemState> states;
   private final int[] slots;

   private MenuItem(@NonNull ItemType type, @NonNull Map<String, ItemState> states, int[] slots) {
      this.type = type;
      this.states = states;
      this.slots = slots;
   }

   @Deprecated
   @NonNull
   public static MenuItem.Builder builder() {
      return button();
   }

   @NonNull
   public static MenuItem.Builder custom() {
      return builder(ItemTypes.CUSTOM);
   }

   @NonNull
   public static MenuItem.Builder button() {
      return builder(ItemTypes.BUTTON);
   }

   @NonNull
   public static MenuItem.Builder builder(@NonNull ItemType type) {
      return new MenuItem.Builder(type);
   }

   @NonNull
   public static MenuItem background(@NonNull Material material, int... slots) {
      return custom().defaultState(NightItem.fromType(material)).slots(slots).build();
   }

   @NonNull
   public ItemState resolveState(@NonNull ViewerContext context) {
      for (ItemState state : this.states.values()) {
         if (state.isVisibleFor(context)) {
            return state;
         }
      }

      return ItemState.INVISIBLE;
   }

   @NonNull
   public ItemType getType() {
      return this.type;
   }

   @NonNull
   public Map<String, ItemState> getStates() {
      return this.states;
   }

   @Nullable
   public ItemState getState(@NonNull String id) {
      return this.states.get(LowerCase.INTERNAL.apply(id));
   }

   public boolean hasState(@NonNull String id) {
      return this.getState(id) != null;
   }

   public int[] getSlots() {
      return this.slots;
   }

   public static class Builder {
      private final ItemType type;
      private final Map<String, ItemState> states;
      private int[] slots = new int[0];

      Builder(@NonNull ItemType type) {
         this.type = type;
         this.states = new LinkedHashMap<>();
      }

      @Deprecated(
         forRemoval = true
      )
      @NonNull
      public MenuItem.Builder state(@NonNull ItemState state) {
         this.states.put(state.getName(), state);
         return this;
      }

      @NonNull
      public MenuItem.Builder state(@NonNull String name, @NonNull ItemState state) {
         this.states.put(LowerCase.INTERNAL.apply(name), state);
         return this;
      }

      @NonNull
      public MenuItem.Builder defaultState(@NonNull ItemState state) {
         return this.state("default", state);
      }

      @NonNull
      public MenuItem.Builder defaultState(@NonNull NightItem icon) {
         return this.defaultState(icon, null);
      }

      @NonNull
      public MenuItem.Builder defaultState(@NonNull NightItem icon, @Nullable MenuItemAction action) {
         return this.defaultState(icon, action, null);
      }

      @NonNull
      public MenuItem.Builder defaultState(@NonNull NightItem icon, @Nullable MenuItemAction action, @Nullable ItemStateCondition condition) {
         return this.defaultState(icon, action, condition, null);
      }

      @NonNull
      public MenuItem.Builder defaultState(
         @NonNull NightItem icon, @Nullable MenuItemAction action, @Nullable ItemStateCondition condition, @Nullable DisplayModifier displayModifier
      ) {
         return this.defaultState(new ItemState(icon, action, condition, displayModifier));
      }

      @NonNull
      public MenuItem.Builder slots(int... slots) {
         this.slots = slots;
         return this;
      }

      @NonNull
      public MenuItem build() {
         return new MenuItem(this.type, this.states, this.slots);
      }
   }
}
