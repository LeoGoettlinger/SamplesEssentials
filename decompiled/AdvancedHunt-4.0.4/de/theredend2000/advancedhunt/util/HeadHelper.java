package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;

public class HeadHelper {
   private static final Pattern SKIN_URL_PATTERN = Pattern.compile("\\\"url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

   public static boolean isHeadMaterialName(String materialName) {
      String name = normalizeMaterialName(materialName);
      if (name == null) {
         return false;
      } else {
         return isLegacySkullMaterial(name)
            ? true
            : name.endsWith("_HEAD") || name.endsWith("_WALL_HEAD") || name.endsWith("_SKULL") || name.endsWith("_WALL_SKULL");
      }
   }

   public static boolean isPlayerHead(ItemStack item) {
      XMaterial material = XMaterial.matchXMaterial(item);
      return material == XMaterial.PLAYER_HEAD || material == XMaterial.PLAYER_WALL_HEAD;
   }

   public static boolean isPlayerHeadMaterialName(String materialName) {
      return isPlayerHeadMaterialName(materialName, null);
   }

   public static boolean isPlayerHeadMaterialName(String materialName, String blockState) {
      String normalizedMaterial = normalizeMaterialName(materialName);
      if (!"PLAYER_HEAD".equals(normalizedMaterial) && !"PLAYER_WALL_HEAD".equals(normalizedMaterial)) {
         String blockStateMaterial = extractMaterialNameFromBlockState(blockState);
         return "PLAYER_HEAD".equals(blockStateMaterial) || "PLAYER_WALL_HEAD".equals(blockStateMaterial);
      } else {
         return true;
      }
   }

   public static String resolveHeadDisplayMaterialName(String materialName, String blockState) {
      String blockStateMaterial = toDisplayHeadMaterialName(extractMaterialNameFromBlockState(blockState));
      if (blockStateMaterial != null) {
         return blockStateMaterial;
      } else {
         String directMaterial = toDisplayHeadMaterialName(normalizeMaterialName(materialName));
         if (directMaterial != null) {
            return directMaterial;
         } else {
            String normalizedMaterial = normalizeMaterialName(materialName);
            if (isLegacySkullMaterial(normalizedMaterial)) {
               return isPlayerHeadMaterialName(materialName, blockState) ? "PLAYER_HEAD" : "SKELETON_SKULL";
            } else {
               return null;
            }
         }
      }
   }

   private static String normalizeMaterialName(String materialName) {
      if (materialName == null) {
         return null;
      } else {
         String name = materialName.trim();
         return name.isEmpty() ? null : name.toUpperCase(Locale.ROOT);
      }
   }

   private static boolean isLegacySkullMaterial(String materialName) {
      return "SKULL".equals(materialName)
         || "SKULL_ITEM".equals(materialName)
         || "LEGACY_SKULL".equals(materialName)
         || "LEGACY_SKULL_ITEM".equals(materialName);
   }

   private static String extractMaterialNameFromBlockState(String blockState) {
      String normalized = normalizeMaterialName(blockState);
      if (normalized == null) {
         return null;
      } else {
         int bracketIndex = normalized.indexOf(91);
         if (bracketIndex != -1) {
            normalized = normalized.substring(0, bracketIndex);
         }

         int colonIndex = normalized.indexOf(58);
         if (colonIndex != -1) {
            normalized = normalized.substring(colonIndex + 1);
         }

         return isHeadMaterialName(normalized) ? normalized : null;
      }
   }

   private static String toDisplayHeadMaterialName(String materialName) {
      if (materialName == null) {
         return null;
      } else if (materialName.endsWith("_WALL_HEAD")) {
         return materialName.substring(0, materialName.length() - "_WALL_HEAD".length()) + "_HEAD";
      } else if (materialName.endsWith("_WALL_SKULL")) {
         return materialName.substring(0, materialName.length() - "_WALL_SKULL".length()) + "_SKULL";
      } else {
         return !materialName.endsWith("_HEAD") && !materialName.endsWith("_SKULL") ? null : materialName;
      }
   }

   public static HeadHelper.SkullProfileData getSkullProfileData(String nbtData) {
      if (nbtData != null && !nbtData.isEmpty()) {
         try {
            ReadWriteNBT nbt = NBT.parseNBT(nbtData);
            ReadWriteNBT profile = getProfileCompound(nbt);
            if (profile != null) {
               return buildProfileData(extractTextureFromProfile(profile), extractProfileName(profile));
            }
         } catch (Throwable var3) {
         }

         return extractProfileDataFromRawNbt(nbtData);
      } else {
         return null;
      }
   }

   public static String getTextureFromNbt(String nbtData) {
      HeadHelper.SkullProfileData profileData = getSkullProfileData(nbtData);
      return profileData == null ? null : profileData.texture();
   }

   public static String getProfileNameFromNbt(String nbtData) {
      HeadHelper.SkullProfileData profileData = getSkullProfileData(nbtData);
      return profileData == null ? null : profileData.ownerName();
   }

   public static boolean applySkullProfile(BlockState state, String nbtData) {
      if (!(state instanceof Skull)) {
         return false;
      } else if (nbtData != null && !nbtData.isEmpty()) {
         Skull skull = (Skull)state;
         boolean copiedStoredProfile = copyStoredProfile(state, nbtData);
         HeadHelper.SkullProfileData profileData = getSkullProfileData(nbtData);
         if (profileData == null) {
            return copiedStoredProfile;
         } else if (profileData.texture() != null && !profileData.texture().isEmpty() && applyTexture(skull, profileData.texture())) {
            return true;
         } else {
            return profileData.ownerName() != null && !profileData.ownerName().isEmpty() && applyOwnerName(skull, profileData.ownerName())
               ? true
               : copiedStoredProfile;
         }
      } else {
         return false;
      }
   }

   private static boolean copyStoredProfile(BlockState state, String nbtData) {
      try {
         ReadWriteNBT source = NBT.parseNBT(nbtData);
         if (!hasStoredProfileCompound(source)) {
            return false;
         } else {
            boolean[] copied = new boolean[]{false};
            NBT.modify(state, nbt -> copied[0] = mergeStoredProfileCompounds(nbt, source));
            return copied[0];
         }
      } catch (Throwable var4) {
         return false;
      }
   }

   private static ReadWriteNBT getProfileCompound(ReadWriteNBT nbt) {
      if (nbt.hasTag("Owner")) {
         return nbt.getCompound("Owner");
      } else if (nbt.hasTag("SkullOwner")) {
         return nbt.getCompound("SkullOwner");
      } else if (nbt.hasTag("profile")) {
         return nbt.getCompound("profile");
      } else {
         return nbt.hasTag("minecraft:profile") ? nbt.getCompound("minecraft:profile") : null;
      }
   }

   private static boolean hasStoredProfileCompound(ReadWriteNBT nbt) {
      return mergeStoredProfileCompounds(null, nbt);
   }

   private static boolean mergeStoredProfileCompounds(ReadWriteNBT target, ReadWriteNBT source) {
      boolean copied = false;
      copied |= mergeStoredProfileCompound(target, source, "Owner");
      copied |= mergeStoredProfileCompound(target, source, "SkullOwner");
      copied |= mergeStoredProfileCompound(target, source, "profile");
      return copied | mergeStoredProfileCompound(target, source, "minecraft:profile");
   }

   private static boolean mergeStoredProfileCompound(ReadWriteNBT target, ReadWriteNBT source, String key) {
      if (source == null) {
         return false;
      } else {
         try {
            if (!source.hasTag(key)) {
               return false;
            } else {
               ReadWriteNBT sourceCompound = source.getCompound(key);
               if (sourceCompound == null) {
                  return false;
               } else {
                  if (target != null) {
                     target.getOrCreateCompound(key).mergeCompound(sourceCompound);
                  }

                  return true;
               }
            }
         } catch (Throwable var4) {
            return false;
         }
      }
   }

   private static HeadHelper.SkullProfileData buildProfileData(String texture, String ownerName) {
      String normalizedTexture = normalizeValue(texture);
      String normalizedOwnerName = normalizeValue(ownerName);
      return normalizedTexture == null && normalizedOwnerName == null ? null : new HeadHelper.SkullProfileData(normalizedTexture, normalizedOwnerName);
   }

   private static boolean applyTexture(Skull skull, String texture) {
      String skinUrl = getSkinUrl(texture);
      if (skinUrl != null && !skinUrl.isEmpty()) {
         try {
            Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Object profile = createPlayerProfile();
            if (profile == null) {
               return false;
            } else {
               Object textures = profileClass.getMethod("getTextures").invoke(profile);
               if (textures == null) {
                  return false;
               } else {
                  Class<?> texturesClass = textures.getClass();
                  texturesClass.getMethod("setSkin", URL.class).invoke(textures, new URL(skinUrl));

                  try {
                     profileClass.getMethod("setTextures", texturesClass).invoke(profile, textures);
                  } catch (Throwable var8) {
                  }

                  skull.getClass().getMethod("setOwnerProfile", profileClass).invoke(skull, profile);
                  return true;
               }
            }
         } catch (Throwable var9) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Object createPlayerProfile() {
      try {
         return Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class).invoke(null, UUID.randomUUID(), null);
      } catch (Throwable var2) {
         try {
            return Bukkit.class.getMethod("createPlayerProfile", UUID.class).invoke(null, UUID.randomUUID());
         } catch (Throwable var1) {
            return null;
         }
      }
   }

   private static boolean applyOwnerName(Skull skull, String ownerName) {
      try {
         Object profile = createNamedPlayerProfile(ownerName);
         if (profile != null) {
            Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            skull.getClass().getMethod("setOwnerProfile", profileClass).invoke(skull, profile);
            return true;
         }
      } catch (Throwable var5) {
      }

      try {
         skull.getClass().getMethod("setOwner", String.class).invoke(skull, ownerName);
         return true;
      } catch (Throwable var4) {
         return false;
      }
   }

   private static Object createNamedPlayerProfile(String ownerName) {
      if (ownerName != null && !ownerName.trim().isEmpty()) {
         try {
            return Bukkit.class.getMethod("createPlayerProfile", String.class).invoke(null, ownerName);
         } catch (Throwable var3) {
            try {
               return Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class).invoke(null, null, ownerName);
            } catch (Throwable var2) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static String getSkinUrl(String texture) {
      if (texture != null && !texture.isEmpty()) {
         try {
            String decoded = new String(Base64.getDecoder().decode(texture), StandardCharsets.UTF_8);
            Matcher matcher = SKIN_URL_PATTERN.matcher(decoded);
            if (matcher.find()) {
               return matcher.group(1);
            }
         } catch (IllegalArgumentException var3) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static String extractTextureFromProfile(ReadWriteNBT profile) {
      if (profile.hasTag("Properties")) {
         ReadWriteNBT props = profile.getCompound("Properties");
         if (props.hasTag("textures")) {
            return getTextureFromList(props.getCompoundList("textures"));
         }
      }

      if (profile.hasTag("properties")) {
         try {
            for (ReadWriteNBT entry : profile.getCompoundList("properties")) {
               if (entry.hasTag("name") && "textures".equals(entry.getString("name"))) {
                  return entry.getString("value");
               }
            }
         } catch (Exception var5) {
         }

         try {
            ReadWriteNBT props = profile.getCompound("properties");
            if (props.hasTag("textures")) {
               return getTextureFromList(props.getCompoundList("textures"));
            }
         } catch (Exception var4) {
         }
      }

      return null;
   }

   private static String extractProfileName(ReadWriteNBT profile) {
      if (profile.hasTag("name")) {
         return normalizeValue(profile.getString("name"));
      } else {
         return profile.hasTag("Name") ? normalizeValue(profile.getString("Name")) : null;
      }
   }

   private static HeadHelper.SkullProfileData extractProfileDataFromRawNbt(String nbtData) {
      String profileBody = findProfileCompoundBody(nbtData);
      if (profileBody == null) {
         return null;
      } else {
         String texture = extractTextureFromRawProfile(profileBody);
         String ownerName = extractTopLevelStringProperty(profileBody, "name", "Name");
         return buildProfileData(texture, ownerName);
      }
   }

   private static String findProfileCompoundBody(String nbtData) {
      String[] keys = new String[]{"SkullOwner", "Owner", "profile", "minecraft:profile"};

      for (String key : keys) {
         int braceIndex = findCompoundStart(nbtData, key);
         if (braceIndex != -1) {
            return extractBalancedSection(nbtData, braceIndex, '{', '}');
         }
      }

      return null;
   }

   private static int findCompoundStart(String nbtData, String key) {
      Pattern pattern = Pattern.compile("(?:^|[\\{,])\\s*\\\"?" + Pattern.quote(key) + "\\\"?\\s*:\\s*\\{");
      Matcher matcher = pattern.matcher(nbtData);
      return matcher.find() ? matcher.end() - 1 : -1;
   }

   private static String extractBalancedSection(String value, int openIndex, char openChar, char closeChar) {
      if (value != null && openIndex >= 0 && openIndex < value.length() && value.charAt(openIndex) == openChar) {
         int depth = 0;
         boolean inString = false;
         boolean escaped = false;

         for (int index = openIndex; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaped) {
               escaped = false;
            } else if (current == '\\') {
               escaped = true;
            } else if (current == '"') {
               inString = !inString;
            } else if (!inString) {
               if (current == openChar) {
                  depth++;
               } else if (current == closeChar) {
                  if (--depth == 0) {
                     return value.substring(openIndex + 1, index);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String extractTextureFromRawProfile(String profileBody) {
      if (profileBody != null && !profileBody.isEmpty()) {
         Matcher matcher = Pattern.compile("\\b(?:Value|value)\\b\\s*:\\s*\"([^\"]+)\"").matcher(profileBody);
         return matcher.find() ? normalizeValue(matcher.group(1)) : null;
      } else {
         return null;
      }
   }

   private static String extractTopLevelStringProperty(String compoundBody, String... keys) {
      if (compoundBody != null && !compoundBody.isEmpty()) {
         Map<String, String> properties = new LinkedHashMap<>();

         for (String entry : splitTopLevel(compoundBody)) {
            int separator = findTopLevelSeparator(entry, ':');
            if (separator != -1) {
               String rawKey = trimQuotes(entry.substring(0, separator).trim());
               String rawValue = entry.substring(separator + 1).trim();
               if (!rawKey.isEmpty() && rawValue.length() >= 2 && rawValue.charAt(0) == '"' && rawValue.charAt(rawValue.length() - 1) == '"') {
                  properties.put(rawKey, trimQuotes(rawValue));
               }
            }
         }

         for (String key : keys) {
            String value = normalizeValue(properties.get(key));
            if (value != null) {
               return value;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<String> splitTopLevel(String compoundBody) {
      List<String> parts = new ArrayList<>();
      int start = 0;
      int compoundDepth = 0;
      int listDepth = 0;
      boolean inString = false;
      boolean escaped = false;

      for (int index = 0; index < compoundBody.length(); index++) {
         char current = compoundBody.charAt(index);
         if (escaped) {
            escaped = false;
         } else if (current == '\\') {
            escaped = true;
         } else if (current == '"') {
            inString = !inString;
         } else if (!inString) {
            if (current == '{') {
               compoundDepth++;
            } else if (current == '}') {
               compoundDepth--;
            } else if (current == '[') {
               listDepth++;
            } else if (current == ']') {
               listDepth--;
            } else if (current == ',' && compoundDepth == 0 && listDepth == 0) {
               parts.add(compoundBody.substring(start, index));
               start = index + 1;
            }
         }
      }

      if (start < compoundBody.length()) {
         parts.add(compoundBody.substring(start));
      }

      return parts;
   }

   private static int findTopLevelSeparator(String value, char separator) {
      int compoundDepth = 0;
      int listDepth = 0;
      boolean inString = false;
      boolean escaped = false;

      for (int index = 0; index < value.length(); index++) {
         char current = value.charAt(index);
         if (escaped) {
            escaped = false;
         } else if (current == '\\') {
            escaped = true;
         } else if (current == '"') {
            inString = !inString;
         } else if (!inString) {
            if (current == '{') {
               compoundDepth++;
            } else if (current == '}') {
               compoundDepth--;
            } else if (current == '[') {
               listDepth++;
            } else if (current == ']') {
               listDepth--;
            } else if (current == separator && compoundDepth == 0 && listDepth == 0) {
               return index;
            }
         }
      }

      return -1;
   }

   private static String trimQuotes(String value) {
      if (value == null || value.length() < 2) {
         return value;
      } else {
         return value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"' ? value.substring(1, value.length() - 1) : value;
      }
   }

   private static String normalizeValue(String value) {
      if (value == null) {
         return null;
      } else {
         String trimmed = value.trim();
         return trimmed.isEmpty() ? null : trimmed;
      }
   }

   private static String getTextureFromList(ReadWriteNBTCompoundList list) {
      if (!list.isEmpty()) {
         ReadWriteNBT entry = (ReadWriteNBT)list.get(0);
         if (entry.hasTag("Value")) {
            return entry.getString("Value");
         }

         if (entry.hasTag("value")) {
            return entry.getString("value");
         }
      }

      return null;
   }

   public static final class SkullProfileData {
      private final String texture;
      private final String ownerName;

      private SkullProfileData(String texture, String ownerName) {
         this.texture = texture;
         this.ownerName = ownerName;
      }

      public String texture() {
         return this.texture;
      }

      public String ownerName() {
         return this.ownerName;
      }

      public boolean hasRenderableData() {
         return this.texture != null && !this.texture.isEmpty() || this.ownerName != null && !this.ownerName.isEmpty();
      }
   }
}
