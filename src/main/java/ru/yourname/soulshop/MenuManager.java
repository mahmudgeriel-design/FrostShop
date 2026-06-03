package ru.yourname.soulshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class MenuManager implements Listener {

    private final Main plugin;
    private final String mainTitle = "§0Монах";
    private final String shopTitle = "§0Магазин Сфер и Свитков";

    public MenuManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
                case 10 -> handlePurchase(player, 5, Material.PAPER, "§cСвиток \"Метеор\"", "meteor");
                case 11 -> handlePurchase(player, 8, Material.FIREWORK_STAR, "§bСвиток \"Священный Купол\"", "dome");
                case 12 -> handlePurchase(player, 6, Material.ENDER_PEARL, "§eСвиток \"Подмена Реальности\"", "swap");
                case 13 -> handlePurchase(player, 7, Material.FEATHER, "§fСвиток \"Печать Безмолвия\"", "silence");
                case 14 -> handlePurchase(player, 12, Material.REDSTONE, "§4Сфера Вампиризма", "vampire");
                case 15 -> handlePurchase(player, 15, Material.BLAZE_POWDER, "§6Сфера Берсерка", "berserk");
                case 16 -> handlePurchase(player, 10, Material.IRON_INGOT, "§7Сфера Magnet", "magnet");
                case 17 -> handlePurchase(player, 14, Material.FLINT, "§8Сфера Перегрузка", "overload");
                case 18 -> handlePurchase(player, 18, Material.CLOCK, "§dСфера Хронос", "chronos");
            }
        }
    }

    private void handlePurchase(Player p, int price, Material m, String name, String key) {
        int souls = 0;
        for (ItemStack i : p.getInventory().getContents()) {
            if (i != null && i.getType() == Material.PLAYER_HEAD && i.hasItemMeta()) {
                if (i.getItemMeta().getPersistentDataContainer().has(plugin.getSoulKey(), PersistentDataType.BYTE)) {
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
            if (item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta()) {
                if (item.getItemMeta().getPersistentDataContainer().has(plugin.getSoulKey(), PersistentDataType.BYTE)) {
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
        
        // ЖЕСТКИЙ ФИКС ОБХОДА BASECOMPONENT: Выдаем кастомный предмет через чистую команду ванильного Bukkit API
        String cmd = "minecraft:give " + p.getName() + " " + m.getKey().getKey() + "{display:{Name:'{\"text\":\"" + name + "\",\"italic\":false}'},soul_shop_key:\"" + key + "\"} 1";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        p.sendMessage("§a[Успешно] §7Вы приобрели товар за " + price + " душ!");
    }
}