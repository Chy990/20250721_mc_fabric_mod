package name.modid.mixin;

import name.modid.ChyModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chy$applySneaking(CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (ChyModClient.isAutoSneaking() && player != null && player.isAlive() && !player.isSpectator()) {
            // Add sneak to fresh vanilla input, leaving physical key states and other controls intact.
            // Vanilla then applies movement, edge protection and sends the input to the server.
            PlayerInput input = playerInput;
            playerInput = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(),
                input.jump(), true, input.sprint());
        }
    }
}
