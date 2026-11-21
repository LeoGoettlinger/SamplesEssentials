package ch.njol.yggdrasil;

import java.io.StreamCorruptedException;
import java.lang.reflect.Field;

public interface FieldHandler {
   boolean excessiveField(Object var1, Fields.FieldContext var2) throws StreamCorruptedException;

   boolean missingField(Object var1, Field var2) throws StreamCorruptedException;

   boolean incompatibleField(Object var1, Field var2, Fields.FieldContext var3) throws StreamCorruptedException;
}
