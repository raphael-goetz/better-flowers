package com.uroria.betterflowers.data;

import java.util.List;

public record FlowerGroupData(List<FlowerData> flowerData, List<Boolean> randomizer, List<Boolean> isGroup) {}