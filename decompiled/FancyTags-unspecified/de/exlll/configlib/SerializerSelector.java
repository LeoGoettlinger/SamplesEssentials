package de.exlll.configlib;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

final class SerializerSelector {
   private static final Map<Class<?>, Serializer<?, ?>> DEFAULT_SERIALIZERS = Map.ofEntries(
      Map.entry(boolean.class, new Serializers.BooleanSerializer()),
      Map.entry(Boolean.class, new Serializers.BooleanSerializer()),
      Map.entry(byte.class, new Serializers.NumberSerializer(byte.class)),
      Map.entry(Byte.class, new Serializers.NumberSerializer(Byte.class)),
      Map.entry(short.class, new Serializers.NumberSerializer(short.class)),
      Map.entry(Short.class, new Serializers.NumberSerializer(Short.class)),
      Map.entry(int.class, new Serializers.NumberSerializer(int.class)),
      Map.entry(Integer.class, new Serializers.NumberSerializer(Integer.class)),
      Map.entry(long.class, new Serializers.NumberSerializer(long.class)),
      Map.entry(Long.class, new Serializers.NumberSerializer(Long.class)),
      Map.entry(float.class, new Serializers.NumberSerializer(float.class)),
      Map.entry(Float.class, new Serializers.NumberSerializer(Float.class)),
      Map.entry(double.class, new Serializers.NumberSerializer(double.class)),
      Map.entry(Double.class, new Serializers.NumberSerializer(Double.class)),
      Map.entry(char.class, new Serializers.CharacterSerializer()),
      Map.entry(Character.class, new Serializers.CharacterSerializer()),
      Map.entry(String.class, new Serializers.StringSerializer()),
      Map.entry(BigInteger.class, new Serializers.BigIntegerSerializer()),
      Map.entry(BigDecimal.class, new Serializers.BigDecimalSerializer()),
      Map.entry(LocalDate.class, new Serializers.LocalDateSerializer()),
      Map.entry(LocalTime.class, new Serializers.LocalTimeSerializer()),
      Map.entry(LocalDateTime.class, new Serializers.LocalDateTimeSerializer()),
      Map.entry(Instant.class, new Serializers.InstantSerializer()),
      Map.entry(UUID.class, new Serializers.UuidSerializer()),
      Map.entry(File.class, new Serializers.FileSerializer()),
      Map.entry(Path.class, new Serializers.PathSerializer()),
      Map.entry(URL.class, new Serializers.UrlSerializer()),
      Map.entry(URI.class, new Serializers.UriSerializer())
   );
   private final ConfigurationProperties properties;
   private ConfigurationElement<?> element;
   private int currentNesting = -1;

   public SerializerSelector(ConfigurationProperties properties) {
      this.properties = Validator.requireNonNull(properties, "configuration properties");
   }

   public Serializer<?, ?> select(ConfigurationElement<?> element) {
      this.element = element;
      this.currentNesting = -1;
      return this.selectForType(element.annotatedType());
   }

   private Serializer<?, ?> selectForType(AnnotatedType annotatedType) {
      this.currentNesting++;
      Serializer<?, ?> custom = this.selectCustomSerializer(annotatedType);
      if (custom != null) {
         return custom;
      } else {
         Type type = annotatedType.getType();
         if (type instanceof Class) {
            return this.selectForClass(annotatedType);
         } else if (type instanceof ParameterizedType) {
            return this.selectForParameterizedType((AnnotatedParameterizedType)annotatedType);
         } else if (type instanceof WildcardType) {
            String msg = this.baseExceptionMessage(type) + "Wildcard types cannot be serialized.";
            throw new ConfigurationException(msg);
         } else if (type instanceof GenericArrayType) {
            String msg = this.baseExceptionMessage(type) + "Generic array types cannot be serialized.";
            throw new ConfigurationException(msg);
         } else if (type instanceof TypeVariable) {
            String msg = this.baseExceptionMessage(type) + "Type variables cannot be serialized.";
            throw new ConfigurationException(msg);
         } else {
            throw new ConfigurationException(this.baseExceptionMessage(type));
         }
      }
   }

   private Serializer<?, ?> selectCustomSerializer(AnnotatedType annotatedType) {
      return this.findConfigurationElementSerializer(annotatedType)
         .or(() -> this.findSerializerFactoryForType(annotatedType))
         .or(() -> this.findSerializerForType(annotatedType))
         .or(() -> this.findSerializerOnType(annotatedType))
         .or(() -> this.findMetaSerializerOnType(annotatedType))
         .or(() -> this.findSerializerByCondition(annotatedType))
         .orElse(null);
   }

   private Optional<Serializer<?, ?>> findConfigurationElementSerializer(AnnotatedType annotatedType) {
      SerializeWith annotation = this.element.annotation(SerializeWith.class);
      return annotation != null && this.currentNesting == annotation.nesting()
         ? Optional.of(this.newSerializerFromAnnotation(annotatedType, annotation))
         : Optional.empty();
   }

   private Optional<Serializer<?, ?>> findSerializerFactoryForType(AnnotatedType annotatedType) {
      if (annotatedType.getType() instanceof Class<?> cls && this.properties.getSerializerFactories().containsKey(cls)) {
         SerializerContextImpl context = new SerializerContextImpl(this.properties, this.element, annotatedType);
         Function<? super SerializerContext, ? extends Serializer<?, ?>> factory = this.properties.getSerializerFactories().get(cls);
         Serializer<?, ?> serializer = (Serializer<?, ?>)factory.apply(context);
         if (serializer == null) {
            String msg = "Serializer factories must not return null.";
            throw new ConfigurationException(msg);
         } else {
            return Optional.of(serializer);
         }
      } else {
         return Optional.empty();
      }
   }

   private Optional<Serializer<?, ?>> findSerializerForType(AnnotatedType annotatedType) {
      return annotatedType.getType() instanceof Class<?> cls && this.properties.getSerializers().containsKey(cls)
         ? Optional.of(this.properties.getSerializers().get(cls))
         : Optional.empty();
   }

   private Optional<Serializer<?, ?>> findSerializerOnType(AnnotatedType annotatedType) {
      if (annotatedType.getType() instanceof Class<?> cls && cls.getDeclaredAnnotation(SerializeWith.class) != null) {
         SerializeWith annotation = cls.getDeclaredAnnotation(SerializeWith.class);
         return Optional.of(this.newSerializerFromAnnotation(annotatedType, annotation));
      } else {
         return Optional.empty();
      }
   }

   private Optional<Serializer<?, ?>> findMetaSerializerOnType(AnnotatedType annotatedType) {
      if (annotatedType.getType() instanceof Class<?> cls) {
         for (Annotation meta : cls.getDeclaredAnnotations()) {
            Class<? extends Annotation> metaType = meta.annotationType();
            SerializeWith annotation = metaType.getDeclaredAnnotation(SerializeWith.class);
            if (annotation != null) {
               return Optional.of(this.newSerializerFromAnnotation(annotatedType, annotation));
            }
         }
      }

      return Optional.empty();
   }

   private Optional<Serializer<?, ?>> findSerializerByCondition(AnnotatedType annotatedType) {
      for (Entry<Predicate<? super Type>, Serializer<?, ?>> entry : this.properties.getSerializersByCondition().entrySet()) {
         if (entry.getKey().test(annotatedType.getType())) {
            return Optional.of(entry.getValue());
         }
      }

      return Optional.empty();
   }

   private Serializer<?, ?> newSerializerFromAnnotation(AnnotatedType annotatedType, SerializeWith annotation) {
      SerializerContextImpl context = new SerializerContextImpl(this.properties, this.element, annotatedType);
      return Serializers.newCustomSerializer((Class<Serializer<?, ?>>)annotation.serializer(), context);
   }

   private Serializer<?, ?> selectForClass(AnnotatedType annotatedType) {
      Class<?> cls = (Class<?>)annotatedType.getType();
      if (DEFAULT_SERIALIZERS.containsKey(cls)) {
         return DEFAULT_SERIALIZERS.get(cls);
      } else if (Reflect.isEnumType(cls)) {
         return new Serializers.EnumSerializer((Class<? extends Enum<?>>)cls);
      } else if (Reflect.isArrayType(cls)) {
         return this.selectForArray((AnnotatedArrayType)annotatedType);
      } else if (Reflect.isConfigurationType(cls)) {
         return TypeSerializer.newSerializerFor(cls, this.properties);
      } else {
         String msg = "Missing serializer for type "
            + cls
            + ".\nEither annotate the type with @Configuration, make it a Java record, or provide a custom serializer for it.";
         throw new ConfigurationException(msg);
      }
   }

   private Serializer<?, ?> selectForArray(AnnotatedArrayType annotatedType) {
      AnnotatedType annotatedElementType = annotatedType.getAnnotatedGenericComponentType();
      Class<?> elementType = (Class<?>)annotatedElementType.getType();
      if (elementType == boolean.class) {
         return new Serializers.PrimitiveBooleanArraySerializer();
      } else if (elementType == char.class) {
         return new Serializers.PrimitiveCharacterArraySerializer();
      } else if (elementType == byte.class) {
         return new Serializers.PrimitiveByteArraySerializer();
      } else if (elementType == short.class) {
         return new Serializers.PrimitiveShortArraySerializer();
      } else if (elementType == int.class) {
         return new Serializers.PrimitiveIntegerArraySerializer();
      } else if (elementType == long.class) {
         return new Serializers.PrimitiveLongArraySerializer();
      } else if (elementType == float.class) {
         return new Serializers.PrimitiveFloatArraySerializer();
      } else if (elementType == double.class) {
         return new Serializers.PrimitiveDoubleArraySerializer();
      } else {
         Serializer<?, ?> elementSerializer = this.selectForType(annotatedElementType);
         boolean inputNulls = this.properties.inputNulls();
         boolean outputNulls = this.properties.outputNulls();
         return new Serializers.ArraySerializer<>(elementType, elementSerializer, outputNulls, inputNulls);
      }
   }

   private Serializer<?, ?> selectForParameterizedType(AnnotatedParameterizedType annotatedType) {
      ParameterizedType type = (ParameterizedType)annotatedType.getType();
      Class<?> rawType = (Class<?>)type.getRawType();
      AnnotatedType[] typeArgs = annotatedType.getAnnotatedActualTypeArguments();
      boolean inputNulls = this.properties.inputNulls();
      boolean outputNulls = this.properties.outputNulls();
      if (Reflect.isListType(rawType)) {
         Serializer<?, ?> elementSerializer = this.selectForType(typeArgs[0]);
         return new Serializers.ListSerializer<>(elementSerializer, outputNulls, inputNulls);
      } else if (Reflect.isSetType(rawType)) {
         Serializer<?, ?> elementSerializer = this.selectForType(typeArgs[0]);
         return (Serializer<?, ?>)(this.properties.serializeSetsAsLists()
            ? new Serializers.SetAsListSerializer<>(elementSerializer, outputNulls, inputNulls)
            : new Serializers.SetSerializer<>(elementSerializer, outputNulls, inputNulls));
      } else if (!Reflect.isMapType(rawType)) {
         String msg = this.baseExceptionMessage(type) + "Parameterized types other than lists, sets, and maps cannot be serialized.";
         throw new ConfigurationException(msg);
      } else if (!(typeArgs[0].getType() instanceof Class<?> cls && (DEFAULT_SERIALIZERS.containsKey(cls) || Reflect.isEnumType(cls)))) {
         String msg = this.baseExceptionMessage(type) + "Map keys can only be of simple or enum type.";
         throw new ConfigurationException(msg);
      } else {
         Serializer<?, ?> keySerializer = this.selectForClass(typeArgs[0]);
         Serializer<?, ?> valSerializer = this.selectForType(typeArgs[1]);
         return new Serializers.MapSerializer<>(keySerializer, valSerializer, outputNulls, inputNulls);
      }
   }

   private String baseExceptionMessage(Type type) {
      return "Cannot select serializer for type '%s'.\n".formatted(type);
   }
}
