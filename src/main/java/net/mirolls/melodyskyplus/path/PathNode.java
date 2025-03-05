package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;

public class PathNode {
  public BlockPos pos;

  public BlockPos posParent;

  public double gCost;

  public double hCost;
  public PathNode nodeParent;

  public PathNode(double g, double h, PathNode parent, BlockPos pos) {
    this.gCost = g;
    this.hCost = h;
    this.nodeParent = parent;
    if (parent != null) {
      this.posParent = parent.pos;
    }

    this.pos = pos;
  }

  public double fCost() {
    return gCost + hCost;
  }
}
