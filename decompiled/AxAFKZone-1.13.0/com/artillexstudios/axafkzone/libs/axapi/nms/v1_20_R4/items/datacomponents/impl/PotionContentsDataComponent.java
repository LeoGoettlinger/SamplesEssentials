package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R4.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.components.data.PotionContents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.PotionRegistry;
import org.bukkit.craftbukkit.v1_20_R4.potion.CraftPotionType;
import org.bukkit.craftbukkit.v1_20_R4.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public final class PotionContentsDataComponent implements DataComponentHandler<PotionContents, net.minecraft.world.item.alchemy.PotionContents> {
   public net.minecraft.world.item.alchemy.PotionContents toNMS(PotionContents from) {
      Optional<Holder<PotionRegistry>> potionHolder = from.type().isEmpty()
         ? Optional.empty()
         : Optional.of(CraftPotionType.bukkitToMinecraftHolder(from.type().get()));
      List<MobEffect> effects = new ArrayList<>(from.effects().size());

      for (PotionEffect effect : from.effects()) {
         effects.add(CraftPotionUtil.fromBukkit(effect));
      }

      return new net.minecraft.world.item.alchemy.PotionContents(potionHolder, from.color(), effects);
   }

   public PotionContents fromNMS(net.minecraft.world.item.alchemy.PotionContents data) {
      Optional<PotionType> type = data.e().isEmpty() ? Optional.empty() : Optional.of(CraftPotionType.minecraftHolderToBukkit((Holder)data.e().get()));
      List<PotionEffect> types = new ArrayList<>(data.d().size());

      for (MobEffect effectInstance : data.d()) {
         types.add(CraftPotionUtil.toBukkit(effectInstance));
      }

      return new PotionContents(type, data.f(), types, Optional.empty());
   }
}
