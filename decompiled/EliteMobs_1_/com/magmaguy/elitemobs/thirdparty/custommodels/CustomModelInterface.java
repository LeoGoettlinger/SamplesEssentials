package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Location;

public interface CustomModelInterface {
   void shoot();

   void melee();

   void playAnimationByName(String var1);

   void setName(String var1, boolean var2);

   void setNameVisible(boolean var1);

   void addPassenger(CustomBossEntity var1);

   void switchPhase();

   Location getNametagBoneLocation();

   boolean hasNametagBone();

   void setSyncMovement(boolean var1);
}
