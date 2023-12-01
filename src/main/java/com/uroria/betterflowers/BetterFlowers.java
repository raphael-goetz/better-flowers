package com.uroria.betterflowers;

import com.uroria.betterflowers.commands.Flower;
import com.uroria.betterflowers.commands.FlowerBrush;
import com.uroria.betterflowers.commands.UndoFlower;
import com.uroria.betterflowers.listeners.CustomFlowerBrushListener;
import com.uroria.betterflowers.listeners.CustomFlowerPlaceListener;
import com.uroria.betterflowers.managers.FlowerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BetterFlowers extends JavaPlugin {

    private final FlowerManager flowerManager;

    public BetterFlowers() {
        this.flowerManager = new FlowerManager();
    }

    @Override
    public void onEnable() {

        getCommand("flower").setExecutor(new Flower(this));
        getCommand("flowerbrush").setExecutor(new FlowerBrush(this));
        getCommand("uf").setExecutor(new UndoFlower(this));
        Bukkit.getPluginManager().registerEvents(new CustomFlowerPlaceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CustomFlowerBrushListener(this), this);
    }
}