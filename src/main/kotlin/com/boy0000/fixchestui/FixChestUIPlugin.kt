package com.boy0000.fixchestui

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLib
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FixChestUIPlugin : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(PacketHandler(this))
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    class PacketHandler(plugin: JavaPlugin) : PacketAdapter(
        plugin, ListenerPriority.MONITOR, PacketType.Play.Server.OPEN_WINDOW, PacketType.Play.Server.WINDOW_ITEMS
    ) {
        private var uuid = UUID.randomUUID()
        private var stateId = 0
        private var itemList = listOf<ItemStack>()
        private val manager: ProtocolManager = ProtocolLibrary.getProtocolManager()
        override fun onPacketSending(event: PacketEvent) {
            val packet = event.packet.deepClone()
            val player = event.player
            val inventory = player.openInventory.topInventory
            if (packet.type == PacketType.Play.Server.WINDOW_ITEMS) {
                uuid = player.uniqueId
                stateId = packet.integers.read(1).toInt()
                itemList = inventory.contents.toMutableList().apply {
                    this.replaceAll { it ?: ItemStack(Material.AIR) }
                }.toList().filterNotNull()
                return
            }

            val size = maxOf(1, inventory.size / 9)
            val title = "\uE108:vanilla_chest_${size}:\uE112\uE110\uE108${player.openInventory.title}"

            if (inventory.type !in setOf(InventoryType.ENDER_CHEST, InventoryType.CHEST, InventoryType.CRAFTING)) return
            if (inventory.holder !is BlockInventoryHolder && (inventory.holder == event.player && inventory.type != InventoryType.CRAFTING)) return

            // When inventory is opened via Player.openInventory(type) it seems to be delayed
            // So schedule a sync task to resend the packet with the same window id
            if (size == 1 && inventory.type == InventoryType.CRAFTING) {
                plugin.server.scheduler.scheduleSyncDelayedTask(plugin) {
                    if (":.*:".toRegex() in player.openInventory.title) return@scheduleSyncDelayedTask
                    if (uuid != player.uniqueId) return@scheduleSyncDelayedTask

                    val items = manager.createPacket(PacketType.Play.Server.WINDOW_ITEMS)
                    items.integers.write(0, packet.integers.read(0))
                    items.integers.write(1, stateId)
                    items.itemListModifier.write(0, itemList)
                    manager.sendServerPacket(player, packet)
                    manager.sendServerPacket(player, items)
                }
            } else event.packet.chatComponents.write(0, WrappedChatComponent.fromText(title))

        }
    }
}
