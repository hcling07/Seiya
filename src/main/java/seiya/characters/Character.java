package seiya.characters;

import seiya.actions.Attack;
import seiya.actions.ConsumableAttack;
import seiya.game.RuleSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Character {
    private final RuleSet ruleSet;
    private final String name;
    private final double maxHealth;
    private double health;
    private final int totalArmor;
    private int armorWorn;
    private int armorUsed;
    private boolean consumableUnlocked;
    private boolean defeated;
    private double spirit;
    private int defendPercent;
    private final List<Attack> attackMoves;
    private final List<ConsumableAttack> consumables;

    protected Character(
        RuleSet ruleSet,
        String name,
        double maxHealth,
        int totalArmor,
        double startingSpirit,
        List<Attack> attackMoves,
        List<ConsumableAttack> consumables
    ) {
        this.ruleSet = ruleSet;
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

    public RuleSet ruleSet() {
        return ruleSet;
    }

    public double health() {
        return health;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double spirit() {
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

    public int remainingArmor() {
        return Math.max(0, totalArmor - armorUsed);
    }

    public boolean hasArmorEquipped() {
        return armorWorn > 0;
    }

    public boolean isConsumableUnlocked() {
        return consumableUnlocked;
    }

    public boolean isAlive() {
        if (!ruleSet.tracksHealth()) {
            return !defeated;
        }
        return health > 0;
    }

    public List<Attack> attackMoves() {
        return Collections.unmodifiableList(attackMoves);
    }

    public List<ConsumableAttack> consumables() {
        return Collections.unmodifiableList(consumables);
    }

    public void gainSpirit(double amount) {
        spirit += Math.max(0.0, amount);
    }

    public void spendSpirit(double amount) {
        if (amount < 0.0 || amount > spirit) {
            throw new IllegalArgumentException("Invalid spirit spend: " + amount);
        }
        spirit -= amount;
    }

    public boolean canWearArmor() {
        return armorUsed < totalArmor;
    }

    public void wearArmorPiece() {
        if (!canWearArmor()) {
            return;
        }
        armorWorn++;
        armorUsed++;
        consumableUnlocked = true;
    }

    public void breakArmorPiece() {
        if (armorWorn <= 0) {
            return;
        }
        armorWorn--;
    }

    public void activateDefense(int damageReductionPercent) {
        defendPercent = Math.max(0, Math.min(100, damageReductionPercent));
    }

    public double previewDamageTaken(double rawDamage) {
        if (!ruleSet.tracksHealth()) {
            return previewArmorLoss(rawDamage);
        }
        double reducedByDefense = rawDamage * defendPercent / 100.0;
        double reducedByArmor = armorWorn;
        double finalDamage = rawDamage - reducedByDefense - reducedByArmor;
        return Math.max(1.0, finalDamage);
    }

    public double receiveDamage(double rawDamage) {
        if (!ruleSet.tracksHealth()) {
            double armorLoss = previewArmorLoss(rawDamage);
            if (armorLoss <= 0.0) {
                defendPercent = 0;
                return 0.0;
            }

            int lossCount = (int) Math.round(armorLoss);
            if (armorWorn - lossCount < 0) {
                armorWorn = -1;
                defeated = true;
            } else {
                armorWorn -= lossCount;
            }
            defendPercent = 0;
            return armorLoss;
        }

        double reducedByDefense = rawDamage * defendPercent / 100.0;
        double remainingAfterDefense = rawDamage - reducedByDefense;
        double reducedByArmor = armorWorn;
        double damageBeforeFloor = remainingAfterDefense - reducedByArmor;
        double damage = Math.max(1.0, damageBeforeFloor);

        if (armorWorn > 0 && damageBeforeFloor > 0.0) {
            breakArmorPiece();
        }

        health = Math.max(0.0, health - damage);
        defendPercent = 0;
        return damage;
    }

    public boolean wouldBeDefeatedBy(double rawDamage) {
        if (!ruleSet.tracksHealth()) {
            return previewDamageTaken(rawDamage) > armorWorn;
        }
        return previewDamageTaken(rawDamage) >= health;
    }

    public String impactLabel() {
        return ruleSet.tracksHealth() ? "damage" : "armor loss";
    }

    public boolean hasConsumable(ConsumableAttack consumable) {
        return consumables.contains(consumable);
    }

    public void consume(ConsumableAttack consumable) {
        consumables.remove(consumable);
    }

    private double previewArmorLoss(double rawDamage) {
        if (rawDamage <= 0.0) {
            return 0.0;
        }
        return Math.floor(rawDamage / 5.0) + 1.0;
    }
}
