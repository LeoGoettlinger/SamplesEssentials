package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity/Player/World from UUID")
@Description({"Get an entity, player, offline player or world from a UUID.", "Unloaded entities or players that are offline (when using 'player from %uuid%') will return nothing."})
@Example.Examples({@Example("set {_player} to player from \"a0789aeb-7b46-43f6-86fb-cb671fed5775\" parsed as uuid"), @Example("set {_offline player} to offline player from {_some uuid}"), @Example("set {_entity} to entity from {_some uuid}"), @Example("set {_world} to world from {_some uuid}")})
@Since({"2.11"})
public class ExprFromUUID extends SimpleExpression<Object> {
   private Expression<UUID> uuids;
   private boolean player;
   private boolean offline;
   private boolean world;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.uuids = (Expression<UUID>)exprs[0];
      this.player = matchedPattern == 0;
      this.offline = parseResult.hasTag("offline");
      this.world = matchedPattern == 2;
      return true;
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      List<Object> entities = new ArrayList<>();

      for (UUID uuid : this.uuids.getArray(event)) {
         if (this.player) {
            if (this.offline) {
               entities.add(Bukkit.getOfflinePlayer(uuid));
            } else {
               Player player = Bukkit.getPlayer(uuid);
               if (player != null) {
                  entities.add(player);
               }
            }
         } else if (this.world) {
            World world = Bukkit.getWorld(uuid);
            if (world != null) {
               entities.add(world);
            }
         } else {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
               entities.add(entity);
            }
         }
      }

      if (this.player) {
         return (Object[])(this.offline ? entities.toArray(OfflinePlayer[]::new) : entities.toArray(Player[]::new));
      } else {
         return (Object[])(this.world ? entities.toArray(World[]::new) : entities.toArray(Entity[]::new));
      }
   }

   @Override
   public boolean isSingle() {
      return this.uuids.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      if (this.world) {
         return World.class;
      } else if (this.player) {
         return this.offline ? OfflinePlayer.class : Player.class;
      } else {
         return Entity.class;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.world) {
         builder.append("worlds");
      } else if (this.player) {
         if (this.offline) {
            builder.append("offline");
         }

         builder.append("players");
      } else {
         builder.append("entities");
      }

      builder.append("from", this.uuids);
      return builder.toString();
   }

   static {
      Skript.registerExpression(
         ExprFromUUID.class, Object.class, ExpressionType.PROPERTY, "[:offline[ ]]player[s] from %uuids%", "entit(y|ies) from %uuids%", "world[s] from %uuids%"
      );
   }
}
