package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.math.Rotation;

public class Walk extends Node {
  public Walk(BlockPos pos, Rotation nextRotation) {
    super(pos, nextRotation);
  }
}
