package su.nightexpress.nightcore.util.nbt;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.lang.reflect.Method;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.Version;

public class DataFixerUtil {
   private static final Class<?> CLS_DATA_FIXERS = Reflex.safeClass("net.minecraft.util.datafix", "DataFixers", "DataConverterRegistry");
   private static final Class<?> CLS_REFERENCES = Reflex.safeClass("net.minecraft.util.datafix.fixes", "References", "DataConverterTypes");
   private static final Method GET_DATA_FIXER = Reflex.safeMethod(CLS_DATA_FIXERS, "getDataFixer", "a");
   private static final DataFixer DATA_FIXER = (DataFixer)Reflex.invokeMethod(GET_DATA_FIXER, CLS_DATA_FIXERS);
   private static final TypeReference ITEM_STACK_REFERENCE = (TypeReference)Reflex.getFieldValue(CLS_REFERENCES, "ITEM_STACK", "u");

   @NonNull
   public static Object updateItemStack(@NonNull Object compoundTag, int sourceVersion) {
      if (DATA_FIXER == null) {
         throw new IllegalStateException("DataFixer is null!");
      } else {
         int targetVersion = Version.getCurrent().getDataVersion();
         if (targetVersion <= 0) {
            return compoundTag;
         } else if (sourceVersion <= targetVersion && sourceVersion > 0) {
            Dynamic<?> dynamic = new Dynamic((DynamicOps)NbtOps.INSTANCE, compoundTag);
            return DATA_FIXER.update(ITEM_STACK_REFERENCE, dynamic, sourceVersion, targetVersion).getValue();
         } else {
            return compoundTag;
         }
      }
   }
}
