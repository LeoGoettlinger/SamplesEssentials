package xyz.lychee.lagfixer.nms.v1_20_R3;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreed;
import net.minecraft.world.entity.ai.goal.PathfinderGoalTempt;
import net.minecraft.world.entity.ai.goal.PathfinderGoalWrapped;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.animal.EntityBee;
import net.minecraft.world.entity.animal.EntityCat;
import net.minecraft.world.entity.animal.EntityChicken;
import net.minecraft.world.entity.animal.EntityCow;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.animal.EntityMushroomCow;
import net.minecraft.world.entity.animal.EntityOcelot;
import net.minecraft.world.entity.animal.EntityPanda;
import net.minecraft.world.entity.animal.EntityParrot;
import net.minecraft.world.entity.animal.EntityPig;
import net.minecraft.world.entity.animal.EntityRabbit;
import net.minecraft.world.entity.animal.EntitySheep;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.EntityHorse;
import net.minecraft.world.entity.animal.horse.EntityHorseDonkey;
import net.minecraft.world.entity.animal.horse.EntityHorseMule;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.entity.animal.horse.EntityLlamaTrader;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.monster.EntityStrider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftCreature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

public class MobAiReducer extends MobAiReducerModule.NMS implements Listener {
   private final Map<EntityCreature, Boolean> optimizedMobs = new MapMaker().weakKeys().concurrencyLevel(4).makeMap();
   private final Map<Class<? extends Entity>, PathfinderTargetCondition> temptTargeting = new HashMap<>();
   private final PathfinderTargetCondition breedTargeting = PathfinderTargetCondition.b().d();

   public MobAiReducer(MobAiReducerModule module) {
      super(module);
   }

   @Override
   public void load() {
      this.breedTargeting.a(this.getModule().getBreedRange());
      this.register(EntityCow.class, Items.pt);
      this.register(EntityMushroomCow.class, Items.pt);
      this.register(EntitySheep.class, Items.pt);
      this.register(EntityPig.class, Items.tY, Items.tZ, Items.vd);
      this.register(EntityChicken.class, Items.ps, Items.rW, Items.rV, Items.ve);
      this.register(EntityRabbit.class, Items.tY, Items.ud, Items.dj);
      this.register(EntityHorse.class, Items.oq, Items.pX, Items.ud, Items.rx, Items.hC, Items.pt);
      this.register(EntityHorseDonkey.class, Items.oq, Items.pX, Items.ud, Items.rx, Items.hC, Items.pt);
      this.register(EntityHorseMule.class, Items.oq, Items.pX, Items.ud, Items.rx, Items.hC, Items.pt);
      this.register(EntityLlama.class, Items.hC, Items.pt);
      this.register(EntityLlamaTrader.class, Items.hC, Items.pt);
      this.register(EntityParrot.class, Items.ps, Items.rW, Items.rV, Items.ve);
      this.register(EntityOcelot.class, Items.qW, Items.qX);
      this.register(EntityCat.class, Items.qW, Items.qX);
      this.register(EntityPanda.class, Items.dQ, Items.ry);
      this.register(EntityFox.class, Items.wi, Items.wj);
      this.register(EntityTurtle.class, Items.cR);
      this.register(
         EntityBee.class, Items.dk, Items.dj, Items.dl, Items.dm, Items.dn, Items.do, Items.dp, Items.dq, Items.dr, Items.ds, Items.dt, Items.du, Items.hW
      );
      this.register(EntityStrider.class, Items.dC);
      this.register(Goat.class, Items.pt);
      this.register(Axolotl.class, Items.qF);
      this.register(Frog.class, Items.qN);
      this.register(Camel.class, Items.eV);
      this.register(Sniffer.class, Items.vb, Items.vc);
   }

   private void register(Class<? extends Entity> clazz, Item... items) {
      Set<Item> itemSet = Sets.newHashSet(items);
      this.temptTargeting
         .computeIfAbsent(clazz, k -> PathfinderTargetCondition.b().d())
         .a(this.getModule().getTemptRange())
         .a(entity -> itemSet.contains(entity.eT().d()) || this.getModule().isTemptTriggerBothHands() && itemSet.contains(entity.eU().d()));
   }

   @Override
   public void optimize(org.bukkit.entity.Entity ent, boolean init) {
      if (ent instanceof CraftCreature creature) {
         EntityCreature handle = creature.getHandle();
         if (!this.optimizedMobs.containsKey(handle)) {
            MobAiReducerModule module = this.getModule();
            boolean keepDedicated = module.isKeep_dedicated();
            boolean aiListMode = module.isAi_list_mode();
            HashSet<String> aiList = module.getAi_list();
            handle.collides = module.isCollides();
            handle.d(module.isSilent());
            this.optimizedMobs.put(handle, Boolean.TRUE);
            boolean isAnimal = handle instanceof EntityAnimal;
            Class<?> handleClass = handle.getClass();
            PathfinderTargetCondition temptTargeting = module.isTemptEnabled() ? this.temptTargeting.get(handleClass) : null;
            Set<PathfinderGoalWrapped> goals = handle.bO.b();
            synchronized (goals) {
               HashSet<PathfinderGoalWrapped> toAdd = new HashSet<>();
               HashSet<PathfinderGoalWrapped> toRemove = new HashSet<>();

               for (PathfinderGoalWrapped pgw : goals) {
                  Class<?> goalClass = pgw.k().getClass();
                  if (!keepDedicated || goalClass.getName().contains("ai.goal")) {
                     if (isAnimal && module.isBreedEnabled() && goalClass == PathfinderGoalBreed.class) {
                        toRemove.add(pgw);
                        pgw.d();
                        toAdd.add(new PathfinderGoalWrapped(pgw.i(), new OptimizedBreedGoal(this.getModule(), (EntityAnimal)handle, this.breedTargeting)));
                     } else if (module.isTemptEnabled() && goalClass == PathfinderGoalTempt.class && temptTargeting != null) {
                        toRemove.add(pgw);
                        pgw.d();
                        toAdd.add(new PathfinderGoalWrapped(pgw.i(), new OptimizedTemptGoal(this.getModule(), handle, temptTargeting)));
                     } else {
                        String simpleName = goalClass.getSimpleName();
                        if (aiList.stream().anyMatch(simpleName::contains) == aiListMode) {
                           toRemove.add(pgw);
                           pgw.d();
                        }
                     }
                  }
               }

               if (!toRemove.isEmpty()) {
                  goals.removeAll(toRemove);
               }

               if (!toAdd.isEmpty()) {
                  goals.addAll(toAdd);
               }
            }
         }
      }
   }

   @Override
   public void purge() {
      synchronized (this.optimizedMobs) {
         this.optimizedMobs.keySet().removeIf(ent -> !ent.bx() || !ent.valid);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLoad(EntitiesLoadEvent e) {
      if (this.getModule().canContinue(e.getWorld())) {
         if (this.getModule().isAsync()) {
            Chunk chunk = e.getChunk();
            SupportManager.getInstance()
               .getFork()
               .runNow(true, new Location(chunk.getWorld(), chunk.getX() << 4, 64.0, chunk.getZ() << 4), () -> this.optimizeEntities(e.getEntities()));
         } else {
            this.optimizeEntities(e.getEntities());
         }
      }
   }

   public void optimizeEntities(List<org.bukkit.entity.Entity> list) {
      for (org.bukkit.entity.Entity entity : list) {
         if (this.getModule().isEnabled(entity)) {
            this.optimize(entity, false);
         }
      }
   }
}
