package net.mirolls.melodyskyplus.libs;

import net.minecraftforge.common.MinecraftForge;
import xyz.Melody.Event.EventBus;

public class MiningSkillExecutor {
  public MiningSkillExecutor() {
    MinecraftForge.EVENT_BUS.register(this);
    EventBus.getInstance().register(this);
  }


}
