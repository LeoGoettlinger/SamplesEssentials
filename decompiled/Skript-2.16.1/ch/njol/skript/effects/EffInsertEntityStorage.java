package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Insert Entity Storage")
@Description({"Add an entity into the entity storage of a block (e.g. beehive).", "The entity must be of the right type for the block (e.g. bee for beehive).", "Due to unstable behavior on older versions, adding entities to an entity storage requires Minecraft version 1.21+."})
@Example("add last spawned bee into the entity storage of {_beehive}")
@RequiredPlugins({"Minecraft 1.21+"})
@Since({"2.11"})
public class EffInsertEntityStorage extends Effect {
   private static final Map<Class<? extends BlockState>, Class<? extends Entity>> STORAGES = new HashMap<>();
   private Expression<? extends Entity> entities;
   private Expression<Block> block;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<? extends Entity>)exprs[0];
      this.block = (Expression<Block>)exprs[1];
      return true;
   }

   @Override
   protected void execute(Event event) {
      Block block = this.block.getSingle(event);
      if (block != null && block.getState() instanceof EntityBlockStorage<?> blockStorage) {
         Class<? extends Entity> entityClass = this.getEntityClass(blockStorage);
         if (entityClass != null) {
            this.addEntities(entityClass, blockStorage, this.entities.getArray(event));
         }
      }
   }

   private <T extends EntityBlockStorage<R>, R extends Entity> void addEntities(Class<R> entityClass, BlockState blockState, Entity[] entities) {
      T typedStorage = (T)blockState;

      for (Entity entity : entities) {
         if (entityClass.isInstance(entity)) {
            if (typedStorage.getEntityCount() >= typedStorage.getMaxEntities()) {
               break;
            }

            typedStorage.addEntity(entity);
         }
      }

      typedStorage.update(true, false);
   }

   @Nullable
   private Class<? extends Entity> getEntityClass(BlockState blockState) {
      for (Class<? extends BlockState> stateClass : STORAGES.keySet()) {
         if (stateClass.isInstance(blockState)) {
            return STORAGES.get(stateClass);
         }
      }

      return null;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "add " + this.entities.toString(event, debug) + " into the entity storage of " + this.block.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffInsertEntityStorage.class, "(add|insert) %livingentities% [in[ ]]to [the] (stored entities|entity storage) of %block%");
      STORAGES.put(Beehive.class, Bee.class);
   }
}
