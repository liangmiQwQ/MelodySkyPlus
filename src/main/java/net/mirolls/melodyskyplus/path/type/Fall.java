package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.math.Rotation;

public class Fall extends Node {
  public int fallDistance = 0;

  public Fall(BlockPos pos, Rotation nextRotation, int fallDistance) {
    super(pos, nextRotation);
    this.fallDistance = fallDistance;
  }

}
