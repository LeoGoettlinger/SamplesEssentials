package me.usainsrht.announcement.lib.morepaperlib.scheduling;

import org.bukkit.plugin.Plugin;

public interface ScheduledTask {
   Plugin owningPlugin();

   void cancel();

   boolean isCancelled();
}
