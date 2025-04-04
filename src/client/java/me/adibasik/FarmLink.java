package me.adibasik;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.apache.logging.log4j.LogManager;

public class FarmLink implements ClientModInitializer {
    public static final int MAX_TICK = 20;
    public static boolean tapemouseEnabled = false;
    public static int COMPASS_SLOT_INDEX2 = 12; // Укажите нужный индекс слота здесь
    public static int delayMs = 1500;
    @Environment(EnvType.CLIENT)
    public static ServerInfo lastestServerEntry;
    public static int disconnectTick = 0;
    private boolean wasCompassInHand = false;

    // Счётчик для TapeMouse
    private int tapeMouseTickCounter = 0;
    // Задержка (offset) перед выполнением удара в тиках
    private int pendingHitDelay = 0;
    private static final int HIT_OFFSET = 5; // Задержка в 5 тиков (можно изменить)

    public void clientTick() {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Открытие GUI при нажатии правого Shift
        if (mc.currentScreen == null && org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == 1) {
            mc.setScreen(new FarmLinkScreen());
        }

        // Проверка на компас в руке
        if (mc.player != null) {
            ItemStack mainHandStack = mc.player.getMainHandStack();
            boolean hasCompassInHand = mainHandStack.getItem() == Items.COMPASS;

            if (hasCompassInHand && !wasCompassInHand) {
                // Открываем меню (имитируем правый клик)
                mc.player.networkHandler.sendChatCommand("menu");
                // Через небольшой таймаут нажимаем слоты
                new Thread(() -> {
                    try {
                        Thread.sleep(delayMs); // Задержка для открытия меню
                        mc.execute(() -> {
                            if (mc.player != null && mc.player.currentScreenHandler != null) {
                                // Нажимаем первый слот (например, 21)
                                mc.interactionManager.clickSlot(
                                        mc.player.currentScreenHandler.syncId,
                                        21,
                                        0, // Кнопка (0 - левая)
                                        SlotActionType.PICKUP,
                                        mc.player
                                );
                            }
                        });
                        Thread.sleep(delayMs);
                        mc.execute(() -> {
                            if (mc.player != null && mc.player.currentScreenHandler != null) {
                                // Нажимаем указанный слот (COMPASS_SLOT_INDEX2)
                                mc.interactionManager.clickSlot(
                                        mc.player.currentScreenHandler.syncId,
                                        COMPASS_SLOT_INDEX2,
                                        0, // Кнопка (0 - левая)
                                        SlotActionType.PICKUP,
                                        mc.player
                                );
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            wasCompassInHand = hasCompassInHand;
        }

        // TapeMouse логика с задержкой удара (offset)
        if (tapemouseEnabled && mc.player != null) {
            // Если уже запланирован удар, ждем offset
            if (pendingHitDelay > 0) {
                pendingHitDelay--;
                if (pendingHitDelay == 0) {
                    performAttack(mc);
                }
            } else {
                tapeMouseTickCounter++;
                if (tapeMouseTickCounter >= 20) {
                    tapeMouseTickCounter = 0;
                    // Устанавливаем задержку перед атакой
                    pendingHitDelay = HIT_OFFSET;
                }
            }
        }

        // Оригинальный код для автореконнекта
        if (mc.world != null && mc.getCurrentServerEntry() != null) {
            lastestServerEntry = mc.getCurrentServerEntry();
        }
        if (mc.currentScreen instanceof DisconnectedScreen) {
            disconnectTick++;
            if (disconnectTick == MAX_TICK && lastestServerEntry != null) {
                System.out.println(disconnectTick);
                mc.disconnect();
                ConnectScreen.connect(new TitleScreen(), mc, ServerAddress.parse(lastestServerEntry.address), lastestServerEntry, false);
                disconnectTick = 0;
            }
        } else {
            disconnectTick = 0;
        }
    }

    private void performAttack(MinecraftClient mc) {
        KeyBinding attackKey = mc.options.attackKey;
        // Имитируем нажатие левой кнопки атаки
        attackKey.setPressed(true);
        KeyBinding.onKeyPressed(attackKey.getDefaultKey());
        mc.player.swingHand(Hand.MAIN_HAND);
        // Сбрасываем состояние через 1 тик
        mc.execute(() -> attackKey.setPressed(false));
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> clientTick());
        ClientTickEvents.START_CLIENT_TICK.register(minecraftClient -> clientTick());
        LogManager.getLogger().info("Loading Auto Reconnect");
    }
}
