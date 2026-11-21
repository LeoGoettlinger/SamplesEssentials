package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyRegistry;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class JSONGenerator extends DocumentationGenerator {
   public static final Version JSON_VERSION = new Version(2, 0);
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
   private static final Map<Property<?>, Set<SyntaxInfo<?>>> PROPERTY_RELATED_SYNTAXES = new HashMap<>();
   private static final PropertyRegistry PROPERTY_REGISTRY = Skript.instance().registry(PropertyRegistry.class);
   @NotNull
   private final SkriptAddon source;

   @Contract("_ -> new")
   public static JSONGenerator of(@NotNull SkriptAddon source) {
      return new JSONGenerator(source);
   }

   private JSONGenerator(@NotNull SkriptAddon source) {
      super(Documentation.getDocsTemplateDirectory(), Documentation.getDocsOutputDirectory());
      Preconditions.checkNotNull(source, "addon cannot be null");
      this.source = source;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13"
   )
   public JSONGenerator(File templateDir, File outputDir) {
      super(templateDir, outputDir);
      this.source = Skript.instance();
   }

   private static JsonObject getVersion() {
      JsonObject version = new JsonObject();
      version.addProperty("major", JSON_VERSION.getMajor());
      version.addProperty("minor", JSON_VERSION.getMinor());
      return version;
   }

   private static JsonArray convertToJsonArray(@Nullable String... strings) {
      if (strings != null && strings.length != 0) {
         JsonArray jsonArray = new JsonArray();

         for (String string : strings) {
            jsonArray.add(new JsonPrimitive(string));
         }

         return jsonArray;
      } else {
         return null;
      }
   }

   private JsonObject generatedAnnotatedElement(SyntaxInfo<?> syntaxInfo) {
      Class<?> syntaxClass = syntaxInfo.type();
      Name name = syntaxClass.getAnnotation(Name.class);
      if (name != null && syntaxClass.getAnnotation(NoDoc.class) == null) {
         JsonObject syntaxJsonObject = new JsonObject();
         syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(syntaxInfo));
         syntaxJsonObject.addProperty("name", name.value());
         Since since = syntaxClass.getAnnotation(Since.class);
         syntaxJsonObject.add("since", since == null ? null : convertToJsonArray(since.value()));
         Deprecated deprecated = syntaxClass.getAnnotation(Deprecated.class);
         syntaxJsonObject.addProperty("deprecated", deprecated != null);
         Description description = syntaxClass.getAnnotation(Description.class);
         syntaxJsonObject.add("description", description == null ? null : convertToJsonArray(description.value()));
         syntaxJsonObject.add("patterns", cleanPatterns(syntaxInfo.patterns().toArray(new String[0])));
         RelatedProperty relatedProperty = syntaxClass.getAnnotation(RelatedProperty.class);
         Property<?> property = null;
         if (relatedProperty != null && (property = PROPERTY_REGISTRY.get(relatedProperty.value())) != null) {
            PROPERTY_RELATED_SYNTAXES.computeIfAbsent(property, key -> new HashSet<>()).add(syntaxInfo);
         }

         syntaxJsonObject.add("property", property == null ? null : getPropertyDetails(property));
         syntaxJsonObject.add("propertyTypes", property == null ? null : getPropertyRelatedClassInfos(property));
         if (syntaxClass.isAnnotationPresent(Examples.class)) {
            Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
            syntaxJsonObject.add("examples", convertToJsonArray(examplesAnnotation.value()));
         } else if (syntaxClass.isAnnotationPresent(Example.Examples.class)) {
            Example.Examples examplesAnnotation = syntaxClass.getAnnotation(Example.Examples.class);
            syntaxJsonObject.add("examples", convertToJsonArray(Arrays.stream(examplesAnnotation.value()).map(Example::value).toArray(String[]::new)));
         } else if (syntaxClass.isAnnotationPresent(Example.class)) {
            Example example = syntaxClass.getAnnotation(Example.class);
            syntaxJsonObject.add("examples", convertToJsonArray(example.value()));
         } else {
            syntaxJsonObject.add("examples", null);
         }

         syntaxJsonObject.add("events", this.getAnnotatedEvents(syntaxClass.getAnnotation(Events.class)));
         RequiredPlugins requirements = syntaxClass.getAnnotation(RequiredPlugins.class);
         syntaxJsonObject.add("requirements", requirements == null ? null : convertToJsonArray(requirements.value()));
         Keywords keywords = syntaxClass.getAnnotation(Keywords.class);
         syntaxJsonObject.add("keywords", keywords == null ? null : convertToJsonArray(keywords.value()));
         if (syntaxInfo instanceof DefaultSyntaxInfos.Expression<?, ?> expression) {
            syntaxJsonObject.add("returns", getExpressionReturnTypes(expression));
         }

         return syntaxJsonObject;
      } else {
         return null;
      }
   }

   @Nullable
   private JsonArray getAnnotatedEvents(Events events) {
      if (events != null && events.value() != null) {
         JsonArray array = new JsonArray();

         for (String event : events.value()) {
            JsonObject object = new JsonObject();
            List<BukkitSyntaxInfos.Event<?>> candidates = new ArrayList<>();

            for (BukkitSyntaxInfos.Event<?> info : this.source.syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY)) {
               String infoName = info.name().toLowerCase(Locale.ENGLISH);
               if (infoName.startsWith("on ")) {
                  infoName = infoName.substring(3);
               }

               if (!infoName.equals(event.toLowerCase(Locale.ENGLISH)) && !info.id().equals(event)) {
                  if (event.equals(info.documentationId())) {
                     candidates.clear();
                     candidates.add(info);
                     break;
                  }
               } else {
                  candidates.add(info);
               }
            }

            if (candidates.isEmpty()) {
               throw new IllegalArgumentException("No matching info found for event annotation: " + event);
            }

            if (candidates.size() != 1) {
               throw new IllegalArgumentException(
                  "Multiple matching info found for event annotation: "
                     + event
                     + "\nDifferentiate by specifying a documentationId on the relevant event infos."
               );
            }

            BukkitSyntaxInfos.Event<?> info = candidates.getFirst();
            String id = info.documentationId();
            if (id == null) {
               id = info.id();
            }

            String name = info.name();
            object.addProperty("id", id);
            object.addProperty("name", name);
            array.add(object);
         }

         return array;
      } else {
         return null;
      }
   }

   @NotNull
   private static JsonObject getExpressionReturnTypes(DefaultSyntaxInfos.Expression<?, ?> expression) {
      ClassInfo<?> exact = Classes.getSuperClassInfo(expression.returnType());
      String name = Objects.requireNonNullElse(exact.getDocName(), exact.getName().getSingular());
      if (name.equals(ClassInfo.NO_DOC)) {
         Class<?> parent = expression.returnType().getSuperclass();
         if (parent == null || parent == Object.class) {
            for (Class<?> parentInterface : expression.returnType().getInterfaces()) {
               exact = Classes.getSuperClassInfo(parentInterface);
               if (exact.getC() != Object.class) {
                  parent = parentInterface;
                  break;
               }
            }

            if (parent == null) {
               parent = Object.class;
            }
         }

         exact = Classes.getSuperClassInfo(parent);
         name = Objects.requireNonNullElse(exact.getDocName(), exact.getName().getSingular());
      }

      JsonObject object = new JsonObject();
      object.addProperty("id", exact.getCodeName());
      object.addProperty("name", name);
      return object;
   }

   private static JsonObject generateEventElement(BukkitSyntaxInfos.Event<?> info) {
      JsonObject syntaxJsonObject = new JsonObject();
      syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(info));
      syntaxJsonObject.addProperty("name", info.name());
      syntaxJsonObject.addProperty("cancellable", isCancellable(info));
      syntaxJsonObject.add("since", convertToJsonArray(info.since().toArray(new String[0])));
      syntaxJsonObject.add("patterns", cleanPatterns(info.patterns().toArray(new String[0])));
      syntaxJsonObject.add("description", convertToJsonArray(info.description().toArray(new String[0])));
      syntaxJsonObject.add("requirements", convertToJsonArray(info.requiredPlugins().toArray(new String[0])));
      syntaxJsonObject.add("examples", convertToJsonArray(info.examples().toArray(new String[0])));
      syntaxJsonObject.add("eventValues", getEventValues(info));
      syntaxJsonObject.add("keywords", convertToJsonArray(info.keywords().toArray(new String[0])));
      return syntaxJsonObject;
   }

   private static JsonArray getEventValues(BukkitSyntaxInfos.Event<?> info) {
      Set<JsonObject> eventValues = new HashSet<>();
      Multimap<Class<? extends Event>, EventValues.EventValueInfo<?, ?>> allEventValues = EventValues.getPerEventEventValues();

      for (Class<? extends Event> supportedEvent : info.events()) {
         for (Entry<Class<? extends Event>, EventValues.EventValueInfo<?, ?>> entry : allEventValues.entries()) {
            Class<? extends Event> event = entry.getKey();
            EventValues.EventValueInfo<?, ?> eventValueInfo = entry.getValue();
            if (event != null && event.isAssignableFrom(supportedEvent)) {
               Class<?>[] excludes = eventValueInfo.excludes();
               if (excludes == null || !Set.of(excludes).contains(event)) {
                  Class<?> valueClass = eventValueInfo.valueClass();
                  ClassInfo<?> classInfo;
                  if (valueClass.isArray()) {
                     classInfo = Classes.getSuperClassInfo(valueClass.componentType());
                  } else {
                     classInfo = Classes.getSuperClassInfo(valueClass);
                  }

                  String name = classInfo.getName().getSingular();
                  if (valueClass.isArray()) {
                     name = classInfo.getName().getPlural();
                  }

                  if (!name.isBlank()) {
                     if (eventValueInfo.time() == EventValues.TIME_PAST) {
                        name = "past " + name;
                     } else if (eventValueInfo.time() == EventValues.TIME_FUTURE) {
                        name = "future " + name;
                     }

                     JsonObject object = new JsonObject();
                     object.addProperty("id", DocumentationIdProvider.getId(classInfo));
                     object.addProperty("name", name.toLowerCase(Locale.ENGLISH));
                     eventValues.add(object);
                  }
               }
            }
         }
      }

      if (eventValues.isEmpty()) {
         return null;
      } else {
         JsonArray array = new JsonArray();

         for (JsonObject eventValue : eventValues) {
            array.add(eventValue);
         }

         return array;
      }
   }

   private static boolean isCancellable(BukkitSyntaxInfos.Event<?> info) {
      boolean cancellable = false;

      for (Class<? extends Event> event : info.events()) {
         if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event)) {
            cancellable = true;
            break;
         }
      }

      return cancellable;
   }

   private <T extends SyntaxInfo<? extends Structure>> JsonArray generateStructureElementArray(Collection<T> infos) {
      JsonArray syntaxArray = new JsonArray();
      infos.forEach(info -> {
         if (info instanceof BukkitSyntaxInfos.Event<?> eventInfo) {
            syntaxArray.add(generateEventElement(eventInfo));
         } else {
            JsonObject structureElementJsonObject = this.generatedAnnotatedElement(info);
            if (structureElementJsonObject != null) {
               syntaxArray.add(structureElementJsonObject);
            }
         }
      });
      return syntaxArray;
   }

   private <T extends SyntaxInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Collection<T> infos) {
      JsonArray syntaxArray = new JsonArray();
      infos.forEach(info -> {
         JsonObject syntaxJsonObject = this.generatedAnnotatedElement(info);
         if (syntaxJsonObject != null) {
            syntaxArray.add(syntaxJsonObject);
         }
      });
      return syntaxArray;
   }

   private static JsonObject generateClassInfoElement(ClassInfo<?> classInfo) {
      if (!classInfo.hasDocs()) {
         return null;
      } else {
         JsonObject syntaxJsonObject = new JsonObject();
         syntaxJsonObject.addProperty("id", DocumentationIdProvider.getId(classInfo));
         syntaxJsonObject.addProperty("name", Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName()));
         syntaxJsonObject.addProperty("since", classInfo.getSince());
         syntaxJsonObject.add("patterns", cleanPatterns(classInfo.getUsage()));
         syntaxJsonObject.add("description", convertToJsonArray(classInfo.getDescription()));
         syntaxJsonObject.add("requirements", convertToJsonArray(classInfo.getRequiredPlugins()));
         syntaxJsonObject.add("examples", convertToJsonArray(classInfo.getExamples()));
         syntaxJsonObject.add("properties", getClassInfoProperties(classInfo));
         return syntaxJsonObject;
      }
   }

   private static JsonArray getClassInfoProperties(ClassInfo<?> classInfo) {
      JsonArray array = new JsonArray();

      for (Property<?> property : classInfo.getAllProperties()) {
         JsonObject object = new JsonObject();
         object.addProperty("id", DocumentationIdProvider.getId(property));
         object.addProperty("name", property.name());
         ClassInfo.PropertyDocs docs = classInfo.getPropertyDocumentation(property);
         object.addProperty("description", docs.description());
         object.addProperty("provider", docs.provider().name());
         object.add("relatedSyntax", getPropertyRelatedSyntaxes(property));
         array.add(object);
      }

      return array;
   }

   private static JsonArray generateClassInfoArray(Iterator<ClassInfo<?>> classInfos) {
      JsonArray syntaxArray = new JsonArray();
      classInfos.forEachRemaining(classInfo -> {
         JsonObject classInfoElement = generateClassInfoElement((ClassInfo<?>)classInfo);
         if (classInfoElement != null) {
            syntaxArray.add(classInfoElement);
         }
      });
      return syntaxArray;
   }

   private static JsonArray getPropertyRelatedClassInfos(Property<?> property) {
      JsonArray array = new JsonArray();

      for (ClassInfo<?> classInfo : Classes.getClassInfosByProperty(property)) {
         JsonObject object = new JsonObject();
         object.addProperty("id", DocumentationIdProvider.getId(classInfo));
         object.addProperty("name", Objects.requireNonNullElse(classInfo.getDocName(), classInfo.getCodeName()));
         array.add(object);
      }

      return array;
   }

   private static JsonElement getPropertyRelatedSyntaxes(Property<?> property) {
      JsonArray array = new JsonArray();
      Set<SyntaxInfo<?>> relatedSyntaxes = PROPERTY_RELATED_SYNTAXES.get(property);
      if (relatedSyntaxes != null && !relatedSyntaxes.isEmpty()) {
         for (SyntaxInfo<?> element : relatedSyntaxes) {
            Name name = element.type().getAnnotation(Name.class);
            if (name != null) {
               JsonObject object = new JsonObject();
               object.addProperty("id", DocumentationIdProvider.getId(element));
               object.addProperty("name", element.type().getAnnotation(Name.class).value());
               array.add(object);
            }
         }

         return array;
      } else {
         System.out.println("Property " + property.name() + " has no related syntaxes");
         return null;
      }
   }

   private static JsonObject getPropertyDetails(Property<?> property) {
      JsonObject object = new JsonObject();
      object.addProperty("id", DocumentationIdProvider.getId(property));
      object.addProperty("name", property.name());
      object.addProperty("description", property.description());
      object.addProperty("provider", property.provider().name());
      object.add("since", convertToJsonArray(property.since()));
      return object;
   }

   private static JsonElement generatePropertiesArray(Iterator<Property<?>> iterator) {
      JsonArray array = new JsonArray();
      iterator.forEachRemaining(property -> {
         JsonObject object = getPropertyDetails((Property<?>)property);
         object.add("relatedTypes", getPropertyRelatedClassInfos((Property<?>)property));
         object.add("relatedSyntaxes", getPropertyRelatedSyntaxes((Property<?>)property));
         array.add(object);
      });
      return array;
   }

   private static JsonObject generateFunctionElement(Function<?> function) {
      JsonObject functionJsonObject = new JsonObject();
      functionJsonObject.addProperty("id", DocumentationIdProvider.getId(function));
      functionJsonObject.addProperty("name", function.getName());
      if (function instanceof Documentable documentable) {
         functionJsonObject.addProperty("since", StringUtils.join(documentable.since(), "\n"));
         functionJsonObject.add("description", convertToJsonArray(documentable.description().toArray(new String[0])));
         functionJsonObject.add("examples", convertToJsonArray(documentable.examples().toArray(new String[0])));
      }

      functionJsonObject.add("returns", getReturnType(function));
      String functionSignature = function.getSignature().toString(false, false);
      functionJsonObject.add("patterns", convertToJsonArray(functionSignature));
      return functionJsonObject;
   }

   private static JsonObject getReturnType(Function<?> function) {
      JsonObject object = new JsonObject();
      ClassInfo<?> returnType = function.getReturnType();
      if (returnType == null) {
         return null;
      } else {
         object.addProperty("id", DocumentationIdProvider.getId(returnType));
         object.addProperty("name", Objects.requireNonNullElse(returnType.getDocName(), returnType.getCodeName()));
         return object;
      }
   }

   private static JsonArray generateFunctionArray(Iterator<Function<?>> functions) {
      JsonArray syntaxArray = new JsonArray();
      functions.forEachRemaining(function -> syntaxArray.add(generateFunctionElement((Function<?>)function)));
      return syntaxArray;
   }

   private static JsonArray generateExperiments() {
      JsonArray array = new JsonArray();

      for (Experiment experiment : Skript.experiments().registered()) {
         JsonObject object = new JsonObject();
         if (experiment instanceof Feature feature) {
            object.addProperty("id", experiment.codeName());
            if (feature.displayName().isEmpty()) {
               object.addProperty("name", (String)null);
            } else {
               object.addProperty("name", feature.displayName());
            }

            if (feature.description().isEmpty()) {
               object.add("description", null);
            } else {
               JsonArray description = new JsonArray();

               for (String part : feature.description()) {
                  description.add(part);
               }

               object.add("description", description);
            }

            object.addProperty("pattern", experiment.pattern().toString());
            object.addProperty("phase", experiment.phase().name().toLowerCase(Locale.ENGLISH));
            array.add(object);
         }
      }

      return array;
   }

   private static JsonArray cleanPatterns(String... strings) {
      if (strings != null && strings.length != 0 && (strings.length != 1 || !strings[0].isBlank())) {
         for (int i = 0; i < strings.length; i++) {
            strings[i] = Documentation.cleanPatterns(strings[i], false, false);
         }

         return convertToJsonArray(strings);
      } else {
         return null;
      }
   }

   private JsonObject getSource() {
      JsonObject object = new JsonObject();
      object.addProperty("name", this.source.name());
      Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(this.source.name());
      if (plugin == null) {
         try {
            plugin = JavaPlugin.getProvidingPlugin(this.source.source());
         } catch (Exception var4) {
         }
      }

      object.addProperty("version", plugin == null ? null : plugin.getDescription().getVersion());
      return object;
   }

   public void generate(@NotNull Path path) throws IOException {
      Preconditions.checkNotNull(path, "path cannot be null");
      JsonObject jsonDocs = new JsonObject();
      jsonDocs.add("version", getVersion());
      jsonDocs.add("source", this.getSource());
      jsonDocs.add("conditions", this.generateSyntaxElementArray(this.source.syntaxRegistry().syntaxes(SyntaxRegistry.CONDITION)));
      jsonDocs.add("effects", this.generateSyntaxElementArray(this.source.syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT)));
      jsonDocs.add("expressions", this.generateSyntaxElementArray(this.source.syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)));
      jsonDocs.add("events", this.generateStructureElementArray(this.source.syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY)));
      jsonDocs.add("structures", this.generateStructureElementArray(this.source.syntaxRegistry().syntaxes(SyntaxRegistry.STRUCTURE)));
      jsonDocs.add("sections", this.generateSyntaxElementArray(this.source.syntaxRegistry().syntaxes(SyntaxRegistry.SECTION)));
      jsonDocs.add("types", generateClassInfoArray(Classes.getClassInfos().iterator()));
      jsonDocs.add("functions", generateFunctionArray(Functions.getFunctions().iterator()));
      jsonDocs.add("experiments", generateExperiments());
      jsonDocs.add("properties", generatePropertiesArray(PROPERTY_REGISTRY.iterator()));

      try {
         Files.writeString(path, GSON.toJson(jsonDocs));
      } catch (IOException var4) {
         Skript.exception(var4, "An error occurred while trying to generate JSON documentation");
         throw new IOException(var4);
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13"
   )
   @Override
   public void generate() {
      try {
         this.generate(this.outputDir.toPath());
      } catch (IOException var2) {
         Skript.exception(var2, "An error occurred while trying to generate JSON documentation");
      }
   }
}
