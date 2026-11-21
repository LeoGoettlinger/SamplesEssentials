package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect of Entity/Item")
@Description({"An expression to obtain a specific potion effect type of an entity or item.", "When an entity is affected by a potion effect but already has a weaker version of that effect type, the weaker version becomes hidden. If the weaker version has a longer duration, it returns after the stronger version expires.", "NOTE: Hidden effects are not able to be changed."})
@Example.Examples({@Example("set {_effect} to the player's active speed effect"), @Example("add 10 seconds to the player's slowness effect"), @Example("clear the player's hidden strength effects"), @Example("reset the player's weakness effects"), @Example("delete the player's active jump boost effect")})
@RequiredPlugins({"Paper 1.20.4+ for hidden effects"})
@Since({"2.14"})
public class ExprPotionEffect extends PropertyExpression<Object, SkriptPotionEffect> {
   private Expression<PotionEffectType> types;
   private PotionEffectProvider.RetrievalState state;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprPotionEffect.class,
                  SkriptPotionEffect.class,
                  "[:active|:hidden|both:(active and hidden|hidden and active)] %potioneffecttypes% [potion] effect[s]",
                  "entities/itemtypes",
                  false
               )
               .supplier(ExprPotionEffect::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.types = (Expression<PotionEffectType>)expressions[matchedPattern % 2];
      this.setExpr((Expression<? extends Object>)expressions[(matchedPattern + 1) % 2]);
      this.state = PotionEffectProvider.RetrievalState.fromParseTag(parseResult.tags.isEmpty() ? "" : parseResult.tags.getFirst());
      if (this.state.includesHidden() && !this.getExpr().canReturn(Entity.class)) {
         Skript.error("Only living entities have hidden effects");
         return false;
      } else {
         return true;
      }
   }

   protected SkriptPotionEffect[] get(Event event, Object[] source) {
      List<SkriptPotionEffect> potionEffects = new ArrayList<>();
      PotionEffectType[] types = this.types.getArray(event);

      for (Object object : source) {
         potionEffects.addAll(PotionEffectProvider.of(object, this::error).get(types, this.state));
      }

      return potionEffects.toArray(new SkriptPotionEffect[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, DELETE, RESET -> (Class[])CollectionUtils.array(Timespan.class);
         case REMOVE -> this.state.includesHidden()
            ? (Class[])CollectionUtils.array(SkriptPotionEffect[].class, Timespan.class)
            : (Class[])CollectionUtils.array(Timespan.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Object[] holders = this.getExpr().getArray(event);
      PotionEffectType[] types = this.types.getArray(event);
      switch (mode) {
         case ADD:
         case REMOVE:
            assert delta != null;

            for (Object holder : holders) {
               PotionEffectProvider.of(holder, this::error).modify(types, this.state, delta, mode);
            }
            break;
         case DELETE:
         case RESET:
            for (Object holder : holders) {
               PotionEffectProvider.of(holder, this::error).clear(types, this.state);
            }
            break;
         default:
            assert false;
      }
   }

   @Override
   public boolean isSingle() {
      return this.types.isSingle() && !this.state.includesHidden();
   }

   @Override
   public Class<? extends SkriptPotionEffect> getReturnType() {
      return SkriptPotionEffect.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append(("the " + this.state.displayName()).stripTrailing(), this.types);
      if (this.isSingle()) {
         builder.append("effect");
      } else {
         builder.append("effects");
      }

      builder.append("of", this.getExpr());
      return builder.toString();
   }

   @Internal
   public PotionEffectProvider.RetrievalState getState() {
      return this.state;
   }
}
