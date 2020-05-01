package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.core.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class WarpMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private MaSuiteWarps plugin;

    public WarpMessageListener(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            switch (in.readUTF()) {
                case "WarpPlayer":
                    warpPlayer(in);
                    break;
                case "CreateWarp":
                    createWarp(in);
                    break;
                case "WarpCooldown":
                    warpCooldown(in);
                    break;
                case "SetPerWarpFlag":
                    setPerWarpFlag(in);
                    break;
                case "DelWarp":
                    delWarp(in);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void warpPlayer(DataInputStream in) throws IOException {
        Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
        if (p == null) {
            return;
        }
        Location loc = new Location().deserialize(in.readUTF());

        org.bukkit.Location bukkitLocation = BukkitAdapter.adapt(loc);
        if (bukkitLocation.getWorld() == null) {
            System.out.println("[MaSuite] [Warps] [World=" + loc.getWorld() + "] World  could not be found!");
            return;
        }
        p.teleport(bukkitLocation);
    }

    private void createWarp(DataInputStream in) throws IOException {
        Warp warp = new Warp();
        warp = warp.deserialize(in.readUTF().toLowerCase());
        plugin.warps.put(warp.getName(), warp);
    }

    private void warpCooldown(DataInputStream in) throws IOException {
        Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
        if (p == null) {
            return;
        }
    }

    private void setPerWarpFlag(DataInputStream in) throws IOException {
        plugin.perServerWarps = in.readBoolean();
    }

    private void delWarp(DataInputStream in) throws IOException {
        plugin.warps.remove(in.readUTF());
    }
}
