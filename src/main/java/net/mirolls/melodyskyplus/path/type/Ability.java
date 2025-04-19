package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.find.PathPos;
import xyz.Melody.Utils.math.Rotation;

import java.util.List;

public class Ability extends Node {
  public List<PathPos> nodesBetween;

  public Ability(BlockPos pos, Rotation nextRotation, double distance, List<PathPos> nodesBetween) {
    super(pos, nextRotation, distance);

    this.nodesBetween = nodesBetween;
  }
}
