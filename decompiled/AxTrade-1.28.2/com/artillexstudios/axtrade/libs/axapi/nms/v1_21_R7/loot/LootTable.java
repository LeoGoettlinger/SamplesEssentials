package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R7.loot;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.a;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LootTable implements com.artillexstudios.axtrade.libs.axapi.loot.LootTable {
   private static final IdentityHashMap<LootContextParam<?>, ContextKey<Object>> params = new IdentityHashMap<LootContextParam<?>, ContextKey<Object>>() {
      {
         this.put(LootContextParams.THIS_ENTITY, LootContextParameters.a);
         this.put(LootContextParams.INTERACTING_ENTITY, LootContextParameters.b);
         this.put(LootContextParams.TARGET_ENTITY, LootContextParameters.c);
         this.put(LootContextParams.LAST_DAMAGE_PLAYER, LootContextParameters.d);
         this.put(LootContextParams.DAMAGE_SOURCE, LootContextParameters.e);
         this.put(LootContextParams.ATTACKING_ENTITY, LootContextParameters.f);
         this.put(LootContextParams.ORIGIN, LootContextParameters.h);
         this.put(LootContextParams.BLOCK_STATE, LootContextParameters.i);
         this.put(LootContextParams.TOOL, LootContextParameters.k);
         this.put(LootContextParams.DIRECT_ATTACKING_ENTITY, LootContextParameters.g);
      }
   };
   private static final EnumMap<LootContextParamSets, ContextKeySet> sets = new EnumMap<LootContextParamSets, ContextKeySet>(LootContextParamSets.class) {
      {
         this.put(LootContextParamSets.EMPTY, LootContextParameterSets.b);
         this.put(LootContextParamSets.CHEST, LootContextParameterSets.c);
         this.put(LootContextParamSets.COMMAND, LootContextParameterSets.d);
         this.put(LootContextParamSets.SELECTOR, LootContextParameterSets.e);
         this.put(LootContextParamSets.FISHING, LootContextParameterSets.f);
         this.put(LootContextParamSets.ENTITY, LootContextParameterSets.g);
         this.put(LootContextParamSets.EQUIPMENT, LootContextParameterSets.h);
         this.put(LootContextParamSets.ARCHAEOLOGY, LootContextParameterSets.i);
         this.put(LootContextParamSets.GIFT, LootContextParameterSets.j);
         this.put(LootContextParamSets.PIGLIN_BARTER, LootContextParameterSets.k);
         this.put(LootContextParamSets.VAULT, LootContextParameterSets.l);
         this.put(LootContextParamSets.ADVANCEMENT_REWARD, LootContextParameterSets.m);
         this.put(LootContextParamSets.ADVANCEMENT_ENTITY, LootContextParameterSets.n);
         this.put(LootContextParamSets.ADVANCEMENT_LOCATION, LootContextParameterSets.o);
         this.put(LootContextParamSets.BLOCK_USE, LootContextParameterSets.p);
         this.put(LootContextParamSets.ALL_PARAMS, LootContextParameterSets.q);
         this.put(LootContextParamSets.BLOCK, LootContextParameterSets.r);
         this.put(LootContextParamSets.SHEARING, LootContextParameterSets.s);
         this.put(LootContextParamSets.ENTITY_INTERACT, LootContextParameterSets.t);
         this.put(LootContextParamSets.BLOCK_INTERACT, LootContextParameterSets.u);
         this.put(LootContextParamSets.ENCHANTED_DAMAGE, LootContextParameterSets.v);
         this.put(LootContextParamSets.ENCHANTED_ITEM, LootContextParameterSets.w);
         this.put(LootContextParamSets.ENCHANTED_LOCATION, LootContextParameterSets.x);
         this.put(LootContextParamSets.ENCHANTED_ENTITY, LootContextParameterSets.y);
         this.put(LootContextParamSets.HIT_BLOCK, LootContextParameterSets.z);
      }
   };
   private final net.minecraft.world.level.storage.loot.LootTable lootTable;

   public LootTable(Key key) {
      this.lootTable = MinecraftServer.getServer().be().a(ResourceKey.a(Registries.bG, MinecraftKey.a(key.namespace(), key.value())));
   }

   private static LootParams toLootParams(com.artillexstudios.axtrade.libs.axapi.loot.LootParams lootParams) {
      a builder = new a(((CraftWorld)lootParams.world()).getHandle());

      for (Entry<LootContextParam<?>, Object> param : lootParams.params().entrySet()) {
         Object value = switch (param.getValue()) {
            case Player player -> param.getKey() == LootContextParams.DAMAGE_SOURCE
               ? ((CraftPlayer)player).getHandle().en().a(((CraftPlayer)player).getHandle())
               : ((CraftPlayer)player).getHandle();
            case Entity entity -> param.getKey() == LootContextParams.DAMAGE_SOURCE
               ? ((CraftEntity)entity).getHandle().en().b(((CraftLivingEntity)entity).getHandle())
               : ((CraftEntity)entity).getHandle();
            case BlockData blockData -> ((CraftBlockData)blockData).getState();
            case org.bukkit.inventory.ItemStack itemStack -> CraftItemStack.asNMSCopy(itemStack);
            case Location location -> new Vec3D(location.getX(), location.getY(), location.getZ());
            case null, default -> null;
         };
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
