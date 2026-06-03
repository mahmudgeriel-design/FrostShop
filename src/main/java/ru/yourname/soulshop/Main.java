package ru.yourname.soulshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private final String mainTitle = "§0Монах";
    private final String shopTitle = "§0Магазин Сфер и Свитков";

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ItemsLogic(), this);
        getServer().getPluginManager().registerEvents(new SoulListener(this), this);
        
        if (getCommand("aurs") != null) {
            getCommand("aurs").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            openMainMenu(player);
        }
        return true;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, mainTitle);
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setDisplayName("§e§lИнформация / Квесты");
            bookMeta.setLore(Arrays.asList("§7В разработке..."));
            book.setItemMeta(bookMeta);
        }
        inv.setItem(11, book);

        ItemStack chest = new ItemStack(Material.ENDER_CHEST);
        ItemMeta chestMeta = chest.getItemMeta();
        if (chestMeta != null) {
            chestMeta.setDisplayName("§b§lОбменник Душ");
            chestMeta.setLore(Arrays.asList("§7Нажмите, чтобы открыть магазин."));
            chest.setItemMeta(chestMeta);
        }
        inv.setItem(13, chest);
        player.openInventory(inv);
    }

    public void openShopMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, shopTitle);
        inv.setItem(10, createShopItem(Material.PAPER, "§cСвиток \"Метеор\"", 5, "§7Вызывает мощный взрыв пламени."));
        inv.setItem(11, createShopItem(Material.FIREWORK_STAR, "§bСвиток \"Священный Купол\"", 8, "§7Создает защитный барьер."));
        inv.setItem(12, createShopItem(Material.ENDER_PEARL, "§eСвиток \"Подмена Реальности\"", 6, "§7Меняет местами с врагом."));
        inv.setItem(13, createShopItem(Material.FEATHER, "§fСвиток \"Печать Безмолвия\"", 7, "§7Блокирует магию врага на 10с."));
        inv.setItem(14, createShopItem(Material.REDSTONE, "§4Сфера Вампиризма", 12, "§7Кража HP при PvP ударах."));
        inv.setItem(15, createShopItem(Material.BLAZE_POWDER, "§6Сфера Берсерка", 15, "§7Дает силу, но увеличивает вх. урон."));
        inv.setItem(16, createShopItem(Material.IRON_INGOT, "§7Сфера Magnet", 10, "§7Отбивает стрелы и перлы."));
        inv.setItem(17, createShopItem(Material.FLINT, "§8Сфера Перегрузка", 14, "§7Тратит HP, но ломает броню врага в 3х."));
        inv.setItem(18, createShopItem(Material.CLOCK, "§dСфера Хронос", 18, "§7Возврат в точку через 6 сек."));
        player.openInventory(inv);
    }

    private ItemStack createShopItem(Material mat, String name, int price, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(desc, "", "§7Цена: §b" + price + " Душ"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(mainTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                player.closeInventory();
                player.sendMessage("§e[Монах] §7Данный раздел находится в разработке...");
            } else if (slot == 13) {
                openShopMenu(player);
            }
        } else if (title.equals(shopTitle)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            switch (slot) {
                case 10 -> handlePurchase(player, 5, "minecraft:paper", "§cСвиток \"Метеор\"");
                case 11 -> handlePurchase(player, 8, "minecraft:firework_star", "§bСвиток \"Священный Купол\"");
                case 12 -> handlePurchase(player, 6, "minecraft:ender_pearl", "§eСвиток \"Подмена Реальности\"");
                case 13 -> handlePurchase(player, 7, "minecraft:feather", "§fСвиток \"Печать Безмолвия\"");
                case 14 -> handlePurchase(player, 12, "minecraft:redstone", "§4Сфера Вампиризма");
                case 15 -> handlePurchase(player, 15, "minecraft:blaze_powder", "§6Сфера Берсерка");
                case 16 -> handlePurchase(player, 10, "minecraft:iron_ingot", "§7Сфера Magnet");
                case 17 -> handlePurchase(player, 14, "minecraft:flint", "§8Сфера Перегрузка");
                case 18 -> handlePurchase(player, 18, "minecraft:clock", "§dСфера Хронос");
            }
        }
    }

    private void handlePurchase(Player p, int price, String matKey, String name) {
        int souls = 0;
        for (ItemStack i : p.getInventory().getContents()) {
            if (i != null && i.getType() == Material.PLAYER_HEAD && i.hasItemMeta() && i.getItemMeta().getDisplayName() != null) {
                if (i.getItemMeta().getDisplayName().contains("Душа")) {
                    souls += i.getAmount();
                }
            }
        }
        if (souls < price) {
            p.sendMessage("§c[Ошибка] §7Недостаточно Душ.");
            return;
        }
        int take = price;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
                if (item.getItemMeta().getDisplayName().contains("Душа")) {
                    if (item.getAmount() > take) {
                        item.setAmount(item.getAmount() - take);
                        break;
                    } else {
                        take -= item.getAmount();
                        p.getInventory().setItem(i, null);
                        if (take <= 0) break;
                    }
                }
            }
        }
        String cmd = "give " + p.getName() + " " + matKey + "{display:{Name:'{\"text\":\"" + name + "\",\"italic\":false}'}} 1";
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
        p.sendMessage("§a[Успешно] §7Вы приобрели товар за " + price + " душ!");
    }

    public static Main getInstance() { return instance; }
}
