package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Experience")
@Description({"How much experience was spawned in an experience spawn or block break event. Can be changed."})
@Example.Examples({@Example("on experience spawn:\n\tadd 5 to the spawned experience\n"), @Example("on break of coal ore:\n\tclear dropped experience\n"), @Example("on break of diamond ore:\n\tif tool of player = diamond pickaxe:\n\t\tadd 100 to dropped experience\n"), @Example("on breed:\n\tbreeding father is a cow\n\tset dropped experience to 10\n"), @Example("on fish catch:\n\tadd 70 to dropped experience\n")})
@Since({"2.1, 2.5.3 (block break event), 2.7 (experience change event), 2.10 (breeding, fishing)"})
@Events({"experience spawn", "break / mine", "experience change", "entity breed"})
public class ExprExperience extends SimpleExpression<Experience> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(ExperienceSpawnEvent.class, BlockBreakEvent.class, PlayerExpChangeEvent.class, EntityBreedEvent.class, PlayerFishEvent.class);
   }

   @Nullable
   protected Experience[] get(Event event) {
      Experience[] exp;
      if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
         exp = new Experience[]{new Experience(experienceSpawnEvent.getSpawnedXP())};
      } else if (event instanceof BlockBreakEvent blockBreakEvent) {
         exp = new Experience[]{new Experience(blockBreakEvent.getExpToDrop())};
      } else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
         exp = new Experience[]{new Experience(playerExpChangeEvent.getAmount())};
      } else if (event instanceof EntityBreedEvent entityBreedEvent) {
         exp = new Experience[]{new Experience(entityBreedEvent.getExperience())};
      } else if (event instanceof PlayerFishEvent fishEvent) {
         exp = new Experience[]{new Experience(fishEvent.getExpToDrop())};
      } else {
         exp = new Experience[0];
      }

      return exp;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE -> (Class[])CollectionUtils.array(Experience.class, Integer.class);
         case ADD, REMOVE -> (Class[])CollectionUtils.array(Experience[].class, Integer[].class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int exp;
      if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
         exp = experienceSpawnEvent.getSpawnedXP();
      } else if (event instanceof BlockBreakEvent blockBreakEvent) {
         exp = blockBreakEvent.getExpToDrop();
      } else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
         exp = playerExpChangeEvent.getAmount();
      } else if (event instanceof EntityBreedEvent entityBreedEvent) {
         exp = entityBreedEvent.getExperience();
      } else {
         if (!(event instanceof PlayerFishEvent fishEvent)) {
            return;
         }

         exp = fishEvent.getExpToDrop();
      }

      if (delta != null) {
         for (Object object : delta) {
            int value = object instanceof Experience experience ? experience.getXP() : (Integer)object;
            switch (mode) {
               case SET:
                  exp = value;
               case DELETE:
               case RESET:
               default:
                  break;
               case ADD:
                  exp += value;
                  break;
               case REMOVE:
               case REMOVE_ALL:
                  exp -= value;
            }
         }
      } else {
         exp = 0;
      }

      exp = Math.max(0, exp);
      if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
         experienceSpawnEvent.setSpawnedXP(exp);
      } else if (event instanceof BlockBreakEvent blockBreakEvent) {
         blockBreakEvent.setExpToDrop(exp);
      } else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
         playerExpChangeEvent.setAmount(exp);
      } else if (event instanceof EntityBreedEvent entityBreedEvent) {
         entityBreedEvent.setExperience(exp);
      } else if (event instanceof PlayerFishEvent fishEvent) {
         fishEvent.setExpToDrop(exp);
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Experience> getReturnType() {
      return Experience.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the experience";
   }

   static {
      Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE, "[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
   }
}
