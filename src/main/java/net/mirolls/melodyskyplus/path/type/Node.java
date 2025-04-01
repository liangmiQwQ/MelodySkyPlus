package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.find.PathPos;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;

import java.util.ArrayList;
import java.util.List;

public class Node {
  public BlockPos pos;
  public Rotation nextRotation;

  public Node(BlockPos pos, Rotation nextRotation) {
    this.pos = pos;
    this.nextRotation = nextRotation;
  }

  public static List<Vec3d> toVec3dArray(List<Node> pathPoses) {

    List<Vec3d> returnValue = new ArrayList<>();
    for (Node node : pathPoses) {
      returnValue.add(node.toVec3d());
    }
    return returnValue;
  }

  public static List<Node> fromPathPosList(List<PathPos> path) {
    List<Node> values = new ArrayList<>();

    for (int i = 0; i < path.size(); i++) {
      PathPos pos = path.get(i);
      PathPos nextPos = path.get(i + 1);

      Rotation rotation = Node.calculateAngles(pos.getPos(), nextPos.getPos());

      if (pos.getType() == PathPos.PathNodeType.WALK) {
        values.add(new Walk(pos.getPos(), rotation));
      } else if (pos.getType() == PathPos.PathNodeType.JUMP_END) {
        values.add(new Jump(pos.getPos(), rotation, -1));
      } else if (pos.getType() == PathPos.PathNodeType.ABILITY_END) {
        // 这里再细分一下到底是fall还是JumpEnd
        PathPos prevPos = path.get(i - 1);
        if (prevPos != null && prevPos.getPos().getY() > pos.getPos().getY()) { // 上一个的高度要比这个高
          if (prevPos.getPos().getX() == pos.getPos().getX()) {
            // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
            if (Math.abs(prevPos.getPos().getZ() - pos.getPos().getZ()) == 1) {
              values.add(new Fall(pos.getPos(), rotation, 0));
              continue;
            }
          } else if (prevPos.getPos().getZ() == pos.getPos().getZ()) {
            // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
            if (Math.abs(prevPos.getPos().getX() - pos.getPos().getX()) == 1) {
              values.add(new Fall(pos.getPos(), rotation, 0));
              continue;
            }
          }
        }

        values.add(new Ability(pos.getPos(), rotation));
      } else if (pos.getType() == PathPos.PathNodeType.MINE) {
        values.add(new Mine(pos.getPos(), rotation));
      }
    }

    return values;
  }

  public static Rotation calculateAngles(BlockPos start, BlockPos end) {
    if (start == null || end == null) return null;

    Vec3d startVec = Vec3d.ofCenter(start);
    Vec3d endVec = Vec3d.ofCenter(end);

    double dx = endVec.getX() - startVec.getX();
    double dy = endVec.getY() - startVec.getY();
    double dz = endVec.getZ() - startVec.getZ();

    // 计算Yaw
    double yaw = Math.toDegrees(Math.atan2(dx, dz));
    yaw = (yaw + 360) % 360; // 转换为0-360范围
    if (yaw > 180) yaw -= 360; // 规范到-180到180

    // 计算Pitch
    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
    double pitch;
    if (horizontalDistance == 0) {
      pitch = (dy > 0) ? -90 : (dy < 0) ? 90 : 0; // 处理垂直情况
    } else {
      pitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));
    }

    return new Rotation((float) yaw, (float) pitch);

  }

  public Vec3d toVec3d() {
    return Vec3d.ofCenter(pos);
  }

  public Rotation getNextRotation() {
    return nextRotation;
  }

  public void setNextRotation(Rotation nextRotation) {
    this.nextRotation = nextRotation;
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }
}
