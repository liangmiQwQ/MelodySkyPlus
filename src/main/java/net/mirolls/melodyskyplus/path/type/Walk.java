package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.math.Rotation;

public class Walk extends Node {
  public double advanceFraction;

  public Walk(BlockPos pos, Rotation nextRotation, double distance, double advanceFraction) {
    super(pos, nextRotation, distance);

    this.advanceFraction = advanceFraction;
  }
}
