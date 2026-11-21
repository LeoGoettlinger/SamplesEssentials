package com.earth2me.essentials.api;

import com.earth2me.essentials.IConf;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.IUser;
import org.bukkit.Location;

@Deprecated
public interface IJails extends IConf {
   Location getJail(String jailName) throws Exception;

   Collection<String> getList() throws Exception;

   int getCount();

   void removeJail(String jail) throws Exception;

   void sendToJail(IUser user, String jail, CompletableFuture<Boolean> future) throws Exception;

   void setJail(String jailName, Location loc) throws Exception;

   void startTransaction();

   void stopTransaction(boolean blocking);
}
