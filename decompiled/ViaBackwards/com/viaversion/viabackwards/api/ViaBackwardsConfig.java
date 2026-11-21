package com.viaversion.viabackwards.api;

import com.viaversion.viaversion.api.configuration.Config;

public interface ViaBackwardsConfig extends Config {
   boolean addCustomEnchantsToLore();

   boolean addTeamColorTo1_13Prefix();

   boolean isFix1_13FacePlayer();

   boolean fix1_13FormattedInventoryTitle();

   boolean alwaysShowOriginalMobName();

   boolean handlePingsAsInvAcknowledgements();

   boolean bedrockAtY0();

   boolean sculkShriekerToCryingObsidian();

   boolean scaffoldingToWater();

   boolean mapDarknessEffect();

   boolean mapCustomModelData();

   boolean mapDisplayEntities();

   boolean suppressEmulationWarnings();

   boolean dialogsViaChests();

   DialogStyleConfig dialogStyleConfig();

   boolean codeOfConductAsDialog();

   boolean passOriginalItemNameToResourcePacks();
}
