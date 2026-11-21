package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JacksonException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.MapperFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ContextualDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.SettableBeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl.PropertyBasedCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl.PropertyValueBuffer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMethod;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

class FactoryBasedEnumDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {
   private static final long serialVersionUID = 1L;
   protected final JavaType _inputType;
   protected final AnnotatedMethod _factory;
   protected final JsonDeserializer<?> _deser;
   protected final ValueInstantiator _valueInstantiator;
   protected final SettableBeanProperty[] _creatorProps;
   protected final boolean _hasArgs;
   private transient volatile PropertyBasedCreator _propCreator;

   public FactoryBasedEnumDeserializer(
      Class<?> cls, AnnotatedMethod f, JavaType paramType, ValueInstantiator valueInstantiator, SettableBeanProperty[] creatorProps
   ) {
      super(cls);
      this._factory = f;
      this._hasArgs = true;
      this._inputType = !paramType.hasRawClass(String.class) && !paramType.hasRawClass(CharSequence.class) ? paramType : null;
      this._deser = null;
      this._valueInstantiator = valueInstantiator;
      this._creatorProps = creatorProps;
   }

   public FactoryBasedEnumDeserializer(Class<?> cls, AnnotatedMethod f) {
      super(cls);
      this._factory = f;
      this._hasArgs = false;
      this._inputType = null;
      this._deser = null;
      this._valueInstantiator = null;
      this._creatorProps = null;
   }

   protected FactoryBasedEnumDeserializer(FactoryBasedEnumDeserializer base, JsonDeserializer<?> deser) {
      super(base._valueClass);
      this._inputType = base._inputType;
      this._factory = base._factory;
      this._hasArgs = base._hasArgs;
      this._valueInstantiator = base._valueInstantiator;
      this._creatorProps = base._creatorProps;
      this._deser = deser;
   }

   @Override
   public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
      return this._deser == null && this._inputType != null && this._creatorProps == null
         ? new FactoryBasedEnumDeserializer(this, ctxt.findContextualValueDeserializer(this._inputType, property))
         : this;
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.FALSE;
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Enum;
   }

   @Override
   public boolean isCachable() {
      return true;
   }

   @Override
   public ValueInstantiator getValueInstantiator() {
      return this._valueInstantiator;
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      Object value;
      if (this._deser != null) {
         value = this._deser.deserialize(p, ctxt);
      } else {
         if (!this._hasArgs) {
            p.skipChildren();

            try {
               return this._factory.call();
            } catch (Exception var6) {
               Throwable t = ClassUtil.throwRootCauseIfIOE(var6);
               return ctxt.handleInstantiationProblem(this._valueClass, null, t);
            }
         }

         if (this._creatorProps != null) {
            if (p.isExpectedStartObjectToken()) {
               PropertyBasedCreator pc = this._propCreator;
               if (pc == null) {
                  this._propCreator = pc = PropertyBasedCreator.construct(
                     ctxt, this._valueInstantiator, this._creatorProps, ctxt.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                  );
               }

               p.nextToken();
               return this.deserializeEnumUsingPropertyBased(p, ctxt, pc);
            }

            if (!this._valueInstantiator.canCreateFromString()) {
               JavaType targetType = this.getValueType(ctxt);
               ctxt.reportInputMismatch(
                  targetType,
                  "Input mismatch reading Enum %s: properties-based `@JsonCreator` (%s) expects JSON Object (JsonToken.START_OBJECT), got JsonToken.%s",
                  ClassUtil.getTypeDescription(targetType),
                  this._factory,
                  p.currentToken()
               );
            }
         }

         JsonToken t = p.currentToken();
         boolean unwrapping = t == JsonToken.START_ARRAY && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
         if (unwrapping) {
            t = p.nextToken();
         }

         if (t != null && t.isScalarValue()) {
            value = p.getValueAsString();
         } else {
            value = "";
            p.skipChildren();
         }

         if (unwrapping && p.nextToken() != JsonToken.END_ARRAY) {
            this.handleMissingEndArrayForSingle(p, ctxt);
         }
      }

      try {
         return this._factory.callOnWith(this._valueClass, value);
      } catch (Exception var7) {
         Throwable tx = ClassUtil.throwRootCauseIfIOE(var7);
         return tx instanceof IllegalArgumentException && ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            ? null
            : ctxt.handleInstantiationProblem(this._valueClass, value, tx);
      }
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromAny(p, ctxt);
   }

   protected Object deserializeEnumUsingPropertyBased(JsonParser p, DeserializationContext ctxt, PropertyBasedCreator creator) throws IOException {
      PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, null);

      for (JsonToken t = p.currentToken(); t == JsonToken.FIELD_NAME; t = p.nextToken()) {
         String propName = p.currentName();
         p.nextToken();
         SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
         if (!buffer.readIdProperty(propName) || creatorProp != null) {
            if (creatorProp != null) {
               buffer.assignParameter(creatorProp, this._deserializeWithErrorWrapping(p, ctxt, creatorProp));
            } else {
               p.skipChildren();
            }
         }
      }

      return creator.build(ctxt, buffer);
   }

   protected final Object _deserializeWithErrorWrapping(JsonParser p, DeserializationContext ctxt, SettableBeanProperty prop) throws IOException {
      try {
         return prop.deserialize(p, ctxt);
      } catch (Exception var5) {
         return this.wrapAndThrow(var5, this.handledType(), prop.getName(), ctxt);
      }
   }

   protected Object wrapAndThrow(Throwable t, Object bean, String fieldName, DeserializationContext ctxt) throws IOException {
      throw JsonMappingException.wrapWithPath(this.throwOrReturnThrowable(t, ctxt), bean, fieldName);
   }

   private Throwable throwOrReturnThrowable(Throwable t, DeserializationContext ctxt) throws IOException {
      t = ClassUtil.getRootCause(t);
      ClassUtil.throwIfError(t);
      boolean wrap = ctxt == null || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
      if (t instanceof IOException) {
         if (!wrap || !(t instanceof JacksonException)) {
            throw (IOException)t;
         }
      } else if (!wrap) {
         ClassUtil.throwIfRTE(t);
      }

      return t;
   }
}
