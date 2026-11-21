package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContextualSerializer;

@JacksonStdImpl
public final class BooleanSerializer extends StdScalarSerializer<Object> implements ContextualSerializer {
   private static final long serialVersionUID = 1L;
   protected final boolean _forPrimitive;

   public BooleanSerializer(boolean forPrimitive) {
      super(forPrimitive ? boolean.class : Boolean.class, false);
      this._forPrimitive = forPrimitive;
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(serializers, property, this.handledType());
      if (format != null) {
         JsonFormat.Shape shape = format.getShape();
         if (shape.isNumeric()) {
            return new BooleanSerializer.AsNumber(this._forPrimitive);
         }

         if (shape == JsonFormat.Shape.STRING) {
            return new ToStringSerializer(this._handledType);
         }
      }

      return this;
   }

   @Override
   public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeBoolean(Boolean.TRUE.equals(value));
   }

   @Override
   public final void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      g.writeBoolean(Boolean.TRUE.equals(value));
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      return this.createSchemaNode("boolean", !this._forPrimitive);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      visitor.expectBooleanFormat(typeHint);
   }

   static final class AsNumber extends StdScalarSerializer<Object> implements ContextualSerializer {
      private static final long serialVersionUID = 1L;
      protected final boolean _forPrimitive;

      public AsNumber(boolean forPrimitive) {
         super(forPrimitive ? boolean.class : Boolean.class, false);
         this._forPrimitive = forPrimitive;
      }

      @Override
      public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
         g.writeNumber(Boolean.FALSE.equals(value) ? 0 : 1);
      }

      @Override
      public final void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
         g.writeBoolean(Boolean.TRUE.equals(value));
      }

      @Override
      public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
         this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.INT);
      }

      @Override
      public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
         JsonFormat.Value format = this.findFormatOverrides(serializers, property, Boolean.class);
         if (format != null) {
            JsonFormat.Shape shape = format.getShape();
            if (!shape.isNumeric()) {
               return new BooleanSerializer(this._forPrimitive);
            }
         }

         return this;
      }
   }
}
