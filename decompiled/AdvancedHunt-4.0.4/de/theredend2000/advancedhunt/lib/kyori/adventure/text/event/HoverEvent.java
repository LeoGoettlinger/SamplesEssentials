package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Keyed;
import de.theredend2000.advancedhunt.lib.kyori.adventure.nbt.api.BinaryTagHolder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleBuilderApplicable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.renderer.ComponentRenderer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Index;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.Nullable;

public final class HoverEvent<V> implements HoverEventSource<V>, StyleBuilderApplicable {
   private final HoverEvent.Action<V> action;
   private final V value;

   public static HoverEvent<Component> showText(final ComponentLike text) {
      return showText(text.asComponent());
   }

   public static HoverEvent<Component> showText(final Component text) {
      return new HoverEvent<>(HoverEvent.Action.SHOW_TEXT, text);
   }

   public static HoverEvent<HoverEvent.ShowItem> showItem(final Key item, @Range(from = 0L,to = 2147483647L) final int count) {
      return showItem(item, count, Map.of());
   }

   public static HoverEvent<HoverEvent.ShowItem> showItem(final Keyed item, @Range(from = 0L,to = 2147483647L) final int count) {
      return showItem(item, count, Map.of());
   }

   @Obsolete
   public static HoverEvent<HoverEvent.ShowItem> showItem(
      final Key item, @Range(from = 0L,to = 2147483647L) final int count, @Nullable final BinaryTagHolder nbt
   ) {
      return showItem(HoverEvent.ShowItem.showItem(item, count, nbt));
   }

   @Obsolete
   public static HoverEvent<HoverEvent.ShowItem> showItem(
      final Keyed item, @Range(from = 0L,to = 2147483647L) final int count, @Nullable final BinaryTagHolder nbt
   ) {
      return showItem(HoverEvent.ShowItem.showItem(item, count, nbt));
   }

   public static HoverEvent<HoverEvent.ShowItem> showItem(
      final Keyed item, @Range(from = 0L,to = 2147483647L) final int count, final Map<Key, ? extends DataComponentValue> dataComponents
   ) {
      return showItem(HoverEvent.ShowItem.showItem(item, count, dataComponents));
   }

   public static HoverEvent<HoverEvent.ShowItem> showItem(final HoverEvent.ShowItem item) {
      return new HoverEvent<>(HoverEvent.Action.SHOW_ITEM, item);
   }

   public static HoverEvent<HoverEvent.ShowEntity> showEntity(final Key type, final UUID id) {
      return showEntity(type, id, null);
   }

   public static HoverEvent<HoverEvent.ShowEntity> showEntity(final Keyed type, final UUID id) {
      return showEntity(type, id, null);
   }

   public static HoverEvent<HoverEvent.ShowEntity> showEntity(final Key type, final UUID id, @Nullable final Component name) {
      return showEntity(HoverEvent.ShowEntity.showEntity(type, id, name));
   }

   public static HoverEvent<HoverEvent.ShowEntity> showEntity(final Keyed type, final UUID id, @Nullable final Component name) {
      return showEntity(HoverEvent.ShowEntity.showEntity(type, id, name));
   }

   public static HoverEvent<HoverEvent.ShowEntity> showEntity(final HoverEvent.ShowEntity entity) {
      return new HoverEvent<>(HoverEvent.Action.SHOW_ENTITY, entity);
   }

   @Obsolete
   public static HoverEvent<String> showAchievement(final String value) {
      return new HoverEvent<>(HoverEvent.Action.SHOW_ACHIEVEMENT, value);
   }

   public static <V> HoverEvent<V> hoverEvent(final HoverEvent.Action<V> action, final V value) {
      return new HoverEvent<>(action, value);
   }

   private HoverEvent(final HoverEvent.Action<V> action, final V value) {
      this.action = Objects.requireNonNull(action, "action");
      this.value = Objects.requireNonNull(value, "value");
   }

   public HoverEvent.Action<V> action() {
      return this.action;
   }

   public V value() {
      return this.value;
   }

   @Contract(
      pure = true
   )
   public HoverEvent<V> value(final V value) {
      return new HoverEvent<>(this.action, value);
   }

   public <C> HoverEvent<V> withRenderedValue(final ComponentRenderer<C> renderer, final C context) {
      V oldValue = this.value;
      V newValue = this.action.renderer().render(renderer, context, oldValue);
      return newValue != oldValue ? new HoverEvent<>(this.action, newValue) : this;
   }

   @Override
   public HoverEvent<V> asHoverEvent() {
      return this;
   }

   @Override
   public HoverEvent<V> asHoverEvent(final UnaryOperator<V> op) {
      return op == UnaryOperator.identity() ? this : new HoverEvent<>(this.action, op.apply(this.value));
   }

   @Override
   public void styleApply(final Style.Builder style) {
      style.hoverEvent(this);
   }

   @Override
   public boolean equals(final Object o) {
      return !(o instanceof HoverEvent<?> that) ? false : this.action.equals(that.action) && this.value.equals(that.value);
   }

   @Override
   public int hashCode() {
      int result = this.action.hashCode();
      return 31 * result + this.value.hashCode();
   }

   @Override
   public String toString() {
      return "HoverEvent{action=" + this.action + ", value=" + this.value + "}";
   }

   public static final class Action<V> {
      public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>("show_text", Component.class, true, HoverEventRenderers.SHOW_TEXT);
      public static final HoverEvent.Action<HoverEvent.ShowItem> SHOW_ITEM = new HoverEvent.Action<>(
         "show_item", HoverEvent.ShowItem.class, true, HoverEventRenderers.SHOW_ITEM
      );
      public static final HoverEvent.Action<HoverEvent.ShowEntity> SHOW_ENTITY = new HoverEvent.Action<>(
         "show_entity", HoverEvent.ShowEntity.class, true, HoverEventRenderers.SHOW_ENTITY
      );
      @Obsolete
      public static final HoverEvent.Action<String> SHOW_ACHIEVEMENT = new HoverEvent.Action<>(
         "show_achievement", String.class, true, HoverEventRenderers.SHOW_ACHIEVEMENT
      );
      public static final Index<String, HoverEvent.Action<?>> NAMES = Index.create(
         HoverEvent.Action::toString, SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY, SHOW_ACHIEVEMENT
      );
      private final String name;
      private final Class<V> type;
      private final boolean readable;
      private final HoverEvent.Action.Renderer<V> renderer;

      private Action(final String name, final Class<V> type, final boolean readable, final HoverEvent.Action.Renderer<V> renderer) {
         this.name = name;
         this.type = type;
         this.readable = readable;
         this.renderer = renderer;
      }

      public String name() {
         return this.name;
      }

      public Class<V> type() {
         return this.type;
      }

      public boolean readable() {
         return this.readable;
      }

      public HoverEvent.Action.Renderer<V> renderer() {
         return this.renderer;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public sealed interface Renderer<V>
         permits HoverEventRenderers.ShowAchievement,
         HoverEventRenderers.ShowEntity,
         HoverEventRenderers.ShowItem,
         HoverEventRenderers.ShowText {
         <C> V render(final ComponentRenderer<C> renderer, final C context, final V value);
      }
   }

   public static final class ShowEntity {
      private final Key type;
      private final UUID id;
      @Nullable
      private final Component name;

      public static HoverEvent.ShowEntity showEntity(final Key type, final UUID id) {
         return showEntity(type, id, null);
      }

      public static HoverEvent.ShowEntity showEntity(final Keyed type, final UUID id) {
         return showEntity(type, id, null);
      }

      public static HoverEvent.ShowEntity showEntity(final Key type, final UUID id, @Nullable final Component name) {
         return new HoverEvent.ShowEntity(Objects.requireNonNull(type, "type"), Objects.requireNonNull(id, "id"), name);
      }

      public static HoverEvent.ShowEntity showEntity(final Keyed type, final UUID id, @Nullable final Component name) {
         return new HoverEvent.ShowEntity(Objects.requireNonNull(type, "type").key(), Objects.requireNonNull(id, "id"), name);
      }

      private ShowEntity(final Key type, final UUID id, @Nullable final Component name) {
         this.type = type;
         this.id = id;
         this.name = name;
      }

      public Key type() {
         return this.type;
      }

      public HoverEvent.ShowEntity type(final Key type) {
         return Objects.requireNonNull(type, "type").equals(this.type) ? this : new HoverEvent.ShowEntity(type, this.id, this.name);
      }

      public HoverEvent.ShowEntity type(final Keyed type) {
         return this.type(Objects.requireNonNull(type, "type").key());
      }

      public UUID id() {
         return this.id;
      }

      public HoverEvent.ShowEntity id(final UUID id) {
         return Objects.requireNonNull(id).equals(this.id) ? this : new HoverEvent.ShowEntity(this.type, id, this.name);
      }

      @Nullable
      public Component name() {
         return this.name;
      }

      public HoverEvent.ShowEntity name(@Nullable final Component name) {
         return Objects.equals(name, this.name) ? this : new HoverEvent.ShowEntity(this.type, this.id, name);
      }

      @Override
      public boolean equals(final Object o) {
         return !(o instanceof HoverEvent.ShowEntity that)
            ? false
            : this.type.equals(that.type) && this.id.equals(that.id) && Objects.equals(this.name, that.name);
      }

      @Override
      public int hashCode() {
         int result = this.type.hashCode();
         result = 31 * result + this.id.hashCode();
         return 31 * result + Objects.hashCode(this.name);
      }

      @Override
      public String toString() {
         return "ShowEntity{type=" + this.type + ", id=" + this.id + ", name=" + this.name + "}";
      }
   }

   public static final class ShowItem {
      private final Key item;
      private final int count;
      @Nullable
      private final BinaryTagHolder nbt;
      private final Map<Key, DataComponentValue> dataComponents;

      public static HoverEvent.ShowItem showItem(final Key item, @Range(from = 0L,to = 2147483647L) final int count) {
         return showItem(item, count, Map.of());
      }

      public static HoverEvent.ShowItem showItem(final Keyed item, @Range(from = 0L,to = 2147483647L) final int count) {
         return showItem(item, count, Map.of());
      }

      @Obsolete
      public static HoverEvent.ShowItem showItem(final Key item, @Range(from = 0L,to = 2147483647L) final int count, @Nullable final BinaryTagHolder nbt) {
         return new HoverEvent.ShowItem(Objects.requireNonNull(item, "item"), count, nbt, Map.of());
      }

      @Obsolete
      public static HoverEvent.ShowItem showItem(final Keyed item, @Range(from = 0L,to = 2147483647L) final int count, @Nullable final BinaryTagHolder nbt) {
         return new HoverEvent.ShowItem(Objects.requireNonNull(item, "item").key(), count, nbt, Map.of());
      }

      public static HoverEvent.ShowItem showItem(
         final Keyed item, @Range(from = 0L,to = 2147483647L) final int count, final Map<Key, ? extends DataComponentValue> dataComponents
      ) {
         return new HoverEvent.ShowItem(
            Objects.requireNonNull(item, "item").key(), count, null, Map.copyOf(Objects.requireNonNull(dataComponents, "dataComponents"))
         );
      }

      private ShowItem(final Key item, final int count, @Nullable final BinaryTagHolder nbt, final Map<Key, DataComponentValue> dataComponents) {
         this.item = item;
         this.count = count;
         this.nbt = nbt;
         this.dataComponents = dataComponents;
      }

      public Key item() {
         return this.item;
      }

      public HoverEvent.ShowItem item(final Key item) {
         return Objects.requireNonNull(item, "item").equals(this.item) ? this : new HoverEvent.ShowItem(item, this.count, this.nbt, this.dataComponents);
      }

      @Range(
         from = 0L,
         to = 2147483647L
      )
      public int count() {
         return this.count;
      }

      public HoverEvent.ShowItem count(@Range(from = 0L,to = 2147483647L) final int count) {
         return count == this.count ? this : new HoverEvent.ShowItem(this.item, count, this.nbt, this.dataComponents);
      }

      @Obsolete
      @Nullable
      public BinaryTagHolder nbt() {
         return this.nbt;
      }

      @Obsolete
      public HoverEvent.ShowItem nbt(@Nullable final BinaryTagHolder nbt) {
         return Objects.equals(nbt, this.nbt) ? this : new HoverEvent.ShowItem(this.item, this.count, nbt, this.dataComponents);
      }

      public Map<Key, DataComponentValue> dataComponents() {
         return this.dataComponents;
      }

      public HoverEvent.ShowItem dataComponents(final Map<Key, DataComponentValue> holder) {
         return Objects.equals(this.dataComponents, holder) ? this : new HoverEvent.ShowItem(this.item, this.count, this.nbt, Map.copyOf(holder));
      }

      public <V extends DataComponentValue> Map<Key, V> dataComponentsAs(final Class<V> targetType) {
         if (this.dataComponents.isEmpty()) {
            return Map.of();
         } else {
            Map<Key, V> results = new HashMap<>(this.dataComponents.size());

            for (Entry<Key, DataComponentValue> entry : this.dataComponents.entrySet()) {
               results.put(entry.getKey(), DataComponentValueConverterRegistry.convert(targetType, entry.getKey(), entry.getValue()));
            }

            return Map.copyOf(results);
         }
      }

      @Override
      public boolean equals(final Object o) {
         return !(o instanceof HoverEvent.ShowItem showItem)
            ? false
            : this.count == showItem.count
               && this.item.equals(showItem.item)
               && Objects.equals(this.nbt, showItem.nbt)
               && this.dataComponents.equals(showItem.dataComponents);
      }

      @Override
      public int hashCode() {
         int result = this.item.hashCode();
         result = 31 * result + this.count;
         result = 31 * result + Objects.hashCode(this.nbt);
         return 31 * result + this.dataComponents.hashCode();
      }

      @Override
      public String toString() {
         return "ShowItem{item=" + this.item + ", count=" + this.count + ", nbt=" + this.nbt + ", dataComponents=" + this.dataComponents + "}";
      }
   }
}
