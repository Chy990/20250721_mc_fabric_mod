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
    private static boolean wasAttackKeyPressed = false;

    @Override
    public void onInitializeClient() {
        // 注册客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("chy_autoattack")
                    .executes(context -> {
                        autoClickEnabled = !autoClickEnabled;
                        context.getSource().sendFeedback(Text.of("自动点击已 " + (autoClickEnabled ? "开启" : "关闭")));
                        return 1;
                    })
            );
        });


        // 自动点击逻辑
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (autoClickEnabled && client.player != null && client.interactionManager != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime >= 1000) { // 1秒间隔
                    KeyBinding attackKey = client.options.attackKey;
                    
                    // 保存原始按键状态
                    boolean originalState = attackKey.isPressed();
                    
                    // 模拟按下攻击键
                    attackKey.setPressed(true);
                    
                    // 处理攻击逻辑 - 只在有目标实体时攻击
                    if (client.targetedEntity != null) {
                        client.interactionManager.attackEntity(client.player, client.targetedEntity);
                    }
                    
                    // 播放攻击动画
                    client.player.swingHand(Hand.MAIN_HAND);
                    
                    // 恢复原始按键状态
                    attackKey.setPressed(originalState);
                    
                    lastClickTime = currentTime;
                }
            }
        });
    }
}