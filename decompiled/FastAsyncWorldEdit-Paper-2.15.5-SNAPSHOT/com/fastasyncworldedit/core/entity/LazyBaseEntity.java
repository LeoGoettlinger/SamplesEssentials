package com.fastasyncworldedit.core.entity;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.world.entity.EntityType;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;

public class LazyBaseEntity extends BaseEntity {
   private Supplier<LinCompoundTag> saveTag;

   public LazyBaseEntity(EntityType type, Supplier<LinCompoundTag> saveTag) {
      super(type);
      this.saveTag = saveTag;
   }

   @Nullable
   @Override
   public LinCompoundTag getNbt() {
      Supplier<LinCompoundTag> tmp = this.saveTag;
      if (tmp != null) {
         this.saveTag = null;
         if (Fawe.isMainThread()) {
            this.setNbt(tmp.get());
         } else {
            this.setNbt(TaskManager.taskManager().sync(tmp));
         }
      }

      return super.getNbt();
   }
}
