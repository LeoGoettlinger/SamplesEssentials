package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import java.util.Collection;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonTypeInfo;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.MapperFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.NoClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClassResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.NamedType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeResolverBuilder;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public class StdTypeResolverBuilder implements TypeResolverBuilder<StdTypeResolverBuilder> {
   protected JsonTypeInfo.Id _idType;
   protected JsonTypeInfo.As _includeAs;
   protected String _typeProperty;
   protected boolean _typeIdVisible = false;
   protected Class<?> _defaultImpl;
   protected TypeIdResolver _customIdResolver;

   public StdTypeResolverBuilder() {
   }

   protected StdTypeResolverBuilder(JsonTypeInfo.Id idType, JsonTypeInfo.As idAs, String propName) {
      this._idType = idType;
      this._includeAs = idAs;
      this._typeProperty = propName;
   }

   protected StdTypeResolverBuilder(StdTypeResolverBuilder base, Class<?> defaultImpl) {
      this._idType = base._idType;
      this._includeAs = base._includeAs;
      this._typeProperty = base._typeProperty;
      this._typeIdVisible = base._typeIdVisible;
      this._customIdResolver = base._customIdResolver;
      this._defaultImpl = defaultImpl;
   }

   public static StdTypeResolverBuilder noTypeInfoBuilder() {
      return new StdTypeResolverBuilder().init(JsonTypeInfo.Id.NONE, null);
   }

   public StdTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idRes) {
      if (idType == null) {
         throw new IllegalArgumentException("idType cannot be null");
      } else {
         this._idType = idType;
         this._customIdResolver = idRes;
         this._typeProperty = idType.getDefaultPropertyName();
         return this;
      }
   }

   @Override
   public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
      if (this._idType == JsonTypeInfo.Id.NONE) {
         return null;
      } else if (baseType.isPrimitive() && !this.allowPrimitiveTypes(config, baseType)) {
         return null;
      } else if (this._idType == JsonTypeInfo.Id.DEDUCTION) {
         return AsDeductionTypeSerializer.instance();
      } else {
         TypeIdResolver idRes = this.idResolver(config, baseType, this.subTypeValidator(config), subtypes, true, false);
         switch (this._includeAs) {
            case WRAPPER_ARRAY:
               return new AsArrayTypeSerializer(idRes, null);
            case PROPERTY:
               return new AsPropertyTypeSerializer(idRes, null, this._typeProperty);
            case WRAPPER_OBJECT:
               return new AsWrapperTypeSerializer(idRes, null);
            case EXTERNAL_PROPERTY:
               return new AsExternalTypeSerializer(idRes, null, this._typeProperty);
            case EXISTING_PROPERTY:
               return new AsExistingPropertyTypeSerializer(idRes, null, this._typeProperty);
            default:
               throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + this._includeAs);
         }
      }
   }

   @Override
   public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
      if (this._idType == JsonTypeInfo.Id.NONE) {
         return null;
      } else if (baseType.isPrimitive() && !this.allowPrimitiveTypes(config, baseType)) {
         return null;
      } else {
         PolymorphicTypeValidator subTypeValidator = this.verifyBaseTypeValidity(config, baseType);
         TypeIdResolver idRes = this.idResolver(config, baseType, subTypeValidator, subtypes, false, true);
         JavaType defaultImpl = this.defineDefaultImpl(config, baseType);
         if (this._idType == JsonTypeInfo.Id.DEDUCTION) {
            return new AsDeductionTypeDeserializer(baseType, idRes, defaultImpl, config, subtypes);
         } else {
            switch (this._includeAs) {
               case WRAPPER_ARRAY:
                  return new AsArrayTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
               case PROPERTY:
               case EXISTING_PROPERTY:
                  return new AsPropertyTypeDeserializer(
                     baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl, this._includeAs, this._strictTypeIdHandling(config, baseType)
                  );
               case WRAPPER_OBJECT:
                  return new AsWrapperTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
               case EXTERNAL_PROPERTY:
                  return new AsExternalTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
               default:
                  throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + this._includeAs);
            }
         }
      }
   }

   protected JavaType defineDefaultImpl(DeserializationConfig config, JavaType baseType) {
      if (this._defaultImpl != null) {
         if (this._defaultImpl == Void.class || this._defaultImpl == NoClass.class) {
            return config.getTypeFactory().constructType(this._defaultImpl);
         }

         if (baseType.hasRawClass(this._defaultImpl)) {
            return baseType;
         }

         if (baseType.isTypeOrSuperTypeOf(this._defaultImpl)) {
            return config.getTypeFactory().constructSpecializedType(baseType, this._defaultImpl);
         }

         if (baseType.hasRawClass(this._defaultImpl)) {
            return baseType;
         }
      }

      return config.isEnabled(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL) && !baseType.isAbstract() ? baseType : null;
   }

   public StdTypeResolverBuilder inclusion(JsonTypeInfo.As includeAs) {
      if (includeAs == null) {
         throw new IllegalArgumentException("includeAs cannot be null");
      } else {
         this._includeAs = includeAs;
         return this;
      }
   }

   public StdTypeResolverBuilder typeProperty(String typeIdPropName) {
      if (typeIdPropName == null || typeIdPropName.isEmpty()) {
         typeIdPropName = this._idType.getDefaultPropertyName();
      }

      this._typeProperty = typeIdPropName;
      return this;
   }

   public StdTypeResolverBuilder defaultImpl(Class<?> defaultImpl) {
      this._defaultImpl = defaultImpl;
      return this;
   }

   public StdTypeResolverBuilder typeIdVisibility(boolean isVisible) {
      this._typeIdVisible = isVisible;
      return this;
   }

   public StdTypeResolverBuilder withDefaultImpl(Class<?> defaultImpl) {
      if (this._defaultImpl == defaultImpl) {
         return this;
      } else {
         ClassUtil.verifyMustOverride(StdTypeResolverBuilder.class, this, "withDefaultImpl");
         return new StdTypeResolverBuilder(this, defaultImpl);
      }
   }

   @Override
   public Class<?> getDefaultImpl() {
      return this._defaultImpl;
   }

   public String getTypeProperty() {
      return this._typeProperty;
   }

   public boolean isTypeIdVisible() {
      return this._typeIdVisible;
   }

   protected TypeIdResolver idResolver(
      MapperConfig<?> config, JavaType baseType, PolymorphicTypeValidator subtypeValidator, Collection<NamedType> subtypes, boolean forSer, boolean forDeser
   ) {
      if (this._customIdResolver != null) {
         return this._customIdResolver;
      } else if (this._idType == null) {
         throw new IllegalStateException("Cannot build, 'init()' not yet called");
      } else {
         switch (this._idType) {
            case DEDUCTION:
            case CLASS:
               return ClassNameIdResolver.construct(baseType, config, subtypeValidator);
            case MINIMAL_CLASS:
               return MinimalClassNameIdResolver.construct(baseType, config, subtypeValidator);
            case NAME:
               return TypeNameIdResolver.construct(config, baseType, subtypes, forSer, forDeser);
            case NONE:
               return null;
            case CUSTOM:
            default:
               throw new IllegalStateException("Do not know how to construct standard type id resolver for idType: " + this._idType);
         }
      }
   }

   public PolymorphicTypeValidator subTypeValidator(MapperConfig<?> config) {
      return config.getPolymorphicTypeValidator();
   }

   protected PolymorphicTypeValidator verifyBaseTypeValidity(MapperConfig<?> config, JavaType baseType) {
      PolymorphicTypeValidator ptv = this.subTypeValidator(config);
      if (this._idType == JsonTypeInfo.Id.CLASS || this._idType == JsonTypeInfo.Id.MINIMAL_CLASS) {
         PolymorphicTypeValidator.Validity validity = ptv.validateBaseType(config, baseType);
         if (validity == PolymorphicTypeValidator.Validity.DENIED) {
            return this.reportInvalidBaseType(config, baseType, ptv);
         }

         if (validity == PolymorphicTypeValidator.Validity.ALLOWED) {
            return LaissezFaireSubTypeValidator.instance;
         }
      }

      return ptv;
   }

   protected PolymorphicTypeValidator reportInvalidBaseType(MapperConfig<?> config, JavaType baseType, PolymorphicTypeValidator ptv) {
      throw new IllegalArgumentException(
         String.format(
            "Configured `PolymorphicTypeValidator` (of type %s) denied resolution of all subtypes of base type %s",
            ClassUtil.classNameOf(ptv),
            ClassUtil.classNameOf(baseType.getRawClass())
         )
      );
   }

   protected boolean allowPrimitiveTypes(MapperConfig<?> config, JavaType baseType) {
      return false;
   }

   protected boolean _strictTypeIdHandling(DeserializationConfig config, JavaType baseType) {
      return config.isEnabled(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES) ? true : this._hasTypeResolver(config, baseType);
   }

   protected boolean _hasTypeResolver(DeserializationConfig config, JavaType baseType) {
      AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, baseType.getRawClass());
      AnnotationIntrospector ai = config.getAnnotationIntrospector();
      return ai.findTypeResolver(config, ac, baseType) != null;
   }
}
