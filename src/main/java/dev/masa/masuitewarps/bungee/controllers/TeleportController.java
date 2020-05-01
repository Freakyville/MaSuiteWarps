package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.bungee.Utils;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.QueuedWarp;
import dev.masa.masuitewarps.core.models.Warp;
import io.github.freakyville.hubqueue.api.API;
import io.github.freakyville.hubqueue.api.events.QueuePlayerJoinableEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;

public class TeleportController {
    private final MaSuiteWarps plugin;
    private final API hubQueueAPI;
    private final Utils utils = new Utils();

    private final HashMap<UUID, QueuedWarp> warpQueue = new HashMap<>();

    public TeleportController(MaSuiteWarps p, API api) {
        plugin = p;
        this.hubQueueAPI = api;
    }

    public void teleport(ProxiedPlayer player, String name, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden, boolean silent) {

        Warp warp = plugin.warpService.getWarp(name);

        if (checkWarp(player, warp, hasAccessToGlobal, hasAccessToServer, hasAccessToHidden)) {
            if (this.hubQueueAPI.canJoin(player.getUniqueId(), warp.getServer())) {
                warpPlayer(player, warp, silent);
            } else {
                this.hubQueueAPI.requestJoinEvent(player.getUniqueId(), warp.getServer());
                this.warpQueue.put(player.getUniqueId(), new QueuedWarp(warp, silent));
            }
        }
    }

    @EventHandler
    public void onServerJoinRequest(QueuePlayerJoinableEvent e) {
        QueuedWarp queuedWarp = warpQueue.get(e.getPlayer().getUniqueId());
        if (queuedWarp == null) {
            return;
        }
        warpPlayer(e.getPlayer(), queuedWarp.getWarp(), queuedWarp.isSilent());
    }

    private void warpPlayer(ProxiedPlayer player, Warp warp, boolean silent) {
        plugin.warpService.teleportToWarp(player, warp, silent);
    }

    private boolean checkWarp(ProxiedPlayer player, Warp warp, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden) {
        if (player == null) {
            return false;
        }
        if (warp == null || !utils.sameServerGroup(player.getServer().getInfo().getName(), warp.getServer())) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return false;
        }
        if (warp.isHidden() && !hasAccessToHidden) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (warp.isGlobal() && !hasAccessToGlobal) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal() && !hasAccessToServer) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal()) {
            if (!player.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
                plugin.formator.sendMessage(player, plugin.warpInOtherServer);
                return false;
            }
        }
        return true;
    }
}
