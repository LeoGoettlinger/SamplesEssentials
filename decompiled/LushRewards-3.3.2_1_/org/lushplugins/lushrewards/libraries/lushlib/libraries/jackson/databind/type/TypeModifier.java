package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type;

import java.lang.reflect.Type;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;

public abstract class TypeModifier {
   public abstract JavaType modifyType(JavaType var1, Type var2, TypeBindings var3, TypeFactory var4);
}
