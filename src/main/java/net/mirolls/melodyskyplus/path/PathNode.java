package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;

public class PathNode {
  public BlockPos pos;

  public BlockPos posParent;

  public double movement;

  public Boolean isCustomNode;

  public double gCost;

  public double hCost;
  public PathNode nodeParent;

  public PathNode(double g, double h, PathNode parent, BlockPos pos, Double cost, Boolean custom) {
    this.gCost = g;
    this.hCost = h;
    this.nodeParent = parent;
    this.posParent = parent.pos;

    this.pos = pos;
    this.movement = cost;
    this.isCustomNode = custom;
  }

  public double fCost() {
    return gCost + hCost;
  }
}
