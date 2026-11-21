package net.ess3.provider.providers;

import java.util.Locale;
import net.ess3.provider.BiomeNameProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.block.Block;

@ProviderData(
   description = "Legacy Biome Name Provider"
)
public class LegacyBiomeNameProvider implements BiomeNameProvider {
   @Override
   public String getBiomeName(final Block block) {
      return block.getBiome().name().toLowerCase(Locale.ENGLISH);
   }
}
