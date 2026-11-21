package dev.cwhead.GravesX.event.integration.skript.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.api.grave.GraveCreationAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("GravesX Create Grave")
@Description({"Creates a grave through the GravesX grave creation API."})
@Examples({"create a grave for player", "create a grave for player with 50 experience", "create a grave for player with 50 experience lasting 600000 milliseconds", "create a protected grave for player at player's location with 50 experience lasting 600000 milliseconds protected for 300000 milliseconds"})
public class EffCreateGrave extends Effect {
   private Expression<LivingEntity> victim;
   private Expression<Entity> killer;
   private Expression<Location> location;
   private Expression<Number> experience;
   private Expression<Number> timeAlive;
   private Expression<Number> timeProtection;
   private boolean protection;
   private int matchedPattern;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      this.matchedPattern = matchedPattern;
      this.victim = (Expression<LivingEntity>)expressions[0];
      switch (matchedPattern) {
         case 1:
            this.experience = (Expression<Number>)expressions[1];
            break;
         case 2:
            this.experience = (Expression<Number>)expressions[1];
            this.timeAlive = (Expression<Number>)expressions[2];
            break;
         case 3:
            this.location = (Expression<Location>)expressions[1];
            this.experience = (Expression<Number>)expressions[2];
            this.timeAlive = (Expression<Number>)expressions[3];
            break;
         case 4:
            this.killer = (Expression<Entity>)expressions[1];
            this.location = (Expression<Location>)expressions[2];
            this.experience = (Expression<Number>)expressions[3];
            this.timeAlive = (Expression<Number>)expressions[4];
            break;
         case 5:
            this.protection = true;
            this.location = (Expression<Location>)expressions[1];
            this.experience = (Expression<Number>)expressions[2];
            this.timeAlive = (Expression<Number>)expressions[3];
            this.timeProtection = (Expression<Number>)expressions[4];
            break;
         case 6:
            this.protection = true;
            this.killer = (Expression<Entity>)expressions[1];
            this.location = (Expression<Location>)expressions[2];
            this.experience = (Expression<Number>)expressions[3];
            this.timeAlive = (Expression<Number>)expressions[4];
            this.timeProtection = (Expression<Number>)expressions[5];
      }

      return true;
   }

   protected void execute(Event event) {
      GraveCreationAPI api = SkriptImpl.getGraveCreationAPI();
      if (api != null) {
         LivingEntity victimValue = (LivingEntity)this.victim.getSingle(event);
         if (victimValue != null) {
            Entity killerValue = this.killer != null ? (Entity)this.killer.getSingle(event) : null;
            Location locationValue = this.location != null ? (Location)this.location.getSingle(event) : null;
            int experienceValue = this.toInt(this.experience != null ? (Number)this.experience.getSingle(event) : null, 0);
            long timeAliveValue = this.toLong(this.timeAlive != null ? (Number)this.timeAlive.getSingle(event) : null, 0L);
            long timeProtectionValue = this.toLong(this.timeProtection != null ? (Number)this.timeProtection.getSingle(event) : null, 0L);
            api.createGrave(
               victimValue,
               killerValue,
               killerValue != null ? killerValue.getType() : null,
               locationValue,
               null,
               null,
               experienceValue,
               timeAliveValue,
               this.protection,
               timeProtectionValue
            );
         }
      }
   }

   private int toInt(@Nullable Number number, int fallback) {
      return number != null ? number.intValue() : fallback;
   }

   private long toLong(@Nullable Number number, long fallback) {
      return number != null ? number.longValue() : fallback;
   }

   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.matchedPattern) {
         case 1 -> "create grave for " + this.victim + " with experience " + this.experience;
         case 2 -> "create grave for " + this.victim + " with experience " + this.experience + " lasting " + this.timeAlive + " milliseconds";
         case 3 -> "create grave for "
            + this.victim
            + " at "
            + this.location
            + " with experience "
            + this.experience
            + " lasting "
            + this.timeAlive
            + " milliseconds";
         case 4 -> "create grave for "
            + this.victim
            + " killed by "
            + this.killer
            + " at "
            + this.location
            + " with experience "
            + this.experience
            + " lasting "
            + this.timeAlive
            + " milliseconds";
         case 5 -> "create protected grave for "
            + this.victim
            + " at "
            + this.location
            + " with experience "
            + this.experience
            + " lasting "
            + this.timeAlive
            + " milliseconds protected for "
            + this.timeProtection
            + " milliseconds";
         case 6 -> "create protected grave for "
            + this.victim
            + " killed by "
            + this.killer
            + " at "
            + this.location
            + " with experience "
            + this.experience
            + " lasting "
            + this.timeAlive
            + " milliseconds protected for "
            + this.timeProtection
            + " milliseconds";
         default -> "create grave for " + this.victim;
      };
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.EFFECT,
            SyntaxInfo.builder(EffCreateGrave.class)
               .addPatterns(
                  new String[]{
                     "create [a] grave for %livingentity%",
                     "create [a] grave for %livingentity% with %number% experience",
                     "create [a] grave for %livingentity% with %number% experience lasting %number% milliseconds",
                     "create [a] grave for %livingentity% at %location% with %number% experience lasting %number% milliseconds",
                     "create [a] grave for %livingentity% killed by %entity% at %location% with %number% experience lasting %number% milliseconds",
                     "create [a] protected grave for %livingentity% at %location% with %number% experience lasting %number% milliseconds protected for %number% milliseconds",
                     "create [a] protected grave for %livingentity% killed by %entity% at %location% with %number% experience lasting %number% milliseconds protected for %number% milliseconds"
                  }
               )
               .build()
         );
   }
}
