package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.impl;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.PolymorphicTypeValidator;

public final class LaissezFaireSubTypeValidator extends PolymorphicTypeValidator.Base {
   private static final long serialVersionUID = 1L;
   public static final LaissezFaireSubTypeValidator instance = new LaissezFaireSubTypeValidator();

   @Override
   public PolymorphicTypeValidator.Validity validateBaseType(MapperConfig<?> ctxt, JavaType baseType) {
      return PolymorphicTypeValidator.Validity.INDETERMINATE;
   }

   @Override
   public PolymorphicTypeValidator.Validity validateSubClassName(MapperConfig<?> ctxt, JavaType baseType, String subClassName) {
      return PolymorphicTypeValidator.Validity.ALLOWED;
   }

   @Override
   public PolymorphicTypeValidator.Validity validateSubType(MapperConfig<?> ctxt, JavaType baseType, JavaType subType) {
      return PolymorphicTypeValidator.Validity.ALLOWED;
   }
}
