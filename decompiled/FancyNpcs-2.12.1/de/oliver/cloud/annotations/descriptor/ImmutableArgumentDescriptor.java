package de.oliver.cloud.annotations.descriptor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.oliver.cloud.component.DefaultValue;
import de.oliver.cloud.description.Description;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Generated;

@API(
   status = Status.STABLE,
   consumers = {"de.oliver.cloud.*"}
)
@Generated(
   from = "ArgumentDescriptor",
   generator = "Immutables"
)
public final class ImmutableArgumentDescriptor implements ArgumentDescriptor {
   @NonNull
   private final Parameter parameter;
   @NonNull
   private final String name;
   @Nullable
   private final String parserName;
   @Nullable
   private final String suggestions;
   @Nullable
   private final DefaultValue<?, ?> defaultValue;
   @Nullable
   private final Description description;

   private ImmutableArgumentDescriptor(
      @NonNull Parameter parameter,
      @NonNull String name,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable DefaultValue<?, ?> defaultValue,
      @Nullable Description description
   ) {
      this.parameter = Objects.requireNonNull(parameter, "parameter");
      this.name = Objects.requireNonNull(name, "name");
      this.parserName = parserName;
      this.suggestions = suggestions;
      this.defaultValue = defaultValue;
      this.description = description;
   }

   private ImmutableArgumentDescriptor(
      ImmutableArgumentDescriptor original,
      @NonNull Parameter parameter,
      @NonNull String name,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable DefaultValue<?, ?> defaultValue,
      @Nullable Description description
   ) {
      this.parameter = parameter;
      this.name = name;
      this.parserName = parserName;
      this.suggestions = suggestions;
      this.defaultValue = defaultValue;
      this.description = description;
   }

   @NonNull
   @Override
   public Parameter parameter() {
      return this.parameter;
   }

   @NonNull
   @Override
   public String name() {
      return this.name;
   }

   @Nullable
   @Override
   public String parserName() {
      return this.parserName;
   }

   @Nullable
   @Override
   public String suggestions() {
      return this.suggestions;
   }

   @Nullable
   @Override
   public DefaultValue<?, ?> defaultValue() {
      return this.defaultValue;
   }

   @Nullable
   @Override
   public Description description() {
      return this.description;
   }

   public final ImmutableArgumentDescriptor withParameter(Parameter value) {
      if (this.parameter == value) {
         return this;
      } else {
         Parameter newValue = Objects.requireNonNull(value, "parameter");
         return new ImmutableArgumentDescriptor(this, newValue, this.name, this.parserName, this.suggestions, this.defaultValue, this.description);
      }
   }

   public final ImmutableArgumentDescriptor withName(String value) {
      String newValue = Objects.requireNonNull(value, "name");
      return this.name.equals(newValue)
         ? this
         : new ImmutableArgumentDescriptor(this, this.parameter, newValue, this.parserName, this.suggestions, this.defaultValue, this.description);
   }

   public final ImmutableArgumentDescriptor withParserName(@Nullable String value) {
      return Objects.equals(this.parserName, value)
         ? this
         : new ImmutableArgumentDescriptor(this, this.parameter, this.name, value, this.suggestions, this.defaultValue, this.description);
   }

   public final ImmutableArgumentDescriptor withSuggestions(@Nullable String value) {
      return Objects.equals(this.suggestions, value)
         ? this
         : new ImmutableArgumentDescriptor(this, this.parameter, this.name, this.parserName, value, this.defaultValue, this.description);
   }

   public final ImmutableArgumentDescriptor withDefaultValue(@Nullable DefaultValue<?, ?> value) {
      return this.defaultValue == value
         ? this
         : new ImmutableArgumentDescriptor(this, this.parameter, this.name, this.parserName, this.suggestions, value, this.description);
   }

   public final ImmutableArgumentDescriptor withDescription(@Nullable Description value) {
      return this.description == value
         ? this
         : new ImmutableArgumentDescriptor(this, this.parameter, this.name, this.parserName, this.suggestions, this.defaultValue, value);
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableArgumentDescriptor && this.equalsByValue((ImmutableArgumentDescriptor)another);
   }

   private boolean equalsByValue(ImmutableArgumentDescriptor another) {
      return this.parameter.equals(another.parameter)
         && this.name.equals(another.name)
         && Objects.equals(this.parserName, another.parserName)
         && Objects.equals(this.suggestions, another.suggestions)
         && Objects.equals(this.defaultValue, another.defaultValue)
         && Objects.equals(this.description, another.description);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.parameter.hashCode();
      h += (h << 5) + this.name.hashCode();
      h += (h << 5) + Objects.hashCode(this.parserName);
      h += (h << 5) + Objects.hashCode(this.suggestions);
      h += (h << 5) + Objects.hashCode(this.defaultValue);
      return h + (h << 5) + Objects.hashCode(this.description);
   }

   @Override
   public String toString() {
      return "ArgumentDescriptor{parameter="
         + this.parameter
         + ", name="
         + this.name
         + ", parserName="
         + this.parserName
         + ", suggestions="
         + this.suggestions
         + ", defaultValue="
         + this.defaultValue
         + ", description="
         + this.description
         + "}";
   }

   public static ImmutableArgumentDescriptor of(
      @NonNull Parameter parameter,
      @NonNull String name,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable DefaultValue<?, ?> defaultValue,
      @Nullable Description description
   ) {
      return new ImmutableArgumentDescriptor(parameter, name, parserName, suggestions, defaultValue, description);
   }

   public static ImmutableArgumentDescriptor copyOf(ArgumentDescriptor instance) {
      return instance instanceof ImmutableArgumentDescriptor ? (ImmutableArgumentDescriptor)instance : builder().from(instance).build();
   }

   public static ImmutableArgumentDescriptor.Builder builder() {
      return new ImmutableArgumentDescriptor.Builder();
   }

   @Generated(
      from = "ArgumentDescriptor",
      generator = "Immutables"
   )
   public static final class Builder {
      private static final long INIT_BIT_PARAMETER = 1L;
      private static final long INIT_BIT_NAME = 2L;
      private long initBits = 3L;
      @NonNull
      private Parameter parameter;
      @NonNull
      private String name;
      @Nullable
      private String parserName;
      @Nullable
      private String suggestions;
      @Nullable
      private DefaultValue<?, ?> defaultValue;
      @Nullable
      private Description description;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder from(ArgumentDescriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder from(Descriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      private void mergeInternal(Object object) {
         long bits = 0L;
         if (object instanceof ArgumentDescriptor) {
            ArgumentDescriptor instance = (ArgumentDescriptor)object;
            if ((bits & 1L) == 0L) {
               this.name(instance.name());
               bits |= 1L;
            }

            Description descriptionValue = instance.description();
            if (descriptionValue != null) {
               this.description(descriptionValue);
            }

            String suggestionsValue = instance.suggestions();
            if (suggestionsValue != null) {
               this.suggestions(suggestionsValue);
            }

            String parserNameValue = instance.parserName();
            if (parserNameValue != null) {
               this.parserName(parserNameValue);
            }

            DefaultValue<?, ?> defaultValueValue = instance.defaultValue();
            if (defaultValueValue != null) {
               this.defaultValue(defaultValueValue);
            }

            this.parameter(instance.parameter());
         }

         if (object instanceof Descriptor) {
            Descriptor instancex = (Descriptor)object;
            if ((bits & 1L) == 0L) {
               this.name(instancex.name());
               bits |= 1L;
            }
         }
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder parameter(@NonNull Parameter parameter) {
         this.parameter = Objects.requireNonNull(parameter, "parameter");
         this.initBits &= -2L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder name(@NonNull String name) {
         this.name = Objects.requireNonNull(name, "name");
         this.initBits &= -3L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder parserName(@Nullable String parserName) {
         this.parserName = parserName;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder suggestions(@Nullable String suggestions) {
         this.suggestions = suggestions;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder defaultValue(@Nullable DefaultValue<?, ?> defaultValue) {
         this.defaultValue = defaultValue;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableArgumentDescriptor.Builder description(@Nullable Description description) {
         this.description = description;
         return this;
      }

      public ImmutableArgumentDescriptor build() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         } else {
            return new ImmutableArgumentDescriptor(null, this.parameter, this.name, this.parserName, this.suggestions, this.defaultValue, this.description);
         }
      }

      private String formatRequiredAttributesMessage() {
         List<String> attributes = new ArrayList<>();
         if ((this.initBits & 1L) != 0L) {
            attributes.add("parameter");
         }

         if ((this.initBits & 2L) != 0L) {
            attributes.add("name");
         }

         return "Cannot build ArgumentDescriptor, some of required attributes are not set " + attributes;
      }
   }
}
