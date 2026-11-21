package com.github.retrooper.packetevents.protocol.player;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum ClientVersion {
   V_1_7_2(4),
   V_1_7_10(5),
   V_1_8(47),
   V_1_9(107),
   V_1_9_1(108),
   V_1_9_2(109),
   V_1_9_3(110),
   V_1_10(210),
   V_1_11(315),
   V_1_11_1(316),
   V_1_12(335),
   V_1_12_1(338),
   V_1_12_2(340),
   V_1_13(393),
   V_1_13_1(401),
   V_1_13_2(404),
   V_1_14(477),
   V_1_14_1(480),
   V_1_14_2(485),
   V_1_14_3(490),
   V_1_14_4(498),
   V_1_15(573),
   V_1_15_1(575),
   V_1_15_2(578),
   V_1_16(735),
   V_1_16_1(736),
   V_1_16_2(751),
   V_1_16_3(753),
   V_1_16_4(754),
   V_1_17(755),
   V_1_17_1(756),
   V_1_18(757),
   V_1_18_2(758),
   V_1_19(759),
   V_1_19_1(760),
   V_1_19_3(761),
   V_1_19_4(762),
   V_1_20(763),
   V_1_20_2(764),
   V_1_20_3(765),
   V_1_20_5(766),
   V_1_21(767),
   V_1_21_2(768),
   V_1_21_4(769),
   V_1_21_5(770),
   V_1_21_6(771),
   V_1_21_7(772),
   V_1_21_9(773),
   V_1_21_11(774),
   V_26_1(775),
   V_26_2(776),
   @Deprecated
   LOWER_THAN_SUPPORTED_VERSIONS,
   @Deprecated
   HIGHER_THAN_SUPPORTED_VERSIONS,
   UNKNOWN(-1, true);

   private static final ClientVersion[] VALUES = values();
   private static final ClientVersion[] REVERSED_VALUES;
   private static final int LOWEST_SUPPORTED_PROTOCOL_VERSION;
   private static final int HIGHEST_SUPPORTED_PROTOCOL_VERSION;
   private final int protocolVersion;
   private final String name;
   private ServerVersion serverVersion;

   private ClientVersion(int protocolVersion) {
      this.protocolVersion = protocolVersion;
      this.name = this.name().substring(2).replace("_", ".");
   }

   private ClientVersion(int protocolVersion, boolean isNotRelease) {
      this.protocolVersion = protocolVersion;
      if (isNotRelease) {
         this.name = this.name();
      } else {
         this.name = this.name().substring(2).replace("_", ".");
      }
   }

   public static boolean isPreRelease(int protocolVersion) {
      return getLatest().protocolVersion <= protocolVersion || getOldest().protocolVersion >= protocolVersion;
   }

   public static boolean isRelease(int protocolVersion) {
      return protocolVersion <= getLatest().protocolVersion && protocolVersion >= getOldest().protocolVersion;
   }

   public boolean isPreRelease() {
      return isPreRelease(this.protocolVersion);
   }

   public boolean isRelease() {
      return isRelease(this.protocolVersion);
   }

   public String getReleaseName() {
      return this.name;
   }

   @NotNull
   public static ClientVersion getById(int protocolVersion) {
      if (protocolVersion < LOWEST_SUPPORTED_PROTOCOL_VERSION) {
         return getOldest();
      } else if (protocolVersion > HIGHEST_SUPPORTED_PROTOCOL_VERSION) {
         return getLatest();
      } else {
         for (ClientVersion version : VALUES) {
            if (version.protocolVersion > protocolVersion) {
               break;
            }

            if (version.protocolVersion == protocolVersion) {
               return version;
            }
         }

         return UNKNOWN;
      }
   }

   public static ClientVersion getLatest() {
      return REVERSED_VALUES[3];
   }

   public static ClientVersion getOldest() {
      return VALUES[0];
   }

   @Deprecated
   public ServerVersion toServerVersion() {
      if (this.serverVersion == null) {
         this.serverVersion = ServerVersion.getById(this.protocolVersion);
      }

      return this.serverVersion;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public boolean isNewerThan(ClientVersion target) {
      return this.protocolVersion > target.protocolVersion;
   }

   public boolean isNewerThanOrEquals(ClientVersion target) {
      return this.protocolVersion >= target.protocolVersion;
   }

   public boolean isOlderThan(ClientVersion target) {
      return this.protocolVersion < target.protocolVersion;
   }

   public boolean isOlderThanOrEquals(ClientVersion target) {
      return this.protocolVersion <= target.protocolVersion;
   }

   public boolean is(@NotNull VersionComparison comparison, @NotNull ClientVersion targetVersion) {
      switch (comparison) {
         case EQUALS:
            return this.protocolVersion == targetVersion.protocolVersion;
         case NEWER_THAN:
            return this.isNewerThan(targetVersion);
         case NEWER_THAN_OR_EQUALS:
            return this.isNewerThanOrEquals(targetVersion);
         case OLDER_THAN:
            return this.isOlderThan(targetVersion);
         case OLDER_THAN_OR_EQUALS:
            return this.isOlderThanOrEquals(targetVersion);
         default:
            return false;
      }
   }

   // $VF: Failed to inline enum fields
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   static {
      LOWER_THAN_SUPPORTED_VERSIONS = new ClientVersion(V_1_7_2.protocolVersion - 1, true);
      HIGHER_THAN_SUPPORTED_VERSIONS = new ClientVersion(V_26_2.protocolVersion + 1, true);
      List<ClientVersion> valuesAsList = Arrays.asList(values());
      Collections.reverse(valuesAsList);
      REVERSED_VALUES = valuesAsList.toArray(new ClientVersion[0]);
      LOWEST_SUPPORTED_PROTOCOL_VERSION = LOWER_THAN_SUPPORTED_VERSIONS.protocolVersion + 1;
      HIGHEST_SUPPORTED_PROTOCOL_VERSION = HIGHER_THAN_SUPPORTED_VERSIONS.protocolVersion - 1;
   }
}
