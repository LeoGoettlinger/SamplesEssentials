package com.skyblockexp.ezrtp.platform;

import org.bukkit.Location;
import org.bukkit.World;

public interface PlatformWorldAccess {
   int getSurfaceY(World var1, int var2, int var3);

   Location trySnapshotValidate(World var1, int var2, int var3, int var4, int var5);
}
