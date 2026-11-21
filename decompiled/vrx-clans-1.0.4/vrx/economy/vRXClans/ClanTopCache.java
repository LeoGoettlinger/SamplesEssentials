package vrx.economy.vRXClans;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClanTopCache {
   private final Map<String, List<Clan>> cached = new ConcurrentHashMap<>();
   private long lastRefresh;

   public void refresh(Iterable<Clan> clans) {
      List<Clan> source = new ArrayList<>();
      clans.forEach(source::add);
      this.cached.put("level", this.sorted(source, Comparator.comparingInt(Clan::getLevel).thenComparingDouble(Clan::getXp).reversed()));
      this.cached.put("bank", this.sorted(source, Comparator.comparingDouble(Clan::getBankBalance).reversed()));
      this.cached.put("kills", this.sorted(source, Comparator.comparingInt(Clan::getKills).reversed()));
      this.cached.put("kdr", this.sorted(source, Comparator.comparingDouble(this::kdrValue).reversed()));
      this.lastRefresh = System.currentTimeMillis();
   }

   public List<Clan> get(String category, int limit) {
      String key = category == null ? "level" : category.toLowerCase(Locale.ROOT);
      List<Clan> list = this.cached.getOrDefault(key, List.of());
      return list.stream().limit(Math.max(1, limit)).toList();
   }

   public long getLastRefresh() {
      return this.lastRefresh;
   }

   private List<Clan> sorted(List<Clan> source, Comparator<Clan> comparator) {
      return source.stream().sorted(comparator).toList();
   }

   private double kdrValue(Clan clan) {
      return clan.getDeaths() == 0 ? clan.getKills() : (double)clan.getKills() / clan.getDeaths();
   }
}
