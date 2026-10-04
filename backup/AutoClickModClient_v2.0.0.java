package name.modid;
import org.json.JSONObject;
import org.json.JSONArray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import com.mojang.brigadier.arguments.FloatArgumentType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AutoClickModClient implements ClientModInitializer {
    private static boolean autoClickEnabled = false;
    private static long lastClickTime = 0;
    private static double clickInterval = 0.6;
    private static boolean deepSeekChatMode = false;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"; // 替换为你的API地址
    private static final String API_KEY = "sk-c00f9091f0e94270ad833085d71dc4ce"; // 替换为你的API密钥

    @Override
    public void onInitializeClient() {
        // 注册自动点击命令
        registerAutoClickCommand();
        
        // 注册DeepSeek聊天命令
        registerDeepSeekCommand();
        
        // 自动点击逻辑
        registerAutoClickLogic();
        
        // DeepSeek聊天消息处理
        registerMessageHandler();
    }

    private void registerAutoClickCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("chy_autoattack")
                    .executes(context -> {
                        autoClickEnabled = !autoClickEnabled;
                        context.getSource().sendFeedback(Text.of("自动点击已 " + (autoClickEnabled ? "开启" : "关闭") + " (间隔: " + clickInterval + "秒)"));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("interval", FloatArgumentType.floatArg(0.01f, 10.0f))
                    .executes(context -> {
                        float newInterval = FloatArgumentType.getFloat(context, "interval");
                        clickInterval = newInterval;
                        autoClickEnabled = true;
                        context.getSource().sendFeedback(Text.of("自动点击已开启 (间隔: " + clickInterval + "秒)"));
                        return 1;
                    }))
            );
        });
    }

    private void registerDeepSeekCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("chy_deepseek")
                    .executes(context -> {
                        deepSeekChatMode = !deepSeekChatMode;
                        if (deepSeekChatMode) {
                            context.getSource().sendFeedback(Text.of("DeepSeek聊天模式已开启，请输入你的问题"));
                        } else {
                            context.getSource().sendFeedback(Text.of("DeepSeek聊天模式已关闭"));
                        }
                        return 1;
                    })
            );
        });
    }

    private void registerAutoClickLogic() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (autoClickEnabled && client.player != null && client.interactionManager != null) {
                long currentTime = System.currentTimeMillis();
                long intervalMillis = (long)(clickInterval * 1000);
                
                if (currentTime - lastClickTime >= intervalMillis) {
                    KeyBinding attackKey = client.options.attackKey;
                    boolean originalState = attackKey.isPressed();
                    attackKey.setPressed(true);
                    
                    if (client.targetedEntity != null) {
                        client.interactionManager.attackEntity(client.player, client.targetedEntity);
                    }
                    
                    client.player.swingHand(Hand.MAIN_HAND);
                    attackKey.setPressed(originalState);
                    lastClickTime = currentTime;
                }
            }
        });
    }

    private void registerMessageHandler() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (deepSeekChatMode && MinecraftClient.getInstance().player != null) {
                String userMessage = message.getString();
                
                // 忽略命令消息
                if (userMessage.startsWith("/")) {
                    return;
                }
                
                // 异步发送API请求
                CompletableFuture.runAsync(() -> {
                    try {
                        String response = callDeepSeekAPI(userMessage);
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().player.sendMessage(Text.of("DeepSeek: " + response), false);
                        });
                    } catch (Exception e) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().player.sendMessage(Text.of("调用DeepSeek API出错: " + e.getMessage()), false);
                        });
                    }
                });
            }
        });
    }



    private String callDeepSeekAPI(String message) throws IOException, InterruptedException {
        String requestBody = String.format("""
        {
            "model": "deepseek-chat",
            "messages": [{"role": "user", "content": "%s"}],
            "temperature": 0.7,
            "stream": false
        }
        """, message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 解析 JSON
        JSONObject json = new JSONObject(response.body());
        JSONArray choices = json.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");
            return messageObj.getString("content");
        } else {
            return "未收到有效的回答";
        }
    }

}