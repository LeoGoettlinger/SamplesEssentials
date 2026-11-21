package com.skyblockexp.ezrtp.teleport.filter;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import java.util.List;
import org.bukkit.Location;

public final class SearchFilterChain {
   private final List<SearchFilter> filters;

   public SearchFilterChain(List<SearchFilter> filters) {
      this.filters = filters != null ? List.copyOf(filters) : List.of();
   }

   public SearchFilterResult apply(Location candidate, RandomTeleportSettings settings) {
      for (SearchFilter filter : this.filters) {
         SearchFilterResult result = filter.apply(candidate, settings);
         if (result != null && !result.passed()) {
            return result;
         }
      }

      return SearchFilterResult.pass();
   }
}
