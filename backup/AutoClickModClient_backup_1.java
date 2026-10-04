package name.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class AutoClickModClient implements ClientModInitializer {

    private static boolean autoClickEnabled = false;
    private static long lastClickTime = 0;

    @Override
    public void onInitializeClient() {
        // 注册客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("autoclick")
                    .executes(context -> {
                        autoClickEnabled = !autoClickEnabled;
                        context.getSource().sendFeedback(Text.of("自动点击已 " + (autoClickEnabled ? "开启" : "关闭")));
                        return 1;
                    })
            );
        });

        // 自动点击逻辑
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (autoClickEnabled && client.player != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime >= 1000) {
                    MinecraftClient.getInstance().options.attackKey.setPressed(true);
                    MinecraftClient.getInstance().options.attackKey.setPressed(false);
                    lastClickTime = currentTime;
                }
            }
        });
    }
}
