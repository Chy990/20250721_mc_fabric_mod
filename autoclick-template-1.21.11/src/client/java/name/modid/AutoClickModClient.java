package name.modid;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoClickModClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("autoclick");
    private final AutoAttackState attack = new AutoAttackState();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("auto_attack")
                .executes(context -> {
                    attack.toggle();
                    reportState(context.getSource());
                    return 1;
                })
                .then(ClientCommandManager.argument("interval", DoubleArgumentType.doubleArg(0.01, 10.0))
                    .executes(context -> {
                        attack.start(DoubleArgumentType.getDouble(context, "interval"));
                        reportState(context.getSource());
                        return 1;
                    })));
            dispatcher.register(ClientCommandManager.literal("chy_help")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("=== 自动攻击帮助 ==="));
                    context.getSource().sendFeedback(Text.literal(
                        "/auto_attack - 开启/关闭自动攻击；每次无参数开启恢复 12 tick（0.6 秒）。"));
                    context.getSource().sendFeedback(Text.literal(
                        "/auto_attack <秒数> - 开启或调整间隔（0.01–10 秒），向上取整到整 tick，最短 1 tick。"));
                    context.getSource().sendFeedback(Text.literal(
                        "例如 /auto_attack 1：每 20 tick（1 秒）攻击一次。打开聊天或菜单不关闭自动攻击，退出世界后关闭。"));
                    context.getSource().sendFeedback(Text.literal("/chy_help - 查看帮助。"));
                    return 1;
                }));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> attack.stop());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                attack.stop();
                return;
            }
            // UI screens do not suspend auto attack; keep counting client ticks.
            if (!client.player.isAlive() || client.player.isSpectator()) {
                return;
            }
            if (attack.tick()) {
                if (client.targetedEntity != null) {
                    client.interactionManager.attackEntity(client.player, client.targetedEntity);
                }
                client.player.swingHand(Hand.MAIN_HAND);
            }
        });
        LOGGER.info("AutoClick 已加载，使用 /auto_attack 或 /chy_help。");
    }

    private void reportState(FabricClientCommandSource source) {
        String message = attack.isEnabled()
            ? "自动攻击已开启。自动攻击间隔（" + attack.intervalTicks() + " tick, "
                + attack.intervalSeconds() + " 秒）"
            : "自动攻击已关闭";
        source.sendFeedback(Text.literal(message));
        LOGGER.info("{}", message);
    }
}
