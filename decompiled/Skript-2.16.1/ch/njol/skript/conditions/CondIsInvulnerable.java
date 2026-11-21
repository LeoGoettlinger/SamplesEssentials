package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;

@Name("Is Invulnerable")
@Description({"Checks whether an entity or a gamemode is invulnerable.\nFor gamemodes, Paper and Minecraft 1.20.6 are required"})
@Example.Examples({@Example("target entity is invulnerable"), @Example("loop all gamemodes:\n\tif loop-value is not invulnerable:\n\t\tbroadcast \"the gamemode %loop-value% is vulnerable!\"\n")})
@Since({"2.5, 2.10 (gamemode)"})
public class CondIsInvulnerable extends PropertyCondition<Object> {
   private static final boolean SUPPORTS_GAMEMODE = Skript.methodExists(GameMode.class, "isInvulnerable");

   @Override
   public boolean check(Object object) {
      if (object instanceof Entity entity) {
         return entity.isInvulnerable();
      } else {
         return SUPPORTS_GAMEMODE && object instanceof GameMode gameMode ? gameMode.isInvulnerable() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "invulnerable";
   }

   static {
      register(CondIsInvulnerable.class, "(invulnerable|invincible)", "entities" + (SUPPORTS_GAMEMODE ? "/gamemodes" : ""));
   }
}
