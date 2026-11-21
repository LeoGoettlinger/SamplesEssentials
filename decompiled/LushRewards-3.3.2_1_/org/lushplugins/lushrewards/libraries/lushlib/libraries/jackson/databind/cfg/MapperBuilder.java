package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonAutoDetect;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonSetter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.PropertyAccessor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Base64Variant;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.PrettyPrinter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.StreamReadFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.StreamWriteFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.TokenStreamFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.InjectableValues;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.MapperFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.Module;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ObjectMapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyNamingStrategy;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.DeserializationProblemHandler;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AccessorNamingStrategy;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.VisibilityChecker;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.NamedType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.SubtypeResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeResolverBuilder;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.JsonNodeFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.FilterProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.SerializerFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;

public abstract class MapperBuilder<M extends ObjectMapper, B extends MapperBuilder<M, B>> {
   protected final M _mapper;

   protected MapperBuilder(M mapper) {
      this._mapper = mapper;
   }

   public M build() {
      return this._mapper;
   }

   public boolean isEnabled(MapperFeature f) {
      return this._mapper.isEnabled(f);
   }

   public boolean isEnabled(DeserializationFeature f) {
      return this._mapper.isEnabled(f);
   }

   public boolean isEnabled(SerializationFeature f) {
      return this._mapper.isEnabled(f);
   }

   public boolean isEnabled(JsonParser.Feature f) {
      return this._mapper.isEnabled(f);
   }

   public boolean isEnabled(JsonGenerator.Feature f) {
      return this._mapper.isEnabled(f);
   }

   public TokenStreamFactory streamFactory() {
      return this._mapper.tokenStreamFactory();
   }

   public B enable(MapperFeature... features) {
      this._mapper.enable(features);
      return this._this();
   }

   public B disable(MapperFeature... features) {
      this._mapper.disable(features);
      return this._this();
   }

   public B configure(MapperFeature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(SerializationFeature... features) {
      for (SerializationFeature f : features) {
         this._mapper.enable(f);
      }

      return this._this();
   }

   public B disable(SerializationFeature... features) {
      for (SerializationFeature f : features) {
         this._mapper.disable(f);
      }

      return this._this();
   }

   public B configure(SerializationFeature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(DeserializationFeature... features) {
      for (DeserializationFeature f : features) {
         this._mapper.enable(f);
      }

      return this._this();
   }

   public B disable(DeserializationFeature... features) {
      for (DeserializationFeature f : features) {
         this._mapper.disable(f);
      }

      return this._this();
   }

   public B configure(DeserializationFeature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(DatatypeFeature... features) {
      for (DatatypeFeature f : features) {
         this._mapper.configure(f, true);
      }

      return this._this();
   }

   public B disable(DatatypeFeature... features) {
      for (DatatypeFeature f : features) {
         this._mapper.configure(f, false);
      }

      return this._this();
   }

   public B configure(DatatypeFeature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(JsonParser.Feature... features) {
      this._mapper.enable(features);
      return this._this();
   }

   public B disable(JsonParser.Feature... features) {
      this._mapper.disable(features);
      return this._this();
   }

   public B configure(JsonParser.Feature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(JsonGenerator.Feature... features) {
      this._mapper.enable(features);
      return this._this();
   }

   public B disable(JsonGenerator.Feature... features) {
      this._mapper.disable(features);
      return this._this();
   }

   public B configure(JsonGenerator.Feature feature, boolean state) {
      this._mapper.configure(feature, state);
      return this._this();
   }

   public B enable(StreamReadFeature... features) {
      for (StreamReadFeature f : features) {
         this._mapper.enable(f.mappedFeature());
      }

      return this._this();
   }

   public B disable(StreamReadFeature... features) {
      for (StreamReadFeature f : features) {
         this._mapper.disable(f.mappedFeature());
      }

      return this._this();
   }

   public B configure(StreamReadFeature feature, boolean state) {
      this._mapper.configure(feature.mappedFeature(), state);
      return this._this();
   }

   public B enable(StreamWriteFeature... features) {
      for (StreamWriteFeature f : features) {
         this._mapper.enable(f.mappedFeature());
      }

      return this._this();
   }

   public B disable(StreamWriteFeature... features) {
      for (StreamWriteFeature f : features) {
         this._mapper.disable(f.mappedFeature());
      }

      return this._this();
   }

   public B configure(StreamWriteFeature feature, boolean state) {
      this._mapper.configure(feature.mappedFeature(), state);
      return this._this();
   }

   public B withConfigOverride(Class<?> forType, Consumer<MutableConfigOverride> handler) {
      handler.accept(this._mapper.configOverride(forType));
      return this._this();
   }

   public B withCoercionConfig(LogicalType forType, Consumer<MutableCoercionConfig> handler) {
      handler.accept(this._mapper.coercionConfigFor(forType));
      return this._this();
   }

   public B withCoercionConfig(Class<?> forType, Consumer<MutableCoercionConfig> handler) {
      handler.accept(this._mapper.coercionConfigFor(forType));
      return this._this();
   }

   public B withCoercionConfigDefaults(Consumer<MutableCoercionConfig> handler) {
      handler.accept(this._mapper.coercionConfigDefaults());
      return this._this();
   }

   public B addModule(Module module) {
      this._mapper.registerModule(module);
      return this._this();
   }

   public B addModules(Module... modules) {
      for (Module module : modules) {
         this.addModule(module);
      }

      return this._this();
   }

   public B addModules(Iterable<? extends Module> modules) {
      for (Module module : modules) {
         this.addModule(module);
      }

      return this._this();
   }

   public static List<Module> findModules() {
      return findModules(null);
   }

   public static List<Module> findModules(ClassLoader classLoader) {
      ArrayList<Module> modules = new ArrayList<>();

      for (Module module : secureGetServiceLoader(Module.class, classLoader)) {
         modules.add(module);
      }

      return modules;
   }

   private static <T> ServiceLoader<T> secureGetServiceLoader(final Class<T> clazz, final ClassLoader classLoader) {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null) {
         return classLoader == null ? ServiceLoader.load(clazz) : ServiceLoader.load(clazz, classLoader);
      } else {
         return AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<T>>() {
            public ServiceLoader<T> run() {
               return classLoader == null ? ServiceLoader.load(clazz) : ServiceLoader.load(clazz, classLoader);
            }
         });
      }
   }

   public B findAndAddModules() {
      return this.addModules(findModules());
   }

   public B annotationIntrospector(AnnotationIntrospector intr) {
      this._mapper.setAnnotationIntrospector(intr);
      return this._this();
   }

   public B defaultAttributes(ContextAttributes attrs) {
      this._mapper.setDefaultAttributes(attrs);
      return this._this();
   }

   public B typeFactory(TypeFactory f) {
      this._mapper.setTypeFactory(f);
      return this._this();
   }

   public B subtypeResolver(SubtypeResolver r) {
      this._mapper.setSubtypeResolver(r);
      return this._this();
   }

   public B visibility(VisibilityChecker<?> vc) {
      this._mapper.setVisibility(vc);
      return this._this();
   }

   public B visibility(PropertyAccessor forMethod, JsonAutoDetect.Visibility visibility) {
      this._mapper.setVisibility(forMethod, visibility);
      return this._this();
   }

   public B handlerInstantiator(HandlerInstantiator hi) {
      this._mapper.setHandlerInstantiator(hi);
      return this._this();
   }

   public B propertyNamingStrategy(PropertyNamingStrategy s) {
      this._mapper.setPropertyNamingStrategy(s);
      return this._this();
   }

   public B accessorNaming(AccessorNamingStrategy.Provider s) {
      if (s == null) {
         s = new DefaultAccessorNamingStrategy.Provider();
      }

      this._mapper.setAccessorNaming(s);
      return this._this();
   }

   public B serializerFactory(SerializerFactory f) {
      this._mapper.setSerializerFactory(f);
      return this._this();
   }

   public B filterProvider(FilterProvider prov) {
      this._mapper.setFilterProvider(prov);
      return this._this();
   }

   public B defaultPrettyPrinter(PrettyPrinter pp) {
      this._mapper.setDefaultPrettyPrinter(pp);
      return this._this();
   }

   public B injectableValues(InjectableValues v) {
      this._mapper.setInjectableValues(v);
      return this._this();
   }

   public B nodeFactory(JsonNodeFactory f) {
      this._mapper.setNodeFactory(f);
      return this._this();
   }

   public B constructorDetector(ConstructorDetector cd) {
      this._mapper.setConstructorDetector(cd);
      return this._this();
   }

   public B addHandler(DeserializationProblemHandler h) {
      this._mapper.addHandler(h);
      return this._this();
   }

   public B clearProblemHandlers() {
      this._mapper.clearProblemHandlers();
      return this._this();
   }

   public B defaultSetterInfo(JsonSetter.Value v) {
      this._mapper.setDefaultSetterInfo(v);
      return this._this();
   }

   public B defaultMergeable(Boolean b) {
      this._mapper.setDefaultMergeable(b);
      return this._this();
   }

   public B defaultLeniency(Boolean b) {
      this._mapper.setDefaultLeniency(b);
      return this._this();
   }

   public B defaultDateFormat(DateFormat df) {
      this._mapper.setDateFormat(df);
      return this._this();
   }

   public B defaultTimeZone(TimeZone tz) {
      this._mapper.setTimeZone(tz);
      return this._this();
   }

   public B defaultLocale(Locale locale) {
      this._mapper.setLocale(locale);
      return this._this();
   }

   public B defaultBase64Variant(Base64Variant v) {
      this._mapper.setBase64Variant(v);
      return this._this();
   }

   public B serializationInclusion(JsonInclude.Include incl) {
      this._mapper.setSerializationInclusion(incl);
      return this._this();
   }

   public B defaultPropertyInclusion(JsonInclude.Value incl) {
      this._mapper.setDefaultPropertyInclusion(incl);
      return this._this();
   }

   public B addMixIn(Class<?> target, Class<?> mixinSource) {
      this._mapper.addMixIn(target, mixinSource);
      return this._this();
   }

   public B removeMixIn(Class<?> target) {
      this._mapper.addMixIn(target, null);
      return this._this();
   }

   public B registerSubtypes(Class<?>... subtypes) {
      this._mapper.registerSubtypes(subtypes);
      return this._this();
   }

   public B registerSubtypes(NamedType... subtypes) {
      this._mapper.registerSubtypes(subtypes);
      return this._this();
   }

   public B registerSubtypes(Collection<Class<?>> subtypes) {
      this._mapper.registerSubtypes(subtypes);
      return this._this();
   }

   public B polymorphicTypeValidator(PolymorphicTypeValidator ptv) {
      this._mapper.setPolymorphicTypeValidator(ptv);
      return this._this();
   }

   public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator) {
      this._mapper.activateDefaultTyping(subtypeValidator);
      return this._this();
   }

   public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping dti) {
      this._mapper.activateDefaultTyping(subtypeValidator, dti);
      return this._this();
   }

   public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping applicability, JsonTypeInfo.As includeAs) {
      this._mapper.activateDefaultTyping(subtypeValidator, applicability, includeAs);
      return this._this();
   }

   public B activateDefaultTypingAsProperty(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping applicability, String propertyName) {
      this._mapper.activateDefaultTypingAsProperty(subtypeValidator, applicability, propertyName);
      return this._this();
   }

   public B deactivateDefaultTyping() {
      this._mapper.deactivateDefaultTyping();
      return this._this();
   }

   public B setDefaultTyping(TypeResolverBuilder<?> typer) {
      this._mapper.setDefaultTyping(typer);
      return this._this();
   }

   protected final B _this() {
      return (B)this;
   }
}
