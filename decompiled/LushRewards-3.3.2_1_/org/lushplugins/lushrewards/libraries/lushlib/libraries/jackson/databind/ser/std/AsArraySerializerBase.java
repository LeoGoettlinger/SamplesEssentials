package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.type.WritableTypeId;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedMember;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonschema.JsonSchema;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonschema.SchemaAware;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContainerSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContextualSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.impl.PropertySerializerMap;

public abstract class AsArraySerializerBase<T> extends ContainerSerializer<T> implements ContextualSerializer {
   protected final JavaType _elementType;
   protected final BeanProperty _property;
   protected final boolean _staticTyping;
   protected final Boolean _unwrapSingle;
   protected final TypeSerializer _valueTypeSerializer;
   protected final JsonSerializer<Object> _elementSerializer;
   protected PropertySerializerMap _dynamicSerializers;

   protected AsArraySerializerBase(Class<?> cls, JavaType et, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> elementSerializer) {
      this(cls, et, staticTyping, vts, null, elementSerializer, null);
   }

   @Deprecated
   protected AsArraySerializerBase(
      Class<?> cls, JavaType et, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> elementSerializer
   ) {
      this(cls, et, staticTyping, vts, property, elementSerializer, null);
   }

   protected AsArraySerializerBase(
      Class<?> cls,
      JavaType elementType,
      boolean staticTyping,
      TypeSerializer vts,
      BeanProperty property,
      JsonSerializer<?> elementSerializer,
      Boolean unwrapSingle
   ) {
      super(cls, false);
      this._elementType = elementType;
      this._staticTyping = staticTyping || elementType != null && elementType.isFinal();
      this._valueTypeSerializer = vts;
      this._property = property;
      this._elementSerializer = (JsonSerializer<Object>)elementSerializer;
      this._dynamicSerializers = PropertySerializerMap.emptyForProperties();
      this._unwrapSingle = unwrapSingle;
   }

   protected AsArraySerializerBase(
      AsArraySerializerBase<?> src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle
   ) {
      super(src);
      this._elementType = src._elementType;
      this._staticTyping = src._staticTyping;
      this._valueTypeSerializer = vts;
      this._property = property;
      this._elementSerializer = (JsonSerializer<Object>)elementSerializer;
      this._dynamicSerializers = PropertySerializerMap.emptyForProperties();
      this._unwrapSingle = unwrapSingle;
   }

   @Deprecated
   protected AsArraySerializerBase(AsArraySerializerBase<?> src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer) {
      this(src, property, vts, elementSerializer, src._unwrapSingle);
   }

   @Deprecated
   public final AsArraySerializerBase<T> withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer) {
      return this.withResolved(property, vts, elementSerializer, this._unwrapSingle);
   }

   public abstract AsArraySerializerBase<T> withResolved(BeanProperty var1, TypeSerializer var2, JsonSerializer<?> var3, Boolean var4);

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
      TypeSerializer typeSer = this._valueTypeSerializer;
      if (typeSer != null) {
         typeSer = typeSer.forProperty(property);
      }

      JsonSerializer<?> ser = null;
      Boolean unwrapSingle = null;
      if (property != null) {
         AnnotationIntrospector intr = serializers.getAnnotationIntrospector();
         AnnotatedMember m = property.getMember();
         if (m != null) {
            Object serDef = intr.findContentSerializer(m);
            if (serDef != null) {
               ser = serializers.serializerInstance(m, serDef);
            }
         }
      }

      JsonFormat.Value format = this.findFormatOverrides(serializers, property, this.handledType());
      if (format != null) {
         unwrapSingle = format.getFeature(JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
      }

      if (ser == null) {
         ser = this._elementSerializer;
      }

      ser = this.findContextualConvertingSerializer(serializers, property, ser);
      if (ser == null && this._elementType != null && this._staticTyping && !this._elementType.isJavaLangObject()) {
         ser = serializers.findContentValueSerializer(this._elementType, property);
      }

      return ser == this._elementSerializer
            && property == this._property
            && this._valueTypeSerializer == typeSer
            && Objects.equals(this._unwrapSingle, unwrapSingle)
         ? this
         : this.withResolved(property, typeSer, ser, unwrapSingle);
   }

   @Override
   public JavaType getContentType() {
      return this._elementType;
   }

   @Override
   public JsonSerializer<?> getContentSerializer() {
      return this._elementSerializer;
   }

   @Override
   public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      if (provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) && this.hasSingleElement(value)) {
         this.serializeContents(value, gen, provider);
      } else {
         gen.writeStartArray(value);
         this.serializeContents(value, gen, provider);
         gen.writeEndArray();
      }
   }

   @Override
   public void serializeWithType(T value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.START_ARRAY));
      g.setCurrentValue(value);
      this.serializeContents(value, g, provider);
      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   protected abstract void serializeContents(T var1, JsonGenerator var2, SerializerProvider var3) throws IOException;

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
      ObjectNode o = this.createSchemaNode("array", true);
      if (this._elementSerializer != null) {
         JsonNode schemaNode = null;
         if (this._elementSerializer instanceof SchemaAware) {
            schemaNode = ((SchemaAware)this._elementSerializer).getSchema(provider, null);
         }

         if (schemaNode == null) {
            schemaNode = JsonSchema.getDefaultSchemaNode();
         }

         o.set("items", schemaNode);
      }

      return o;
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      JsonSerializer<?> valueSer = this._elementSerializer;
      if (valueSer == null && this._elementType != null) {
         valueSer = visitor.getProvider().findContentValueSerializer(this._elementType, this._property);
      }

      this.visitArrayFormat(visitor, typeHint, valueSer, this._elementType);
   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, Class<?> type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicSerializers = result.map;
      }

      return result.serializer;
   }

   protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, JavaType type, SerializerProvider provider) throws JsonMappingException {
      PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, this._property);
      if (map != result.map) {
         this._dynamicSerializers = result.map;
      }

      return result.serializer;
   }
}
