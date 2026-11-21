package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind;

import java.util.Collection;
import java.util.Collections;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.ObjectCodec;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Version;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Versioned;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MutableConfigOverride;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.BeanDeserializerModifier;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.DeserializationProblemHandler;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.Deserializers;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.KeyDeserializers;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiators;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.ClassIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.NamedType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.BeanSerializerModifier;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.Serializers;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeModifier;

public abstract class Module implements Versioned {
   public abstract String getModuleName();

   @Override
   public abstract Version version();

   public Object getTypeId() {
      return this.getClass().getName();
   }

   public abstract void setupModule(Module.SetupContext var1);

   public Iterable<? extends Module> getDependencies() {
      return Collections.emptyList();
   }

   public interface SetupContext {
      Version getMapperVersion();

      <C extends ObjectCodec> C getOwner();

      TypeFactory getTypeFactory();

      boolean isEnabled(MapperFeature var1);

      boolean isEnabled(DeserializationFeature var1);

      boolean isEnabled(SerializationFeature var1);

      boolean isEnabled(JsonFactory.Feature var1);

      boolean isEnabled(JsonParser.Feature var1);

      boolean isEnabled(JsonGenerator.Feature var1);

      MutableConfigOverride configOverride(Class<?> var1);

      void addDeserializers(Deserializers var1);

      void addKeyDeserializers(KeyDeserializers var1);

      void addSerializers(Serializers var1);

      void addKeySerializers(Serializers var1);

      void addBeanDeserializerModifier(BeanDeserializerModifier var1);

      void addBeanSerializerModifier(BeanSerializerModifier var1);

      void addAbstractTypeResolver(AbstractTypeResolver var1);

      void addTypeModifier(TypeModifier var1);

      void addValueInstantiators(ValueInstantiators var1);

      void setClassIntrospector(ClassIntrospector var1);

      void insertAnnotationIntrospector(AnnotationIntrospector var1);

      void appendAnnotationIntrospector(AnnotationIntrospector var1);

      void registerSubtypes(Class<?>... var1);

      void registerSubtypes(NamedType... var1);

      void registerSubtypes(Collection<Class<?>> var1);

      void setMixInAnnotations(Class<?> var1, Class<?> var2);

      void addDeserializationProblemHandler(DeserializationProblemHandler var1);

      void setNamingStrategy(PropertyNamingStrategy var1);
   }
}
