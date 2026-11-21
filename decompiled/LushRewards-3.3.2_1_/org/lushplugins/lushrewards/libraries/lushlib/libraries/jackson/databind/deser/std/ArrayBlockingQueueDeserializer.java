package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.NullValueProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;

public class ArrayBlockingQueueDeserializer extends CollectionDeserializer {
   private static final long serialVersionUID = 1L;

   public ArrayBlockingQueueDeserializer(
      JavaType containerType, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser, ValueInstantiator valueInstantiator
   ) {
      super(containerType, valueDeser, valueTypeDeser, valueInstantiator);
   }

   protected ArrayBlockingQueueDeserializer(
      JavaType containerType,
      JsonDeserializer<Object> valueDeser,
      TypeDeserializer valueTypeDeser,
      ValueInstantiator valueInstantiator,
      JsonDeserializer<Object> delegateDeser,
      NullValueProvider nuller,
      Boolean unwrapSingle
   ) {
      super(containerType, valueDeser, valueTypeDeser, valueInstantiator, delegateDeser, nuller, unwrapSingle);
   }

   protected ArrayBlockingQueueDeserializer(ArrayBlockingQueueDeserializer src) {
      super(src);
   }

   protected ArrayBlockingQueueDeserializer withResolved(
      JsonDeserializer<?> dd, JsonDeserializer<?> vd, TypeDeserializer vtd, NullValueProvider nuller, Boolean unwrapSingle
   ) {
      return new ArrayBlockingQueueDeserializer(
         this._containerType, (JsonDeserializer<Object>)vd, vtd, this._valueInstantiator, (JsonDeserializer<Object>)dd, nuller, unwrapSingle
      );
   }

   @Override
   protected Collection<Object> createDefaultInstance(DeserializationContext ctxt) throws IOException {
      return null;
   }

   @Override
   protected Collection<Object> _deserializeFromArray(JsonParser p, DeserializationContext ctxt, Collection<Object> result0) throws IOException {
      if (result0 == null) {
         result0 = new ArrayList<>();
      }

      result0 = super._deserializeFromArray(p, ctxt, result0);
      return result0.isEmpty() ? new ArrayBlockingQueue<>(1, false) : new ArrayBlockingQueue<>(result0.size(), false, result0);
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromArray(p, ctxt);
   }
}
