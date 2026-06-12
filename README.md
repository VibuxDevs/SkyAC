# SkyAC

A high-performance, packet-level, reflection-based Minecraft Anti-Cheat skeleton designed for Spigot, Paper, and Folia (regional multi-threading).

## Features

- **Netty Pipeline Interception**: Early interception of client-bound and server-bound packets directly in the Netty thread, avoiding Bukkit listener delays.
- **Folia Regional Threading Support**: Detects regional threading contexts dynamically to handle player setback teleports safely.
- **Combat Validation**:
  - `Reach`: Calculates eye-to-AABB (Axis-Aligned Bounding Box) distance to prevent elevation false positives. Optimized via direct NMS O(1) entity lookups.
  - `Killaura`: Monitors player camera rotation angle (Yaw/Pitch) alignment with targets on attack packets, adjusting for vertical eye-to-target height.
  - `AutoClicker`: Analyzes click rate and computes click interval standard deviation to detect click macros.
  - `FastBow`: Restricts bows from firing faster than physics thresholds.
- **Movement Checks**:
  - `Fly`: Validates hovering and vertical motion against server-side ground states.
  - `Speed`: Dynamic distance validation factoring in potion speeds and sprinting multipliers.
  - `NoSlow`: Enforces speed limits when items are actively in use (eating, blocking).
  - `NoFall`: Cross-checks client-claimed ground states against server-side block collision vectors.
  - `Jesus`: Rejects players who walk horizontally flat on top of liquids.
  - `Phase`: Verifies player coordinate limits inside solid blocks.
  - `Step`: Restricts instantaneous vertical movement greater than 0.6 blocks.
  - `NoWeb`: Validates speed limitations when standing inside cobweb blocks.
  - `InvMove`: Rejects window clicks while moving at high horizontal velocities.
- **Packet & Block Interaction Verification**:
  - `Timer`: Tracks packet intervals using a rolling average window.
  - `BadPackets`: Sanitizes coordinates (NaN/Infinities) and pitch limits.
  - `Velocity`: Evaluates expected server-sent knockback against client movement responses.
  - `FastBreak`: Compares start-digging and stop-digging packet timestamps to prevent instant break hacks.
  - `Scaffold`: Blocks placing blocks below the feet level without looking down (pitch >= 70).
  - `AirPlace`: Rejects placement packets targeting non-liquid air block coordinates.

## Architecture

- `NettyInjector`: Injects `ChannelDuplexHandler` dynamically into the Netty channel.
- `PlayerData`: Houses active check instances, latency metrics, and server-side velocity updates (knockback compensation).
- `SchedulerUtils`: Cross-platform executor routing tasks safely through Folia's `EntityScheduler` or Spigot's standard task scheduler.

## Compilation

Build the JAR artifact utilizing Maven:

```bash
mvn clean package
```

The output file will be generated in the `target/` directory.

## License

This project is licensed under the terms of **The Okay-Ish License**. Refer to the `LICENSE` file for more details.
