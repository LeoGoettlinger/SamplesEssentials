package ac.grim.grimac.utils.data;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import java.util.List;

public record PistonTemplate(BlockFace dir, List<SimpleCollisionBox> boxes, boolean push, boolean slime, boolean honey) {
}
