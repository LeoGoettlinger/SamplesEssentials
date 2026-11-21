package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JsonPOJOBuilder;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedAndMetadata;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedConstructor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedField;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMember;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMethod;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.ObjectIdInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeBindings;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Annotations;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Converter;

public abstract class BeanDescription {
   protected final JavaType _type;

   protected BeanDescription(JavaType type) {
      this._type = type;
   }

   public JavaType getType() {
      return this._type;
   }

   public Class<?> getBeanClass() {
      return this._type.getRawClass();
   }

   public boolean isRecordType() {
      return this._type.isRecordType();
   }

   public boolean isNonStaticInnerClass() {
      return this.getClassInfo().isNonStaticInnerClass();
   }

   public abstract AnnotatedClass getClassInfo();

   public abstract ObjectIdInfo getObjectIdInfo();

   public abstract boolean hasKnownClassAnnotations();

   @Deprecated
   public abstract TypeBindings bindingsForBeanType();

   @Deprecated
   public abstract JavaType resolveType(Type var1);

   public abstract Annotations getClassAnnotations();

   public abstract List<BeanPropertyDefinition> findProperties();

   public abstract Set<String> getIgnoredPropertyNames();

   public abstract List<BeanPropertyDefinition> findBackReferences();

   @Deprecated
   public abstract Map<String, AnnotatedMember> findBackReferenceProperties();

   public abstract List<AnnotatedConstructor> getConstructors();

   public abstract List<AnnotatedAndMetadata<AnnotatedConstructor, JsonCreator.Mode>> getConstructorsWithMode();

   public abstract List<AnnotatedMethod> getFactoryMethods();

   public abstract List<AnnotatedAndMetadata<AnnotatedMethod, JsonCreator.Mode>> getFactoryMethodsWithMode();

   public abstract AnnotatedConstructor findDefaultConstructor();

   @Deprecated
   public abstract Constructor<?> findSingleArgConstructor(Class<?>... var1);

   @Deprecated
   public abstract Method findFactoryMethod(Class<?>... var1);

   public AnnotatedMember findJsonKeyAccessor() {
      return null;
   }

   public abstract AnnotatedMember findJsonValueAccessor();

   public abstract AnnotatedMember findAnyGetter();

   public abstract AnnotatedMember findAnySetterAccessor();

   public abstract AnnotatedMethod findMethod(String var1, Class<?>[] var2);

   @Deprecated
   public abstract AnnotatedMethod findJsonValueMethod();

   @Deprecated
   public AnnotatedMethod findAnySetter() {
      AnnotatedMember m = this.findAnySetterAccessor();
      return m instanceof AnnotatedMethod ? (AnnotatedMethod)m : null;
   }

   @Deprecated
   public AnnotatedMember findAnySetterField() {
      AnnotatedMember m = this.findAnySetterAccessor();
      return m instanceof AnnotatedField ? m : null;
   }

   public abstract JsonInclude.Value findPropertyInclusion(JsonInclude.Value var1);

   public abstract JsonFormat.Value findExpectedFormat(JsonFormat.Value var1);

   public abstract Converter<Object, Object> findSerializationConverter();

   public abstract Converter<Object, Object> findDeserializationConverter();

   public String findClassDescription() {
      return null;
   }

   public abstract Map<Object, AnnotatedMember> findInjectables();

   public abstract Class<?> findPOJOBuilder();

   public abstract JsonPOJOBuilder.Value findPOJOBuilderConfig();

   public abstract Object instantiateBean(boolean var1);

   public abstract Class<?>[] findDefaultViews();
}
