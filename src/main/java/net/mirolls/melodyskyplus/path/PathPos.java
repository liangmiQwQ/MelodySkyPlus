package net.mirolls.melodyskyplus.path;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PathPos {
  private int type;
  private BlockPos pos;

  public PathPos(int type, BlockPos pos) {
    this.type = type;
    this.pos = pos;
  }

  public static List<Vec3d> toVec3dArray(List<PathPos> pathPoses) {
    List<Vec3d> returnValue = new ArrayList<>();
    for (PathPos pos : pathPoses) {
      returnValue.add(pos.toVec3d());
    }
    return returnValue;
  }

  public Vec3d toVec3d() {
    return new Vec3d(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
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
