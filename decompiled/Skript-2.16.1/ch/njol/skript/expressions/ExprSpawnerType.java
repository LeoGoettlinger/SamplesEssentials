package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Type")
@Description({"The entity type of a spawner (mob spawner).\nChange the entity type, reset it (pig) or clear it (Minecraft 1.20.0+).\n"})
@Example.Examples({@Example("on right click:\n\tif event-block is a spawner:\n\t\tsend \"Spawner's type if %spawner type of event-block%\" to player\n"), @Example("set the creature type of {_spawner} to a trader llama"), @Example("reset {_spawner}'s entity type # Pig"), @Example("clear the spawner type of {_spawner} # Minecraft 1.20.0+")})
@Since({"2.4, 2.9.2 (trial spawner), 2.12 (delete)"})
@RequiredPlugins({"Minecraft 1.20.0+ (delete)"})
public class ExprSpawnerType extends SimplePropertyExpression<Block, EntityData> {
   private static final boolean HAS_TRIAL_SPAWNER = Skript.classExists("org.bukkit.block.TrialSpawner");
   private static final boolean RUNNING_1_20_0 = Skript.isRunningMinecraft(1, 20, 0);

   @Nullable
   public EntityData convert(Block block) {
      if (block.getState() instanceof CreatureSpawner creatureSpawner) {
         EntityType type = creatureSpawner.getSpawnedType();
         return type == null ? null : EntityUtils.toSkriptEntityData(type);
      } else if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner trialSpawner) {
         EntityType type;
         if (trialSpawner.isOminous()) {
            type = trialSpawner.getOminousConfiguration().getSpawnedType();
         } else {
            type = trialSpawner.getNormalConfiguration().getSpawnedType();
         }

         return type == null ? null : EntityUtils.toSkriptEntityData(type);
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET) {
         if (mode == Changer.ChangeMode.DELETE) {
            if (RUNNING_1_20_0) {
               return CollectionUtils.array(EntityData.class);
            }

            Skript.error("You can only delete the spawner type of a spawner on Minecraft 1.20.0 or newer");
         }

         return null;
      } else {
         return CollectionUtils.array(EntityData.class);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      EntityType entityType = null;
      if (delta != null) {
         entityType = EntityUtils.toBukkitEntityType((EntityData<?>)delta[0]);
      } else if (mode == Changer.ChangeMode.RESET) {
         entityType = EntityType.PIG;
      }

      for (Block block : this.getExpr().getArray(event)) {
         if (block.getState() instanceof CreatureSpawner creatureSpawner) {
            creatureSpawner.setSpawnedType(entityType);
            creatureSpawner.update();
         } else if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner trialSpawner) {
            TrialSpawnerConfiguration config;
            if (trialSpawner.isOminous()) {
               config = trialSpawner.getOminousConfiguration();
            } else {
               config = trialSpawner.getNormalConfiguration();
            }

            config.setSpawnedType(entityType);
            trialSpawner.update();
         }
      }
   }

   @Override
   public Class<EntityData> getReturnType() {
      return EntityData.class;
   }

   @Override
   protected String getPropertyName() {
      return "entity type";
   }

   static {
      register(ExprSpawnerType.class, EntityData.class, "(spawner|entity|creature) type[s]", "blocks");
   }
}
