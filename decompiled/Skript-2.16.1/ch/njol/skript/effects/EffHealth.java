package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.DamageUtils;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import java.util.function.Consumer;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Damageable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Damage/Heal/Repair")
@Description({"Damage, heal, or repair an entity or item.", "Servers running Spigot 1.20.4+ can optionally choose to specify a fake damage cause."})
@Example.Examples({@Example("damage player by 5 hearts"), @Example("damage player by 3 hearts with fake cause fall"), @Example("heal the player"), @Example("repair tool of player")})
@Since({"1.0, 2.10 (damage cause)"})
@RequiredPlugins({"Spigot 1.20.4+ (for damage cause)"})
public class EffHealth extends Effect implements SyntaxRuntimeErrorProducer {
   private static final boolean SUPPORTS_DAMAGE_SOURCE = Skript.classExists("org.bukkit.damage.DamageSource");
   private static final Patterns<EffHealth.EffectType> PATTERNS;
   private Expression<?> damageables;
   @Nullable
   private Expression<Number> amount = null;
   private EffHealth.EffectType effectType;
   @Nullable
   private Expression<?> damageCause = null;
   @Nullable
   private Expression<?> damageSource = null;
   private Node node;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.effectType = PATTERNS.getInfo(matchedPattern);
      this.damageables = exprs[0];
      this.amount = (Expression<Number>)exprs[1];
      if (this.effectType == EffHealth.EffectType.DAMAGE && SUPPORTS_DAMAGE_SOURCE) {
         if (matchedPattern == 1) {
            this.damageCause = exprs[2];
         } else if (matchedPattern == 2) {
            this.damageSource = exprs[2];
         }
      }

      this.node = this.getParser().getNode();
      return true;
   }

   @Override
   protected void execute(Event event) {
      Double amount = null;
      if (this.amount != null) {
         Number amountPostCheck = this.amount.getSingle(event);
         if (amountPostCheck == null) {
            return;
         }

         amount = amountPostCheck.doubleValue();
      }

      Object damageData = null;
      if (this.damageCause != null) {
         damageData = this.damageCause.getSingle(event);
      } else if (this.damageSource != null) {
         damageData = this.damageSource.getSingle(event);
      }

      for (Object obj : this.damageables.getArray(event)) {
         if (obj instanceof ItemType itemType) {
            this.handleItem(itemType.getRandom(), amount, integer -> ItemUtils.setDamage(itemType, integer));
         } else if (obj instanceof Slot slot) {
            ItemStack itemStack = slot.getItem();
            if (itemStack != null) {
               this.handleItem(itemStack, amount, integer -> {
                  ItemUtils.setDamage(itemStack, integer);
                  slot.setItem(itemStack);
               });
            }
         } else if (obj instanceof Damageable damageable) {
            this.handleDamageable(damageable, amount, damageData);
         }
      }
   }

   private void handleItem(ItemStack itemStack, @Nullable Double amount, Consumer<Integer> consumer) {
      Integer value = null;
      if (this.effectType == EffHealth.EffectType.DAMAGE) {
         assert amount != null;

         value = Math2.fit(0, ItemUtils.getDamage(itemStack) + amount.intValue(), ItemUtils.getMaxDamage(itemStack));
      } else if (this.effectType == EffHealth.EffectType.REPAIR) {
         value = amount == null ? 0 : Math2.fit(0, ItemUtils.getDamage(itemStack) - amount.intValue(), ItemUtils.getMaxDamage(itemStack));
      }

      if (value != null) {
         consumer.accept(value);
      }
   }

   private void handleDamageable(Damageable damageable, @Nullable Double amount, @Nullable Object object) {
      if (this.effectType == EffHealth.EffectType.DAMAGE) {
         assert amount != null;

         if (SUPPORTS_DAMAGE_SOURCE && object != null) {
            if (object instanceof DamageCause damageCause) {
               HealthUtils.damage(damageable, amount, DamageUtils.getDamageSourceFromCause(damageCause));
               return;
            }

            if (object instanceof DamageSource damageSource) {
               HealthUtils.damage(damageable, amount, damageSource);
               return;
            }
         }

         HealthUtils.damage(damageable, amount);
      } else if (this.effectType == EffHealth.EffectType.HEAL) {
         if (amount == null) {
            HealthUtils.heal(damageable, HealthUtils.getMaxHealth(damageable));
         } else {
            HealthUtils.heal(damageable, amount);
         }
      }
   }

   @Override
   public Node getNode() {
      return this.node;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      switch (this.effectType) {
         case DAMAGE:
            assert this.amount != null;

            builder.append("damage", this.damageables, "by", this.amount);
            if (this.damageCause != null) {
               builder.append("with fake damage cause", this.damageCause);
            } else if (this.damageSource != null) {
               builder.append("using", this.damageSource);
            }
            break;
         case HEAL:
            builder.append("heal", this.damageables);
            if (this.amount != null) {
               builder.append("by", this.amount);
            }
            break;
         case REPAIR:
            builder.append("repair", this.damageables);
            if (this.amount != null) {
               builder.append("by", this.amount);
            }
      }

      return builder.toString();
   }

   static {
      if (!SUPPORTS_DAMAGE_SOURCE) {
         PATTERNS = new Patterns<>(
            new Object[][]{
               {"damage %livingentities/itemtypes/slots% by %number% [heart[s]]", EffHealth.EffectType.DAMAGE},
               {"heal %livingentities% [by %-number% [heart[s]]]", EffHealth.EffectType.HEAL},
               {"repair %itemtypes/slots% [by %-number%]", EffHealth.EffectType.REPAIR}
            }
         );
      } else {
         PATTERNS = new Patterns<>(
            new Object[][]{
               {"damage %livingentities/itemtypes/slots% by %number% [heart[s]]", EffHealth.EffectType.DAMAGE},
               {"damage %livingentities% by %number% [heart[s]] with [fake] [damage] cause %damagecause%", EffHealth.EffectType.DAMAGE},
               {"damage %livingentities% by %number% [heart[s]] (using|with) %damagesource% [as the source]", EffHealth.EffectType.DAMAGE},
               {"heal %livingentities% [by %-number% [heart[s]]]", EffHealth.EffectType.HEAL},
               {"repair %itemtypes/slots% [by %-number%]", EffHealth.EffectType.REPAIR}
            }
         );
      }

      Skript.registerEffect(EffHealth.class, PATTERNS.getPatterns());
   }

   private static enum EffectType {
      DAMAGE,
      HEAL,
      REPAIR;
   }
}
