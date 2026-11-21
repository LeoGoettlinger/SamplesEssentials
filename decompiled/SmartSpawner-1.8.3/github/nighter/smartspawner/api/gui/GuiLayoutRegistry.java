package github.nighter.smartspawner.api.gui;

import java.io.File;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public interface GuiLayoutRegistry {
   boolean registerLayout(String var1, @Nullable GuiLayoutData var2, @Nullable GuiLayoutData var3, @Nullable GuiLayoutData var4);

   boolean registerLayoutFromYaml(String var1, @Nullable File var2, @Nullable File var3, @Nullable File var4);

   boolean unregisterLayout(String var1);

   boolean isRegistered(String var1);

   Set<String> getRegisteredLayouts();
}
