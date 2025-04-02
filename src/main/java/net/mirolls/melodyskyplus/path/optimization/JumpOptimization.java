package net.mirolls.melodyskyplus.path.optimization;

import net.mirolls.melodyskyplus.path.type.Jump;
import net.mirolls.melodyskyplus.path.type.Node;

import java.util.HashMap;
import java.util.List;

public class JumpOptimization {
  // 优化跳跃
  // 原理: 检查每一个跳跃节点

  public static final HashMap<Integer, Integer> JUMP_DISTANCE = new HashMap<>();

  static {
    JUMP_DISTANCE.put(1, 1);
  }

  public List<Node> optimize(List<Node> nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      if (node instanceof Jump && i > 1) {// 要至少前面有2个点
        // 是跳跃类型节点
        Node startNode = nodes.get(i - 1);
        Node prevNode = nodes.get(i - 1);
      }
    }


    return nodes;
  }
}
