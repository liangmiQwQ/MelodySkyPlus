package net.mirolls.melodyskyplus.path.find;

import net.minecraft.util.BlockPos;

public class PathNode {
  public BlockPos pos;

  public BlockPos posParent;

  public double gCost;

  public double hCost;
  public PathNode nodeParent;
  public PathPos.PathNodeType type;

  public PathNode(double g, double h, PathNode parent, BlockPos pos, PathPos.PathNodeType type) {
    this.gCost = g;
    this.hCost = h;
    this.nodeParent = parent;
    if (parent != null) {
      this.posParent = parent.pos;
    }

    this.pos = pos;
    this.type = type;
  }

  public double fCost() {
    return gCost + hCost;
  }

  @Override
  public String toString() {
    return "PathNode{" +
        "pos=" + pos +
        ", posParent=" + posParent +
        ", gCost=" + gCost +
        ", hCost=" + hCost +
        ", nodeParent=" + nodeParent +
        ", type=" + type +
        '}';
  }
}

