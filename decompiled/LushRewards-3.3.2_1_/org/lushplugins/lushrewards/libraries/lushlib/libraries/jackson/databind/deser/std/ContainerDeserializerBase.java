package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.NullValueProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.SettableBeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl.NullsConstantProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.TypeFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.AccessPattern;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public abstract class ContainerDeserializerBase<T> extends StdDeserializer<T> implements ValueInstantiator.Gettable {
   protected final JavaType _containerType;
   protected final NullValueProvider _nullProvider;
   protected final boolean _skipNullValues;
   protected final Boolean _unwrapSingle;

   protected ContainerDeserializerBase(JavaType selfType, NullValueProvider nuller, Boolean unwrapSingle) {
      super(selfType);
      this._containerType = selfType;
      this._unwrapSingle = unwrapSingle;
      this._nullProvider = nuller;
      this._skipNullValues = NullsConstantProvider.isSkipper(nuller);
   }

   protected ContainerDeserializerBase(JavaType selfType) {
      this(selfType, null, null);
   }

   protected ContainerDeserializerBase(ContainerDeserializerBase<?> base) {
      this(base, base._nullProvider, base._unwrapSingle);
   }

   protected ContainerDeserializerBase(ContainerDeserializerBase<?> base, NullValueProvider nuller, Boolean unwrapSingle) {
      super(base._containerType);
      this._containerType = base._containerType;
      this._nullProvider = nuller;
      this._unwrapSingle = unwrapSingle;
      this._skipNullValues = NullsConstantProvider.isSkipper(nuller);
   }

   @Override
   public JavaType getValueType() {
      return this._containerType;
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.TRUE;
   }

   @Override
   public SettableBeanProperty findBackReference(String refName) {
      JsonDeserializer<Object> valueDeser = this.getContentDeserializer();
      if (valueDeser == null) {
         throw new IllegalArgumentException(
            String.format(
               "Cannot handle managed/back reference '%s': type: container deserializer of type %s returned null for 'getContentDeserializer()'",
               refName,
               this.getClass().getName()
            )
         );
      } else {
         return valueDeser.findBackReference(refName);
      }
   }

   public JavaType getContentType() {
      return this._containerType == null ? TypeFactory.unknownType() : this._containerType.getContentType();
   }

   public abstract JsonDeserializer<Object> getContentDeserializer();

   @Override
   public AccessPattern getEmptyAccessPattern() {
      return AccessPattern.DYNAMIC;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      ValueInstantiator vi = this.getValueInstantiator();
      if (vi == null || !vi.canCreateUsingDefault()) {
         JavaType type = this.getValueType();
         ctxt.reportBadDefinition(type, String.format("Cannot create empty instance of %s, no default Creator", type));
      }

      try {
         return vi.createUsingDefault(ctxt);
      } catch (IOException var4) {
         return ClassUtil.throwAsMappingException(ctxt, var4);
      }
   }

   @Deprecated
   protected <BOGUS> BOGUS wrapAndThrow(Throwable t, Object ref, String key) throws IOException {
      return this.wrapAndThrow(null, t, ref, key);
   }

   protected <BOGUS> BOGUS wrapAndThrow(DeserializationContext ctxt, Throwable t, Object ref, String key) throws IOException {
      while (t instanceof InvocationTargetException && t.getCause() != null) {
         t = t.getCause();
      }

      ClassUtil.throwIfError(t);
      if (ctxt != null && !ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)) {
         ClassUtil.throwIfRTE(t);
      }

      if (t instanceof IOException && !(t instanceof JsonMappingException)) {
         throw (IOException)t;
      } else {
         throw JsonMappingException.wrapWithPath(t, ref, ClassUtil.nonNull(key, "N/A"));
      }
   }
}
