package net.milkbowl.vault2.economy;

import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

public class EconomyResponse {
   public final BigDecimal amount;
   public final BigDecimal balance;
   public final EconomyResponse.ResponseType type;
   public final String errorMessage;

   public EconomyResponse(
      @NotNull final BigDecimal amount, @NotNull final BigDecimal balance, @NotNull final EconomyResponse.ResponseType type, @NotNull final String errorMessage
   ) {
      this.amount = amount;
      this.balance = balance;
      this.type = type;
      this.errorMessage = errorMessage;
   }

   public boolean transactionSuccess() {
      switch (this.type) {
         case SUCCESS:
            return true;
         default:
            return false;
      }
   }

   public static enum ResponseType {
      SUCCESS(1),
      FAILURE(2),
      NOT_IMPLEMENTED(3);

      private int id;

      private ResponseType(final int id) {
         this.id = id;
      }

      int getId() {
         return this.id;
      }
   }
}
