package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util;

import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class LegacyComponent {
   private static final LegacyComponent EMPTY = new LegacyComponent(Component.empty(), "");
   private final Component component;
   private final String legacy;

   public LegacyComponent(Component component) {
      this(component, LegacyComponentSerializer.legacySection().serialize(component));
   }

   public LegacyComponent(String legacy) {
      this(LegacyComponentSerializer.legacySection().deserialize(legacy), legacy);
   }

   private LegacyComponent(Component component, String legacy) {
      this.component = component;
      this.legacy = legacy;
   }

   public static LegacyComponent wrapOrEmpty(@Nullable Component component) {
      return component != null && component != Component.empty() ? new LegacyComponent(component) : EMPTY;
   }

   public static LegacyComponent empty() {
      return EMPTY;
   }

   public Component getComponent() {
      return this.component;
   }

   public String getLegacy() {
      return this.legacy;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         LegacyComponent that = (LegacyComponent)obj;
         return !this.component.equals(that.component) ? false : this.legacy.equals(that.legacy);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.component, this.legacy);
   }
}
