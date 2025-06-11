package net.mirolls.melodyskyplus.path.type;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.client.task.TaskHelper;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    TaskHelper taskHelper = new TaskHelper();

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
        if (Verify.isVerified()) {
          Walk node = new Walk(pos.getPos(), rotation, distance, -1);

          values.add(node);
          if (values.size() > 1) {
            Node prevNode = values.get(values.size() - 2);

            taskHelper.addTask(() -> {
              if (values.contains(node) && node.advanceFraction == -1) {
                try {
                  URL url = new URL(AntiBug.ROOT_URL + "hack/path/?bug=" + MelodySkyPlus.antiBug.getBug()
                      + "&speed=1&max=75&angle="
                      + Math.abs(PlayerUtils.getYawDiff(prevNode.nextRotation.getYaw(), node.nextRotation.getYaw()))
                  );

                  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                  conn.setRequestMethod("GET");
                  conn.setRequestProperty("Content-Type", "application/json");
                  conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                  conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                  conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                  conn.setRequestProperty("Connection", "keep-alive");

                  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                  String inputLine;
                  StringBuilder response = new StringBuilder();

                  while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                  }

                  in.close();

                  node.advanceFraction = Double.parseDouble(response.toString());
                } catch (IOException e) {
                  MelodySkyPlus.LOGGER.info(e);
                  throw new RuntimeException(e);
                }
              }
            });
          }

        }
      } else if (pos.getType() == PathPos.PathNodeType.JUMP_END) {
        // 对于技能类型节点 (包括Jump, Fall, Ability) 需要修改A*返回的End模式变为start模式 下面是一个例子

        // 读取上一个节点并且删除
        Node prevNode = values.get(values.size() - 1);
        values.remove(values.size() - 1);

        // 修改上一个节点为跳跃节点 并且添加end节点
        values.add(new Jump(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance, 1));
        values.add(new Walk(pos.getPos(), rotation, distance, 0));
      } else if (pos.getType() == PathPos.PathNodeType.ABILITY_BETWEEN) {
        // 意味着上一个点是Ability开始点 需要读取上一个节点并且删除
        Node prevNode = values.get(values.size() - 1);
        values.remove(values.size() - 1);

        // 读取其后的AbilityBetween节点
        List<PathPos> nodesBetween = new ArrayList<>();

        for (int j = i; j < path.size(); j++) {
          // 循环 通过该循环获取到后续节点
          PathPos nodeBetween = path.get(j);
          if (nodeBetween.getType() == PathPos.PathNodeType.ABILITY_BETWEEN) {
            nodesBetween.add(nodeBetween);
          } else {
            // 相当于直接走过了这些路程 i也不用重新跑了
            // i = j - 1 因为这个点是结束点 要走一遍下一个循环里点ABILITY_END 更不可能是 j 或者 j - 1
            i = j - 1;
            break;
          }
        }

        // 把上一个点补回去
        values.add(new Ability(prevNode.getPos(), prevNode.getNextRotation(), prevNode.distance, nodesBetween));
      } else if (pos.getType() == PathPos.PathNodeType.ABILITY_END) {
        values.add(new Walk(pos.getPos(), rotation, distance, 0));
      } else if (pos.getType() == PathPos.PathNodeType.MINE) {
        // mine已经是前一个了 不需要进行额外处理
        values.add(new Mine(pos.getPos(), rotation, distance));
      }
    }

    taskHelper.run();
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
    double pitch;

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
        yaw = 0;
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
