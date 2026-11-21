package ch.njol.skript.hooks.economy.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.hooks.economy.classes.Money;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Money")
@Description({"How much virtual money a player has (can be changed)."})
@Example.Examples({@Example("message \"You have %player's money%\" # the currency name will be added automatically"), @Example("remove 20$ from the player's balance # replace '$' by whatever currency you use"), @Example("add 200 to the player's account # or omit the currency altogether")})
@Since({"2.0, 2.5 (offline players)"})
@RequiredPlugins({"Vault", "an economy plugin that supports Vault"})
public class ExprBalance extends SimplePropertyExpression<OfflinePlayer, Money> {
   public Money convert(OfflinePlayer player) {
      try {
         return new Money(VaultHook.economy.getBalance(player));
      } catch (Exception var3) {
         return new Money(VaultHook.economy.getBalance(player.getName()));
      }
   }

   @Override
   public Class<? extends Money> getReturnType() {
      return Money.class;
   }

   @Override
   protected String getPropertyName() {
      return "money";
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.REMOVE_ALL ? null : new Class[]{Money.class, Number.class};
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         double money = delta[0] instanceof Number ? ((Number)delta[0]).doubleValue() : ((Money)delta[0]).getAmount();

         for (OfflinePlayer player : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
                  double balance = VaultHook.economy.getBalance(player);
                  if (balance < money) {
                     VaultHook.economy.depositPlayer(player, money - balance);
                  } else if (balance > money) {
                     VaultHook.economy.withdrawPlayer(player, balance - money);
                  }
                  break;
               case ADD:
                  VaultHook.economy.depositPlayer(player, money);
                  break;
               case REMOVE:
                  VaultHook.economy.withdrawPlayer(player, money);
            }
         }
      } else {
         for (OfflinePlayer p : this.getExpr().getArray(event)) {
            VaultHook.economy.withdrawPlayer(p, VaultHook.economy.getBalance(p));
         }
      }
   }

   static {
      register(ExprBalance.class, Money.class, "(money|balance|[bank] account)", "offlineplayers");
   }
}
