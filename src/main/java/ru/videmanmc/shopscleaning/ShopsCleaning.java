package ru.videmanmc.shopscleaning;

import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

import static com.bekvon.bukkit.residence.event.ResidenceRentEvent.RentEventType.RENT_EXPIRE;
import static com.bekvon.bukkit.residence.event.ResidenceRentEvent.RentEventType.UNRENTABLE;

public class ShopsCleaning extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(ResidenceRentEvent e) {
        if (!RENT_EXPIRE.equals(e.getCause())) {
            return;
        }
        deleteShopsInResidence(e.getResidence());
    }

    private void deleteShopsInResidence(ClaimedResidence residence) {
        var shops = findShopsInResidence(residence);
        var shopManager = QuickShop.getInstance().getShopManager();
        shops.forEach(shopManager::deleteShop);
    }

    private List<Shop> findShopsInResidence(ClaimedResidence residence) {
        var residenceArea = residence.getMainArea();
        var residenceChunks = residenceArea.getChunks()
                .stream()
                .map(chunkRef -> residenceArea.getWorld().getChunkAt(chunkRef.getX(), chunkRef.getZ()))
                .toList();

        var shopManager = QuickShop.getInstance().getShopManager();

        return residenceChunks.stream()
                .map(shopManager::getShops)
                .filter(Objects::nonNull)
                .flatMap(shops -> shops.values().stream())
                .filter(shop -> isShopOverlappedByResidence(residence, shop))
                .toList();
    }

    private boolean isShopOverlappedByResidence(ClaimedResidence residence, Shop shop) {
        var minPoint = residence.getMainArea().getLowVector();
        var maxPoint = residence.getMainArea().getHighVector();

        int shopX = shop.getLocation().getBlockX();
        int shopZ = shop.getLocation().getBlockZ();

        for (int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++) {
            if (shopX != x) {
                continue;
            }

            for (int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ(); z++) {
                if (shopZ == z) {
                    return true;
                }
            }
        }

        return false;
    }
}
