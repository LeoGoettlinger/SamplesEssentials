package com.fastasyncworldedit.serverlib;

import com.fastasyncworldedit.serverlib.forks.Akarin;
import com.fastasyncworldedit.serverlib.forks.KibblePatcher;
import com.fastasyncworldedit.serverlib.forks.Sugarcane;
import com.fastasyncworldedit.serverlib.forks.Yatopia;
import com.fastasyncworldedit.serverlib.hybrids.Fabric;
import com.fastasyncworldedit.serverlib.hybrids.Forge;
import com.fastasyncworldedit.serverlib.util.JavaVersionCheck;
import java.util.ArrayList;
import java.util.Collection;

public final class ServerLib {
   private final Collection<Check> checks;

   private ServerLib(Collection<Check> checks) {
      this.checks = checks;
   }

   public static ServerLib.Builder builder() {
      return new ServerLib.Builder();
   }

   public static void checkUnsafeForks() {
      ServerLib serverLib = builder()
         .withCheck(new Akarin())
         .withCheck(new KibblePatcher())
         .withCheck(new Yatopia())
         .withCheck(new Sugarcane())
         .withCheck(new Fabric())
         .withCheck(new Forge())
         .build();
      serverLib.runChecks();
   }

   @Deprecated
   public static void checkJavaLTS() {
      JavaVersionCheck.checkJavaSixteen();
   }

   public static void isJavaSixteen() {
      JavaVersionCheck.checkJavaSixteen();
   }

   @Deprecated
   public static void checkJavaMinor() {
      JavaVersionCheck.checkJavaFifteen();
   }

   public static void isJavaFifteen() {
      JavaVersionCheck.checkJavaFifteen();
   }

   public void runChecks() {
      for (Check check : this.checks) {
         if (check.shouldFlag()) {
            check.printExplanation();
         }
      }
   }

   public static final class Builder {
      private final Collection<Check> checks = new ArrayList<>();

      private Builder(Collection<Check> checks) {
         this.checks.addAll(checks);
      }

      private Builder() {
      }

      public ServerLib.Builder withCheck(Check check) {
         Collection<Check> checks = new ArrayList<>(this.checks);
         checks.add(check);
         return new ServerLib.Builder(checks);
      }

      public ServerLib build() {
         return new ServerLib(this.checks);
      }
   }
}
