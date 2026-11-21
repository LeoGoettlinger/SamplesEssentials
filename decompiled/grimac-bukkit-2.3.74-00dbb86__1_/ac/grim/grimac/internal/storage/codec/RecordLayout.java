package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.lang.reflect.RecordComponent;
import java.util.List;

@ApiStatus.Internal
public record RecordLayout(@NotNull String[] accessorNames, @NotNull Class<?>[] ctorParamTypes, int version) {
   @NotNull
   public static RecordLayout fromReflection(@NotNull Class<?> recordType, @NotNull EncodeShape shape) {
      RecordComponent[] components = recordType.getRecordComponents();
      if (components == null) {
         throw new IllegalStateException(
            "no record components for "
               + recordType.getName()
               + " — the RecordComponents attribute was stripped (obfuscation?) and no captured binding was found; ensure the record lives in the obf keep-set and was captured at build time"
         );
      } else {
         Class<?>[] ctorParamTypes = new Class[components.length];

         for (int i = 0; i < components.length; i++) {
            ctorParamTypes[i] = components[i].getType();
         }

         List<EncodeShape.FieldDef> fields = shape.fields();
         String[] accessorNames = new String[fields.size()];

         for (int i = 0; i < fields.size(); i++) {
            EncodeShape.FieldDef f = fields.get(i);
            accessorNames[i] = components[f.recordIndex()].getName();
         }

         Persistent p = recordType.getAnnotation(Persistent.class);
         return new RecordLayout(accessorNames, ctorParamTypes, p == null ? 1 : p.version());
      }
   }
}
