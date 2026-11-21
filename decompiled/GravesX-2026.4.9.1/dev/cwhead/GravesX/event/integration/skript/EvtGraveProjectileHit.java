package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveProjectileHitEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Projectile Hit Event")
@Description({"Triggered when a grave block is broken. Provides access to the grave, player, block, and block type."})
@Examples({"on grave projectile hit:", "\tbroadcast \"%event-grave% \""})
public class EvtGraveProjectileHit extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;
   private Literal<Block> block;
   private Literal<Entity> entity;
   private Literal<LivingEntity> livingEntity;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveProjectileHitEvent event) {
         if (this.player != null) {
            this.player.check(event, new Predicate<Player>() {
               public boolean test(Player p) {
                  return p.equals(event.getPlayer());
               }
            });
         }

         if (this.grave != null) {
            this.grave.check(event, new Predicate<Grave>() {
               public boolean test(Grave g) {
                  return g.equals(event.getGrave());
               }
            });
         }

         if (this.block != null) {
            this.block.check(event, new Predicate<Block>() {
               public boolean test(Block b) {
                  return b.equals(event.getBlock());
               }
            });
         }

         if (this.entity != null) {
            this.entity.check(event, new Predicate<Entity>() {
               public boolean test(Entity e) {
                  return e.equals(event.getEntity());
               }
            });
         }

         if (this.livingEntity != null) {
            this.livingEntity.check(event, new Predicate<LivingEntity>() {
               public boolean test(LivingEntity le) {
                  return le.equals(event.getLivingEntity());
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave break event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.block != null ? " with block " + this.block.toString(e, debug) : "")
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.livingEntity != null ? " with living entity " + this.livingEntity.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveProjectileHit.class, "Grave Projectile Hit")
                  .addEvent(GraveProjectileHitEvent.class)
                  .addPatterns(new String[]{"[grave] projectil(e|es) hi(t|tting)"}))
               .addDescription("Triggered when a grave block is broken. Provides access to the grave, player, block, and block type.")
               .addExamples(new String[]{"on grave projectile hit:", "\tbroadcast \"%event-grave% \""})
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveProjectileHitEvent.class, Player.class).getter(GraveEntityEvent::getPlayer).patterns(new String[]{"player"}).build()
      );
      registry.register(EventValue.builder(GraveProjectileHitEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(EventValue.builder(GraveProjectileHitEvent.class, Block.class).getter(GraveEvent::getBlock).patterns(new String[]{"block"}).build());
      registry.register(
         EventValue.builder(GraveProjectileHitEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build()
      );
      registry.register(
         EventValue.builder(GraveProjectileHitEvent.class, LivingEntity.class)
            .getter(GraveEntityEvent::getLivingEntity)
            .patterns(new String[]{"living[-]entity"})
            .build()
      );
   }
}
