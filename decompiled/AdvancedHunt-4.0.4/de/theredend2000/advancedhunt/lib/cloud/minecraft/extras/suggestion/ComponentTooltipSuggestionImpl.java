package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.suggestion;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Generated;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "ComponentTooltipSuggestion",
   generator = "Immutables"
)
final class ComponentTooltipSuggestionImpl implements ComponentTooltipSuggestion {
   @NonNull
   private final String suggestion;
   @Nullable
   private final Component tooltip;

   private ComponentTooltipSuggestionImpl(@NonNull String suggestion, @Nullable Component tooltip) {
      this.suggestion = Objects.requireNonNull(suggestion, "suggestion");
      this.tooltip = tooltip;
   }

   private ComponentTooltipSuggestionImpl(ComponentTooltipSuggestionImpl original, @NonNull String suggestion, @Nullable Component tooltip) {
      this.suggestion = suggestion;
      this.tooltip = tooltip;
   }

   @NonNull
   @Override
   public String suggestion() {
      return this.suggestion;
   }

   @Nullable
   @Override
   public Component tooltip() {
      return this.tooltip;
   }

   public final ComponentTooltipSuggestionImpl withSuggestion(String value) {
      String newValue = Objects.requireNonNull(value, "suggestion");
      return this.suggestion.equals(newValue) ? this : new ComponentTooltipSuggestionImpl(this, newValue, this.tooltip);
   }

   public final ComponentTooltipSuggestionImpl withTooltip(@Nullable Component value) {
      return this.tooltip == value ? this : new ComponentTooltipSuggestionImpl(this, this.suggestion, value);
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ComponentTooltipSuggestionImpl && this.equalsByValue((ComponentTooltipSuggestionImpl)another);
   }

   private boolean equalsByValue(ComponentTooltipSuggestionImpl another) {
      return this.suggestion.equals(another.suggestion) && Objects.equals(this.tooltip, another.tooltip);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.suggestion.hashCode();
      return h + (h << 5) + Objects.hashCode(this.tooltip);
   }

   @Override
   public String toString() {
      return "ComponentTooltipSuggestion{suggestion=" + this.suggestion + ", tooltip=" + this.tooltip + "}";
   }

   public static ComponentTooltipSuggestionImpl of(@NonNull String suggestion, @Nullable Component tooltip) {
      return new ComponentTooltipSuggestionImpl(suggestion, tooltip);
   }

   public static ComponentTooltipSuggestionImpl copyOf(ComponentTooltipSuggestion instance) {
      return instance instanceof ComponentTooltipSuggestionImpl ? (ComponentTooltipSuggestionImpl)instance : of(instance.suggestion(), instance.tooltip());
   }
}
