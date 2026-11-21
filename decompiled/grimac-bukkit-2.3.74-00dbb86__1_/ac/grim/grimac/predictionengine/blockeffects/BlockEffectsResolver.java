package ac.grim.grimac.predictionengine.blockeffects;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.math.Vector3dm;
import java.util.List;

public interface BlockEffectsResolver {
   void applyEffectsFromBlocks(GrimPlayer var1, Vector3dm var2, boolean var3, List<GrimPlayer.Movement> var4);
}
