package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.loot;

import com.artillexstudios.axtrade.libs.axapi.loot.LootContextParam;
import com.artillexstudios.axtrade.libs.axapi.loot.LootContextParamSets;
import com.artillexstudios.axtrade.libs.axapi.loot.LootContextParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import net.kyori.adventure.key.Key;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.a;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LootTable implements com.artillexstudios.axtrade.libs.axapi.loot.LootTable {
   private static final IdentityHashMap<LootContextParam<?>, LootContextParameter<Object>> params = new IdentityHashMap<LootContextParam<?>, LootContextParameter<Object>>() {
      {
         this.put(LootContextParams.ATTACKING_ENTITY, LootContextParameters.d);
         this.put(LootContextParams.BLOCK_STATE, LootContextParameters.g);
         this.put(LootContextParams.DAMAGE_SOURCE, LootContextParameters.c);
         this.put(LootContextParams.ORIGIN, LootContextParameters.f);
         this.put(LootContextParams.TOOL, LootContextParameters.i);
         this.put(LootContextParams.DIRECT_ATTACKING_ENTITY, LootContextParameters.e);
         this.put(LootContextParams.LAST_DAMAGE_PLAYER, LootContextParameters.b);
         this.put(LootContextParams.THIS_ENTITY, LootContextParameters.a);
      }
   };
   private static final EnumMap<LootContextParamSets, LootContextParameterSet> sets = new EnumMap<LootContextParamSets, LootContextParameterSet>(
      LootContextParamSets.class
   ) {
      {
         this.put(LootContextParamSets.EMPTY, LootContextParameterSets.a);
         this.put(LootContextParamSets.CHEST, LootContextParameterSets.b);
         this.put(LootContextParamSets.COMMAND, LootContextParameterSets.c);
         this.put(LootContextParamSets.SELECTOR, LootContextParameterSets.d);
         this.put(LootContextParamSets.FISHING, LootContextParameterSets.e);
         this.put(LootContextParamSets.ENTITY, LootContextParameterSets.f);
         this.put(LootContextParamSets.ARCHAEOLOGY, LootContextParameterSets.g);
         this.put(LootContextParamSets.GIFT, LootContextParameterSets.h);
         this.put(LootContextParamSets.PIGLIN_BARTER, LootContextParameterSets.i);
         this.put(LootContextParamSets.ADVANCEMENT_REWARD, LootContextParameterSets.j);
         this.put(LootContextParamSets.ADVANCEMENT_ENTITY, LootContextParameterSets.k);
         this.put(LootContextParamSets.ADVANCEMENT_LOCATION, LootContextParameterSets.l);
         this.put(LootContextParamSets.ALL_PARAMS, LootContextParameterSets.m);
         this.put(LootContextParamSets.BLOCK, LootContextParameterSets.n);
      }
   };
   private final net.minecraft.world.level.storage.loot.LootTable lootTable;

   public LootTable(Key key) {
      this.lootTable = MinecraftServer.getServer().aH().getLootTable(new MinecraftKey(key.namespace(), key.value()));
   }

   private static LootParams toLootParams(com.artillexstudios.axtrade.libs.axapi.loot.LootParams lootParams) {
      a builder = new a(((CraftWorld)lootParams.world()).getHandle());

      for (Entry<LootContextParam<?>, Object> param : lootParams.params().entrySet()) {
         Object value;
         if (param.getValue() instanceof Player player) {
            if (param.getKey() == LootContextParams.DAMAGE_SOURCE) {
               value = ((CraftPlayer)player).getHandle().dJ().a(((CraftPlayer)player).getHandle());
            } else {
               value = ((CraftPlayer)player).getHandle();
            }
         } else if (param.getValue() instanceof Entity entity) {
            if (param.getKey() == LootContextParams.DAMAGE_SOURCE) {
               value = ((CraftEntity)entity).getHandle().dJ().b(((CraftLivingEntity)entity).getHandle());
            } else {
               value = ((CraftEntity)entity).getHandle();
            }
         } else if (param.getValue() instanceof BlockData blockData) {
            value = ((CraftBlockData)blockData).getState();
         } else if (param.getValue() instanceof org.bukkit.inventory.ItemStack itemStack) {
            value = CraftItemStack.asNMSCopy(itemStack);
         } else if (param.getValue() instanceof Location location) {
            value = new Vec3D(location.x(), location.y(), location.z());
         } else {
            value = null;
         }

         if (value != null) {
            builder.a(params.get(param.getKey()), value);
         }
      }

      return builder.a(sets.get(lootParams.sets()));
   }

   @Override
   public List<org.bukkit.inventory.ItemStack> randomItems(com.artillexstudios.axtrade.libs.axapi.loot.LootParams params) {
      ObjectArrayList<ItemStack> items = this.lootTable.a(toLootParams(params));
      ObjectArrayList<org.bukkit.inventory.ItemStack> returning = new ObjectArrayList(items.size());
      ObjectListIterator var4 = items.iterator();

      while (var4.hasNext()) {
         ItemStack item = (ItemStack)var4.next();
         returning.add(CraftItemStack.asCraftMirror(item));
      }

      return returning;
   }
}
