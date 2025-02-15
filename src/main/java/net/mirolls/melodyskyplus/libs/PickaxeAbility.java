package net.mirolls.melodyskyplus.libs;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PickaxeAbility {
  private boolean pickaxeAbility = false;

  public PickaxeAbility() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onPickaxeAbility(ClientChatReceivedEvent event) {
    String message = event.message.getUnformattedText();

    if (message.contains("You used your Mining Speed Boost Pickaxe Ability!")) {
      this.pickaxeAbility = true;
    } else if (message.contains("Your Mining Speed Boost has expired!")) {
      this.pickaxeAbility = false;
    }
  }

  public boolean isPickaxeAbility() {
    return pickaxeAbility;
  }
}
