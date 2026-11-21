package org.enginehub.piston.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.enginehub.piston.Command;

class CommandInfoCache {
   private final LoadingCache<Command, CommandInfo> commandCache = CacheBuilder.newBuilder().softValues().build(CacheLoader.from(CommandInfo::from));

   public CommandInfo getInfo(Command command) {
      return (CommandInfo)this.commandCache.getUnchecked(command);
   }
}
