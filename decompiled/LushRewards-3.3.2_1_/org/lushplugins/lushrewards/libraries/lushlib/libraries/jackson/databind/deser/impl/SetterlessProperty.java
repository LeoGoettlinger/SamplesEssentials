package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.MapperFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.NullValueProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.SettableBeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMember;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMethod;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Annotations;

public final class SetterlessProperty extends SettableBeanProperty {
   private static final long serialVersionUID = 1L;
   protected final AnnotatedMethod _annotated;
   protected final Method _getter;

   public SetterlessProperty(BeanPropertyDefinition propDef, JavaType type, TypeDeserializer typeDeser, Annotations contextAnnotations, AnnotatedMethod method) {
      super(propDef, type, typeDeser, contextAnnotations);
      this._annotated = method;
      this._getter = method.getAnnotated();
   }

   protected SetterlessProperty(SetterlessProperty src, JsonDeserializer<?> deser, NullValueProvider nva) {
      super(src, deser, nva);
      this._annotated = src._annotated;
      this._getter = src._getter;
   }

   protected SetterlessProperty(SetterlessProperty src, PropertyName newName) {
      super(src, newName);
      this._annotated = src._annotated;
      this._getter = src._getter;
   }

   @Override
   public SettableBeanProperty withName(PropertyName newName) {
      return new SetterlessProperty(this, newName);
   }

   @Override
   public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
      if (this._valueDeserializer == deser) {
         return this;
      } else {
         NullValueProvider nvp = (NullValueProvider)(this._valueDeserializer == this._nullProvider ? deser : this._nullProvider);
         return new SetterlessProperty(this, deser, nvp);
      }
   }

   @Override
   public SettableBeanProperty withNullProvider(NullValueProvider nva) {
      return new SetterlessProperty(this, this._valueDeserializer, nva);
   }

   @Override
   public void fixAccess(DeserializationConfig config) {
      this._annotated.fixAccess(config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
   }

   @Override
   public <A extends Annotation> A getAnnotation(Class<A> acls) {
      return this._annotated.getAnnotation(acls);
   }

   @Override
   public AnnotatedMember getMember() {
      return this._annotated;
   }

   @Override
   public final void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
      if (!p.hasToken(JsonToken.VALUE_NULL)) {
         if (this._valueTypeDeserializer != null) {
            ctxt.reportBadDefinition(
               this.getType(),
               String.format("Problem deserializing 'setterless' property (\"%s\"): no way to handle typed deser with setterless yet", this.getName())
            );
         }

         Object toModify;
         try {
            toModify = this._getter.invoke(instance, (Object[])null);
         } catch (Exception var6) {
            this._throwAsIOE(p, var6);
            return;
         }

         if (toModify == null) {
            ctxt.reportBadDefinition(
               this.getType(), String.format("Problem deserializing 'setterless' property '%s': get method returned null", this.getName())
            );
         }

         this._valueDeserializer.deserialize(p, ctxt, toModify);
      }
   }

   @Override
   public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
      this.deserializeAndSet(p, ctxt, instance);
      return instance;
   }

   @Override
   public final void set(Object instance, Object value) throws IOException {
      throw new UnsupportedOperationException("Should never call `set()` on setterless property ('" + this.getName() + "')");
   }

   @Override
   public Object setAndReturn(Object instance, Object value) throws IOException {
      this.set(instance, value);
      return instance;
   }
}
