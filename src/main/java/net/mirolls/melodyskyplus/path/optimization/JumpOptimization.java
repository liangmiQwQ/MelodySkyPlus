package net.mirolls.melodyskyplus.path.optimization;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.type.Jump;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Utils.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JumpOptimization {
  // 优化跳跃
  // 原理: 检查每一个跳跃节点

  public static final HashMap<Integer, Integer> JUMP_DISTANCE = new HashMap<>();

  static {
    JUMP_DISTANCE.put(1, 4);
    JUMP_DISTANCE.put(2, 4);
    JUMP_DISTANCE.put(3, 4);
    JUMP_DISTANCE.put(4, 3);
    JUMP_DISTANCE.put(5, 3);
    JUMP_DISTANCE.put(6, 2);

  }

  public ArrayList<Vec3d> routeVec = new ArrayList<>();

  public List<Node> optimize(List<Node> nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      if (node instanceof Jump) {
        // 是跳跃类型节点
        Node endNode = nodes.get(i + 1);
        Node prevNode = nodes.get(i - 1);

        int height = endNode.getPos().getY() - node.getPos().getY();

        if (Math.hypot(endNode.getPos().getX() - endNode.getPos().getX(), endNode.getPos().getZ() - endNode.getPos().getZ()) > JUMP_DISTANCE.get(height) + 1) {
          // 首先要基本先大于
          if (isInAir(node.getPos(), prevNode.getPos(), height)) {
            ((Jump) node).jumpDistance = JUMP_DISTANCE.get(height);

            nodes.set(i, node);
          }
        }
      }
    }

    return nodes;
  }

  private boolean isInAir(BlockPos start, BlockPos end, int height) {
    List<BlockPos> posList = calculate(start, end, JUMP_DISTANCE.get(height));
    for (BlockPos pos : posList) {
      for (int i = 0; i < height + 2; i++) {
        BlockPos posChecking = pos.add(0, height + 1, 0);
        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(posChecking);
        if (blockState.getBlock() != Blocks.air) {
          return false;
        }
      }
    }

    return true;
  }

  private ArrayList<BlockPos> calculate(BlockPos start, BlockPos end, int distance) {
    routeVec = updateVec(start, end, distance);
    if (!routeVec.isEmpty()) {

      int minX = Math.min(start.getX(), end.getX());
      int maxX = Math.max(start.getX(), end.getX());
      int minZ = Math.min(start.getZ(), end.getZ());
      int maxZ = Math.max(start.getZ(), end.getZ());
      ArrayList<Vec3d> blocks = new ArrayList<>();

      for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
          BlockPos blockPos = new BlockPos(x, start.getY(), z);
          blocks.add(Vec3d.ofCenter(blockPos));
        }
      }

      blocks.removeIf(this::checkDistance);

      ArrayList<BlockPos> poses = new ArrayList<>();

      for (Vec3d v3d : blocks) {
        poses.add(new BlockPos(v3d.getX(), v3d.getY(), v3d.getZ()));
      }

      return poses.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    return new ArrayList<>();
  }

  private ArrayList<Vec3d> updateVec(BlockPos start, BlockPos end, int distance) {
    ArrayList<Vec3d> vecPoses = new ArrayList<>();

    double deltaX = end.getX() - start.getX();
    // double deltaY = end.getY() - start.getY();
    double deltaZ = end.getZ() - start.getZ();
    double maxDist = Math.max(Math.abs(deltaX), /*Math.abs(deltaY),*/ Math.abs(deltaZ)) * 25.0;
    double incX = deltaX / maxDist;
    // double incY = deltaY / maxDist;
    double incZ = deltaZ / maxDist;
    Vec3d startVec = Vec3d.ofCenter(start);
    double xPos = startVec.getX();
    // double yPos = startVec.getY();
    double zPos = startVec.getZ();


    for (int huh = 1; (double) huh <= maxDist; ++huh) {
      xPos += incX;
      // yPos += incY;
      zPos += incZ;

      Vec3d addingVec = new Vec3d(xPos, /* yPos */ start.getY(), zPos);

      if (Math.hypot(xPos - startVec.getX(), zPos - startVec.getZ()) < distance) {
        vecPoses.add(addingVec);
      } else {
        break;
      }
    }

    return vecPoses;
  }

  private boolean checkDistance(Vec3d vec) {
    Iterator<Vec3d> var2 = routeVec.iterator();

    boolean x;
    // boolean y;
    boolean z;
    do {
      if (!var2.hasNext()) {
        return true;
      }

      Vec3d v = var2.next();
      x = Math.abs(v.getX() - vec.getX()) <= 2.0;
      // y = Math.abs(v.getY() - vec.getY()) <= 1.0;
      z = Math.abs(v.getZ() - vec.getZ()) <= 2.0;
    } while (!x || /*!y ||*/ !z);

    return false;
  }
}
