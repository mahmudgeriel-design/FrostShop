package ru.yourname.soulshop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

public class ItemsLogic implements Listener {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Long> silenceDebuff = new HashMap<>();
    private final Map<UUID, Long> vampirismActive = new HashMap<>();
    private final Map<UUID, Long> berserkActive = new HashMap<>();

    private boolean checkCooldown(Player player, String sphereType, int minutes) {
        long now = System.currentTimeMillis();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        if (playerCooldowns.containsKey(sphereType) && playerCooldowns.get(sphereType) > now) {
            long remaining = (playerCooldowns.get(sphereType) - now) / 1000;
            player.sendMessage("§c[Откат] §7Доступно через §c" + remaining + " сек§7.");
            return false;
        }
        playerCooldowns.put(sphereType, now + (minutes * 60L * 1000L));
        return true;
    }

    private boolean isSilenced(Player player) {
        if (silenceDebuff.containsKey(player.getUniqueId()) && silenceDebuff.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage("§c[Магия] §7Вы под эффектом Безмолвия! Использование сфер запрещено.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.hasItem()) {
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta() || item.getItemMeta().getDisplayName() == null) return;

            String displayName = item.getItemMeta().getDisplayName();
            String type = null;
            
            if (displayName.contains("Метеор")) type = "meteor";
            else if (displayName.contains("Купол")) type = "dome";
            else if (displayName.contains("Подмена")) type = "swap";
            else if (displayName.contains("Печать")) type = "silence";
            else if (displayName.contains("Вампиризма")) type = "vampire";
            else if (displayName.contains("Берсерка")) type = "berserk";
            else if (displayName.contains("Магнитный")) type = "magnet";
            else if (displayName.contains("Перегрузка")) type = "overload";
            else if (displayName.contains("Хронос")) type = "chronos";

            if (type == null) return;
            event.setCancelled(true);

            if (type.equals("vampire") || type.equals("berserk") || type.equals("magnet") || type.equals("overload") || type.equals("chronos")) {
                if (isSilenced(player)) return;
            }

            if (type.equals("meteor") || type.equals("dome") || type.equals("swap") || type.equals("silence")) {
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else player.getInventory().remove(item);
            }

            switch (type) {
                case "meteor" -> executeMeteor(player);
                case "dome" -> executeDome(player);
                case "swap" -> executeSwap(player);
                case "silence" -> executeSilence(player);
                case "vampire" -> { if(checkCooldown(player, "vampire", 4)) executeVampirism(player); }
                case "berserk" -> { if(checkCooldown(player, "berserk", 5)) executeBerserk(player); }
                case "magnet" -> { if(checkCooldown(player, "magnet", 3)) executeMagnet(player); }
                case "overload" -> { if(checkCooldown(player, "overload", 5)) executeOverload(player); }
                case "chronos" -> { if(checkCooldown(player, "chronos", 6)) executeChronos(player); }
            }
        }
    }

    private void executeMeteor(Player player) {
        Block targetBlock = player.getTargetBlock(null, 40);
        Location loc = targetBlock.getLocation().add(0.5, 1, 0.5);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.3, 0.3, 0.3, 0.1);
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
                    if (entity instanceof Player p) {
                        double distance = p.getLocation().distance(loc);
                        if (distance <= 0.7) p.setHealth(0.0);
                        else if (distance <= 5.0) p.damage((1.0 - (distance / 5.0)) * 20.0);
                    }
                }
            }, 2L);
        }, 16L);
    }

    private void executeDome(Player player) {
        Location center = player.getLocation();
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 2;
                if (ticks >= 160) { Bukkit.getScheduler().cancelTask(ticks); return; }
                for (int i = 0; i < 360; i += 15) {
                    double angle = Math.toRadians(i);
                    Location pLoc = center.clone().add(5 * Math.cos(angle), 0.2, 5 * Math.sin(angle));
                    pLoc.getWorld().spawnParticle(Particle.FLAME, pLoc, 1, 0, 0, 0, 0);
                }
                for (Entity entity : center.getWorld().getNearbyEntities(center, 5, 3, 5)) {
                    if (entity instanceof Player p && p != player) {
                        p.setVelocity(p.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.0).setY(0.2));
                    } else if (entity instanceof Arrow a) {
                        a.remove();
                    }
                }
            }
        }, 0L, 2L);
    }

    private void executeSwap(Player player) {
        for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player enemy && player.hasLineOfSight(enemy)) {
                Location pLoc = player.getLocation();
                Location eLoc = enemy.getLocation();
                player.teleport(eLoc);
                enemy.teleport(pLoc);
                break;
            }
        }
    }

    private void executeSilence(Player player) {
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player enemy) {
                silenceDebuff.put(enemy.getUniqueId(), System.currentTimeMillis() + 10000L);
                enemy.sendMessage("§c[Безмолвие] §7Магия заблокирована на 10 сек.");
                break;
            }
        }
    }
    private void executeVampirism(Player player) {
        vampirismActive.put(player.getUniqueId(), System.currentTimeMillis() + 8000L);
        player.sendMessage("§b[Вампиризм] §7Активен на 8 сек!");
    }

    private void executeBerserk(Player player) {
        berserkActive.put(player.getUniqueId(), System.currentTimeMillis() + 10000L);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
    }

    private void executeMagnet(Player player) {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 2;
                if (ticks >= 240 || !player.isOnline()) { Bukkit.getScheduler().cancelTask(ticks); return; }
                Location loc = player.getLocation();
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 6, 6, 6)) {
                    if (entity instanceof Arrow a) {
                        a.setVelocity(a.getVelocity().multiply(-1));
                    } else if (entity instanceof EnderPearl p) {
                        p.remove();
                    }
                }
            }
        }, 0L, 2L);
    }

    private void executeOverload(Player player) {
        player.setHealth(Math.max(1.0, player.getHealth() - 8.0));
        player.setMetadata("overload_active", new FixedMetadataValue(Main.getInstance(), System.currentTimeMillis() + 6000L));
    }

    private void executeChronos(Player player) {
        Location startLoc = player.getLocation().clone();
        double startHP = player.getHealth();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline() && !player.isDead()) {
                player.teleport(startLoc);
                player.setHealth(startHP);
            }
        }, 120L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim) {
            long now = System.currentTimeMillis();
            UUID dUUID = damager.getUniqueId();
            UUID vUUID = victim.getUniqueId();

            if (vampirismActive.containsKey(dUUID) && vampirismActive.get(dUUID) > now) {
                double healAmount = event.getFinalDamage() * 0.15;
                damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + healAmount));
            }

            if (berserkActive.containsKey(vUUID) && berserkActive.get(vUUID) > now) {
                event.setDamage(event.getDamage() * 1.3);
            }

            if (damager.hasMetadata("overload_active")) {
                long expire = damager.getMetadata("overload_active").get(0).asLong();
                if (expire > now) {
                    for (ItemStack armor : victim.getInventory().getArmorContents()) {
                        if (armor != null && armor.getItemMeta() instanceof Damageable dmg) {
                            dmg.setDamage(dmg.getDamage() + 6);
                            armor.setItemMeta((ItemMeta) dmg);
                        }
                    }
                }
            }
        }
    }
}