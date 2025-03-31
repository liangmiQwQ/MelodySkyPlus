package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.math.Rotation;

public class Jump extends Node {
  public int jumpDistance = 0;

  public Jump(BlockPos pos, Rotation nextRotation, int jumpDistance) {
    super(pos, nextRotation);
    this.jumpDistance = jumpDistance;
  }
}
