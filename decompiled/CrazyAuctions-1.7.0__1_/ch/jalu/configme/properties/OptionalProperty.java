package ch.jalu.configme.properties;

import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionalProperty<T> implements Property<Optional<T>> {
   private final Property<T> baseProperty;
   private final Optional<T> defaultValue;

   public OptionalProperty(@NotNull Property<T> baseProperty) {
      this.baseProperty = baseProperty;
      this.defaultValue = Optional.empty();
   }

   public OptionalProperty(@NotNull Property<T> baseProperty, @NotNull T defaultValue) {
      this.baseProperty = baseProperty;
      this.defaultValue = Optional.of(defaultValue);
   }

   @NotNull
   @Override
   public String getPath() {
      return this.baseProperty.getPath();
   }

   @NotNull
   @Override
   public PropertyValue<Optional<T>> determineValue(@NotNull PropertyReader reader) {
      PropertyValue<T> basePropertyValue = this.baseProperty.determineValue(reader);
      Optional<T> value = basePropertyValue.isValidInResource() ? Optional.ofNullable(basePropertyValue.getValue()) : Optional.empty();
      boolean isWrongInResource = !basePropertyValue.isValidInResource() && reader.contains(this.baseProperty.getPath());
      return isWrongInResource ? PropertyValue.withValueRequiringRewrite(value) : PropertyValue.withValidValue(value);
   }

   @NotNull
   public Optional<T> getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isValidValue(@Nullable Optional<T> value) {
      return value == null ? false : value.map(this.baseProperty::isValidValue).orElse(true);
   }

   @Nullable
   public Object toExportValue(@NotNull Optional<T> value) {
      return value.map(this.baseProperty::toExportValue).orElse(null);
   }
}
