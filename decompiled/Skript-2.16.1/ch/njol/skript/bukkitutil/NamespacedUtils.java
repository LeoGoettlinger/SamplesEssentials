package ch.njol.skript.bukkitutil;

import ch.njol.skript.localization.Message;
import ch.njol.skript.util.ValidationResult;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;

public class NamespacedUtils {
   public static final Message NAMEDSPACED_FORMAT_MESSAGE = new Message("misc.namespacedutils.format");

   public static boolean isValidNamespaceChar(char character) {
      return character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.' || character == '_' || character == '-';
   }

   public static boolean isValidKeyChar(char character) {
      return isValidNamespaceChar(character) || character == '/';
   }

   public static ValidationResult<NamespacedKey> checkValidation(String string) {
      if (string.length() > 32767) {
         return new ValidationResult<>(false, "A namespaced key can not be longer than 32767 characters.");
      } else {
         String[] split = string.split(":");
         if (split.length > 2) {
            return new ValidationResult<>(false, "A namespaced key can not have more than one ':'.");
         } else {
            String key = split.length == 2 ? split[1] : split[0];
            if (key.isEmpty()) {
               return new ValidationResult<>(false, "The key cannot be empty.");
            } else {
               for (char character : key.toCharArray()) {
                  if (!isValidKeyChar(character)) {
                     return new ValidationResult<>(false, "Invalid character '" + character + "'.");
                  }
               }

               boolean emptyNamespace = false;
               NamespacedKey namespacedKey;
               if (split.length == 2) {
                  String namespace = split[0];
                  if (!namespace.isEmpty()) {
                     for (char characterx : namespace.toCharArray()) {
                        if (!isValidNamespaceChar(characterx)) {
                           return new ValidationResult<>(false, "Invalid character '" + characterx + "'.");
                        }
                     }

                     namespacedKey = new NamespacedKey(namespace, key);
                  } else {
                     emptyNamespace = true;
                     namespacedKey = NamespacedKey.minecraft(key);
                  }
               } else {
                  namespacedKey = NamespacedKey.minecraft(key);
               }

               return emptyNamespace
                  ? new ValidationResult<>(true, "The namespace section of the key is empty. Consider removing the ':'.", namespacedKey)
                  : new ValidationResult<>(true, namespacedKey);
            }
         }
      }
   }

   @Nullable
   public static NamespacedKey checkValidationAndSend(String string, RuntimeErrorProducer producer) {
      ValidationResult<NamespacedKey> validationResult = checkValidation(string);
      String validationMessage = validationResult.message();
      if (!validationResult.valid()) {
         producer.error(validationMessage + ". " + NAMEDSPACED_FORMAT_MESSAGE);
         return null;
      } else {
         if (validationMessage != null) {
            producer.warning(validationMessage);
         }

         return validationResult.data();
      }
   }

   public static boolean isValid(String string) {
      return checkValidation(string).valid();
   }
}
