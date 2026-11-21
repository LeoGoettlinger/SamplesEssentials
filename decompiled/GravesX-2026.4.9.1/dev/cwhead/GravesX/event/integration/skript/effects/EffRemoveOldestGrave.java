package dev.cwhead.GravesX.event.integration.skript.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("GravesX Remove Oldest Grave")
@Description({"Removes a living entity's oldest grave through the GravesX grave management API."})
@Examples({"remove oldest grave of player"})
public class EffRemoveOldestGrave extends Effect {
   private Expression<LivingEntity> entity;

   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      this.entity = (Expression<LivingEntity>)expressions[0];
      return true;
   }

   protected void execute(Event event) {
      GraveManagementAPI api = SkriptImpl.getGraveManagementAPI();
      LivingEntity entityValue = (LivingEntity)this.entity.getSingle(event);
      if (api != null && entityValue != null) {
         api.removeOldestGrave(entityValue);
      }
   }

   public String toString(@Nullable Event event, boolean debug) {
      return "remove oldest grave of " + this.entity;
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            SyntaxRegistry.EFFECT,
            SyntaxInfo.builder(EffRemoveOldestGrave.class).addPatterns(new String[]{"remove [the] oldest grave of %livingentity%"}).build()
         );
   }
}
