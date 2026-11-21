package com.artillexstudios.axtrade.libs.axapi.libs.hikari;

public interface HikariPoolMXBean {
   int getIdleConnections();

   int getActiveConnections();

   int getTotalConnections();

   int getThreadsAwaitingConnection();

   void softEvictConnections();

   void suspendPool();

   void resumePool();
}
