package de.tr7zw.nbtapi;

import com.mojang.authlib.GameProfile;
import de.tr7zw.nbtapi.utils.GameprofileUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import de.tr7zw.nbtapi.utils.nmsmappings.ObjectCreator;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;

public class NBTGameProfile {
   @Deprecated
   public static NBTCompound toNBT(GameProfile profile) {
      return (NBTCompound)(MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)
         ? (NBTCompound)GameprofileUtil.writeGameProfile(NBT.createNBTObject(), profile)
         : new NBTContainer(ReflectionMethod.GAMEPROFILE_SERIALIZE.run(null, ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(), profile)));
   }

   @Deprecated
   public static GameProfile fromNBT(NBTCompound compound) {
      return MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)
         ? GameprofileUtil.readGameProfile(compound)
         : (GameProfile)ReflectionMethod.GAMEPROFILE_DESERIALIZE.run(null, NBTReflectionUtil.getToCompount(compound.getCompound(), compound));
   }
}
