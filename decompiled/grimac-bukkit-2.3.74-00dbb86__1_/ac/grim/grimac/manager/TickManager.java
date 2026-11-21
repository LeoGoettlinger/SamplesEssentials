package ac.grim.grimac.manager;

import ac.grim.grimac.manager.tick.Tickable;
import ac.grim.grimac.manager.tick.impl.ClearRecentlyUpdatedBlocks;
import ac.grim.grimac.manager.tick.impl.ClientVersionSetter;
import ac.grim.grimac.manager.tick.impl.ResetTick;
import ac.grim.grimac.manager.tick.impl.TickInventory;
import ac.grim.grimac.manager.tick.impl.TickPermissions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap.Builder;

public class TickManager {
   public int currentTick;
   private final ClassToInstanceMap<Tickable> syncTick = new Builder().put(ResetTick.class, new ResetTick()).build();
   private final ClassToInstanceMap<Tickable> asyncTick = new Builder()
      .put(ClientVersionSetter.class, new ClientVersionSetter())
      .put(TickPermissions.class, new TickPermissions())
      .put(TickInventory.class, new TickInventory())
      .put(ClearRecentlyUpdatedBlocks.class, new ClearRecentlyUpdatedBlocks())
      .build();

   public void tickSync() {
      this.currentTick++;

      for (Tickable tickable : this.syncTick.values()) {
         tickable.tick();
      }
   }

   public void tickAsync() {
      for (Tickable tickable : this.asyncTick.values()) {
         tickable.tick();
      }
   }
}
