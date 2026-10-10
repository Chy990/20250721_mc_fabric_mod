package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import name.modid.mixin.MinecraftClientInvoker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChyModClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("chy");
    private static final ChyState STATE = new ChyState();

    public static boolean isAutoSneaking() {
        return STATE.isSneaking();
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerClickCommand(dispatcher, "left_click", "左", STATE.leftClick);
            registerClickCommand(dispatcher, "right_click", "右", STATE.rightClick);
            dispatcher.register(ClientCommandManager.literal("shift")
                .executes(context -> {
                    STATE.toggleSneaking();
                    report(context.getSource(), STATE.isSneaking() ? "自动潜行已开启" : "自动潜行已关闭");
                    return 1;
                }));
            dispatcher.register(ClientCommandManager.literal("chy_help")
                .executes(context -> {
                    String[] lines = {
                        "=== chy 3.2.0 帮助 ===",
                        "/left_click - 开启/关闭自动左键；攻击准星指向的实体，无实体时挥手，不挖方块。",
                        "/right_click - 开启/关闭自动右键；手持方块时放置，也可触发原版右键交互。",
                        "/left_click <tick>、/right_click <tick> - 开启或调整间隔，参数为正整数 tick（最小 1）。",
                        "两个功能独立计时；每次无参数开启均恢复 12 tick。例如 /right_click 20：每 20 tick 右键一次。",
                        "开启提示：自动左/右键，间隔xx tick。再次输入相应无参数命令即可关闭。",
                        "/shift - 开启/关闭持续潜行，相当于一直按住潜行键，可配合右键放置方块。",
                        "打开聊天、背包或菜单不关闭功能；单人世界暂停时无法继续实际操作。退出世界后全部关闭。",
                        "/chy_help - 查看帮助。旧命令 /auto_attack 已改为 /left_click。"
                    };
                    for (String line : lines) sendFeedback(context.getSource(), line);
                    return 1;
                }));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STATE.reset());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                STATE.reset();
                return;
            }
            // Screens do not disable automation. The paused single-player world cannot act.
            if (client.isPaused() || !client.player.isAlive() || client.player.isSpectator()) {
                return;
            }
            boolean leftDue = STATE.leftClick.tick();
            boolean rightDue = STATE.rightClick.tick();
            if (leftDue || rightDue) client.gameRenderer.updateCrosshairTarget(1.0F);
            if (leftDue) {
                if (client.targetedEntity != null) {
                    client.interactionManager.attackEntity(client.player, client.targetedEntity);
                }
                client.player.swingHand(Hand.MAIN_HAND);
            }
            if (rightDue && !client.player.isUsingItem()) {
                // Use the vanilla one-click path for placement, offhand fallback and animations.
                ((MinecraftClientInvoker) client).chy$doItemUse();
            }
        });
        LOGGER.info("chy 3.2.0 已加载，使用 /left_click、/right_click、/shift 或 /chy_help。");
    }

    private static void registerClickCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                            String command, String button, ClickState state) {
        dispatcher.register(ClientCommandManager.literal(command)
            .executes(context -> {
                state.toggle();
                reportClick(context.getSource(), button, state);
                return 1;
            })
            .then(ClientCommandManager.argument("tick", IntegerArgumentType.integer(1))
                .executes(context -> {
                    state.start(IntegerArgumentType.getInteger(context, "tick"));
                    reportClick(context.getSource(), button, state);
                    return 1;
                })));
    }

    private static void reportClick(FabricClientCommandSource source, String button, ClickState state) {
        report(source, state.isEnabled()
            ? "自动" + button + "键，间隔" + state.intervalTicks() + " tick"
            : "自动" + button + "键已关闭");
    }

    private static void report(FabricClientCommandSource source, String message) {
        sendFeedback(source, message);
        LOGGER.info("{}", message);
    }

    private static void sendFeedback(FabricClientCommandSource source, String message) {
        source.sendFeedback(Text.literal(message).formatted(Formatting.RED));
    }
}
