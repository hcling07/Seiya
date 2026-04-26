# Seiya

## Run UI

Use these commands from the project root:

```bash
./gradlew -q classes
java -cp build/classes/java/main seiya.Main ui
```

The UI start screen lets you choose:

- Human character
- AI character
- Rule set: `Default` or `Classic`

## Other Modes

`Main` also supports:

- `ava` for AI vs AI using default rules
- `ava classic` for AI vs AI using classic rules

## Balance Simulation

Run the classic-rule character balance simulation from the project root:

```bash
./gradlew -q testClasses
java -cp build/classes/java/main:build/classes/java/test seiya.sim.ClassicBalanceSimulation
```

Optionally pass the number of matches per ordered matchup:

```bash
java -cp build/classes/java/main:build/classes/java/test seiya.sim.ClassicBalanceSimulation 5000
```
