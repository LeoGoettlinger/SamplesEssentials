package su.nightexpress.nightcore.integration.currency.type;

import java.util.UUID;
import java.util.function.UnaryOperator;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.core.CoreConfig;
import su.nightexpress.nightcore.integration.currency.impl.DummyCurrency;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;

public abstract class AbstractCurrency implements Currency {
   protected final String originalId;
   protected final String internalId;

   public AbstractCurrency(@NonNull String id) {
      this(id, id);
   }

   public AbstractCurrency(@NonNull String originalId, @NonNull String internalId) {
      this.originalId = originalId;
      this.internalId = LowerCase.INTERNAL.apply(internalId);
   }

   @NonNull
   @Override
   public UnaryOperator<String> replacePlaceholders() {
      return Placeholders.CURRENCY.replacer(this);
   }

   @Override
   public final boolean isDummy() {
      return this == DummyCurrency.INSTANCE;
   }

   @Override
   public double floorIfNeeded(double amount) {
      return !this.canHandleDecimals() ? Math.floor(amount) : amount;
   }

   @NonNull
   @Override
   public String formatValue(double amount) {
      return NumberUtil.format(this.floorIfNeeded(amount));
   }

   @NonNull
   @Override
   public String format(double amount) {
      String format = this.replacePlaceholders().apply(this.getFormat().replace("%amount%", this.formatValue(amount)).replace("%name%", this.getName()));
      if (CoreConfig.ECONOMY_PLACEHOLDERS_API_FORMAT.get()) {
         format = CommonPlaceholders.setPAPIPlaceholders(null, format);
      }

      return format;
   }

   @NonNull
   @Override
   public String applyFormat(@NonNull String format, double amount) {
      return this.replacePlaceholders().apply(format).replace("%amount%", this.format(amount));
   }

   @Override
   public double queryBalance(@NonNull Player player) {
      return this.queryBalance(player.getUniqueId());
   }

   @Override
   public double queryBalance(@NonNull UUID playerId) {
      return Players.findById(playerId).map(this::queryBalanceDirect).orElse(0.0);
   }

   protected abstract double queryBalanceDirect(@NonNull Player var1);

   @Override
   public void deposit(@NonNull Player player, double amount) {
      if (player.isOnline()) {
         this.depositDirect(player, amount);
      } else {
         this.depositAsync(player, amount);
      }
   }

   @Override
   public void deposit(@NonNull UUID playerId, double amount) {
      Player player = Players.getPlayer(playerId);
      if (player != null) {
         this.depositDirect(player, amount);
      } else {
         this.depositAsync(playerId, amount);
      }
   }

   protected abstract void depositDirect(@NonNull Player var1, double var2);

   @Override
   public void withdraw(@NonNull Player player, double amount) {
      if (player.isOnline()) {
         this.withdrawDirect(player, amount);
      } else {
         this.withdrawAsync(player, amount);
      }
   }

   @Override
   public void withdraw(@NonNull UUID playerId, double amount) {
      Player player = Players.getPlayer(playerId);
      if (player != null) {
         this.withdrawDirect(player, amount);
      } else {
         this.withdrawAsync(playerId, amount);
      }
   }

   protected abstract void withdrawDirect(@NonNull Player var1, double var2);

   @NonNull
   @Override
   public String getOriginalId() {
      return this.originalId;
   }

   @NonNull
   @Override
   public String getInternalId() {
      return this.internalId;
   }
}
