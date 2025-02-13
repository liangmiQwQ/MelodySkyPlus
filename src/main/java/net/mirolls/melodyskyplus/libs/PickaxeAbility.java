package net.mirolls.melodyskyplus.libs;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.Melody.module.modules.macros.Mining.MiningSkill;

public class PickaxeAbility {
  private boolean pickaxeAbility = false;

  public PickaxeAbility() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onPickaxeAbility(ClientChatReceivedEvent event) {
    String message = event.message.getUnformattedText();

    if (message.equalsIgnoreCase(MiningSkill.getINSTANCE().used.getValue())) {
      this.pickaxeAbility = true;
    } else if (message.equalsIgnoreCase(MiningSkill.getINSTANCE().expire.getValue())) {
      this.pickaxeAbility = false;
    }
  }

  public boolean isPickaxeAbility() {
    return pickaxeAbility;
  }
}
