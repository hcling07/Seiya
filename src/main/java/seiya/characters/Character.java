package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Character {
    private final String name;
    private final double maxHealth;
    private double health;
    private final int totalArmor;
    private int armorWorn;
    private int spirit;
    private int defendPercent;
    private final List<Attack> attackMoves;
    private final List<ConsumableAttack> consumables;

    protected Character(
        String name,
        double maxHealth,
        int totalArmor,
        int startingSpirit,
        List<Attack> attackMoves,
        List<ConsumableAttack> consumables
    ) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.totalArmor = totalArmor;
        this.spirit = startingSpirit;
        this.attackMoves = new ArrayList<>(attackMoves);
        this.consumables = new ArrayList<>(consumables);
    }

    public String name() {
        return name;
    }

    public double health() {
        return health;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public int spirit() {
        return spirit;
    }

    public int armorWorn() {
        return armorWorn;
    }

    public int defendPercent() {
        return defendPercent;
    }

    public int totalArmor() {
        return totalArmor;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public List<Attack> attackMoves() {
        return Collections.unmodifiableList(attackMoves);
    }

    public List<ConsumableAttack> consumables() {
        return Collections.unmodifiableList(consumables);
    }

    public void gainSpirit(int amount) {
        spirit += Math.max(0, amount);
    }

    public void spendSpirit(int amount) {
        if (amount < 0 || amount > spirit) {
            throw new IllegalArgumentException("Invalid spirit spend: " + amount);
        }
        spirit -= amount;
    }

    public boolean canWearArmor() {
        return armorWorn < totalArmor;
    }

    public void wearArmorPiece() {
        if (!canWearArmor()) {
            return;
        }
        armorWorn++;
    }

    public void activateDefense(int damageReductionPercent) {
        defendPercent = Math.max(0, Math.min(100, damageReductionPercent));
    }

    public double previewDamageTaken(double rawDamage) {
        double reducedByDefense = rawDamage * defendPercent / 100.0;
        double reducedByArmor = armorWorn;
        double finalDamage = rawDamage - reducedByDefense - reducedByArmor;
        return Math.max(1.0, finalDamage);
    }

    public double receiveDamage(double rawDamage) {
        double damage = previewDamageTaken(rawDamage);
        health = Math.max(0.0, health - damage);
        defendPercent = 0;
        return damage;
    }

    public boolean hasConsumable(ConsumableAttack consumable) {
        return consumables.contains(consumable);
    }

    public void consume(ConsumableAttack consumable) {
        consumables.remove(consumable);
    }
}
