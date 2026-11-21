package com.earth2me.essentials.api;

import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.io.File;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Location;

@Deprecated
public interface IWarps extends IConf {
   Location getWarp(String warp) throws WarpNotFoundException;

   boolean isWarp(String name);

   Collection<String> getList();

   int getCount();

   void removeWarp(String name) throws Exception;

   void setWarp(String name, Location loc) throws Exception;

   void setWarp(IUser user, String name, Location loc) throws Exception;

   UUID getLastOwner(String warp) throws WarpNotFoundException;

   boolean isEmpty();

   @Deprecated
   File getWarpFile(String name) throws net.ess3.api.InvalidNameException;
}
