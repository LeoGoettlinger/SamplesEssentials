package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R5.loot;

import com.artillexstudios.axafkzone.libs.axapi.loot.LootContextParam;
import com.artillexstudios.axafkzone.libs.axapi.loot.LootContextParamSets;
import com.artillexstudios.axafkzone.libs.axapi.loot.LootContextParams;
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
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LootTable implements com.artillexstudios.axafkzone.libs.axapi.loot.LootTable {
   private static final IdentityHashMap<LootContextParam<?>, ContextKey<Object>> params = new IdentityHashMap<LootContextParam<?>, ContextKey<Object>>() {
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
         this.put(LootContextParamSets.ENCHANTED_DAMAGE, LootContextParameterSets.t);
         this.put(LootContextParamSets.ENCHANTED_ITEM, LootContextParameterSets.u);
         this.put(LootContextParamSets.ENCHANTED_LOCATION, LootContextParameterSets.v);
         this.put(LootContextParamSets.ENCHANTED_ENTITY, LootContextParameterSets.w);
         this.put(LootContextParamSets.HIT_BLOCK, LootContextParameterSets.x);
      }
   };
   private final net.minecraft.world.level.storage.loot.LootTable lootTable;

   public LootTable(Key key) {
      this.lootTable = MinecraftServer.getServer().bc().a(ResourceKey.a(Registries.bw, MinecraftKey.a(key.namespace(), key.value())));
   }

   private static LootParams toLootParams(com.artillexstudios.axafkzone.libs.axapi.loot.LootParams lootParams) {
      a builder = new a(((CraftWorld)lootParams.world()).getHandle());

      for (Entry<LootContextParam<?>, Object> param : lootParams.params().entrySet()) {
         Object value = switch (param.getValue()) {
            case Player player -> param.getKey() == LootContextParams.DAMAGE_SOURCE
               ? ((CraftPlayer)player).getHandle().ea().a(((CraftPlayer)player).getHandle())
               : ((CraftPlayer)player).getHandle();
            case Entity entity -> param.getKey() == LootContextParams.DAMAGE_SOURCE
               ? ((CraftEntity)entity).getHandle().ea().b(((CraftLivingEntity)entity).getHandle())
               : ((CraftEntity)entity).getHandle();
            case BlockData blockData -> ((CraftBlockData)blockData).getState();
            case org.bukkit.inventory.ItemStack itemStack -> CraftItemStack.asNMSCopy(itemStack);
            case Location location -> new Vec3D(location.x(), location.y(), location.z());
            case null, default -> null;
         };
         if (value != null) {
            builder.a(params.get(param.getKey()), value);
         }
      }

      return builder.a(sets.get(lootParams.sets()));
   }

   @Override
   public List<org.bukkit.inventory.ItemStack> randomItems(com.artillexstudios.axafkzone.libs.axapi.loot.LootParams params) {
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
