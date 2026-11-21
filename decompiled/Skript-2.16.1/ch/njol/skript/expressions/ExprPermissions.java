package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.Nullable;

@Name("All Permissions")
@Description({"Returns all permissions of the defined player(s). Note that the modifications to resulting list do not actually change permissions."})
@Example("set {_permissions::*} to all permissions of the player")
@Since({"2.2-dev33"})
public class ExprPermissions extends SimpleExpression<String> {
   private Expression<Player> players;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.players = (Expression<Player>)exprs[0];
      return true;
   }

   @Nullable
   protected String[] get(Event e) {
      Set<String> permissions = new HashSet<>();

      for (Player player : this.players.getArray(e)) {
         for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            permissions.add(permission.getPermission());
         }
      }

      return permissions.toArray(new String[permissions.size()]);
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<String> getReturnType() {
      return String.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "permissions of " + this.players.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprPermissions.class,
         String.class,
         ExpressionType.PROPERTY,
         "[(all [[of] the]|the)] permissions (from|of) %players%",
         "[(all [[of] the]|the)] %players%'[s] permissions"
      );
   }
}
