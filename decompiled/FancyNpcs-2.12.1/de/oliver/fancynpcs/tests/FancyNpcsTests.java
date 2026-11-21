package de.oliver.fancynpcs.tests;

import de.oliver.fancynpcs.tests.api.ChatColorHandlerTest;
import de.oliver.fancynpcs.tests.api.CreateNpcTest;
import de.oliver.fancynpcs.tests.commands.ActionCMDTest;
import de.oliver.fancynpcs.tests.commands.CreateCMDTest;
import de.oliver.fancynpcs.tests.commands.DisplayNameCMDTest;
import de.oliver.fancynpcs.tests.commands.TurnToPlayerCMDTest;
import de.oliver.fancynpcs.tests.commands.TypeCMDTest;
import de.oliver.fancynpcs.tests.skins.SkinManagerTest;
import de.oliver.plugintests.FPTestClass;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class FancyNpcsTests {
   private final List<FPTestClass> tests = new ArrayList<>();

   public FancyNpcsTests() {
      this.addTest(CreateNpcTest.class);
      this.addTest(ChatColorHandlerTest.class);
      this.addTest(SkinManagerTest.class);
      this.addTest(CreateCMDTest.class);
      this.addTest(TypeCMDTest.class);
      this.addTest(DisplayNameCMDTest.class);
      this.addTest(TurnToPlayerCMDTest.class);
      this.addTest(ActionCMDTest.class);
   }

   public FancyNpcsTests addTest(Class<?> testClass) {
      this.tests.add(FPTestClass.fromClass(testClass));
      return this;
   }

   public boolean runAllTests(Player player) {
      for (FPTestClass test : this.tests) {
         try {
            if (!test.runTests(player)) {
               return false;
            }
         } catch (Exception var5) {
            return false;
         }
      }

      return true;
   }

   public int getTestCount() {
      int count = 0;

      for (FPTestClass test : this.tests) {
         count += test.testMethods().size();
      }

      return count;
   }
}
