package com.viaversion.viaversion.libs.mcstructs.text.serializer.verify;

import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.DelegatingConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class VerifyingConverter<T> extends DelegatingConverter<T> {
   private final TextVerifier textVerifier;

   public static <T, V extends TextVerifier> BiFunction<DataConverter<?>, T, Result<Void>> verify(
      Class<V> verifierType, BiPredicate<V, T> predicate, String message
   ) {
      return verify(verifierType, (type, verifier) -> predicate.test(verifier, type) ? null : Result.error(message + ": " + type));
   }

   public static <T, V extends TextVerifier> BiFunction<DataConverter<?>, T, Result<Void>> verify(
      Class<V> verifierType, BiFunction<T, V, Result<Void>> verifier
   ) {
      return (converter, type) -> {
         if (!(converter instanceof VerifyingConverter)) {
            return null;
         } else {
            VerifyingConverter<?> verifyingConverter = (VerifyingConverter<?>)converter;
            if (!verifierType.isInstance(verifyingConverter.textVerifier)) {
               throw new IllegalStateException(
                  "Invalid verifier. Expected " + verifierType.getName() + " but got " + verifyingConverter.textVerifier.getClass().getName()
               );
            } else {
               return verifier.apply(type, (V)verifyingConverter.textVerifier);
            }
         }
      };
   }

   public static <T, V extends TextVerifier> boolean isValid(DataConverter<?> converter, T type, Class<V> verifierType, BiPredicate<V, T> predicate) {
      if (!(converter instanceof VerifyingConverter)) {
         return true;
      } else {
         VerifyingConverter<?> verifyingConverter = (VerifyingConverter<?>)converter;
         if (!verifierType.isInstance(verifyingConverter.textVerifier)) {
            throw new IllegalStateException(
               "Invalid verifier. Expected " + verifierType.getName() + " but got " + verifyingConverter.textVerifier.getClass().getName()
            );
         } else {
            return predicate.test((V)verifyingConverter.textVerifier, type);
         }
      }
   }

   public VerifyingConverter(DataConverter<T> delegate, TextVerifier textVerifier) {
      super(delegate instanceof VerifyingConverter ? ((VerifyingConverter)delegate).getDelegate() : delegate);
      this.textVerifier = textVerifier;
   }

   public TextVerifier getTextVerifier() {
      return this.textVerifier;
   }
}
