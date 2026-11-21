package de.theredend2000.advancedhunt.lib.cloud.parser.flag;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.component.TypedCommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.description.Description;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.permission.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public final class CommandFlag<T> {
   @NonNull
   private final String name;
   @NonNull
   private final String[] aliases;
   @NonNull
   private final Description description;
   @NonNull
   private final Permission permission;
   @NonNull
   private final CommandFlag.FlagMode mode;
   @Nullable
   private final TypedCommandComponent<?, T> commandComponent;

   private CommandFlag(
      @NonNull final String name,
      @NonNull final String[] aliases,
      @NonNull final Description description,
      @NonNull final Permission permission,
      @Nullable final TypedCommandComponent<?, T> commandComponent,
      @NonNull final CommandFlag.FlagMode mode
   ) {
      this.name = Objects.requireNonNull(name, "name cannot be null");
      this.aliases = Objects.requireNonNull(aliases, "aliases cannot be null");
      this.description = Objects.requireNonNull(description, "description cannot be null");
      this.permission = Objects.requireNonNull(permission, "permission cannot be null");
      this.commandComponent = commandComponent;
      this.mode = Objects.requireNonNull(mode, "mode cannot be null");
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandFlag.Builder<C, Void> builder(@NonNull final String name) {
      return new CommandFlag.Builder<>(name);
   }

   @NonNull
   public String name() {
      return this.name;
   }

   @NonNull
   public Collection<String> aliases() {
      return Arrays.asList(this.aliases);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CommandFlag.FlagMode mode() {
      return this.mode;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public Description description() {
      return this.description;
   }

   @API(
      status = API.Status.STABLE
   )
   @Nullable
   public CommandComponent<?> commandComponent() {
      return this.commandComponent;
   }

   @API(
      status = API.Status.STABLE
   )
   public Permission permission() {
      return this.permission;
   }

   @Override
   public String toString() {
      return String.format("--%s", this.name);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandFlag<?> that = (CommandFlag<?>)o;
         return this.name().equals(that.name());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name());
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class Builder<C, T> {
      private final String name;
      private final String[] aliases;
      private final Description description;
      private final Permission permission;
      private final TypedCommandComponent<C, T> commandComponent;
      private final CommandFlag.FlagMode mode;

      private Builder(
         @NonNull final String name,
         final String[] aliases,
         @NonNull final Description description,
         @NonNull final Permission permission,
         @Nullable final TypedCommandComponent<C, T> commandComponent,
         @NonNull final CommandFlag.FlagMode mode
      ) {
         this.name = name;
         this.aliases = aliases;
         this.description = description;
         this.permission = permission;
         this.commandComponent = commandComponent;
         this.mode = mode;
      }

      private Builder(@NonNull final String name) {
         this(name, new String[0], Description.empty(), Permission.empty(), null, CommandFlag.FlagMode.SINGLE);
      }

      @NonNull
      public CommandFlag.Builder<C, T> withAliases(final String... aliases) {
         return this.withAliases(Arrays.asList(aliases));
      }

      @API(
         status = API.Status.STABLE
      )
      @NonNull
      public CommandFlag.Builder<C, T> withAliases(@NonNull final Collection<String> aliases) {
         Set<String> filteredAliases = new HashSet<>();

         for (String alias : aliases) {
            if (!alias.isEmpty()) {
               if (alias.length() > 1) {
                  throw new IllegalArgumentException(String.format("Alias '%s' has name longer than one character. This is not allowed", alias));
               }

               filteredAliases.add(alias);
            }
         }

         return new CommandFlag.Builder<>(
            this.name, filteredAliases.toArray(new String[0]), this.description, this.permission, this.commandComponent, this.mode
         );
      }

      @API(
         status = API.Status.STABLE
      )
      @NonNull
      public CommandFlag.Builder<C, T> withDescription(@NonNull final Description description) {
         return new CommandFlag.Builder<>(this.name, this.aliases, description, this.permission, this.commandComponent, this.mode);
      }

      @NonNull
      public <N> CommandFlag.Builder<C, N> withComponent(@NonNull final TypedCommandComponent<C, N> component) {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, this.permission, component, this.mode);
      }

      @NonNull
      public <N> CommandFlag.Builder<C, N> withComponent(@NonNull final ParserDescriptor<? super C, N> parserDescriptor) {
         return this.withComponent(CommandComponent.builder(this.name, parserDescriptor));
      }

      @NonNull
      public <N> CommandFlag.Builder<C, N> withComponent(@NonNull final CommandComponent.Builder<C, N> builder) {
         return this.withComponent(builder.build());
      }

      @API(
         status = API.Status.STABLE
      )
      @NonNull
      public CommandFlag.Builder<C, T> withPermission(@NonNull final Permission permission) {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, permission, this.commandComponent, this.mode);
      }

      @API(
         status = API.Status.STABLE
      )
      @NonNull
      public CommandFlag.Builder<C, T> withPermission(@NonNull final String permissionString) {
         return this.withPermission(Permission.of(permissionString));
      }

      @API(
         status = API.Status.STABLE
      )
      @NonNull
      public CommandFlag.Builder<C, T> asRepeatable() {
         return new CommandFlag.Builder<>(this.name, this.aliases, this.description, this.permission, this.commandComponent, CommandFlag.FlagMode.REPEATABLE);
      }

      @NonNull
      public CommandFlag<T> build() {
         return new CommandFlag<>(this.name, this.aliases, this.description, this.permission, this.commandComponent, this.mode);
      }
   }

   @API(
      status = API.Status.STABLE
   )
   public static enum FlagMode {
      SINGLE,
      REPEATABLE;
   }
}
