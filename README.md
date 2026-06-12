# SkyAC

A high-performance, packet-level, reflection-based Minecraft Anti-Cheat skeleton designed for Spigot, Paper, and Folia (regional multi-threading).

## Features

- **Netty Pipeline Interception**: Early interception of client-bound and server-bound packets directly in the Netty thread, avoiding Bukkit listener delays.
- **Folia Regional Threading Support**: Detects regional threading contexts dynamically to handle player setback teleports safely.
- **Combat Validation**:
  - `Reach`: Calculates eye-to-AABB (Axis-Aligned Bounding Box) distance to prevent elevation false positives.
  - `Killaura`: Monitors player camera rotation angle (Yaw/Pitch) alignment with targets on attack packets.
- **Movement Checks**:
  - `Fly`: Validates hovering and vertical motion against server-side ground states.
  - `Speed`: Dynamic distance validation factoring in potion speeds and sprinting multipliers.
  - `NoSlow`: Enforces speed limits when items are actively in use (eating, blocking).
  - `NoFall`: Cross-checks client-claimed ground states against server-side block collision vectors.
- **Packet & Utility Verification**:
  - `Timer`: Tracks packet intervals using a rolling average window.
  - `BadPackets`: Sanitizes coordinates (NaN/Infinities) and pitch limits.
  - `AutoClicker`: Analyzes click rate and computes click interval standard deviation to detect click macros.

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
