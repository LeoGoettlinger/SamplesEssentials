package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import java.lang.invoke.StringConcatFactory;
import java.util.Locale;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public class EnumEntryValidator<E extends Enum<E>> extends EntryValidator {
   private final Class<E> enumType;
   private final Consumer<E> setter;
   @Nullable
   private String allowedValues = null;

   public EnumEntryValidator(Class<E> enumType, Consumer<E> setter) {
      assert enumType != null;

      this.enumType = enumType;
      this.setter = setter;
      if (((Enum[])enumType.getEnumConstants()).length <= 12) {
         StringBuilder b = new StringBuilder(((Enum[])enumType.getEnumConstants())[0].name());

         for (E e : (Enum[])enumType.getEnumConstants()) {
            if (b.length() != 0) {
               b.append(", ");
            }

            b.append(e.name());
         }

         this.allowedValues = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
      }
   }

   public EnumEntryValidator(Class<E> enumType, Consumer<E> setter, String allowedValues) {
      assert enumType != null;

      this.enumType = enumType;
      this.setter = setter;
      this.allowedValues = allowedValues;
   }

   @Override
   public boolean validate(Node node) {
      if (!super.validate(node)) {
         return false;
      } else {
         EntryNode n = (EntryNode)node;

         try {
            E e = Enum.valueOf(this.enumType, n.getValue().toUpperCase(Locale.ENGLISH).replace(' ', '_'));

            assert e != null;

            this.setter.accept(e);
            return true;
         } catch (IllegalArgumentException var4) {
            Skript.error(
               "'"
                  + n.getValue()
                  + "' is not a valid value for '"
                  + n.getKey()
                  + "'"
                  + (this.allowedValues == null ? "" : ". Allowed values are: " + this.allowedValues)
            );
            return false;
         }
      }
   }
}
