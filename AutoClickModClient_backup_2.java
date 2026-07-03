package name.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

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
                if (currentTime - lastClickTime >= 1000) { // 1秒间隔
                    // 安全执行攻击逻辑
                    KeyBinding attackKey = client.options.attackKey;
                    
                    // 模拟按下攻击键
                    attackKey.setPressed(true);
                    
                    // 播放攻击动画
                    client.player.swingHand(Hand.MAIN_HAND);
                    
                    // 如果有目标实体才执行攻击
                    if (client.targetedEntity != null) {
                        client.interactionManager.attackEntity(client.player, client.targetedEntity);
                    }
                    
                    // 释放攻击键
                    attackKey.setPressed(false);
                    lastClickTime = currentTime;
                }
            }
        });
    }
}