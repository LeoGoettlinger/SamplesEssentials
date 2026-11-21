package com.donutorders.libs.mysql.cj.log;

import com.donutorders.libs.mysql.cj.Query;
import com.donutorders.libs.mysql.cj.Session;
import com.donutorders.libs.mysql.cj.protocol.Resultset;

public interface ProfilerEventHandler {
   void init(Log var1);

   void destroy();

   void consumeEvent(ProfilerEvent var1);

   void processEvent(byte var1, Session var2, Query var3, Resultset var4, long var5, Throwable var7, String var8);
}
