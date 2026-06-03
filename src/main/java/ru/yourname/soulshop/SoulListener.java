package ru.yourname.soulshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Arrays;
import java.util.Random;

public class SoulListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public SoulListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer == victim) return;
        String worldName = victim.getWorld().getName().toLowerCase();
        if (worldName.contains("duel") || worldName.contains("arena")) return;

        if (random.nextInt(100) < 25) {
            ItemStack soul = createSoulItem(victim.getName());
            event.getDrops().add(soul);
        }
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName() != null && event.getRightClicked().getName().contains("Монах")) {
            event.setCancelled(true);
            plugin.openMainMenu(event.getPlayer());
        }
    }

    private ItemStack createSoulItem(String victimName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(victimName));
            meta.setDisplayName("§b§lДуша");
            meta.setLore(Arrays.asList("§7Используйте у Монаха на спавне."));
            head.setItemMeta(meta);
        }
        return head;
    }
}
