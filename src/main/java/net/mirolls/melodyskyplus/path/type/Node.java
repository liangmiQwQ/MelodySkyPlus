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
  public double distance;

  public Node(BlockPos pos, Rotation nextRotation, double distance) {
    this.pos = pos;
    this.nextRotation = nextRotation;
    this.distance = distance;
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

      Rotation rotation = null;
      double distance = -1;
      if (i != path.size() - 1) {
        PathPos nextPos = path.get(i + 1);
        rotation = Node.calculateAngles(pos.getPos(), nextPos.getPos());
        distance = Math.hypot(nextPos.getPos().getX() - pos.getPos().getX(), nextPos.getPos().getZ() - pos.getPos().getZ());
      }


      if (pos.getType() == PathPos.PathNodeType.WALK) {
        values.add(new Walk(pos.getPos(), rotation, distance));
      } else if (pos.getType() == PathPos.PathNodeType.JUMP_END) {
        // 对于技能类型节点 (包括Jump, Fall, Ability) 需要修改A*返回的End模式变为start模式 下面是一个例子

        // 读取上一个节点并且删除
        Node prevNode = values.get(values.size() - 1);
        values.remove(values.size() - 1);

        // 修改上一个节点为跳跃节点 并且添加end节点
        values.add(new Jump(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance, 1));
        values.add(new Walk(pos.getPos(), rotation, distance));
      } else if (pos.getType() == PathPos.PathNodeType.ABILITY_END) {
        // 这里再细分一下到底是fall还是JumpEnd
        PathPos prevPos = path.get(i - 1);
        if (prevPos != null && prevPos.getPos().getY() > pos.getPos().getY()) { // 上一个的高度要比这个高
          if (prevPos.getPos().getX() == pos.getPos().getX()) {
            // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
            if (Math.abs(prevPos.getPos().getZ() - pos.getPos().getZ()) == 1) {
              // 读取上一个节点并且删除
              Node prevNode = values.get(values.size() - 1);
              values.remove(values.size() - 1);

              // 修改上一个节点为摔落节点 并且添加end节点
              values.add(new Fall(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance, 1));
              values.add(new Walk(pos.getPos(), rotation, distance));
              continue;
            }
          } else if (prevPos.getPos().getZ() == pos.getPos().getZ()) {
            // X 或者 Z 中需要至少有一个相同的 不相同的那个应当差1
            if (Math.abs(prevPos.getPos().getX() - pos.getPos().getX()) == 1) {
              // 读取上一个节点并且删除
              Node prevNode = values.get(values.size() - 1);
              values.remove(values.size() - 1);

              // 修改上一个节点为摔落节点 并且添加end节点
              values.add(new Fall(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance, 0));
              values.add(new Walk(pos.getPos(), rotation, distance));
              continue;
            }
          }
        }

        // 读取上一个节点并且删除
        Node prevNode = values.get(values.size() - 1);
        values.remove(values.size() - 1);

        // 修改上一个节点为摔落节点 并且添加end节点
        values.add(new Ability(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance));
        values.add(new Walk(pos.getPos(), rotation, distance));
      } else if (pos.getType() == PathPos.PathNodeType.MINE) {
        // mine已经是前一个了 不需要进行额外处理
        values.add(new Mine(pos.getPos(), rotation, distance));
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

    // 使用反正切函数进行计算 x/z(对边/邻边)
    double yaw = getYaw(dx, dz);


    // 再次使用反正切函数进行计算  y/直线距离(对边/邻边)
    double rawPitch = Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(Math.hypot(dx, dz))));

    // rawPitch处理 将它从角度规范化
    double pitch = 0;

    if (dy > 0) {
      // 抬头
      pitch = -rawPitch;
    } else if (dy < 0) {
      // 低头
      pitch = rawPitch;
    } else {
      // 平视
      pitch = 0;
    }

    return new Rotation((float) yaw, (float) pitch);
  }

  private static double getYaw(double dx, double dz) {
    double rawYaw = Math.toDegrees(Math.atan2(Math.abs(dx), Math.abs(dz)));

    // 确定象限 找到真raw
    double yaw = 0;
    // rawYaw处理 将它从角度规范化
    if (dx < 0) {
      // 在开始点的西边
      if (dz < 0) {
        // 在开始点的西北侧
        yaw = 180 - rawYaw;
      } else if (dz > 0) {
        // 在开始点的西南侧
        yaw = 0 + rawYaw;
      } else {
        // 在开始点的正西
        yaw = 90;
      }
    } else if (dx > 0) {
      // 在开始点的东边
      if (dz < 0) {
        // 在开始点的东北侧
        yaw = -(180 - rawYaw);
      } else if (dz > 0) {
        // 在开始点的东南侧
        yaw = -(0 + rawYaw);
      } else {
        // 在开始点的正东
        yaw = -90;
      }
    } else {
      // 在正北或者正南
      if (dz < 0) {
        // 在开始点的正北
        yaw = 180;
      } else if (dz > 0) {
        // 在开始点的正南
        yaw = -180;
      }
    }
    return yaw;
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
