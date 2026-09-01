package com.tetris;

public class Item {
    public enum ItemType {
        CLEAR_POTION,
        BOMB,
        STAT_POTION,
        RANDOM_STAT_POTION,
        REVIVE_CROSS,
        CLEAR_DEBUFF_POTION,
        GEM_ATK,
        GEM_DEF,
        GEM_HP,
        WEAPON,
        ARMOR,
        GOLD,
        DUNGEON_KEY
    }

    private String name;
    private ItemType type;
    private int value;

    public Item(String name, ItemType type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
