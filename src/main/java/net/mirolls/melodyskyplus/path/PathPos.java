package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;

public class PathPos {
  private boolean toMine;
  private BlockPos pos;

  public PathPos(boolean toMine, BlockPos pos) {
    this.toMine = toMine;
    this.pos = pos;
  }

  public boolean isToMine() {
    return toMine;
  }

  public void setToMine(boolean toMine) {
    this.toMine = toMine;
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }
}
