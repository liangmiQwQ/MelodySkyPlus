package net.mirolls.melodyskyplus.mixin;

import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.client.cmd.AutoHollowCommand;
import net.mirolls.melodyskyplus.client.cmd.RayTraceCommand;
import net.mirolls.melodyskyplus.client.cmd.SmartyPathFinderCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.System.Commands.Command;
import xyz.Melody.System.Managers.Client.CommandManager;

import java.util.List;

@Mixin(value = CommandManager.class, remap = false)
public class CommandManagerMixin {
  @Shadow
  private List<Command> commands;

  @Inject(remap = false, method = "init",
      at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1, remap = false)
  )
  public void init(CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      this.commands.add(new SmartyPathFinderCommand());
      this.commands.add(new AutoHollowCommand());
      this.commands.add(new RayTraceCommand());
    }
  }
}
