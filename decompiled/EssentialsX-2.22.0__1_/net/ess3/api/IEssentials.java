package net.ess3.api;

import com.earth2me.essentials.items.CustomItemResolver;
import java.util.Collection;

public interface IEssentials extends com.earth2me.essentials.IEssentials {
   Collection<String> getVanishedPlayersNew();

   CustomItemResolver getCustomItemResolver();
}
