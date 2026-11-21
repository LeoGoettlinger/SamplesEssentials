package vrx.economy.vRXClans.libs.cloud.util;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
public final class TypeUtils {
   private TypeUtils() {
   }

   public static String simpleName(@NonNull final Type type) {
      String simpleName = GenericTypeReflector.erase(type).getSimpleName();
      if (type instanceof ParameterizedType) {
         String paramTypes = Arrays.stream(((ParameterizedType)type).getActualTypeArguments()).map(TypeUtils::simpleName).collect(Collectors.joining(", "));
         return simpleName + '<' + paramTypes + '>';
      } else {
         return simpleName;
      }
   }
}
