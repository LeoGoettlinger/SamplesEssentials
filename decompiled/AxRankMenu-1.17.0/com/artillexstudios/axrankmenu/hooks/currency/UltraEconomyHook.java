package com.artillexstudios.axrankmenu.hooks.currency;

import com.artillexstudios.axrankmenu.AxRankMenu;
import java.util.Optional;
import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.Currency;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UltraEconomyHook implements CurrencyHook {
   private Currency currency = null;

   @Override
   public void setup() {
      Optional<Currency> currencyOptional = UltraEconomy.getAPI()
         .getCurrencies()
         .name(AxRankMenu.CONFIG.getString("hooks.UltraEconomy.currency-name", "coins"));
      if (!currencyOptional.isPresent()) {
         throw new RuntimeException("Currency not found!");
      } else {
         this.currency = currencyOptional.get();
      }
   }

   @Override
   public String getName() {
      return "UltraEconomy";
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public double getBalance(@NotNull Player p) {
      Optional<Account> account = UltraEconomy.getAPI().getAccounts().uuid(p.getUniqueId());
      return !account.isPresent() ? 0.0 : account.get().getBalance(this.currency).getOnHand();
   }

   @Override
   public void giveBalance(@NotNull Player p, double amount) {
      Optional<Account> account = UltraEconomy.getAPI().getAccounts().uuid(p.getUniqueId());
      if (account.isPresent()) {
         account.get().addBalance(this.currency, amount);
      }
   }

   @Override
   public void takeBalance(@NotNull Player p, double amount) {
      Optional<Account> account = UltraEconomy.getAPI().getAccounts().uuid(p.getUniqueId());
      if (account.isPresent()) {
         account.get().removeBalance(this.currency, amount);
      }
   }
}
