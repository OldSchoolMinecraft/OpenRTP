package com.oldschoolminecraft.openrtp;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class PlayerHandler extends PlayerListener
{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            Block block = event.getClickedBlock();
            if (block != null)
            {
                String yes = ChatColor.GREEN + "Yes";
                String no = ChatColor.RED + "No";
                event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "Is block empty? " + (block.isEmpty() ? yes : no));
            }
        }
    }
}
