package com.skyblockexp.ezrtp.storage;

import java.util.UUID;

public interface RtpUsageStorage {
   long getLastRtpTime(UUID var1, String var2);

   void setLastRtpTime(UUID var1, String var2, long var3);

   int getUsageCount(UUID var1, String var2, String var3);

   void incrementUsage(UUID var1, String var2, String var3);

   void resetUsage(UUID var1, String var2, String var3);

   void save();

   void reload();
}
