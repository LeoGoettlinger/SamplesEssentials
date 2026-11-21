package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import java.util.function.BiConsumer;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Effects")
@Description({"The active effects of a beacon.", "The secondary effect can be set to anything, but the icon in the GUI will not display correctly.", "The secondary effect can only be set when the beacon is at max tier.", "The primary and secondary effect can not be the same, primary will always retain the potion type and secondary will be cleared."})
@Example("set primary beacon effect of {_block} to haste\nset secondary effect of {_block} to resistance\n")
@Events({"Beacon Effect", "Beacon Toggle", "Beacon Change Effect"})
@Since({"2.10"})
public class ExprBeaconEffects extends PropertyExpression<Block, PotionEffectType> {
   private static final boolean SUPPORTS_CHANGE_EVENT = Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent");
   private boolean primary;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Block>)expressions[0]);
      this.primary = parseResult.hasTag("primary");
      return true;
   }

   protected PotionEffectType[] get(Event event, Block[] blocks) {
      return this.get(blocks, block -> {
         if (block.getState() instanceof Beacon beacon) {
            if (SUPPORTS_CHANGE_EVENT && event instanceof PlayerChangeBeaconEffectEvent changeEvent && block.equals(changeEvent.getBeacon())) {
               return this.primary ? changeEvent.getPrimary() : changeEvent.getSecondary();
            } else {
               PotionEffect effect = this.primary ? beacon.getPrimaryEffect() : beacon.getSecondaryEffect();
               return effect == null ? null : effect.getType();
            }
         } else {
            return null;
         }
      });
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET, DELETE -> (Class[])CollectionUtils.array(PotionEffectType.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      PotionEffectType type = delta == null ? null : (PotionEffectType)delta[0];
      BiConsumer<Beacon, PotionEffectType> changer = this.primary ? Beacon::setPrimaryEffect : Beacon::setSecondaryEffect;

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getState() instanceof Beacon beacon) {
            changer.accept(beacon, type);
            beacon.update(true);
         }
      }
   }

   @Override
   public Class<PotionEffectType> getReturnType() {
      return PotionEffectType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.primary ? "primary" : "secondary") + " beacon effect of " + this.getExpr().toString(event, debug);
   }

   static {
      registerDefault(ExprBeaconEffects.class, PotionEffectType.class, "(:primary|secondary) [beacon] effect", "blocks");
   }
}
