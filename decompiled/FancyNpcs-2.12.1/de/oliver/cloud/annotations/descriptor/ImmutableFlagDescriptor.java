package de.oliver.cloud.annotations.descriptor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.oliver.cloud.description.Description;
import de.oliver.cloud.permission.Permission;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
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
   from = "FlagDescriptor",
   generator = "Immutables"
)
public final class ImmutableFlagDescriptor implements FlagDescriptor {
   @NonNull
   private final Parameter parameter;
   @NonNull
   private final String name;
   @NonNull
   private final Collection<String> aliases;
   @Nullable
   private final String parserName;
   @Nullable
   private final String suggestions;
   @Nullable
   private final Permission permission;
   @Nullable
   private final Description description;
   private final boolean repeatable;

   private ImmutableFlagDescriptor(
      @NonNull Parameter parameter,
      @NonNull String name,
      @NonNull Collection<String> aliases,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable Permission permission,
      @Nullable Description description,
      boolean repeatable
   ) {
      this.parameter = Objects.requireNonNull(parameter, "parameter");
      this.name = Objects.requireNonNull(name, "name");
      this.aliases = Objects.requireNonNull(aliases, "aliases");
      this.parserName = parserName;
      this.suggestions = suggestions;
      this.permission = permission;
      this.description = description;
      this.repeatable = repeatable;
   }

   private ImmutableFlagDescriptor(
      ImmutableFlagDescriptor original,
      @NonNull Parameter parameter,
      @NonNull String name,
      @NonNull Collection<String> aliases,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable Permission permission,
      @Nullable Description description,
      boolean repeatable
   ) {
      this.parameter = parameter;
      this.name = name;
      this.aliases = aliases;
      this.parserName = parserName;
      this.suggestions = suggestions;
      this.permission = permission;
      this.description = description;
      this.repeatable = repeatable;
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

   @NonNull
   @Override
   public Collection<String> aliases() {
      return this.aliases;
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
   public Permission permission() {
      return this.permission;
   }

   @Nullable
   @Override
   public Description description() {
      return this.description;
   }

   @Override
   public boolean repeatable() {
      return this.repeatable;
   }

   public final ImmutableFlagDescriptor withParameter(Parameter value) {
      if (this.parameter == value) {
         return this;
      } else {
         Parameter newValue = Objects.requireNonNull(value, "parameter");
         return new ImmutableFlagDescriptor(
            this, newValue, this.name, this.aliases, this.parserName, this.suggestions, this.permission, this.description, this.repeatable
         );
      }
   }

   public final ImmutableFlagDescriptor withName(String value) {
      String newValue = Objects.requireNonNull(value, "name");
      return this.name.equals(newValue)
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, newValue, this.aliases, this.parserName, this.suggestions, this.permission, this.description, this.repeatable
         );
   }

   public final ImmutableFlagDescriptor withAliases(Collection<String> value) {
      if (this.aliases == value) {
         return this;
      } else {
         Collection<String> newValue = Objects.requireNonNull(value, "aliases");
         return new ImmutableFlagDescriptor(
            this, this.parameter, this.name, newValue, this.parserName, this.suggestions, this.permission, this.description, this.repeatable
         );
      }
   }

   public final ImmutableFlagDescriptor withParserName(@Nullable String value) {
      return Objects.equals(this.parserName, value)
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, this.name, this.aliases, value, this.suggestions, this.permission, this.description, this.repeatable
         );
   }

   public final ImmutableFlagDescriptor withSuggestions(@Nullable String value) {
      return Objects.equals(this.suggestions, value)
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, this.name, this.aliases, this.parserName, value, this.permission, this.description, this.repeatable
         );
   }

   public final ImmutableFlagDescriptor withPermission(@Nullable Permission value) {
      return this.permission == value
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, this.name, this.aliases, this.parserName, this.suggestions, value, this.description, this.repeatable
         );
   }

   public final ImmutableFlagDescriptor withDescription(@Nullable Description value) {
      return this.description == value
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, this.name, this.aliases, this.parserName, this.suggestions, this.permission, value, this.repeatable
         );
   }

   public final ImmutableFlagDescriptor withRepeatable(boolean value) {
      return this.repeatable == value
         ? this
         : new ImmutableFlagDescriptor(
            this, this.parameter, this.name, this.aliases, this.parserName, this.suggestions, this.permission, this.description, value
         );
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableFlagDescriptor && this.equalsByValue((ImmutableFlagDescriptor)another);
   }

   private boolean equalsByValue(ImmutableFlagDescriptor another) {
      return this.parameter.equals(another.parameter)
         && this.name.equals(another.name)
         && this.aliases.equals(another.aliases)
         && Objects.equals(this.parserName, another.parserName)
         && Objects.equals(this.suggestions, another.suggestions)
         && Objects.equals(this.permission, another.permission)
         && Objects.equals(this.description, another.description)
         && this.repeatable == another.repeatable;
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.parameter.hashCode();
      h += (h << 5) + this.name.hashCode();
      h += (h << 5) + this.aliases.hashCode();
      h += (h << 5) + Objects.hashCode(this.parserName);
      h += (h << 5) + Objects.hashCode(this.suggestions);
      h += (h << 5) + Objects.hashCode(this.permission);
      h += (h << 5) + Objects.hashCode(this.description);
      return h + (h << 5) + Boolean.hashCode(this.repeatable);
   }

   @Override
   public String toString() {
      return "FlagDescriptor{parameter="
         + this.parameter
         + ", name="
         + this.name
         + ", aliases="
         + this.aliases
         + ", parserName="
         + this.parserName
         + ", suggestions="
         + this.suggestions
         + ", permission="
         + this.permission
         + ", description="
         + this.description
         + ", repeatable="
         + this.repeatable
         + "}";
   }

   public static ImmutableFlagDescriptor of(
      @NonNull Parameter parameter,
      @NonNull String name,
      @NonNull Collection<String> aliases,
      @Nullable String parserName,
      @Nullable String suggestions,
      @Nullable Permission permission,
      @Nullable Description description,
      boolean repeatable
   ) {
      return new ImmutableFlagDescriptor(parameter, name, aliases, parserName, suggestions, permission, description, repeatable);
   }

   public static ImmutableFlagDescriptor copyOf(FlagDescriptor instance) {
      return instance instanceof ImmutableFlagDescriptor ? (ImmutableFlagDescriptor)instance : builder().from(instance).build();
   }

   public static ImmutableFlagDescriptor.Builder builder() {
      return new ImmutableFlagDescriptor.Builder();
   }

   @Generated(
      from = "FlagDescriptor",
      generator = "Immutables"
   )
   public static final class Builder {
      private static final long INIT_BIT_PARAMETER = 1L;
      private static final long INIT_BIT_NAME = 2L;
      private static final long INIT_BIT_ALIASES = 4L;
      private static final long INIT_BIT_REPEATABLE = 8L;
      private long initBits = 15L;
      @NonNull
      private Parameter parameter;
      @NonNull
      private String name;
      @NonNull
      private Collection<String> aliases;
      @Nullable
      private String parserName;
      @Nullable
      private String suggestions;
      @Nullable
      private Permission permission;
      @Nullable
      private Description description;
      private boolean repeatable;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder from(FlagDescriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder from(Descriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      private void mergeInternal(Object object) {
         long bits = 0L;
         if (object instanceof FlagDescriptor) {
            FlagDescriptor instance = (FlagDescriptor)object;
            this.aliases(instance.aliases());
            this.repeatable(instance.repeatable());
            this.parameter(instance.parameter());
            if ((bits & 1L) == 0L) {
               this.name(instance.name());
               bits |= 1L;
            }

            Description descriptionValue = instance.description();
            if (descriptionValue != null) {
               this.description(descriptionValue);
            }

            Permission permissionValue = instance.permission();
            if (permissionValue != null) {
               this.permission(permissionValue);
            }

            String suggestionsValue = instance.suggestions();
            if (suggestionsValue != null) {
               this.suggestions(suggestionsValue);
            }

            String parserNameValue = instance.parserName();
            if (parserNameValue != null) {
               this.parserName(parserNameValue);
            }
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
      public final ImmutableFlagDescriptor.Builder parameter(@NonNull Parameter parameter) {
         this.parameter = Objects.requireNonNull(parameter, "parameter");
         this.initBits &= -2L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder name(@NonNull String name) {
         this.name = Objects.requireNonNull(name, "name");
         this.initBits &= -3L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder aliases(@NonNull Collection<String> aliases) {
         this.aliases = Objects.requireNonNull(aliases, "aliases");
         this.initBits &= -5L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder parserName(@Nullable String parserName) {
         this.parserName = parserName;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder suggestions(@Nullable String suggestions) {
         this.suggestions = suggestions;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder permission(@Nullable Permission permission) {
         this.permission = permission;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder description(@Nullable Description description) {
         this.description = description;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableFlagDescriptor.Builder repeatable(boolean repeatable) {
         this.repeatable = repeatable;
         this.initBits &= -9L;
         return this;
      }

      public ImmutableFlagDescriptor build() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         } else {
            return new ImmutableFlagDescriptor(
               null, this.parameter, this.name, this.aliases, this.parserName, this.suggestions, this.permission, this.description, this.repeatable
            );
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

         if ((this.initBits & 4L) != 0L) {
            attributes.add("aliases");
         }

         if ((this.initBits & 8L) != 0L) {
            attributes.add("repeatable");
         }

         return "Cannot build FlagDescriptor, some of required attributes are not set " + attributes;
      }
   }
}
