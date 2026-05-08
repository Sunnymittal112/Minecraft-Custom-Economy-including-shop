# TitanEconomy

![Version](https://img.shields.io/badge/version-1.1-blue.svg) ![Java](https://img.shields.io/badge/Java-21-orange) ![PaperMC](https://img.shields.io/badge/Server-PaperMC-green)

**The Ultimate Server Core Plugin.** TitanEconomy is a high-performance, all-in-one solution for Minecraft servers. It combines an advanced Economy system with RPG Leveling, a GUI Shop with quantity selection, a Global Auction House, Physical Bank Notes, and a sleek Scoreboard HUD.

---

## Patch Update: 8 May 2026

* **Asynchronous Data Management:** Rewrote the data-saving engine to handle player balances, levels, and auction listings on background threads. This eliminates main-thread lag during disk I/O operations.
* **Automated Data Protection:** Implemented a scheduled asynchronous auto-save task that triggers every 5 minutes to ensure zero data loss in case of unexpected server shutdowns.
* **Smart Currency Formatting:** Introduced the FormatUtil system to support readable currency suffixes (k, M, B, T, Q). This is integrated into the Scoreboard HUD, Balance commands, Leaderboards, and Shop GUIs.
* **High-Performance UI:** Updated the Scoreboard and Action Bar systems to utilize Paper's latest Component API, reducing overhead and improving text rendering efficiency.
* **Thread-Safe Data Structures:** Implemented collection cloning during save cycles to prevent ConcurrentModificationExceptions and ensure system stability under heavy load.

---

## Features

* **Advanced Economy:** Secure balance management with Vault support and asynchronous processing.
* **GUI Shop:** Configurable category-based buy/sell shop with x1, x32, x64 quantity selection, /sell, and /sellhand.
* **Auction House:** Global /ah system for players to sell items to each other with background data persistence.
* **RPG Leveling:** Earn XP by killing mobs/mining. Higher levels = Higher money multipliers!
* **Bank Notes:** Withdraw physical money items to trade or store securely.
* **Scoreboard HUD:** Optimized sidebar display for Balance, Level, and Server Name using modern formatting.
* **Leaderboards:** View the richest players with /baltop using optimized asynchronous sorting.

---

## Installation

1. Download the built TitanEconomy-1.1.jar.
2. Place it in your server's plugins folder.
3. [Optional but Recommended] Install Vault for compatibility with other plugins (like EssentialsX, GriefPrevention).
4. Restart your server.

---

## Commands & Permissions

### Player Commands
| Command | Description |
| :--- | :--- |
| /bal | Check your current wallet balance. |
| /pay <player> <amount> | Send money to another player. |
| /withdraw <amount> | Convert balance into a physical Bank Note. |
| /shop | Open the server Shop GUI. |
| /sell | Open the sell menu for your inventory and hotbar. |
| /sellhand | Sell the full stack in your main hand. |
| /ah | Open the Auction House to buy items. |
| /ah sell <price> | Sell the item in your hand on the Auction House. |
| /baltop | View the top 10 richest players. |

### Admin Commands (Requires titaneconomy.admin)
| Command | Description |
| :--- | :--- |
| /eco give <player> <amt> | Add money to a player's account. |
| /eco take <player> <amt> | Remove money from a player's account. |
| /eco set <player> <amt> | Set a player's balance to a specific amount. |

---

## Configuration

### Adding Items to Shop
You can add unlimited items via plugins/TitanEconomy/shops.yml.

```yaml
menus:
  food:
    items:
      golden_apple:
        material: GOLDEN_APPLE
        slot: 12
        name: '&6Golden Apple'
        buy: 100.0
        sell: 20.0
```

buy is the shop purchase price. sell is the price used by /sell and /sellhand.

Legacy entries that still use only price are migrated automatically on startup to buy and sell.

### Scoreboard Configuration
You can customize the scoreboard title and footer in plugins/TitanEconomy/config.yml.

```yaml
scoreboard:
  header: "  &6&lTITAN NETWORK  "
  footer: "&6play.titan.com"
```
