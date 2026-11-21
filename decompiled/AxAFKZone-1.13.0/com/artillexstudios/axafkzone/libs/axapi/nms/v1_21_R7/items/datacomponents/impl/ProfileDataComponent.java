package com.artillexstudios.axafkzone.libs.axapi.nms.v1_21_R7.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axafkzone.libs.axapi.utils.PlayerSkin;
import com.artillexstudios.axafkzone.libs.axapi.utils.ResolvableProfile;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Map.Entry;

public final class ProfileDataComponent implements DataComponentHandler<ResolvableProfile, net.minecraft.world.item.component.ResolvableProfile> {
   public net.minecraft.world.item.component.ResolvableProfile toNMS(ResolvableProfile from) {
      Multimap<String, Property> propertyMap = HashMultimap.create();

      for (Entry<String, ProfileProperties.Property> entry : from.getPartialProfile().properties().properties().entries()) {
         ProfileProperties.Property property = entry.getValue();
         propertyMap.put(entry.getKey(), new Property(property.name(), property.value(), property.signature()));
      }

      GameProfile gameProfile = new GameProfile(
         from.getPartialProfile().properties().uuid(), from.getPartialProfile().properties().name(), new PropertyMap(propertyMap)
      );
      return net.minecraft.world.item.component.ResolvableProfile.a(gameProfile);
   }

   public ResolvableProfile fromNMS(net.minecraft.world.item.component.ResolvableProfile data) {
      ProfileProperties profileProperties = new ProfileProperties(data.b().id(), data.d().orElse(""));
      data.b().properties().forEach((k, v) -> profileProperties.put(k, new ProfileProperties.Property(v.name(), v.value(), v.signature())));
      com.artillexstudios.axafkzone.libs.axapi.utils.GameProfile gameProfile = new com.artillexstudios.axafkzone.libs.axapi.utils.GameProfile(
         data.b().name(), data.b().id(), profileProperties
      );
      return new ResolvableProfile(gameProfile, new PlayerSkin.Patch(null, null, null, null));
   }
}
