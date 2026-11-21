package org.sethomegui.Storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.route.Route;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;

public final class PlayerDataCodec {
   private static final String KEY_HOMES = "homes";
   private static final String KEY_FRIENDS = "friends";

   private PlayerDataCodec() {
   }

   public static YamlDocument documentFrom(String yaml) throws IOException {
      String content = yaml != null && !yaml.trim().isEmpty() ? yaml : "{}";
      return YamlDocument.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT);
   }

   public static PlayerRecord toRecord(UUID uuid, Section document) throws IOException {
      PlayerRecord record = new PlayerRecord(uuid);
      if (document == null) {
         return record;
      } else {
         List<String> homeNames = document.getStringList("homes");
         if (homeNames == null) {
            homeNames = new ArrayList<>();
         }

         List<HomeEntry> homes = new ArrayList<>();

         for (String homeName : homeNames) {
            if (homeName != null && !homeName.isEmpty()) {
               HomeEntry entry = new HomeEntry(homeName);
               entry.setWorld(document.getString(homeName + ".world", "world"));
               entry.setX(document.getDouble(homeName + ".x", 0.0));
               entry.setY(document.getDouble(homeName + ".y", 0.0));
               entry.setZ(document.getDouble(homeName + ".z", 0.0));
               entry.setYaw(document.getDouble(homeName + ".yaw", 0.0));
               entry.setPitch(document.getDouble(homeName + ".pitch", 0.0));
               entry.setIcon(document.getString(homeName + ".icon", null));
               entry.setAccess(document.getString(homeName + ".access", "nobody"));
               List<String> allowed = document.getStringList(homeName + ".allowed-friends");
               entry.setAllowedFriends(allowed == null ? new ArrayList<>() : new ArrayList<>(allowed));
               homes.add(entry);
            }
         }

         record.setHomes(homes);
         List<String> friends = document.getStringList("friends");
         record.setFriends(friends == null ? new ArrayList<>() : new ArrayList<>(friends));
         record.setExtraData(extractExtraData(document, homeNames));
         return record;
      }
   }

   public static String toYaml(PlayerRecord record) throws IOException {
      YamlDocument document = documentFrom(record.getExtraData());
      List<String> names = new ArrayList<>();

      for (HomeEntry home : record.getHomes()) {
         if (home.getName() != null && !home.getName().isEmpty()) {
            names.add(home.getName());
            String base = home.getName();
            document.set(base + ".world", home.getWorld());
            document.set(base + ".x", home.getX());
            document.set(base + ".y", home.getY());
            document.set(base + ".z", home.getZ());
            document.set(base + ".yaw", home.getYaw());
            document.set(base + ".pitch", home.getPitch());
            if (home.getIcon() != null && !home.getIcon().isEmpty()) {
               document.set(base + ".icon", home.getIcon());
            }

            if (home.getAccess() != null && !home.getAccess().equalsIgnoreCase("nobody")) {
               document.set(base + ".access", home.getAccess());
            }

            if (!home.getAllowedFriends().isEmpty()) {
               document.set(base + ".allowed-friends", new ArrayList<>(home.getAllowedFriends()));
            }
         }
      }

      document.set("homes", names);
      if (!record.getFriends().isEmpty()) {
         document.set("friends", new ArrayList<>(record.getFriends()));
      }

      return document.dump();
   }

   public static YamlDocument plainCopy(Section source) throws IOException {
      YamlDocument copy = documentFrom(null);
      if (source == null) {
         return copy;
      } else {
         Map<Route, Object> values = source.getRouteMappedValues(true);

         for (Entry<Route, Object> entry : values.entrySet()) {
            if (!(entry.getValue() instanceof Section)) {
               copy.set(entry.getKey(), entry.getValue());
            }
         }

         return copy;
      }
   }

   private static String extractExtraData(Section document, List<String> homeNames) throws IOException {
      YamlDocument leftovers = plainCopy(document);
      leftovers.remove("homes");
      leftovers.remove("friends");
      Set<String> consumed = new HashSet<>();

      for (String homeName : homeNames) {
         if (homeName != null && !homeName.isEmpty()) {
            int dot = homeName.indexOf(46);
            consumed.add(dot >= 0 ? homeName.substring(0, dot) : homeName);
         }
      }

      for (String key : consumed) {
         leftovers.remove(key);
      }

      return leftovers.getKeys().isEmpty() ? null : leftovers.dump();
   }
}
