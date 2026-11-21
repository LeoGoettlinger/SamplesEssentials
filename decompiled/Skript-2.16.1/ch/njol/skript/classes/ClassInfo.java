package ch.njol.skript.classes;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.iterator.ArrayIterator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@SuppressFBWarnings({"DM_STRING_VOID_CTOR"})
public class ClassInfo<T> implements Debuggable {
   private final Class<T> c;
   private final String codeName;
   private final Noun name;
   @Nullable
   private DefaultExpression<T> defaultExpression = null;
   @Nullable
   private Parser<? extends T> parser = null;
   @Nullable
   private Cloner<T> cloner = null;
   @Nullable
   Pattern[] userInputPatterns = null;
   @Nullable
   private Changer<? super T> changer = null;
   @Nullable
   private Supplier<Iterator<T>> supplier = null;
   @Nullable
   private Serializer<? super T> serializer = null;
   @Nullable
   private Class<?> serializeAs = null;
   @Nullable
   private Class<?> mathRelativeType = null;
   @Nullable
   private String docName = null;
   @Nullable
   private String[] description = null;
   @Nullable
   private String[] usage = null;
   @Nullable
   private String[] examples = null;
   @Nullable
   private String since = null;
   @Nullable
   private String[] requiredPlugins = null;
   @Nullable
   private String documentationId = null;
   public static final String NO_DOC = new String();
   @Nullable
   private Set<String> before;
   private final Set<String> after = new HashSet<>();
   private final Map<Property<?>, Property.PropertyInfo<?>> propertyInfos = new HashMap<>();
   private final Map<Property<?>, ClassInfo.PropertyDocs> propertyDocumentation = new HashMap<>();

   public ClassInfo(Class<T> c, String codeName) {
      this.c = c;
      if (!isValidCodeName(codeName)) {
         throw new IllegalArgumentException("Code names for classes must be lowercase and only consist of latin letters and arabic numbers");
      } else {
         this.codeName = codeName;
         this.name = new Noun("types." + codeName);
      }
   }

   public static boolean isValidCodeName(String name) {
      return name.matches("(?:any-)?[a-z0-9]+");
   }

   public ClassInfo<T> parser(Parser<? extends T> parser) {
      assert this.parser == null;

      this.parser = parser;
      return this;
   }

   public ClassInfo<T> cloner(Cloner<T> cloner) {
      assert this.cloner == null;

      this.cloner = cloner;
      return this;
   }

   public ClassInfo<T> user(String... userInputPatterns) throws PatternSyntaxException {
      assert this.userInputPatterns == null;

      this.userInputPatterns = new Pattern[userInputPatterns.length];

      for (int i = 0; i < userInputPatterns.length; i++) {
         assert this.userInputPatterns != null;

         this.userInputPatterns[i] = Pattern.compile(userInputPatterns[i]);
      }

      return this;
   }

   public ClassInfo<T> defaultExpression(DefaultExpression<T> defaultExpression) {
      assert this.defaultExpression == null;

      if (!defaultExpression.isDefault()) {
         throw new IllegalArgumentException("defaultExpression.isDefault() must return true for the default expression of a class");
      } else {
         this.defaultExpression = defaultExpression;
         return this;
      }
   }

   public ClassInfo<T> supplier(Supplier<Iterator<T>> supplier) {
      if (this.supplier != null) {
         throw new SkriptAPIException("supplier of this class is already set");
      } else {
         this.supplier = supplier;
         return this;
      }
   }

   public ClassInfo<T> supplier(T[] values) {
      return this.supplier(() -> new ArrayIterator<>(values));
   }

   public ClassInfo<T> serializer(Serializer<? super T> serializer) {
      assert this.serializer == null;

      if (this.serializeAs != null) {
         throw new IllegalStateException("Can't set a serializer if this class is set to be serialized as another one");
      } else {
         this.serializer = serializer;
         serializer.register(this);
         return this;
      }
   }

   public ClassInfo<T> serializeAs(Class<?> serializeAs) {
      assert this.serializeAs == null;

      if (this.serializer != null) {
         throw new IllegalStateException("Can't set this class to be serialized as another one if a serializer is already set");
      } else {
         this.serializeAs = serializeAs;
         return this;
      }
   }

   public ClassInfo<T> changer(Changer<? super T> changer) {
      assert this.changer == null;

      this.changer = changer;
      return this;
   }

   public ClassInfo<T> name(String name) {
      assert this.docName == null;

      this.docName = name;
      return this;
   }

   public ClassInfo<T> description(String... description) {
      assert this.description == null;

      this.description = description;
      return this;
   }

   public ClassInfo<T> usage(String... usage) {
      assert this.usage == null;

      this.usage = usage;
      return this;
   }

   public ClassInfo<T> examples(String... examples) {
      assert this.examples == null;

      this.examples = examples;
      return this;
   }

   public ClassInfo<T> since(String since) {
      assert this.since == null;

      this.since = since;
      return this;
   }

   public ClassInfo<T> requiredPlugins(String... pluginNames) {
      assert this.requiredPlugins == null;

      this.requiredPlugins = pluginNames;
      return this;
   }

   public ClassInfo<T> documentationId(String id) {
      assert this.documentationId == null;

      this.documentationId = id;
      return this;
   }

   public Class<T> getC() {
      return this.c;
   }

   public Noun getName() {
      return this.name;
   }

   public String getCodeName() {
      return this.codeName;
   }

   @Nullable
   public DefaultExpression<T> getDefaultExpression() {
      return this.defaultExpression;
   }

   @Nullable
   public Parser<? extends T> getParser() {
      return this.parser;
   }

   @Nullable
   public Cloner<? extends T> getCloner() {
      return this.cloner;
   }

   public T clone(T t) {
      return this.cloner == null ? t : this.cloner.clone(t);
   }

   @Nullable
   public Pattern[] getUserInputPatterns() {
      return this.userInputPatterns;
   }

   public boolean matchesUserInput(String input) {
      if (this.userInputPatterns == null) {
         return false;
      } else {
         for (Pattern typePattern : this.userInputPatterns) {
            if (typePattern.matcher(input).matches()) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public Changer<? super T> getChanger() {
      return this.changer;
   }

   @Nullable
   public Supplier<Iterator<T>> getSupplier() {
      if (this.supplier == null && this.c.isEnum()) {
         this.supplier = () -> new ArrayIterator<>(this.c.getEnumConstants());
      }

      return this.supplier;
   }

   @Nullable
   public Serializer<? super T> getSerializer() {
      return this.serializer;
   }

   @Nullable
   public Class<?> getSerializeAs() {
      return this.serializeAs;
   }

   @Nullable
   public String[] getDescription() {
      return this.description;
   }

   @Nullable
   public String[] getUsage() {
      return this.usage;
   }

   @Nullable
   public String[] getExamples() {
      return this.examples;
   }

   @Nullable
   public String getSince() {
      return this.since;
   }

   @Nullable
   public String getDocName() {
      return this.docName;
   }

   @Nullable
   public String[] getRequiredPlugins() {
      return this.requiredPlugins;
   }

   @Nullable
   public String getDocumentationID() {
      return this.documentationId;
   }

   public boolean hasDocs() {
      return this.getDocName() != null && !NO_DOC.equals(this.getDocName());
   }

   public ClassInfo<T> before(String... before) {
      assert this.before == null;

      this.before = new HashSet<>(Arrays.asList(before));
      return this;
   }

   public ClassInfo<T> after(String... after) {
      this.after.addAll(Arrays.asList(after));
      return this;
   }

   @Nullable
   public Set<String> before() {
      return this.before;
   }

   public Set<String> after() {
      return this.after;
   }

   @NotNull
   @Override
   public String toString() {
      return this.getName().getSingular();
   }

   public String toString(int flags) {
      return this.getName().toString(flags);
   }

   @NotNull
   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return debug ? this.codeName + " (" + this.c.getCanonicalName() + ")" : this.getName().getSingular();
   }

   @Experimental
   public <Handler extends PropertyHandler<T>> ClassInfo<T> property(
      Property<? super Handler> property, String description, SkriptAddon addon, @NotNull Handler handler
   ) {
      if (this.propertyInfos.containsKey(property)) {
         throw new IllegalStateException("Property " + property.name() + " is already registered for the " + this.c.getName() + " type.");
      } else {
         this.propertyInfos.put(property, new Property.PropertyInfo<>(property, handler));
         Classes.hasProperty(property, this);
         this.propertyDocumentation.put(property, new ClassInfo.PropertyDocs(property, description, addon));
         return this;
      }
   }

   @Experimental
   public boolean hasProperty(Property<?> property) {
      return this.propertyInfos.containsKey(property);
   }

   @Experimental
   @Unmodifiable
   public Collection<Property<?>> getAllProperties() {
      return Collections.unmodifiableCollection(this.propertyInfos.keySet());
   }

   @Experimental
   @Nullable
   public <Handler extends PropertyHandler<?>> Property.PropertyInfo<Handler> getPropertyInfo(Property<Handler> property) {
      return !this.propertyInfos.containsKey(property) ? null : (Property.PropertyInfo)this.propertyInfos.get(property);
   }

   @Experimental
   public ClassInfo.PropertyDocs getPropertyDocumentation(Property<?> property) {
      return this.propertyDocumentation.get(property);
   }

   @Experimental
   public record PropertyDocs(Property<?> property, String description, SkriptAddon provider) {
   }
}
