package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.KeyDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.MapperFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.KeyDeserializers;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedAndMetadata;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedConstructor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMember;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMethod;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.EnumResolver;

public class StdKeyDeserializers implements KeyDeserializers, Serializable {
   private static final long serialVersionUID = 1L;

   public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver) {
      return new StdKeyDeserializer.EnumKD(enumResolver, null);
   }

   public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver, AnnotatedMethod factory) {
      return new StdKeyDeserializer.EnumKD(enumResolver, factory);
   }

   public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver, EnumResolver enumNamingResolver) {
      return new StdKeyDeserializer.EnumKD(enumResolver, null, enumNamingResolver);
   }

   public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver, AnnotatedMethod factory, EnumResolver enumNamingResolver) {
      return new StdKeyDeserializer.EnumKD(enumResolver, factory, enumNamingResolver);
   }

   public static KeyDeserializer constructDelegatingKeyDeserializer(DeserializationConfig config, JavaType type, JsonDeserializer<?> deser) {
      return new StdKeyDeserializer.DelegatingKD(type.getRawClass(), deser);
   }

   public static KeyDeserializer findStringBasedKeyDeserializer(DeserializationConfig config, JavaType type) throws JsonMappingException {
      BeanDescription beanDesc = config.introspectForCreation(type);
      AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode> ctorInfo = _findStringConstructor(beanDesc);
      if (ctorInfo != null && ctorInfo.metadata != null) {
         return _constructCreatorKeyDeserializer(config, ctorInfo.annotated);
      } else {
         List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> factoryCandidates = beanDesc.getFactoryMethodsWithMode();
         factoryCandidates.removeIf(
            m -> m.annotated.getParameterCount() != 1 || m.annotated.getRawParameterType(0) != String.class || m.metadata == JsonCreator.Mode.PROPERTIES
         );
         AnnotatedMethod explicitFactory = _findExplicitStringFactoryMethod(factoryCandidates);
         if (explicitFactory != null) {
            return _constructCreatorKeyDeserializer(config, explicitFactory);
         } else if (ctorInfo != null) {
            return _constructCreatorKeyDeserializer(config, ctorInfo.annotated);
         } else {
            return !factoryCandidates.isEmpty() ? _constructCreatorKeyDeserializer(config, (AnnotatedMember)factoryCandidates.get(0).annotated) : null;
         }
      }
   }

   private static KeyDeserializer _constructCreatorKeyDeserializer(DeserializationConfig config, AnnotatedMember creator) {
      if (creator instanceof AnnotatedConstructor) {
         Constructor<?> rawCtor = ((AnnotatedConstructor)creator).getAnnotated();
         if (config.canOverrideAccessModifiers()) {
            ClassUtil.checkAndFixAccess(rawCtor, config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
         }

         return new StdKeyDeserializer.StringCtorKeyDeserializer(rawCtor);
      } else {
         Method m = ((AnnotatedMethod)creator).getAnnotated();
         if (config.canOverrideAccessModifiers()) {
            ClassUtil.checkAndFixAccess(m, config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
         }

         return new StdKeyDeserializer.StringFactoryKeyDeserializer(m);
      }
   }

   private static AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode> _findStringConstructor(BeanDescription beanDesc) {
      for (AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode> entry : beanDesc.getConstructorsWithMode()) {
         AnnotatedConstructor ctor = entry.annotated;
         if (ctor.getParameterCount() == 1 && String.class == ctor.getRawParameterType(0)) {
            return entry;
         }
      }

      return null;
   }

   private static AnnotatedMethod _findExplicitStringFactoryMethod(List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> candidates) throws JsonMappingException {
      AnnotatedMethod match = null;

      for (AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode> entry : candidates) {
         if (entry.metadata != null) {
            if (match != null) {
               Class<?> rawKeyType = entry.annotated.getDeclaringClass();
               throw new IllegalArgumentException(
                  "Multiple suitable annotated Creator factory methods to be used as the Key deserializer for type " + ClassUtil.nameOf(rawKeyType)
               );
            }

            match = entry.annotated;
         }
      }

      return match;
   }

   @Override
   public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
      Class<?> raw = type.getRawClass();
      if (raw.isPrimitive()) {
         raw = ClassUtil.wrapperType(raw);
      }

      return StdKeyDeserializer.forType(raw);
   }
}
