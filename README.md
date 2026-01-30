# ⚔️ TitanEconomy

![Version](https://img.shields.io/badge/version-1.0-blue.svg) ![Java](https://img.shields.io/badge/Java-21-orange) ![PaperMC](https://img.shields.io/badge/Server-PaperMC-green)

**The Ultimate Server Core Plugin.** TitanEconomy is a high-performance, all-in-one solution for Minecraft servers. It combines an advanced Economy system with RPG Leveling, a GUI Shop with quantity selection, a Global Auction House, Physical Bank Notes, and a sleek Scoreboard HUD.

---

## ✨ Features

* **💰 Advanced Economy:** Secure balance management with Vault support.
* **🛒 GUI Shop:** Configurable category-based shop with `x1`, `x32`, `x64` quantity selection.
* **⚖️ Auction House:** Global `/ah` system for players to sell items to each other.
* **📈 RPG Leveling:** Earn XP by killing mobs/mining. Higher levels = Higher money multipliers!
* **💵 Bank Notes:** Withdraw physical money items to trade or store securely.
* **📊 Scoreboard HUD:** Lag-free sidebar display for Balance, Level, and Server Name.
* **🏆 Leaderboards:** View the richest players with `/baltop`.

---

## 📥 Installation

1.  Download the `TitanEconomy.jar`.
2.  Place it in your server's `plugins` folder.
3.  **[Optional but Recommended]** Install **Vault** for compatibility with other plugins (like EssentialsX, GriefPrevention).
4.  Restart your server.

---

## 📜 Commands & Permissions

### 🟢 Player Commands
| Command | Description |
| :--- | :--- |
| `/bal` | Check your current wallet balance. |
| `/pay <player> <amount>` | Send money to another player. |
| `/withdraw <amount>` | Convert balance into a physical Bank Note. |
| `/shop` | Open the server Shop GUI. |
| `/ah` | Open the Auction House to buy items. |
| `/ah sell <price>` | Sell the item in your hand on the Auction House. |
| `/baltop` | View the top 10 richest players. |

### 🔴 Admin Commands (Requires `titaneconomy.admin`)
| Command | Description |
| :--- | :--- |
| `/eco give <player> <amt>` | Add money to a player's account. |
| `/eco take <player> <amt>` | Remove money from a player's account. |
| `/eco set <player> <amt>` | Set a player's balance to a specific amount. |

---

## ⚙️ Configuration

### Adding Items to Shop
You can add unlimited items via `src/main/resources/shops.yml`.

```yaml
  golden_apple:           # Unique Item ID
    material: GOLDEN_APPLE # Minecraft Material Name
    slot: 12              # Slot in GUI (0-53)
    price: 100.0          # Price per 1 unit
    name: "&6Golden Apple" # Display Name
