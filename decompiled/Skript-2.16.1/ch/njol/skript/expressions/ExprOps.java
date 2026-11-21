package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("All Operators")
@Description({"The list of operators on the server."})
@Example("set {_ops::*} to all operators")
@Since({"2.7"})
public class ExprOps extends SimpleExpression<OfflinePlayer> {
   private boolean nonOps;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.nonOps = parseResult.hasTag("non");
      return true;
   }

   protected OfflinePlayer[] get(Event event) {
      if (this.nonOps) {
         List<Player> nonOpsList = new ArrayList<>();

         for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOp()) {
               nonOpsList.add(player);
            }
         }

         return nonOpsList.toArray(new Player[0]);
      } else {
         return Bukkit.getOperators().toArray(new OfflinePlayer[0]);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.nonOps) {
         return null;
      } else {
         switch (mode) {
            case ADD:
            case SET:
            case REMOVE:
            case RESET:
            case DELETE:
               return CollectionUtils.array(OfflinePlayer[].class);
            default:
               return null;
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE) {
         switch (mode) {
            case SET:
               for (OfflinePlayer player : Bukkit.getOperators()) {
                  player.setOp(false);
               }
            case ADD:
               for (Object player : delta) {
                  ((OfflinePlayer)player).setOp(true);
               }
               break;
            case REMOVE:
               for (Object player : delta) {
                  ((OfflinePlayer)player).setOp(false);
               }
               break;
            case RESET:
            case DELETE:
               for (OfflinePlayer player : Bukkit.getOperators()) {
                  player.setOp(false);
               }
               break;
            default:
               assert false;
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends OfflinePlayer> getReturnType() {
      return OfflinePlayer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.nonOps ? "all non-operators" : "all operators";
   }

   static {
      Skript.registerExpression(ExprOps.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[all [[of] the]|the] [server] [:non(-| )]op[erator]s");
   }
}
