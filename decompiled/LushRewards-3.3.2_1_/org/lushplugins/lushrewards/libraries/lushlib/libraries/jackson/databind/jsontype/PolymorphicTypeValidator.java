package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;

public abstract class PolymorphicTypeValidator implements Serializable {
   private static final long serialVersionUID = 1L;

   public abstract PolymorphicTypeValidator.Validity validateBaseType(MapperConfig<?> var1, JavaType var2);

   public abstract PolymorphicTypeValidator.Validity validateSubClassName(MapperConfig<?> var1, JavaType var2, String var3) throws JsonMappingException;

   public abstract PolymorphicTypeValidator.Validity validateSubType(MapperConfig<?> var1, JavaType var2, JavaType var3) throws JsonMappingException;

   public abstract static class Base extends PolymorphicTypeValidator implements Serializable {
      private static final long serialVersionUID = 1L;

      @Override
      public PolymorphicTypeValidator.Validity validateBaseType(MapperConfig<?> config, JavaType baseType) {
         return PolymorphicTypeValidator.Validity.INDETERMINATE;
      }

      @Override
      public PolymorphicTypeValidator.Validity validateSubClassName(MapperConfig<?> config, JavaType baseType, String subClassName) throws JsonMappingException {
         return PolymorphicTypeValidator.Validity.INDETERMINATE;
      }

      @Override
      public PolymorphicTypeValidator.Validity validateSubType(MapperConfig<?> config, JavaType baseType, JavaType subType) throws JsonMappingException {
         return PolymorphicTypeValidator.Validity.INDETERMINATE;
      }
   }

   public static enum Validity {
      ALLOWED,
      DENIED,
      INDETERMINATE;
   }
}
