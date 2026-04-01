package seiya.game;

public enum RuleSet {
    DEFAULT("Default", 2.0, 50, 50.0, 3.0, true),
    CLASSIC("Classic", 1.0, 0, 5.0, 5.0, false);

    private final String label;
    private final double gatherGain;
    private final int defendReductionPercent;
    private final double defendValue;
    private final double wearArmorDefenseValue;
    private final boolean tracksHealth;

    RuleSet(
        String label,
        double gatherGain,
        int defendReductionPercent,
        double defendValue,
        double wearArmorDefenseValue,
        boolean tracksHealth
    ) {
        this.label = label;
        this.gatherGain = gatherGain;
        this.defendReductionPercent = defendReductionPercent;
        this.defendValue = defendValue;
        this.wearArmorDefenseValue = wearArmorDefenseValue;
        this.tracksHealth = tracksHealth;
    }

    public double gatherGain() {
        return gatherGain;
    }

    public int defendReductionPercent() {
        return defendReductionPercent;
    }

    public double defendValue() {
        return defendValue;
    }

    public double wearArmorDefenseValue() {
        return wearArmorDefenseValue;
    }

    public boolean tracksHealth() {
        return tracksHealth;
    }

    @Override
    public String toString() {
        return label;
    }
}
