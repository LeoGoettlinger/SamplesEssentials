package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.SerializableString;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.EnumNamingStrategy;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JacksonStdImpl;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.EnumNamingStrategyFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ArrayNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node.ObjectNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContextualSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.EnumValues;

@JacksonStdImpl
public class EnumSerializer extends StdScalarSerializer<Enum<?>> implements ContextualSerializer {
   private static final long serialVersionUID = 1L;
   protected final EnumValues _values;
   protected final Boolean _serializeAsIndex;
   protected final EnumValues _valuesByEnumNaming;

   public EnumSerializer(EnumValues v, Boolean serializeAsIndex) {
      super(v.getEnumClass(), false);
      this._values = v;
      this._serializeAsIndex = serializeAsIndex;
      this._valuesByEnumNaming = null;
   }

   public EnumSerializer(EnumValues v, Boolean serializeAsIndex, EnumValues valuesByEnumNaming) {
      super(v.getEnumClass(), false);
      this._values = v;
      this._serializeAsIndex = serializeAsIndex;
      this._valuesByEnumNaming = valuesByEnumNaming;
   }

   public static EnumSerializer construct(Class<?> enumClass, SerializationConfig config, BeanDescription beanDesc, JsonFormat.Value format) {
      EnumValues v = EnumValues.constructFromName(config, (Class<Enum<?>>)enumClass);
      EnumValues valuesByEnumNaming = constructEnumNamingStrategyValues(config, (Class<Enum<?>>)enumClass, beanDesc.getClassInfo());
      Boolean serializeAsIndex = _isShapeWrittenUsingIndex(enumClass, format, true, null);
      return new EnumSerializer(v, serializeAsIndex, valuesByEnumNaming);
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(serializers, property, this.handledType());
      if (format != null) {
         Class<?> type = this.handledType();
         Boolean serializeAsIndex = _isShapeWrittenUsingIndex(type, format, false, this._serializeAsIndex);
         if (!Objects.equals(serializeAsIndex, this._serializeAsIndex)) {
            return new EnumSerializer(this._values, serializeAsIndex);
         }
      }

      return this;
   }

   public EnumValues getEnumValues() {
      return this._values;
   }

   public final void serialize(Enum<?> en, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      if (this._valuesByEnumNaming != null) {
         gen.writeString(this._valuesByEnumNaming.serializedValueFor(en));
      } else if (this._serializeAsIndex(serializers)) {
         gen.writeNumber(en.ordinal());
      } else if (serializers.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
         gen.writeString(en.toString());
      } else {
         gen.writeString(this._values.serializedValueFor(en));
      }
   }

   @Deprecated
   @Override
   public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
      if (this._serializeAsIndex(provider)) {
         return this.createSchemaNode("integer", true);
      } else {
         ObjectNode objectNode = this.createSchemaNode("string", true);
         if (typeHint != null) {
            JavaType type = provider.constructType(typeHint);
            if (type.isEnumType()) {
               ArrayNode enumNode = objectNode.putArray("enum");

               for (SerializableString value : this._values.values()) {
                  enumNode.add(value.getValue());
               }
            }
         }

         return objectNode;
      }
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      SerializerProvider serializers = visitor.getProvider();
      if (this._serializeAsIndex(serializers)) {
         this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.INT);
      } else {
         JsonStringFormatVisitor stringVisitor = visitor.expectStringFormat(typeHint);
         if (stringVisitor != null) {
            Set<String> enums = new LinkedHashSet<>();
            if (serializers != null && serializers.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
               for (Enum<?> e : this._values.enums()) {
                  enums.add(e.toString());
               }
            } else {
               for (SerializableString value : this._values.values()) {
                  enums.add(value.getValue());
               }
            }

            stringVisitor.enumTypes(enums);
         }
      }
   }

   protected final boolean _serializeAsIndex(SerializerProvider serializers) {
      return this._serializeAsIndex != null ? this._serializeAsIndex : serializers.isEnabled(SerializationFeature.WRITE_ENUMS_USING_INDEX);
   }

   protected static Boolean _isShapeWrittenUsingIndex(Class<?> enumClass, JsonFormat.Value format, boolean fromClass, Boolean defaultValue) {
      JsonFormat.Shape shape = format == null ? null : format.getShape();
      if (shape == null) {
         return defaultValue;
      } else if (shape == JsonFormat.Shape.ANY || shape == JsonFormat.Shape.SCALAR) {
         return defaultValue;
      } else if (shape == JsonFormat.Shape.STRING || shape == JsonFormat.Shape.NATURAL) {
         return Boolean.FALSE;
      } else if (!shape.isNumeric() && shape != JsonFormat.Shape.ARRAY) {
         throw new IllegalArgumentException(
            String.format(
               "Unsupported serialization shape (%s) for Enum %s, not supported as %s annotation", shape, enumClass.getName(), fromClass ? "class" : "property"
            )
         );
      } else {
         return Boolean.TRUE;
      }
   }

   protected static EnumValues constructEnumNamingStrategyValues(SerializationConfig config, Class<Enum<?>> enumClass, AnnotatedClass annotatedClass) {
      Object namingDef = config.getAnnotationIntrospector().findEnumNamingStrategy(config, annotatedClass);
      EnumNamingStrategy enumNamingStrategy = EnumNamingStrategyFactory.createEnumNamingStrategyInstance(namingDef, config.canOverrideAccessModifiers());
      return enumNamingStrategy == null ? null : EnumValues.constructUsingEnumNamingStrategy(config, enumClass, enumNamingStrategy);
   }
}
