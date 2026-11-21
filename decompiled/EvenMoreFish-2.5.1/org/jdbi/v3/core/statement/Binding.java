package org.jdbi.v3.core.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.TypedValue;
import org.jdbi.v3.core.qualifier.QualifiedType;

public class Binding {
   protected final Map<Integer, Object> positionals = new TreeMap<>();
   protected final Map<String, Object> named = new LinkedHashMap<>();
   protected final List<NamedArgumentFinder> namedArgumentFinder = new ArrayList<>();
   private final ArgumentBinder.ArgumentFactoryLocator locator;

   protected Binding(StatementContext ctx) {
      this.locator = new ArgumentBinder.ArgumentFactoryLocator(ctx);
   }

   public void addPositional(int position, Argument argument) {
      this.addPositional(position, (Object)argument);
   }

   public void addNamed(String name, Argument argument) {
      this.addNamed(name, (Object)argument);
   }

   public void addPositional(int position, Object argument) {
      this.positionals.put(position, argument);
   }

   public void addNamed(String name, Object argument) {
      this.named.put(name, argument);
   }

   public void addPositional(int position, Object argument, QualifiedType<?> type) {
      this.positionals.put(position, new TypedValue(type, argument));
   }

   public void addNamed(String name, Object argument, QualifiedType<?> type) {
      this.named.put(name, new TypedValue(type, argument));
   }

   public void addNamedArgumentFinder(NamedArgumentFinder args) {
      this.namedArgumentFinder.add(args);
   }

   @Deprecated(
      since = "3.11.0",
      forRemoval = true
   )
   public Optional<Argument> findForName(String name, StatementContext ctx2) {
      Object found = this.named.get(name);
      return found == null && !this.named.containsKey(name)
         ? this.namedArgumentFinder.stream().flatMap(arguments -> arguments.find(name, ctx2).stream()).findFirst()
         : Optional.of(this.locator.argumentFactoryForType(this.locator.typeOf(found)).apply(ArgumentBinder.unwrap(found)));
   }

   @Deprecated(
      since = "3.11.0",
      forRemoval = true
   )
   public Collection<String> getNames() {
      Set<String> names = new HashSet<>(this.named.keySet());
      this.namedArgumentFinder.forEach(args -> names.addAll(args.getNames()));
      return Collections.unmodifiableSet(names);
   }

   @Deprecated(
      since = "3.11.0",
      forRemoval = true
   )
   public Optional<Argument> findForPosition(int position) {
      Object found = this.positionals.get(position);
      return Optional.ofNullable(this.locator.argumentFactoryForType(this.locator.typeOf(found)).apply(ArgumentBinder.unwrap(found)));
   }

   @Override
   public String toString() {
      return this.describe(Integer.MAX_VALUE);
   }

   public String describe(final int maxLength) {
      boolean firstSection = true;
      boolean firstElem = true;
      StringBuilder buf = new StringBuilder();
      buf.append('{');
      if (!this.positionals.isEmpty()) {
         firstSection = false;
         buf.append("pos:{");

         for (Entry<Integer, Object> e : this.positionals.entrySet()) {
            if (!firstElem) {
               buf.append(',');
            }

            firstElem = false;
            buf.append(e.getKey()).append(':').append(ArgumentBinder.unwrap(e.getValue()));
            if (buf.length() > maxLength) {
               break;
            }
         }

         buf.append('}');
      }

      if (!this.named.isEmpty() && buf.length() < maxLength) {
         if (!firstSection) {
            buf.append(", ");
         }

         firstSection = false;
         firstElem = true;
         buf.append("named:{");

         for (Entry<String, Object> e : this.named.entrySet()) {
            if (!firstElem) {
               buf.append(',');
            }

            firstElem = false;
            buf.append(e.getKey()).append(':').append(ArgumentBinder.unwrap(e.getValue()));
            if (buf.length() > maxLength) {
               break;
            }
         }

         buf.append('}');
      }

      if (!this.namedArgumentFinder.isEmpty() && buf.length() < maxLength) {
         if (!firstSection) {
            buf.append(", ");
         }

         firstElem = true;
         buf.append("finder:[");

         for (NamedArgumentFinder naf : this.namedArgumentFinder) {
            if (!firstElem) {
               buf.append(',');
            }

            firstElem = false;
            buf.append(naf);
            if (buf.length() > maxLength) {
               break;
            }
         }

         buf.append('}');
      }

      if (buf.length() > maxLength - 2) {
         buf.setLength(maxLength - 2);
         buf.append('…');
      }

      return buf + "}";
   }

   public void clear() {
      this.positionals.clear();
      this.named.clear();
      this.namedArgumentFinder.clear();
   }

   public boolean isEmpty() {
      return this.positionals.isEmpty() && this.named.isEmpty() && this.namedArgumentFinder.isEmpty();
   }
}
