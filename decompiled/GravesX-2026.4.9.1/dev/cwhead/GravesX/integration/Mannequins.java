package dev.cwhead.GravesX.integration;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.listener.integration.mannequins.MannequinInteractListener;
import dev.cwhead.GravesX.manager.ChunkManager;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public final class Mannequins extends EntityDataManager {
   private static final String ENTITY_TYPE_NAME = "MANNEQUIN";
   private static final String MANNEQUIN_CLASS_NAME = "org.bukkit.entity.Mannequin";
   private static final String TAG_PREFIX = "gravesx_corpse:";
   private static final String HARDCODED_DESCRIPTION = "<empty>";
   private static final long PROFILE_TIMEOUT_SECONDS = 5L;
   private final Graves plugin;
   private final boolean supported;
   private final Class<?> mannequinClass;
   private final Method setDescriptionString;
   private final Method setDescriptionComponent;
   private final Method setHideDescription;
   private final Method setImmovable;
   private final Method mannequinSetPose;
   private final Method entitySetPose;
   private final Method entitySetPoseFixed;
   private final Method setProfile;
   private final Method setPlayerProfile;

   public Mannequins(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      Mannequins.Reflection r = Mannequins.Reflection.resolve(plugin);
      this.supported = r.supported;
      this.mannequinClass = r.mannequinClass;
      this.setDescriptionString = r.setDescriptionString;
      this.setDescriptionComponent = r.setDescriptionComponent;
      this.setHideDescription = r.setHideDescription;
      this.setImmovable = r.setImmovable;
      this.mannequinSetPose = r.mannequinSetPose;
      this.entitySetPose = r.entitySetPose;
      this.entitySetPoseFixed = r.entitySetPoseFixed;
      this.setProfile = r.setProfile;
      this.setPlayerProfile = r.setPlayerProfile;
      this.registerListeners();
   }

   public void registerListeners() {
      Bukkit.getServer().getPluginManager().registerEvents(new MannequinInteractListener(this.plugin), this.plugin);
   }

   public boolean isSupported() {
      return this.supported;
   }

   public void createCorpse(UUID uuid, Location location, Grave grave) {
      this.plugin
         .getSchedulerManager()
         .runTask(
            () -> {
               if (this.supported) {
                  if (this.plugin.getConfigManager().getConfigSection("mannequins.corpse.enabled", grave).getBoolean("mannequins.corpse.enabled")) {
                     if (grave.getOwnerType() == EntityType.PLAYER) {
                        if (location != null && location.getWorld() != null) {
                           Location mannequinLocation = location.clone();

                           try {
                              double x = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("mannequins.corpse.offset.x", grave)
                                 .getDouble("mannequins.corpse.offset.x");
                              double y = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("mannequins.corpse.offset.y", grave)
                                 .getDouble("mannequins.corpse.offset.y");
                              double z = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("mannequins.corpse.offset.z", grave)
                                 .getDouble("mannequins.corpse.offset.z");
                              mannequinLocation.add(x, y, z);
                           } catch (IllegalArgumentException var15) {
                              mannequinLocation.add(-0.5, 0.0, -0.5);
                           }

                           EntityType mannequinType;
                           try {
                              mannequinType = EntityType.valueOf("MANNEQUIN");
                           } catch (Throwable var14) {
                              return;
                           }

                           Entity spawned;
                           try {
                              spawned = Objects.requireNonNull(mannequinLocation.getWorld()).spawnEntity(mannequinLocation, mannequinType);
                           } catch (Throwable var13) {
                              return;
                           }

                           if (spawned instanceof LivingEntity living && this.mannequinClass.isInstance(spawned)) {
                              String idNoDashes = uuid.toString().replace("-", "");
                              living.addScoreboardTag("gravesx_corpse:" + idNoDashes);
                              living.setAI(false);
                              living.setSilent(true);
                              living.setCanPickupItems(false);
                              living.setRemoveWhenFarAway(false);
                              living.setCollidable(false);
                              living.setInvulnerable(true);
                              living.setPersistent(true);

                              try {
                                 if (this.setDescriptionString != null) {
                                    this.setDescriptionString.invoke(living, "<empty>");
                                 }

                                 if (this.setDescriptionComponent != null) {
                                    Object comp = this.buildAdventureText("<empty>");
                                    if (comp != null) {
                                       this.setDescriptionComponent.invoke(living, comp);
                                    }
                                 }

                                 if (this.setHideDescription != null) {
                                    this.setHideDescription.invoke(living, true);
                                 }

                                 if (this.setImmovable != null) {
                                    this.setImmovable.invoke(living, true);
                                 }
                              } catch (Throwable var12) {
                              }

                              this.applyMannequinPose(
                                 living,
                                 this.plugin
                                    .getConfigManager()
                                    .getConfigSection("mannequins.corpse.pose", grave)
                                    .getString("mannequins.corpse.pose", "SWIMMING")
                                    .toUpperCase()
                              );

                              try {
                                 this.createEntityData(living.getLocation(), living.getUniqueId(), grave.getUUID(), EntityData.Type.MANNEQUIN);
                                 this.plugin
                                    .debugMessage(
                                       "[Mannequins] spawned="
                                          + spawned.getClass().getName()
                                          + " setProfile="
                                          + (this.setProfile != null)
                                          + " setPlayerProfile="
                                          + (this.setPlayerProfile != null),
                                       2
                                    );
                                 this.applySkin(living, grave);
                                 if (this.plugin.getConfigManager().getConfigSection("mannequins.corpse.armor", grave).getBoolean("mannequins.corpse.armor")) {
                                    this.equipArmor(living, grave);
                                 }

                                 if (this.plugin.getConfigManager().getConfigSection("mannequins.corpse.hand", grave).getBoolean("mannequins.corpse.hand")) {
                                    this.equipHands(living, grave);
                                 }
                              } catch (Throwable var11) {
                                 this.plugin.getLogger().severe("[Mannequins] createEntityData failed: " + var11.getCause());
                                 this.plugin.logStackTrace(var11);
                              }
                           } else {
                              spawned.remove();
                           }
                        }
                     }
                  }
               }
            }
         );
   }

   public void removeCorpse(Grave grave) {
      if (this.supported && grave != null) {
         Location base = this.safeGraveLocation(grave);
         String idNoDashes = grave.getUUID().toString().replace("-", "");
         String tag = "gravesx_corpse:" + idNoDashes;
         if (base != null && base.getWorld() != null) {
            Location anchor = base.clone();
            this.plugin.getChunkManager().ensureLoadedAndExecute(anchor, base, false, false, () -> {
               try {
                  for (Entity ex : base.getWorld().getNearbyEntities(base, 16.0, 16.0, 16.0)) {
                     if (this.isTaggedCorpse(ex, tag)) {
                        try {
                           EntityData data = new EntityData(ex.getLocation(), ex.getUniqueId(), grave.getUUID(), EntityData.Type.MANNEQUIN);
                           this.plugin.getDataManager().removeEntityData(data);
                        } catch (Throwable var8x) {
                           this.plugin.getLogger().severe(var8x.getMessage());
                           this.plugin.logStackTrace(var8x);
                        }

                        ex.remove();
                     }
                  }
               } catch (Throwable var9x) {
                  this.plugin.getLogger().severe(var9x.getMessage());
                  this.plugin.logStackTrace(var9x);
               }
            });
         } else if (this.plugin.getChunkManager().getChunkType().effective() != ChunkManager.ChunkType.FOLIA) {
            for (World w : Bukkit.getWorlds()) {
               for (Entity e : w.getEntities()) {
                  if (this.isTaggedCorpse(e, tag)) {
                     try {
                        EntityData data = new EntityData(e.getLocation(), e.getUniqueId(), grave.getUUID(), EntityData.Type.MANNEQUIN);
                        this.plugin.getDataManager().removeEntityData(data);
                     } catch (Throwable var10) {
                        this.plugin.getLogger().severe(var10.getMessage());
                        this.plugin.logStackTrace(var10);
                     }

                     e.remove();
                  }
               }
            }
         }
      }
   }

   public boolean hasCorpse(Grave grave) {
      if (this.supported && grave != null) {
         Location base = this.safeGraveLocation(grave);
         String idNoDashes = grave.getUUID().toString().replace("-", "");
         String tag = "gravesx_corpse:" + idNoDashes;
         if (base != null && base.getWorld() != null) {
            for (Entity e : base.getWorld().getNearbyEntities(base, 16.0, 16.0, 16.0)) {
               if (this.isTaggedCorpse(e, tag)) {
                  return true;
               }
            }

            return false;
         } else {
            for (World w : Bukkit.getWorlds()) {
               for (Entity ex : w.getEntities()) {
                  if (this.isTaggedCorpse(ex, tag)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isTaggedCorpse(Entity e, String tag) {
      if (e == null) {
         return false;
      } else {
         return !this.mannequinClass.isInstance(e) ? false : e.getScoreboardTags().contains(tag);
      }
   }

   private void applyMannequinPose(LivingEntity mannequin, String pose) {
      try {
         if (this.mannequinSetPose != null) {
            try {
               this.mannequinSetPose.invoke(mannequin, Pose.valueOf(pose));
            } catch (Exception var6) {
               this.plugin.getLogger().severe("There was an issue using pose " + pose + ". Cause:" + var6.getMessage());
               this.plugin.logStackTrace(var6);
               this.mannequinSetPose.invoke(mannequin, Pose.SWIMMING);
            }

            return;
         }

         if (this.entitySetPoseFixed != null) {
            try {
               this.entitySetPoseFixed.invoke(mannequin, Pose.valueOf(pose), true);
            } catch (Exception var5) {
               this.plugin.getLogger().severe("There was an issue using pose " + pose + ". Cause:" + var5.getMessage());
               this.plugin.logStackTrace(var5);
               this.entitySetPoseFixed.invoke(mannequin, Pose.SWIMMING, true);
            }

            return;
         }

         if (this.entitySetPose != null) {
            try {
               this.entitySetPose.invoke(mannequin, Pose.valueOf(pose));
            } catch (Exception var4) {
               this.plugin.getLogger().severe("There was an issue using pose " + pose + ". Cause:" + var4.getMessage());
               this.plugin.logStackTrace(var4);
               this.entitySetPose.invoke(mannequin, Pose.SWIMMING);
            }

            return;
         }

         if (Objects.equals(pose, "SWIMMING")) {
            mannequin.setSwimming(true);
         }
      } catch (Throwable var7) {
      }
   }

   private void equipArmor(LivingEntity mannequin, Grave grave) {
      EntityEquipment eq = mannequin.getEquipment();
      if (eq != null) {
         if (grave.getEquipmentMap().containsKey(EquipmentSlot.HEAD)) {
            eq.setHelmet(grave.getEquipmentMap().get(EquipmentSlot.HEAD));
         }

         if (grave.getEquipmentMap().containsKey(EquipmentSlot.CHEST)) {
            eq.setChestplate(grave.getEquipmentMap().get(EquipmentSlot.CHEST));
         }

         if (grave.getEquipmentMap().containsKey(EquipmentSlot.LEGS)) {
            eq.setLeggings(grave.getEquipmentMap().get(EquipmentSlot.LEGS));
         }

         if (grave.getEquipmentMap().containsKey(EquipmentSlot.FEET)) {
            eq.setBoots(grave.getEquipmentMap().get(EquipmentSlot.FEET));
         }
      }
   }

   private void equipHands(LivingEntity mannequin, Grave grave) {
      EntityEquipment eq = mannequin.getEquipment();
      if (eq != null) {
         if (grave.getEquipmentMap().containsKey(EquipmentSlot.HAND)) {
            eq.setItemInMainHand(grave.getEquipmentMap().get(EquipmentSlot.HAND));
         }

         if (this.plugin.getVersionManager().hasSecondHand() && grave.getEquipmentMap().containsKey(EquipmentSlot.OFF_HAND)) {
            eq.setItemInOffHand(grave.getEquipmentMap().get(EquipmentSlot.OFF_HAND));
         }
      }
   }

   private void applySkin(LivingEntity mannequin, Grave grave) {
      if (grave != null) {
         UUID owner = grave.getOwnerUUID();
         if (owner != null) {
            String username = null;

            try {
               OfflinePlayer off = Bukkit.getOfflinePlayer(owner);
               username = off != null ? off.getName() : null;
            } catch (Throwable var8) {
            }

            if (username == null || username.isEmpty()) {
               username = "GravesX";
            }

            String texturesBase64 = toValidBase64Texture(grave.getOwnerTexture());
            String sig = safeString(grave::getOwnerTextureSignature);
            if (this.setProfile != null) {
               String ptName = this.setProfile.getParameterTypes()[0].getName();
               if ("io.papermc.paper.datacomponent.item.ResolvableProfile".equals(ptName)) {
                  this.plugin.debugMessage("[Mannequins] applySkin: Paper ResolvableProfile path; name=" + username, 1);
                  this.applySkinPaperResolvable(mannequin, username, texturesBase64, sig);
                  return;
               }
            }

            if (this.setPlayerProfile != null) {
               String ptName = this.setPlayerProfile.getParameterTypes()[0].getName();
               if ("org.bukkit.profile.PlayerProfile".equals(ptName)) {
                  this.plugin.debugMessage("[Mannequins] applySkin: Bukkit PlayerProfile path; name=" + username, 1);
                  this.applySkinBukkitProfile(mannequin, owner, username, texturesBase64, sig);
                  return;
               }
            }

            this.plugin.debugMessage("[Mannequins] applySkin: no compatible skin setter found", 1);
         }
      }
   }

   private void applySkinPaperResolvable(LivingEntity mannequin, String username, String texturesBase64, String sig) {
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            Object resolved;
            if (texturesBase64 != null && !texturesBase64.isEmpty()) {
               resolved = Mannequins.PaperSkin.buildResolvedFromTextures(username, texturesBase64, sig);
            } else {
               CompletableFuture<Object> fut = CompletableFuture.supplyAsync(() -> Mannequins.PaperSkin.buildResolvedFromLookup(username));
               resolved = fut.get(5L, TimeUnit.SECONDS);
            }

            if (resolved == null) {
               this.plugin.debugMessage("[Mannequins] applySkinPaperResolvable: resolved profile is null", 1);
               return;
            }

            Object finalResolved = resolved;
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               if (mannequin.isValid()) {
                  try {
                     this.setProfile.invoke(mannequin, finalResolved);
                     this.plugin.debugMessage("[Mannequins] applySkinPaperResolvable: setProfile success", 1);
                  } catch (Throwable var4x) {
                     this.plugin.debugMessage("[Mannequins] applySkinPaperResolvable: setProfile failed: " + var4x, 1);
                  }
               }
            });
         } catch (Throwable var7) {
            this.plugin.debugMessage("[Mannequins] applySkinPaperResolvable failed: " + var7, 1);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               if (mannequin.isValid()) {
                  try {
                     Object resolvedx = Mannequins.PaperSkin.buildResolvedDefault(username);
                     if (resolvedx != null) {
                        this.setProfile.invoke(mannequin, resolvedx);
                        this.plugin.debugMessage("[Mannequins] applySkinPaperResolvable: default skin applied", 2);
                     }
                  } catch (Throwable var4x) {
                  }
               }
            });
         }
      });
   }

   private void applySkinBukkitProfile(LivingEntity mannequin, UUID uuid, String username, String texturesBase64, String sig) {
      try {
         Object profile = Mannequins.BukkitSkin.buildProfile(uuid, username, texturesBase64, sig);
         if (profile == null) {
            this.plugin.debugMessage("[Mannequins] applySkinBukkitProfile: profile null", 1);
            return;
         }

         this.setPlayerProfile.invoke(mannequin, profile);
         this.plugin.debugMessage("[Mannequins] applySkinBukkitProfile: setPlayerProfile success", 1);
      } catch (NoSuchMethodError | NoClassDefFoundError var7) {
         this.plugin.debugMessage("[Mannequins] applySkinBukkitProfile: Bukkit profile API not present: " + var7, 1);
      } catch (Throwable var8) {
         this.plugin.debugMessage("[Mannequins] applySkinBukkitProfile failed: " + var8, 1);
      }
   }

   private static String toValidBase64Texture(String graveTexture) {
      if (graveTexture == null) {
         return "";
      } else {
         String s = graveTexture.trim();
         if (s.isEmpty()) {
            return "";
         } else if (s.startsWith("eyJ")) {
            return s;
         } else {
            String url = s;
            if (s.matches("^[0-9a-fA-F]{32,}$")) {
               url = "https://textures.minecraft.net/texture/" + s;
            } else if (s.startsWith("textures.minecraft.net/texture/")) {
               url = "https://" + s;
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
               return "";
            } else {
               String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url);
               return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            }
         }
      }
   }

   private Object buildAdventureText(String text) {
      try {
         Class<?> component = Class.forName("com.ranull.graves.libraries.kyori.adventure.text.Component");
         Method m = component.getMethod("text", String.class);
         return m.invoke(null, text);
      } catch (Throwable var4) {
         return null;
      }
   }

   private Location safeGraveLocation(Grave grave) {
      try {
         Method m = grave.getClass().getMethod("getLocation");
         return m.invoke(grave) instanceof Location l ? l : null;
      } catch (Throwable var5) {
         return null;
      }
   }

   private static String safeString(Mannequins.SupplierThrows<String> sup) {
      try {
         String s = sup.get();
         return s == null ? null : s.trim();
      } catch (Throwable var2) {
         return null;
      }
   }

   private static final class BukkitSkin {
      private static final Pattern BASE64_JSON_URL = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

      static Object buildProfile(UUID uuid, String name, String textureString, String sigIgnored) {
         try {
            PlayerProfile profile = Bukkit.createPlayerProfile(uuid, name);
            URL skinUrl = extractSkinUrl(textureString);
            if (skinUrl != null) {
               PlayerTextures textures = profile.getTextures();
               textures.setSkin(skinUrl);
               profile.setTextures(textures);
            }

            return profile;
         } catch (NoSuchMethodError | NoClassDefFoundError var7) {
            return null;
         } catch (Throwable var8) {
            return null;
         }
      }

      private static URL extractSkinUrl(String textureValue) {
         if (textureValue == null) {
            return null;
         } else {
            String s = textureValue.trim();
            if (s.isEmpty()) {
               return null;
            } else {
               try {
                  if (s.startsWith("http://") || s.startsWith("https://")) {
                     return new URL(s);
                  }

                  if (s.startsWith("textures.minecraft.net/texture/")) {
                     return new URL("https://" + s);
                  }

                  if (s.matches("^[0-9a-fA-F]{32,}$")) {
                     return new URL("https://textures.minecraft.net/texture/" + s);
                  }

                  String json = new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
                  Matcher m = BASE64_JSON_URL.matcher(json);
                  if (m.find()) {
                     String url = m.group(1);
                     if (url.startsWith("http://") || url.startsWith("https://")) {
                        return new URL(url);
                     }

                     if (url.startsWith("textures.minecraft.net/texture/")) {
                        return new URL("https://" + url);
                     }
                  }
               } catch (Throwable var5) {
               }

               return null;
            }
         }
      }
   }

   private static final class PaperSkin {
      static Object buildResolvedFromTextures(String username, String texturesBase64, String sig) {
         try {
            com.destroystokyo.paper.profile.PlayerProfile profile = (com.destroystokyo.paper.profile.PlayerProfile)Bukkit.createPlayerProfile(username);
            if (sig != null && !sig.isEmpty()) {
               profile.setProperty(new ProfileProperty("textures", texturesBase64, sig));
            } else {
               profile.setProperty(new ProfileProperty("textures", texturesBase64));
            }

            return ResolvableProfile.resolvableProfile(profile);
         } catch (NoSuchMethodError | NoClassDefFoundError var4) {
            return null;
         }
      }

      static Object buildResolvedFromLookup(String username) {
         try {
            com.destroystokyo.paper.profile.PlayerProfile profile = (com.destroystokyo.paper.profile.PlayerProfile)Bukkit.createPlayerProfile(username);
            profile.update();
            return ResolvableProfile.resolvableProfile(profile);
         } catch (NoSuchMethodError | NoClassDefFoundError var2) {
            return null;
         }
      }

      static Object buildResolvedDefault(String username) {
         try {
            com.destroystokyo.paper.profile.PlayerProfile profile = (com.destroystokyo.paper.profile.PlayerProfile)Bukkit.createPlayerProfile(username);
            return ResolvableProfile.resolvableProfile(profile);
         } catch (NoSuchMethodError | NoClassDefFoundError var2) {
            return null;
         }
      }
   }

   private static final class Reflection {
      final boolean supported;
      final Class<?> mannequinClass;
      final Method setDescriptionString;
      final Method setDescriptionComponent;
      final Method setHideDescription;
      final Method setImmovable;
      final Method mannequinSetPose;
      final Method entitySetPose;
      final Method entitySetPoseFixed;
      final Method setProfile;
      final Method setPlayerProfile;

      private Reflection(
         boolean supported,
         Class<?> mannequinClass,
         Method setDescriptionString,
         Method setDescriptionComponent,
         Method setHideDescription,
         Method setImmovable,
         Method mannequinSetPose,
         Method entitySetPose,
         Method entitySetPoseFixed,
         Method setProfile,
         Method setPlayerProfile
      ) {
         this.supported = supported;
         this.mannequinClass = mannequinClass;
         this.setDescriptionString = setDescriptionString;
         this.setDescriptionComponent = setDescriptionComponent;
         this.setHideDescription = setHideDescription;
         this.setImmovable = setImmovable;
         this.mannequinSetPose = mannequinSetPose;
         this.entitySetPose = entitySetPose;
         this.entitySetPoseFixed = entitySetPoseFixed;
         this.setProfile = setProfile;
         this.setPlayerProfile = setPlayerProfile;
      }

      static Mannequins.Reflection resolve(Graves plugin) {
         try {
            EntityType.valueOf("MANNEQUIN");
            Class<?> mannequin = Class.forName("org.bukkit.entity.Mannequin");
            Method setImmovable = tryMethod(mannequin, "setImmovable", boolean.class);
            Method setHideDescription = tryMethod(mannequin, "setHideDescription", boolean.class);
            Method setDescriptionString = tryMethod(mannequin, "setDescription", String.class);
            Method mannequinSetPose = tryMethod(mannequin, "setPose", Pose.class);
            Method entitySetPose = null;
            Method entitySetPoseFixed = null;

            try {
               Class<?> entity = Class.forName("org.bukkit.entity.Entity");
               entitySetPose = tryMethod(entity, "setPose", Pose.class);
               entitySetPoseFixed = tryMethod(entity, "setPose", Pose.class, boolean.class);
            } catch (Throwable var15) {
            }

            Method setProfile = null;
            Method setPlayerProfile = null;

            for (Method m : mannequin.getMethods()) {
               if (m.getName().equals("setProfile") && m.getParameterCount() == 1) {
                  setProfile = m;
               }

               if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) {
                  setPlayerProfile = m;
               }
            }

            Method setDescriptionComponent = null;

            try {
               Class<?> component = Class.forName("com.ranull.graves.libraries.kyori.adventure.text.Component");
               setDescriptionComponent = tryMethod(mannequin, "setDescription", component);
            } catch (Throwable var14) {
            }

            boolean ok = setImmovable != null
               && (mannequinSetPose != null || entitySetPose != null || entitySetPoseFixed != null)
               && (setProfile != null || setPlayerProfile != null);
            plugin.debugMessage(
               "[Mannequins] Reflection: ok="
                  + ok
                  + " setProfile="
                  + (setProfile != null ? setProfile.getParameterTypes()[0].getName() : "null")
                  + " setPlayerProfile="
                  + (setPlayerProfile != null ? setPlayerProfile.getParameterTypes()[0].getName() : "null"),
               2
            );
            return new Mannequins.Reflection(
               ok,
               mannequin,
               setDescriptionString,
               setDescriptionComponent,
               setHideDescription,
               setImmovable,
               mannequinSetPose,
               entitySetPose,
               entitySetPoseFixed,
               setProfile,
               setPlayerProfile
            );
         } catch (Throwable var16) {
            plugin.debugMessage("[Mannequins] Reflection failed: " + var16, 1);
            return new Mannequins.Reflection(false, Object.class, null, null, null, null, null, null, null, null, null);
         }
      }

      private static Method tryMethod(Class<?> clazz, String name, Class<?>... params) {
         try {
            return clazz.getMethod(name, params);
         } catch (Throwable var4) {
            return null;
         }
      }
   }

   @FunctionalInterface
   private interface SupplierThrows<T> {
      T get() throws Throwable;
   }
}
