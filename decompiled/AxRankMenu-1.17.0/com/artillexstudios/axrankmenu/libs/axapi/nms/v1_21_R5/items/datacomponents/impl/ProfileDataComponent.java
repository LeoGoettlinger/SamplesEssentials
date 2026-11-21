package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R5.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axrankmenu.libs.axapi.utils.PlayerSkin;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ResolvableProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Map.Entry;

public final class ProfileDataComponent implements DataComponentHandler<ResolvableProfile, net.minecraft.world.item.component.ResolvableProfile> {
   public net.minecraft.world.item.component.ResolvableProfile toNMS(ResolvableProfile from) {
      GameProfile gameProfile = new GameProfile(from.getPartialProfile().properties().uuid(), from.getPartialProfile().properties().name());

      for (Entry<String, ProfileProperties.Property> entry : from.getPartialProfile().properties().properties().entries()) {
         ProfileProperties.Property property = entry.getValue();
         gameProfile.getProperties().put(entry.getKey(), new Property(property.name(), property.value(), property.signature()));
      }

      return new net.minecraft.world.item.component.ResolvableProfile(gameProfile);
   }

   public ResolvableProfile fromNMS(net.minecraft.world.item.component.ResolvableProfile data) {
      ProfileProperties profileProperties = new ProfileProperties(data.g().getId(), data.d().orElse(""));
      data.g().getProperties().forEach((k, v) -> profileProperties.put(k, new ProfileProperties.Property(v.name(), v.value(), v.signature())));
      com.artillexstudios.axrankmenu.libs.axapi.utils.GameProfile gameProfile = new com.artillexstudios.axrankmenu.libs.axapi.utils.GameProfile(
         data.g().getName(), data.g().getId(), profileProperties
      );
      return new ResolvableProfile(gameProfile, new PlayerSkin.Patch(null, null, null, null));
   }
}
