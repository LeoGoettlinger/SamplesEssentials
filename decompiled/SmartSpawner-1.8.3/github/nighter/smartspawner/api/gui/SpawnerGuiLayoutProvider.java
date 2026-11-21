package github.nighter.smartspawner.api.gui;

import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import org.bukkit.entity.Player;

public interface SpawnerGuiLayoutProvider {
   GuiLayoutData getLayout(SpawnerDataDTO var1, Player var2, GuiLayoutType var3);

   String getProviderName();
}
