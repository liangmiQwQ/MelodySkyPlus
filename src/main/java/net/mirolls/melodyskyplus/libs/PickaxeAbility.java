package net.mirolls.melodyskyplus.libs;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class PickaxeAbility {
  public boolean check = false;
  private boolean pickaxeAbility;

  public PickaxeAbility() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onPickaxeAbility(ClientChatReceivedEvent event) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    String message = event.message.getUnformattedText();

    if (message.contains("You used your Mining Speed Boost Pickaxe Ability!")) {
      this.pickaxeAbility = true;
    } else if (message.contains("Your Mining Speed Boost has expired!")) {
      this.pickaxeAbility = false;
    }

    if (check) {
      if (Pattern.compile("^(.*?) is now available!$").matcher(message).matches()) {
        // ready
        Class<?> client = Class.forName("xyz.Melody.Client");
        Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
        pickaxeField.setAccessible(true);
        pickaxeField.set(client, true);
      } else if (Pattern.compile("^You used your (.*?) Pickaxe Ability!$").matcher(message).matches()) {
        // used
        Class<?> client = Class.forName("xyz.Melody.Client");
        Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
        Field pickaxeField2 = client.getDeclaredField("pickaxeAbility");
        pickaxeField.setAccessible(true);
        pickaxeField2.setAccessible(true);
        pickaxeField.set(client, false);
        pickaxeField2.set(client, true);
      } else if (Pattern.compile("^Your (.*?) has expired!$").matcher(message).matches()) {
        // expire
        Class<?> client = Class.forName("xyz.Melody.Client");
        Field pickaxeField = client.getDeclaredField("pickaxeAbilityReady");
        Field pickaxeField2 = client.getDeclaredField("pickaxeAbility");
        pickaxeField.setAccessible(true);
        pickaxeField2.setAccessible(true);
        pickaxeField.set(client, false);
        pickaxeField2.set(client, false);
      }
    }
  }

  public boolean isPickaxeAbility() {
    return pickaxeAbility;
  }
}
