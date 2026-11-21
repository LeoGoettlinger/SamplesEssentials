package de.oliver.cloud.annotations.descriptor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.oliver.cloud.annotations.SyntaxFragment;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@API(
   status = Status.STABLE,
   consumers = {"de.oliver.cloud.*"}
)
@Generated(
   from = "CommandDescriptor",
   generator = "Immutables"
)
public final class ImmutableCommandDescriptor implements CommandDescriptor {
   @NonNull
   private final Method method;
   @NonNull
   private final String name;
   @NonNull
   private final List<SyntaxFragment> syntax;
   @NonNull
   private final String commandToken;
   @NonNull
   private final Class<?> requiredSender;

   private ImmutableCommandDescriptor(
      @NonNull Method method, @NonNull String name, Iterable<? extends SyntaxFragment> syntax, @NonNull String commandToken, @NonNull Class<?> requiredSender
   ) {
      this.method = Objects.requireNonNull(method, "method");
      this.name = Objects.requireNonNull(name, "name");
      this.syntax = createUnmodifiableList(false, createSafeList(syntax, true, false));
      this.commandToken = Objects.requireNonNull(commandToken, "commandToken");
      this.requiredSender = Objects.requireNonNull(requiredSender, "requiredSender");
   }

   private ImmutableCommandDescriptor(ImmutableCommandDescriptor.Builder builder) {
      this.method = builder.method;
      this.syntax = builder.syntax == null ? Collections.emptyList() : createUnmodifiableList(true, builder.syntax);
      this.commandToken = builder.commandToken;
      this.requiredSender = builder.requiredSender;
      this.name = builder.name != null ? builder.name : Objects.requireNonNull(CommandDescriptor.super.name(), "name");
   }

   private ImmutableCommandDescriptor(
      ImmutableCommandDescriptor original,
      @NonNull Method method,
      @NonNull String name,
      @NonNull List<SyntaxFragment> syntax,
      @NonNull String commandToken,
      @NonNull Class<?> requiredSender
   ) {
      this.method = method;
      this.name = name;
      this.syntax = syntax;
      this.commandToken = commandToken;
      this.requiredSender = requiredSender;
   }

   @NonNull
   @Override
   public Method method() {
      return this.method;
   }

   @NonNull
   @Override
   public String name() {
      return this.name;
   }

   @NonNull
   @Override
   public List<SyntaxFragment> syntax() {
      return this.syntax;
   }

   @NonNull
   @Override
   public String commandToken() {
      return this.commandToken;
   }

   @NonNull
   @Override
   public Class<?> requiredSender() {
      return this.requiredSender;
   }

   public final ImmutableCommandDescriptor withMethod(Method value) {
      if (this.method == value) {
         return this;
      } else {
         Method newValue = Objects.requireNonNull(value, "method");
         return new ImmutableCommandDescriptor(this, newValue, this.name, this.syntax, this.commandToken, this.requiredSender);
      }
   }

   public final ImmutableCommandDescriptor withName(String value) {
      String newValue = Objects.requireNonNull(value, "name");
      return this.name.equals(newValue)
         ? this
         : new ImmutableCommandDescriptor(this, this.method, newValue, this.syntax, this.commandToken, this.requiredSender);
   }

   public final ImmutableCommandDescriptor withSyntax(SyntaxFragment... elements) {
      List<SyntaxFragment> newValue = createUnmodifiableList(false, createSafeList(Arrays.asList(elements), true, false));
      return new ImmutableCommandDescriptor(this, this.method, this.name, newValue, this.commandToken, this.requiredSender);
   }

   public final ImmutableCommandDescriptor withSyntax(Iterable<? extends SyntaxFragment> elements) {
      if (this.syntax == elements) {
         return this;
      } else {
         List<SyntaxFragment> newValue = createUnmodifiableList(false, createSafeList(elements, true, false));
         return new ImmutableCommandDescriptor(this, this.method, this.name, newValue, this.commandToken, this.requiredSender);
      }
   }

   public final ImmutableCommandDescriptor withCommandToken(String value) {
      String newValue = Objects.requireNonNull(value, "commandToken");
      return this.commandToken.equals(newValue)
         ? this
         : new ImmutableCommandDescriptor(this, this.method, this.name, this.syntax, newValue, this.requiredSender);
   }

   public final ImmutableCommandDescriptor withRequiredSender(Class<?> value) {
      if (this.requiredSender == value) {
         return this;
      } else {
         Class<?> newValue = Objects.requireNonNull(value, "requiredSender");
         return new ImmutableCommandDescriptor(this, this.method, this.name, this.syntax, this.commandToken, newValue);
      }
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableCommandDescriptor && this.equalsByValue((ImmutableCommandDescriptor)another);
   }

   private boolean equalsByValue(ImmutableCommandDescriptor another) {
      return this.method.equals(another.method)
         && this.name.equals(another.name)
         && this.syntax.equals(another.syntax)
         && this.commandToken.equals(another.commandToken)
         && this.requiredSender.equals(another.requiredSender);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.method.hashCode();
      h += (h << 5) + this.name.hashCode();
      h += (h << 5) + this.syntax.hashCode();
      h += (h << 5) + this.commandToken.hashCode();
      return h + (h << 5) + this.requiredSender.hashCode();
   }

   @Override
   public String toString() {
      return "CommandDescriptor{method="
         + this.method
         + ", name="
         + this.name
         + ", syntax="
         + this.syntax
         + ", commandToken="
         + this.commandToken
         + ", requiredSender="
         + this.requiredSender
         + "}";
   }

   public static ImmutableCommandDescriptor of(
      @NonNull Method method, @NonNull String name, @NonNull List<SyntaxFragment> syntax, @NonNull String commandToken, @NonNull Class<?> requiredSender
   ) {
      return of(method, name, syntax, commandToken, requiredSender);
   }

   public static ImmutableCommandDescriptor of(
      @NonNull Method method, @NonNull String name, Iterable<? extends SyntaxFragment> syntax, @NonNull String commandToken, @NonNull Class<?> requiredSender
   ) {
      return new ImmutableCommandDescriptor(method, name, syntax, commandToken, requiredSender);
   }

   public static ImmutableCommandDescriptor copyOf(CommandDescriptor instance) {
      return instance instanceof ImmutableCommandDescriptor ? (ImmutableCommandDescriptor)instance : builder().from(instance).build();
   }

   public static ImmutableCommandDescriptor.Builder builder() {
      return new ImmutableCommandDescriptor.Builder();
   }

   private static <T> List<T> createSafeList(Iterable<? extends T> iterable, boolean checkNulls, boolean skipNulls) {
      ArrayList<T> list;
      if (iterable instanceof Collection) {
         int size = ((Collection)iterable).size();
         if (size == 0) {
            return Collections.emptyList();
         }

         list = new ArrayList<>(size);
      } else {
         list = new ArrayList<>();
      }

      for (T element : iterable) {
         if (!skipNulls || element != null) {
            if (checkNulls) {
               Objects.requireNonNull(element, "element");
            }

            list.add(element);
         }
      }

      return list;
   }

   private static <T> List<T> createUnmodifiableList(boolean clone, List<? extends T> list) {
      switch (list.size()) {
         case 0:
            return Collections.emptyList();
         case 1:
            return Collections.singletonList((T)list.get(0));
         default:
            if (clone) {
               return Collections.unmodifiableList(new ArrayList<>(list));
            } else {
               if (list instanceof ArrayList) {
                  ((ArrayList)list).trimToSize();
               }

               return Collections.unmodifiableList(list);
            }
      }
   }

   @Generated(
      from = "CommandDescriptor",
      generator = "Immutables"
   )
   public static final class Builder {
      private static final long INIT_BIT_METHOD = 1L;
      private static final long INIT_BIT_COMMAND_TOKEN = 2L;
      private static final long INIT_BIT_REQUIRED_SENDER = 4L;
      private long initBits = 7L;
      @NonNull
      private Method method;
      @NonNull
      private String name;
      private List<SyntaxFragment> syntax = null;
      @NonNull
      private String commandToken;
      @NonNull
      private Class<?> requiredSender;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder from(CommandDescriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder from(Descriptor instance) {
         Objects.requireNonNull(instance, "instance");
         this.mergeInternal(instance);
         return this;
      }

      private void mergeInternal(Object object) {
         long bits = 0L;
         if (object instanceof CommandDescriptor) {
            CommandDescriptor instance = (CommandDescriptor)object;
            if ((bits & 1L) == 0L) {
               this.name(instance.name());
               bits |= 1L;
            }

            this.addAllSyntax(instance.syntax());
            this.commandToken(instance.commandToken());
            this.method(instance.method());
            this.requiredSender(instance.requiredSender());
         }

         if (object instanceof Descriptor) {
            Descriptor instance = (Descriptor)object;
            if ((bits & 1L) == 0L) {
               this.name(instance.name());
               bits |= 1L;
            }
         }
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder method(@NonNull Method method) {
         this.method = Objects.requireNonNull(method, "method");
         this.initBits &= -2L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder name(@NonNull String name) {
         this.name = Objects.requireNonNull(name, "name");
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder addSyntax(SyntaxFragment element) {
         if (this.syntax == null) {
            this.syntax = new ArrayList<>();
         }

         this.syntax.add(Objects.requireNonNull(element, "syntax element"));
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder addSyntax(SyntaxFragment... elements) {
         if (this.syntax == null) {
            this.syntax = new ArrayList<>();
         }

         for (SyntaxFragment element : elements) {
            this.syntax.add(Objects.requireNonNull(element, "syntax element"));
         }

         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder syntax(Iterable<? extends SyntaxFragment> elements) {
         this.syntax = new ArrayList<>();
         return this.addAllSyntax(elements);
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder addAllSyntax(Iterable<? extends SyntaxFragment> elements) {
         Objects.requireNonNull(elements, "syntax element");
         if (this.syntax == null) {
            this.syntax = new ArrayList<>();
         }

         for (SyntaxFragment element : elements) {
            this.syntax.add(Objects.requireNonNull(element, "syntax element"));
         }

         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder commandToken(@NonNull String commandToken) {
         this.commandToken = Objects.requireNonNull(commandToken, "commandToken");
         this.initBits &= -3L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableCommandDescriptor.Builder requiredSender(@NonNull Class<?> requiredSender) {
         this.requiredSender = Objects.requireNonNull(requiredSender, "requiredSender");
         this.initBits &= -5L;
         return this;
      }

      public ImmutableCommandDescriptor build() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         } else {
            return new ImmutableCommandDescriptor(this);
         }
      }

      private String formatRequiredAttributesMessage() {
         List<String> attributes = new ArrayList<>();
         if ((this.initBits & 1L) != 0L) {
            attributes.add("method");
         }

         if ((this.initBits & 2L) != 0L) {
            attributes.add("commandToken");
         }

         if ((this.initBits & 4L) != 0L) {
            attributes.add("requiredSender");
         }

         return "Cannot build CommandDescriptor, some of required attributes are not set " + attributes;
      }
   }
}
