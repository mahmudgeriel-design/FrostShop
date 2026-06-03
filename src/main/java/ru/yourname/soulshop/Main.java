package ru.yourname.soulshop;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private NamespacedKey soulKey;
    private MenuManager menuManager;
    private ItemsLogic itemsLogic;

    @Override
    public void onEnable() {
        instance = this;
        this.soulKey = new NamespacedKey(this, "soul_item");
        this.menuManager = new MenuManager(this);
        this.itemsLogic = new ItemsLogic();

        getServer().getPluginManager().registerEvents(new SoulListener(this, menuManager), this);
        getServer().getPluginManager().registerEvents(itemsLogic, this);
        
        // Меняем регистрацию команды на /aurs
        if (getCommand("aurs") != null) {
            getCommand("aurs").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            menuManager.openMainMenu(player);
        }
        return true;
    }

    public static Main getInstance() { return instance; }
    public NamespacedKey getSoulKey() { return soulKey; }
}