package net.milkbowl.vault2.economy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MultiEconomyResponse {
   private final Map<UUID, BigDecimal> balances = new HashMap<>();
   public final BigDecimal amount;
   public final EconomyResponse.ResponseType type;
   public final String errorMessage;

   public MultiEconomyResponse(final BigDecimal amount, final EconomyResponse.ResponseType type, final String errorMessage) {
      this.amount = amount;
      this.type = type;
      this.errorMessage = errorMessage;
   }

   public void addBalance(final UUID uuid, final BigDecimal amount) {
      this.balances.put(uuid, amount);
   }

   public Optional<BigDecimal> balance(final UUID uuid) {
      return Optional.ofNullable(this.balances.get(uuid));
   }

   public BigDecimal amount() {
      return this.amount;
   }

   public EconomyResponse.ResponseType type() {
      return this.type;
   }

   public String errorMessage() {
      return this.errorMessage;
   }
}
