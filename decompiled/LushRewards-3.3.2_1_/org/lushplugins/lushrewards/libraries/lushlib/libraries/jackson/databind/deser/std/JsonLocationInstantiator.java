package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonLocation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.ContentReference;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyMetadata;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.CreatorProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.SettableBeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;

public class JsonLocationInstantiator extends ValueInstantiator.Base {
   private static final long serialVersionUID = 1L;

   public JsonLocationInstantiator() {
      super(JsonLocation.class);
   }

   @Override
   public boolean canCreateFromObjectWith() {
      return true;
   }

   @Override
   public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
      JavaType intType = config.constructType(int.class);
      JavaType longType = config.constructType(long.class);
      return new SettableBeanProperty[]{
         creatorProp("sourceRef", config.constructType(Object.class), 0),
         creatorProp("byteOffset", longType, 1),
         creatorProp("charOffset", longType, 2),
         creatorProp("lineNr", intType, 3),
         creatorProp("columnNr", intType, 4)
      };
   }

   private static CreatorProperty creatorProp(String name, JavaType type, int index) {
      return CreatorProperty.construct(PropertyName.construct(name), type, null, null, null, null, index, null, PropertyMetadata.STD_REQUIRED);
   }

   @Override
   public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) {
      ContentReference srcRef = ContentReference.rawReference(args[0]);
      return new JsonLocation(srcRef, _long(args[1]), _long(args[2]), _int(args[3]), _int(args[4]));
   }

   private static final long _long(Object o) {
      return o == null ? 0L : ((Number)o).longValue();
   }

   private static final int _int(Object o) {
      return o == null ? 0 : ((Number)o).intValue();
   }
}
