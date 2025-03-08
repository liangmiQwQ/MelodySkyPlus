package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;

public class PathPos {
  private int type;
  private BlockPos pos;

  public PathPos(int type, BlockPos pos) {
    this.type = type;
    this.pos = pos;
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
