package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "RichVariable",
   generator = "Immutables"
)
final class RichVariableImpl implements RichVariable {
   @NonNull
   private final String key;
   @NonNull
   private final Component component;

   private RichVariableImpl(@NonNull String key, @NonNull Component component) {
      this.key = Objects.requireNonNull(key, "key");
      this.component = Objects.requireNonNull(component, "component");
   }

   private RichVariableImpl(RichVariableImpl original, @NonNull String key, @NonNull Component component) {
      this.key = key;
      this.component = component;
   }

   @NonNull
   @Override
   public String key() {
      return this.key;
   }

   @NonNull
   @Override
   public Component component() {
      return this.component;
   }

   public final RichVariableImpl withKey(String value) {
      String newValue = Objects.requireNonNull(value, "key");
      return this.key.equals(newValue) ? this : new RichVariableImpl(this, newValue, this.component);
   }

   public final RichVariableImpl withComponent(Component value) {
      if (this.component == value) {
         return this;
      } else {
         Component newValue = Objects.requireNonNull(value, "component");
         return new RichVariableImpl(this, this.key, newValue);
      }
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof RichVariableImpl && this.equalsByValue((RichVariableImpl)another);
   }

   private boolean equalsByValue(RichVariableImpl another) {
      return this.key.equals(another.key) && this.component.equals(another.component);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.key.hashCode();
      return h + (h << 5) + this.component.hashCode();
   }

   @Override
   public String toString() {
      return "RichVariable{key=" + this.key + ", component=" + this.component + "}";
   }

   public static RichVariableImpl of(@NonNull String key, @NonNull Component component) {
      return new RichVariableImpl(key, component);
   }

   public static RichVariableImpl copyOf(RichVariable instance) {
      return instance instanceof RichVariableImpl ? (RichVariableImpl)instance : of(instance.key(), instance.component());
   }
}
