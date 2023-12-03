package com.uroria.betterflowers.menus;

import com.uroria.betterflowers.BetterFlowers;
import com.uroria.betterflowers.data.BrushData;
import com.uroria.betterflowers.data.FlowerGroupData;
import com.uroria.betterflowers.managers.FlowerManager;
import com.uroria.betterflowers.utils.BukkitPlayerInventory;
import com.uroria.betterflowers.utils.ItemBuilder;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public final class FlowerBrushMenu extends BukkitPlayerInventory {

    private final FlowerManager flowerManager;
    private final Map<FlowerGroupData, Material> maskData;
    private final List<FlowerGroupData> flowerGroupData;
    private final Player player;

    private int radius;
    private float airRandomizer;

    public FlowerBrushMenu(BetterFlowers betterFlowers, Player player) {
        super(MiniMessage.miniMessage().deserialize("<gradient:#232526:#414345>Flower Brush"), 4);

        this.flowerManager = betterFlowers.getFlowerManager();
        this.flowerGroupData = new ArrayList<>();
        this.maskData = new HashMap<>();

        this.player = player;
        this.radius = 3;
        this.airRandomizer = 0.0f;

        generateOverlay();
    }

    public void open() {
        this.openInventory(player);
    }

    private void generateOverlay() {

        this.setSlot(0, new ItemBuilder(Material.ECHO_SHARD).setName("Create").build(), this::onCreateBrush);
        this.setSlot(1, new ItemBuilder(Material.ENDER_EYE).setName("Current radius: " + radius).build(), this::onChangeRadius);
        this.setSlot(2, new ItemBuilder(Material.STRUCTURE_VOID).setName("Current air percentage" + airRandomizer).build(), this::onChangeAirRandomizer);

        for (int index = 18; index < 27; index++) {
            this.setSlot(index, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("Insert Flower Mask!").build(), this::onMaskAdd);
        }

        for (int index = 27; index < 36; index++) {
            this.setSlot(index, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("Insert Flower Placer!").build(), this::onFlowerAdd);
        }
    }

    private void onFlowerAdd(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        final var flowerItem = inventoryClickEvent.getCursor();
        if (flowerItem.getItemMeta() == null || flowerItem.getType() != Material.BLAZE_POWDER) return;
        if (!flowerManager.getFlowers().containsKey(flowerItem)) return;

        final var slot = inventoryClickEvent.getSlot();
        final var flowers = flowerManager.getFlowers().get(flowerItem);

        this.flowerGroupData.add(flowers);
        this.setSlot(slot, flowerItem, this::onFlowerRemove);
    }

    private void onFlowerRemove(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        final var slot = inventoryClickEvent.getSlot();
        final var flowerItem = this.inventory.getItem(slot);

        if (flowerItem == null || flowerItem.getItemMeta() == null) return;
        if (flowerItem.getItemMeta() == null || flowerItem.getType() != Material.BLAZE_POWDER) return;
        if (!flowerManager.getFlowers().containsKey(flowerItem)) return;

        final var flowers = flowerManager.getFlowers().get(flowerItem);
        this.flowerGroupData.remove(flowers);
        this.setSlot(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build(), this::onFlowerAdd);
    }

    private void onChangeAirRandomizer(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        if (inventoryClickEvent.isLeftClick() && airRandomizer > 1f) airRandomizer += 0.1f;
        if (inventoryClickEvent.isRightClick() && airRandomizer < 0f) airRandomizer -= 0.1f;
        if (airRandomizer > 1f) airRandomizer = 1f;
        if (radius < 0f) airRandomizer = 0f;

        this.setSlot(2, new ItemBuilder(Material.STRUCTURE_VOID).setName("Current air percentage" + airRandomizer).build(), this::onChangeAirRandomizer);
    }

    private void onChangeRadius(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        if (inventoryClickEvent.isLeftClick()) radius += 1;
        if (inventoryClickEvent.isRightClick()) radius -= 1;
        if (radius > 20) radius = 20;
        if (radius < 1) radius = 1;

        this.setSlot(1, new ItemBuilder(Material.ENDER_EYE).setName("Current radius: " + radius).build(), this::onChangeRadius);
    }

    private void onCreateBrush(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        if (flowerGroupData.isEmpty()) return;
        if (radius >= 51) return;

        final var name = "<green>ID: " + System.currentTimeMillis() + "</green>";
        final var brushItem = new ItemBuilder(Material.BLAZE_ROD).setName(name).build();
        this.flowerManager.getBrushes().put(brushItem, new BrushData(flowerGroupData, radius, airRandomizer, maskData));

        this.player.getInventory().addItem(brushItem);
        this.player.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#a8ff78:#78ffd6>Brush has been created"));
        this.player.getInventory().close();
    }

    private void onMaskAdd(InventoryClickEvent inventoryClickEvent) {

        final var slot = inventoryClickEvent.getSlot();
        final var maskItemType = inventoryClickEvent.getCursor().getType();
        final var flowerGroupData = getFlowerGroupFromMask(inventoryClickEvent);
        System.out.println(flowerGroupData.toString());

        if (flowerGroupData.isEmpty()) return;
        this.maskData.put(flowerGroupData.get(), maskItemType);
        this.setSlot(slot, new ItemBuilder(maskItemType).setName("Current Mask: " + maskItemType).build(), this::onMaskRemove);
    }

    private void onMaskRemove(InventoryClickEvent inventoryClickEvent) {

        final var slot = inventoryClickEvent.getSlot();
        final var flowerGroupData = getFlowerGroupFromMask(inventoryClickEvent);

        System.out.println(flowerGroupData.toString());
        if (flowerGroupData.isEmpty()) return;
        this.maskData.remove(flowerGroupData.get());
        this.setSlot(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("Insert Flower Mask!").build(), this::onMaskAdd);
    }

    private Optional<FlowerGroupData> getFlowerGroupFromMask(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);

        final var slot = inventoryClickEvent.getSlot();
        System.out.println("1");
        if (slot >= 26) return Optional.empty();

        final var coherentSlot = slot + 9;
        final var flowerItem = this.inventory.getItem(coherentSlot);

        System.out.println("2");
        if (!flowerManager.getFlowers().containsKey(flowerItem)) return Optional.empty();
        System.out.println("3");
        return Optional.of(flowerManager.getFlowers().get(flowerItem));
    }
}