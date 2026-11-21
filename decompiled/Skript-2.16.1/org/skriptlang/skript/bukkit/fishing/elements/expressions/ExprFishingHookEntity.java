package org.skriptlang.skript.bukkit.fishing.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Hooked Entity")
@Description({"Returns the hooked entity in the hooked event."})
@Example("on entity hooked:\n\tif hooked entity is a player:\n\t\tteleport hooked entity to player\n")
@Events({"Fishing"})
@Since({"2.10"})
public class ExprFishingHookEntity extends SimpleExpression<Entity> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFishingHookEntity.class, Entity.class
                     )
                     .addPatterns(new String[]{"[the] hook[ed] entity"}))
                  .supplier(ExprFishingHookEntity::new))
               .priority(EventValueExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(PlayerFishEvent.class)) {
         Skript.error("The 'hooked entity' expression can only be used in the fishing event.");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   protected Entity[] get(Event event) {
      return event instanceof PlayerFishEvent fishEvent ? new Entity[]{fishEvent.getHook().getHookedEntity()} : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE -> (Class[])CollectionUtils.array(Entity.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerFishEvent fishEvent) {
         FishHook hook = fishEvent.getHook();
         switch (mode) {
            case SET:
               hook.setHookedEntity((Entity)delta[0]);
               break;
            case DELETE:
               if (hook.getHookedEntity() != null && !(hook.getHookedEntity() instanceof Player)) {
                  hook.getHookedEntity().remove();
               }
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + mode);
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "hooked entity";
   }
}
