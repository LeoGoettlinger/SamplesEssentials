package ch.njol.yggdrasil;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

public interface YggdrasilSerializable {
   public interface YggdrasilExtendedSerializable extends YggdrasilSerializable {
      Fields serialize() throws NotSerializableException;

      void deserialize(@NotNull Fields var1) throws StreamCorruptedException, NotSerializableException;
   }

   public interface YggdrasilRobustEnum {
      Enum<?> excessiveConstant(String var1);
   }

   public interface YggdrasilRobustSerializable extends YggdrasilSerializable {
      boolean incompatibleField(@NotNull Field var1, @NotNull Fields.FieldContext var2) throws StreamCorruptedException;

      boolean excessiveField(@NotNull Fields.FieldContext var1) throws StreamCorruptedException;

      boolean missingField(@NotNull Field var1) throws StreamCorruptedException;
   }
}
