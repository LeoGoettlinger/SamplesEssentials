package com.earth2me.essentials.utils;

import com.earth2me.essentials.paperlib.PaperLib;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ess3.nms.refl.ReflUtil;
import org.bukkit.Bukkit;

public final class VersionUtil {
   public static final VersionUtil.BukkitVersion v1_14_R01 = VersionUtil.BukkitVersion.fromString("1.14-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_8_8_R01 = VersionUtil.BukkitVersion.fromString("1.8.8-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_9_R01 = VersionUtil.BukkitVersion.fromString("1.9-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_9_4_R01 = VersionUtil.BukkitVersion.fromString("1.9.4-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_10_2_R01 = VersionUtil.BukkitVersion.fromString("1.10.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_11_R01 = VersionUtil.BukkitVersion.fromString("1.11-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_11_2_R01 = VersionUtil.BukkitVersion.fromString("1.11.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_12_0_R01 = VersionUtil.BukkitVersion.fromString("1.12.0-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_12_2_R01 = VersionUtil.BukkitVersion.fromString("1.12.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_13_0_R01 = VersionUtil.BukkitVersion.fromString("1.13.0-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_13_2_R01 = VersionUtil.BukkitVersion.fromString("1.13.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_14_4_R01 = VersionUtil.BukkitVersion.fromString("1.14.4-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_15_R01 = VersionUtil.BukkitVersion.fromString("1.15-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_15_2_R01 = VersionUtil.BukkitVersion.fromString("1.15.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_16_1_R01 = VersionUtil.BukkitVersion.fromString("1.16.1-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_16_5_R01 = VersionUtil.BukkitVersion.fromString("1.16.5-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_17_R01 = VersionUtil.BukkitVersion.fromString("1.17-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_17_1_R01 = VersionUtil.BukkitVersion.fromString("1.17.1-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_18_2_R01 = VersionUtil.BukkitVersion.fromString("1.18.2-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_19_R01 = VersionUtil.BukkitVersion.fromString("1.19-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_19_4_R01 = VersionUtil.BukkitVersion.fromString("1.19.4-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_20_1_R01 = VersionUtil.BukkitVersion.fromString("1.20.1-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_20_4_R01 = VersionUtil.BukkitVersion.fromString("1.20.4-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_20_6_R01 = VersionUtil.BukkitVersion.fromString("1.20.6-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_21_R01 = VersionUtil.BukkitVersion.fromString("1.21-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_21_3_R01 = VersionUtil.BukkitVersion.fromString("1.21.3-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_21_5_R01 = VersionUtil.BukkitVersion.fromString("1.21.5-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_21_8_R01 = VersionUtil.BukkitVersion.fromString("1.21.8-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v1_21_11_R01 = VersionUtil.BukkitVersion.fromString("1.21.11-R0.1-SNAPSHOT");
   public static final VersionUtil.BukkitVersion v26_1_R01 = VersionUtil.BukkitVersion.fromString("26.1.2-R0.1-SNAPSHOT");
   private static final Set<VersionUtil.BukkitVersion> supportedVersions = ImmutableSet.of(
      v1_8_8_R01,
      v1_9_4_R01,
      v1_10_2_R01,
      v1_11_2_R01,
      v1_12_2_R01,
      v1_13_2_R01,
      new VersionUtil.BukkitVersion[]{v1_14_4_R01, v1_15_2_R01, v1_16_5_R01, v1_17_1_R01, v1_18_2_R01, v1_19_4_R01, v1_20_6_R01, v1_21_11_R01, v26_1_R01}
   );
   public static final boolean PRE_FLATTENING = getServerBukkitVersion().isLowerThan(v1_13_0_R01);
   public static final List<String> officialPlugins = Arrays.asList(
      "EssentialsAntiBuild",
      "EssentialsChat",
      "EssentialsDiscord",
      "EssentialsDiscordLink",
      "EssentialsGeoIP",
      "EssentialsProtect",
      "EssentialsSpawn",
      "EssentialsXMPP"
   );
   public static final List<String> warnPlugins = Arrays.asList("PermissionsEx", "GroupManager", "bPermissions", "NoChatReports", "NoEncryption");
   private static final Map<String, VersionUtil.SupportStatus> unsupportedServerClasses;
   private static final String PFX = make("8(;4>`");
   private static VersionUtil.BukkitVersion serverVersion;
   private static VersionUtil.SupportStatus supportStatus;
   private static String supportStatusClass;

   private VersionUtil() {
   }

   public static boolean isPaper() {
      return PaperLib.isPaper();
   }

   public static VersionUtil.BukkitVersion getServerBukkitVersion() {
      if (serverVersion == null) {
         serverVersion = VersionUtil.BukkitVersion.fromString(Bukkit.getServer().getBukkitVersion());
      }

      return serverVersion;
   }

   public static VersionUtil.SupportStatus getServerSupportStatus() {
      if (supportStatus != null) {
         return supportStatus;
      } else {
         for (Entry<String, VersionUtil.SupportStatus> entry : unsupportedServerClasses.entrySet()) {
            if (entry.getKey().startsWith(PFX)) {
               if (Bukkit.getName().equalsIgnoreCase(entry.getKey().replaceFirst(PFX, ""))) {
                  supportStatusClass = entry.getKey();
                  return supportStatus = entry.getValue();
               }
            } else {
               boolean inverted = entry.getKey().contains("!");
               String clazz = entry.getKey().replace("!", "").split("#")[0];
               String method = "";
               if (entry.getKey().contains("#")) {
                  method = entry.getKey().split("#")[1];
               }

               try {
                  Class<?> lolClass = Class.forName(clazz);
                  if (!method.isEmpty()) {
                     for (Method mth : lolClass.getDeclaredMethods()) {
                        if (mth.getName().equals(method) && !inverted) {
                           supportStatusClass = entry.getKey();
                           return supportStatus = entry.getValue();
                        }
                     }
                  } else if (!inverted) {
                     supportStatusClass = entry.getKey();
                     return supportStatus = entry.getValue();
                  }
               } catch (ClassNotFoundException var10) {
                  if (inverted) {
                     supportStatusClass = entry.getKey();
                     return supportStatus = entry.getValue();
                  }
               }
            }
         }

         return !isSupportedVersion(getServerBukkitVersion())
            ? (supportStatus = VersionUtil.SupportStatus.OUTDATED)
            : (supportStatus = PaperLib.isPaper() ? VersionUtil.SupportStatus.FULL : VersionUtil.SupportStatus.LIMITED);
      }
   }

   public static String getSupportStatusClass() {
      return supportStatusClass;
   }

   private static boolean isSupportedVersion(final VersionUtil.BukkitVersion version) {
      if (supportedVersions.contains(version)) {
         return true;
      } else {
         for (VersionUtil.BukkitVersion supported : supportedVersions) {
            if (version.equalsBaseVersion(supported)) {
               return true;
            }
         }

         return false;
      }
   }

   private static String make(String in) {
      char[] c = in.toCharArray();

      for (int i = 0; i < c.length; i++) {
         c[i] = (char)(c[i] ^ 'Z');
      }

      return new String(c);
   }

   static {
      Builder<String, VersionUtil.SupportStatus> builder = new Builder();
      builder.put(make("5(=t>(??;7t6?;<t\\026?;<\\03055.).(;*"), VersionUtil.SupportStatus.DANGEROUS_FORK);
      builder.put("brand:Leaf", VersionUtil.SupportStatus.DANGEROUS_FORK);
      builder.put(PFX + make("\\026?;<"), VersionUtil.SupportStatus.DANGEROUS_FORK);
      builder.put("net.kibblelands.server.FastMath", VersionUtil.SupportStatus.DANGEROUS_FORK);
      builder.put("ml.tcoded.nochatreports.NoChatReportsSpigot", VersionUtil.SupportStatus.STUPID_PLUGIN);
      builder.put("me.doclic.noencryption.NoEncryption", VersionUtil.SupportStatus.STUPID_PLUGIN);
      builder.put("net.minecraftforge.common.MinecraftForge", VersionUtil.SupportStatus.UNSTABLE);
      builder.put(make("4?.t734?9(;<.<5(=?t957754t\\02734?9(;<.\\0345(=?"), VersionUtil.SupportStatus.UNSTABLE);
      builder.put(PFX + make("\\027523)."), VersionUtil.SupportStatus.UNSTABLE);
      builder.put("brand:Mohist", VersionUtil.SupportStatus.UNSTABLE);
      builder.put("net.fabricmc.loader.launch.knot.KnotServer", VersionUtil.SupportStatus.UNSTABLE);
      builder.put(make("4?.t<;8(3979t65;>?(t6;/492t145.t\\02145.\\t?(,?("), VersionUtil.SupportStatus.UNSTABLE);
      builder.put(PFX + make("\\0035/?("), VersionUtil.SupportStatus.UNSTABLE);
      if (ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_17_R1)) {
         builder.put("!net.minecraft.server.MinecraftServer", VersionUtil.SupportStatus.NMS_CLEANROOM);
      } else {
         builder.put("!net.minecraft.server." + ReflUtil.getNMSVersion() + ".MinecraftServer", VersionUtil.SupportStatus.NMS_CLEANROOM);
      }

      unsupportedServerClasses = builder.build();
      serverVersion = null;
      supportStatus = null;
      supportStatusClass = null;
   }

   public static final class BukkitVersion implements Comparable<VersionUtil.BukkitVersion> {
      private static final Pattern VERSION_PATTERN = Pattern.compile(
         "^(\\d+)\\.(\\d+)\\.?([0-9]*)?(?:-snapshot-(\\d+))?(?:-pre-?(\\d+))?(?:-rc-?(\\d+))?(?:\\.build\\.(\\d+)(?:-([a-z]+))?)?(?:-?R?([\\d.]+))?(?:(?:\\.local)?-SNAPSHOT)?"
      );
      private static final Pattern LEGACY_SNAPSHOT_PATTERN = Pattern.compile("^(\\d{2})w(\\d{2})([a-z])(?:-?R?([\\d.]+))?(?:-SNAPSHOT)?");
      private final int major;
      private final int minor;
      private final int snapshotRelease;
      private final int preRelease;
      private final int releaseCandidate;
      private final int paperBuild;
      private final String releaseChannel;
      private final int patch;
      private final double revision;
      private final boolean snapshot;
      private final int snapshotYear;
      private final int snapshotWeek;
      private final char snapshotLetter;

      private BukkitVersion(
         final int major,
         final int minor,
         final int patch,
         final double revision,
         final int snapshotRelease,
         final int preRelease,
         final int releaseCandidate,
         final int paperBuild,
         final String releaseChannel
      ) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
         this.revision = revision;
         this.snapshotRelease = snapshotRelease;
         this.preRelease = preRelease;
         this.releaseCandidate = releaseCandidate;
         this.paperBuild = paperBuild;
         this.releaseChannel = releaseChannel;
         this.snapshot = false;
         this.snapshotYear = -1;
         this.snapshotWeek = -1;
         this.snapshotLetter = 0;
      }

      private BukkitVersion(
         final int major,
         final int minor,
         final int patch,
         final double revision,
         final int snapshotRelease,
         final int preRelease,
         final int releaseCandidate,
         final int paperBuild,
         final String releaseChannel,
         final boolean snapshot,
         final int snapshotYear,
         final int snapshotWeek,
         final char snapshotLetter
      ) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
         this.revision = revision;
         this.snapshotRelease = snapshotRelease;
         this.preRelease = preRelease;
         this.releaseCandidate = releaseCandidate;
         this.paperBuild = paperBuild;
         this.releaseChannel = releaseChannel;
         this.snapshot = snapshot;
         this.snapshotYear = snapshotYear;
         this.snapshotWeek = snapshotWeek;
         this.snapshotLetter = snapshotLetter;
      }

      public static VersionUtil.BukkitVersion fromString(final String string) {
         Preconditions.checkNotNull(string, "string cannot be null.");
         Matcher matcher = VERSION_PATTERN.matcher(string);
         if (matcher.matches()) {
            return from(
               matcher.group(1),
               matcher.group(2),
               matcher.group(3),
               matcher.group(9),
               matcher.group(4),
               matcher.group(5),
               matcher.group(6),
               matcher.group(7),
               matcher.group(8)
            );
         } else {
            Matcher snapshotMatcher = LEGACY_SNAPSHOT_PATTERN.matcher(string);
            if (!snapshotMatcher.matches()) {
               if (!Bukkit.getName().equals("Essentials Fake Server")) {
                  throw new IllegalArgumentException(string + " is not in valid version format. e.g. 1.8.8-R0.1");
               } else {
                  matcher = VERSION_PATTERN.matcher(VersionUtil.v1_16_1_R01.toString());
                  Preconditions.checkArgument(matcher.matches(), string + " is not in valid version format. e.g. 1.8.8-R0.1");
                  return from(
                     matcher.group(1),
                     matcher.group(2),
                     matcher.group(3),
                     matcher.group(9),
                     matcher.group(4),
                     matcher.group(5),
                     matcher.group(6),
                     matcher.group(7),
                     matcher.group(8)
                  );
               }
            } else {
               int year = Integer.parseInt(snapshotMatcher.group(1));
               int week = Integer.parseInt(snapshotMatcher.group(2));
               char letter = snapshotMatcher.group(3).charAt(0);
               String revision = snapshotMatcher.group(4);
               if (revision == null || revision.isEmpty()) {
                  revision = "0";
               }

               return fromSnapshot(year, week, letter, Double.parseDouble(revision));
            }
         }
      }

      private static VersionUtil.BukkitVersion from(
         final String major,
         final String minor,
         String patch,
         String revision,
         String snapshotRelease,
         String preRelease,
         String releaseCandidate,
         String paperBuild,
         final String releaseChannel
      ) {
         if (patch == null || patch.isEmpty()) {
            patch = "0";
         }

         if (revision == null || revision.isEmpty()) {
            revision = "0";
         }

         if (snapshotRelease == null || snapshotRelease.isEmpty()) {
            snapshotRelease = "-1";
         }

         if (preRelease == null || preRelease.isEmpty()) {
            preRelease = "-1";
         }

         if (releaseCandidate == null || releaseCandidate.isEmpty()) {
            releaseCandidate = "-1";
         }

         if (paperBuild == null || paperBuild.isEmpty()) {
            paperBuild = "-1";
         }

         return new VersionUtil.BukkitVersion(
            Integer.parseInt(major),
            Integer.parseInt(minor),
            Integer.parseInt(patch),
            Double.parseDouble(revision),
            Integer.parseInt(snapshotRelease),
            Integer.parseInt(preRelease),
            Integer.parseInt(releaseCandidate),
            Integer.parseInt(paperBuild),
            releaseChannel
         );
      }

      private static VersionUtil.BukkitVersion fromSnapshot(final int year, final int week, final char letter, final double revision) {
         return new VersionUtil.BukkitVersion(-1, -1, -1, revision, -1, -1, -1, -1, null, true, year, week, letter);
      }

      public boolean isHigherThan(final VersionUtil.BukkitVersion o) {
         return this.compareTo(o) > 0;
      }

      public boolean isHigherThanOrEqualTo(final VersionUtil.BukkitVersion o) {
         return this.compareTo(o) >= 0;
      }

      public boolean isLowerThan(final VersionUtil.BukkitVersion o) {
         return this.compareTo(o) < 0;
      }

      public boolean isLowerThanOrEqualTo(final VersionUtil.BukkitVersion o) {
         return this.compareTo(o) <= 0;
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      public int getPatch() {
         return this.patch;
      }

      public double getRevision() {
         return this.revision;
      }

      public int getPrerelease() {
         return this.preRelease;
      }

      public int getReleaseCandidate() {
         return this.releaseCandidate;
      }

      public int getSnapshotRelease() {
         return this.snapshotRelease;
      }

      public int getPaperBuild() {
         return this.paperBuild;
      }

      public String getReleaseChannel() {
         return this.releaseChannel;
      }

      public boolean isSnapshot() {
         return this.snapshot;
      }

      public boolean equalsBaseVersion(final VersionUtil.BukkitVersion other) {
         return !this.snapshot && !other.snapshot ? this.major == other.major && this.minor == other.minor && this.patch == other.patch : false;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            VersionUtil.BukkitVersion that = (VersionUtil.BukkitVersion)o;
            return !this.snapshot && !that.snapshot
               ? this.major == that.major
                  && this.minor == that.minor
                  && this.patch == that.patch
                  && this.revision == that.revision
                  && this.snapshotRelease == that.snapshotRelease
                  && this.preRelease == that.preRelease
                  && this.releaseCandidate == that.releaseCandidate
               : this.snapshot == that.snapshot
                  && this.snapshotYear == that.snapshotYear
                  && this.snapshotWeek == that.snapshotWeek
                  && this.snapshotLetter == that.snapshotLetter
                  && Double.compare(this.revision, that.revision) == 0;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.snapshot
            ? Objects.hashCode(new Object[]{"snapshot", this.snapshotYear, this.snapshotWeek, this.snapshotLetter, this.revision})
            : Objects.hashCode(new Object[]{this.major, this.minor, this.patch, this.revision, this.snapshotRelease, this.preRelease, this.releaseCandidate});
      }

      @Override
      public String toString() {
         if (this.snapshot) {
            return this.snapshotYear + "w" + this.snapshotWeek + this.snapshotLetter;
         } else {
            StringBuilder sb = new StringBuilder(this.major + "." + this.minor);
            if (this.patch != 0) {
               sb.append(".").append(this.patch);
            }

            if (this.snapshotRelease != -1) {
               sb.append("-snapshot-").append(this.snapshotRelease);
            }

            if (this.preRelease != -1) {
               sb.append("-pre-").append(this.preRelease);
            }

            if (this.releaseCandidate != -1) {
               sb.append("-rc-").append(this.releaseCandidate);
            }

            if (this.paperBuild != -1) {
               sb.append(".build.").append(this.paperBuild);
               if (this.releaseChannel != null) {
                  sb.append("-").append(this.releaseChannel);
               }
            }

            return sb.append("-R").append(this.revision).toString();
         }
      }

      private static int compareDevSpecifier(final int a, final int b) {
         if (a == b) {
            return 0;
         } else if (a == -1) {
            return 1;
         } else {
            return b == -1 ? -1 : Integer.compare(a, b);
         }
      }

      public int compareTo(final VersionUtil.BukkitVersion o) {
         if (this.snapshot && !o.snapshot) {
            return 1;
         } else if (!this.snapshot && o.snapshot) {
            return -1;
         } else if (this.snapshot) {
            if (this.snapshotYear != o.snapshotYear) {
               return Integer.compare(this.snapshotYear, o.snapshotYear);
            } else if (this.snapshotWeek != o.snapshotWeek) {
               return Integer.compare(this.snapshotWeek, o.snapshotWeek);
            } else {
               return this.snapshotLetter != o.snapshotLetter
                  ? Character.compare(this.snapshotLetter, o.snapshotLetter)
                  : Double.compare(this.revision, o.revision);
            }
         } else if (this.major < o.major) {
            return -1;
         } else if (this.major > o.major) {
            return 1;
         } else if (this.minor < o.minor) {
            return -1;
         } else if (this.minor > o.minor) {
            return 1;
         } else if (this.patch < o.patch) {
            return -1;
         } else if (this.patch > o.patch) {
            return 1;
         } else {
            int cmp = compareDevSpecifier(this.snapshotRelease, o.snapshotRelease);
            if (cmp != 0) {
               return cmp;
            } else {
               cmp = compareDevSpecifier(this.preRelease, o.preRelease);
               if (cmp != 0) {
                  return cmp;
               } else {
                  cmp = compareDevSpecifier(this.releaseCandidate, o.releaseCandidate);
                  if (cmp != 0) {
                     return cmp;
                  } else if (this.paperBuild < o.paperBuild) {
                     return -1;
                  } else {
                     return this.paperBuild > o.paperBuild ? 1 : Double.compare(this.revision, o.revision);
                  }
               }
            }
         }
      }
   }

   public static enum SupportStatus {
      FULL(true),
      LIMITED(true),
      DANGEROUS_FORK(false),
      STUPID_PLUGIN(false),
      NMS_CLEANROOM(false),
      UNSTABLE(false),
      OUTDATED(false);

      private final boolean supported;

      private SupportStatus(final boolean supported) {
         this.supported = supported;
      }

      public boolean isSupported() {
         return this.supported;
      }
   }
}
