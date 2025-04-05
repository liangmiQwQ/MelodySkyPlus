package net.mirolls.melodyskyplus.path.find;

import net.minecraft.util.BlockPos;
import xyz.Melody.Utils.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathPos {
  private PathNodeType type;
  private BlockPos pos;

  public PathPos(PathNodeType type, BlockPos pos) {
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
    return Vec3d.ofCenter(pos);
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }

  public PathNodeType getType() {
    return type;
  }

  public void setType(PathNodeType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "PathPos{" +
        "type=" + type +
        ", pos=" + pos +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PathPos)) return false;
    PathPos pathPos = (PathPos) o;
    return getType() == pathPos.getType() && Objects.equals(getPos(), pathPos.getPos());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getPos());
  }

  public enum PathNodeType {
    WALK,
    MINE,
    ABILITY,
    JUMP_END,
    ABILITY_START,
    ABILITY_END,
  }
}
