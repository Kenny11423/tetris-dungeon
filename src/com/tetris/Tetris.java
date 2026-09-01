package com.tetris;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class Tetris extends JFrame {

    private JPanel cards;
    private CardLayout cardLayout;
    
    private Board board;
    private JLabel scoreValueLabel;
    
    // Gameplay mechanics
    private int level = 1;
    private int enemyHp;
    private int maxEnemyHp;
    private int attackTimer = 0;
    private int maxAttackTimer;
    private Timer bossTimer;
    private Timer bossSkillTimer;
    private JLabel skillStatusLabel;
    private int bossSkillInterval = 5000;
    private int bonusGarbageLines = 0;
    private boolean isBossRushMode = false;
    private int bossRushCount = 0;
    private boolean hideNextPiece = false;

    private JPanel bossHpBar;
    private JPanel attackTimerBar;
    private JLabel levelLabel;
    private JLabel bossAnimLabel;
    
    // Event Panel components
    private JLabel eventTitleLabel;
    private JLabel eventDescLabel;
    private JButton btnDropChest;
    private JButton btnEventChest;
    private JButton btnLockedChest;
    private JButton btnDevilsChallenge;
    private JButton btnShop;
    private JButton btnStatTrainer;
    private JButton btnNext;
    private GamepadManager gamepadManager;
    private Runnable pendingEventAction;
    
    // Inventory System
    // Inventory System
    private Item[] inventory = new Item[12];
    private JPanel[] backpackSlotsUI = new JPanel[12];
    private JLabel lblGold;
    
    private Item[] quickSlots = new Item[3];
    private JPanel[] quickSlotsUI = new JPanel[3];       // Inventory screen slots
    private JPanel[] gameQuickSlotsUI = new JPanel[3];   // In-game HUD slots
    private int playerGold = 0;
    
    // Equipment Gems
    private Item[] weaponGems = new Item[6];
    private Item[] armorGems = new Item[6];
    
    // Player Stats
    private int playerAtk = 10;
    private int playerDef = 5;
    private int playerLuck = 2;
    private int playerEvade = 0;        // % evade chance
    private int playerCritChance = 5;   // % crit chance
    private int playerCritDmg = 150;    // % crit damage multiplier
    private int playerDebuffResist = 0; // % debuff resist chance

    // Temporary Buffs (from Stat Potions)
    private int tempAtkBuff = 0;
    private int tempDefBuff = 0;
    
    // Debuff tracking - ordered by when applied (oldest first)
    private java.util.LinkedList<String> activeDebuffs = new java.util.LinkedList<>();
    private Random random = new Random();

    private Shape currentNextPiece;
    private JPanel nextBlockPreview;
    
    private Shape currentHoldPiece;
    private JPanel holdBlockPreview;

    public void updateDebuffStatus(String text) {
        if (skillStatusLabel != null) {
            String debuffText = (text == null || text.trim().isEmpty()) ? "<span style='color: #5cb85c; font-weight: bold;'>NORMAL</span>" : "<span style='color: #ff4444; font-weight: bold;'>" + text + "</span>";
            skillStatusLabel.setText("<html><div style='width:160px; font-family:sans-serif; font-size:11px;'><b style='color:#5bc0de;'>🛡️ TRẠNG THÁI & HIỆU ỨNG</b><br><div style='background-color:#222228; padding:6px; border-radius:4px; margin-top:4px;'><b>DEBUFF:</b> " + debuffText + "</div><div style='background-color:#222228; padding:6px; border-radius:4px; margin-top:6px;'><b style='color:#ffd700;'>🧪 THUỐC & HIỆU ỨNG:</b><br>• <b>[Z] Clear Potion:</b> Xóa 5 hàng<br>• <b>[X] Stat Potion:</b> Tăng +5 ATK/DEF<br>• <b>[C] Random:</b> Buff ngẫu nhiên<br>• <b>[V] Hold:</b> Giữ khối</div></div></html>");
        }
    }

    public void updateNextPiece(Shape nextPiece) {
        this.currentNextPiece = nextPiece;
        if (nextBlockPreview != null) {
            nextBlockPreview.repaint();
        }
    }

    public void updateHoldPiece(Shape holdPiece) {
        this.currentHoldPiece = holdPiece;
        if (holdBlockPreview != null) {
            holdBlockPreview.repaint();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape, int sqSize) {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };
        var color = colors[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, sqSize - 2, sqSize - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + sqSize - 1, x, y);
        g.drawLine(x, y, x + sqSize - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + sqSize - 1, x + sqSize - 1, y + sqSize - 1);
        g.drawLine(x + sqSize - 1, y + sqSize - 1, x + sqSize - 1, y + 1);
    }

    public Tetris() {
        initUI();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(createHomePanel(), "Home");
        cards.add(createGamePanel(), "Game");
        cards.add(createEventPanel(), "Event");
        cards.add(createLeaderboardPanel(), "Leaderboard");
        cards.add(createInventoryPanel(), "Inventory");

        add(cards);

        setTitle("Tetris Dungeon");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        gamepadManager = new GamepadManager(this, board);
        gamepadManager.startPolling();
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("Tetris Dungeon");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));

        JLabel modeDescLabel = new JLabel("Chế độ Thám Hiểm Hầm Ngục: Vượt qua các tầng quái vật & Boss ngẫu nhiên.");
        modeDescLabel.setForeground(new Color(200, 200, 200));
        modeDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        modeDescLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnNormalMode = createStyledButton("Normal Mode");
        btnNormalMode.setPreferredSize(new Dimension(140, 36));

        JButton btnBossRushMode = createStyledButton("Boss Rush Mode 👑");
        btnBossRushMode.setPreferredSize(new Dimension(140, 36));

        Runnable updateModeSelectionUI = () -> {
            if (isBossRushMode) {
                btnBossRushMode.setBackground(new Color(217, 83, 79));
                btnBossRushMode.setForeground(Color.WHITE);
                btnNormalMode.setBackground(new Color(50, 50, 50));
                btnNormalMode.setForeground(Color.GRAY);
                modeDescLabel.setText("Chế độ Boss Rush 👑: Đấu liên tục với các Boss Tier từ Stage 5 trở đi!");
            } else {
                btnNormalMode.setBackground(new Color(91, 192, 222));
                btnNormalMode.setForeground(Color.BLACK);
                btnBossRushMode.setBackground(new Color(50, 50, 50));
                btnBossRushMode.setForeground(Color.GRAY);
                modeDescLabel.setText("Chế độ Thám Hiểm Hầm Ngục: Vượt qua các tầng quái vật & Boss ngẫu nhiên.");
            }
        };

        btnNormalMode.addActionListener(e -> {
            isBossRushMode = false;
            updateModeSelectionUI.run();
        });

        btnBossRushMode.addActionListener(e -> {
            isBossRushMode = true;
            updateModeSelectionUI.run();
        });

        updateModeSelectionUI.run(); // Initial UI highlight

        JButton btnStartGame = createStyledButton("BẮT ĐẦU (START GAME)");
        btnStartGame.setPreferredSize(new Dimension(290, 45));
        btnStartGame.setBackground(new Color(92, 184, 92)); // Bright green start button
        btnStartGame.setForeground(Color.WHITE);
        btnStartGame.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStartGame.addActionListener(e -> {
            bossRushCount = 0;
            cardLayout.show(cards, "Game");
            board.start();
            resetGameMechanics();
            javax.swing.SwingUtilities.invokeLater(() -> board.requestFocusInWindow());
        });

        JButton btnLeaderboard = createStyledButton("Leaderboard");
        btnLeaderboard.setPreferredSize(new Dimension(290, 38));
        btnLeaderboard.addActionListener(e -> {
            updateLeaderboardPanel();
            cardLayout.show(cards, "Leaderboard");
        });

        JButton btnControls = createStyledButton("Controller & Settings 🎮");
        btnControls.setPreferredSize(new Dimension(290, 38));
        btnControls.addActionListener(e -> showControlSettingsDialog());

        JButton btnQuit = createStyledButton("Quit Game");
        btnQuit.setPreferredSize(new Dimension(290, 38));
        btnQuit.addActionListener(e -> System.exit(0));

        JPanel modeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        modeButtonsPanel.setOpaque(false);
        modeButtonsPanel.add(btnNormalMode);
        modeButtonsPanel.add(btnBossRushMode);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(modeButtonsPanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(modeDescLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(btnStartGame, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(btnLeaderboard, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(btnControls, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnQuit, gbc);

        return panel;
    }

    private JPanel leaderboardContent;

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("High Scores", SwingConstants.CENTER);
        title.setForeground(new Color(91, 192, 222));
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        leaderboardContent = new JPanel();
        leaderboardContent.setLayout(new BoxLayout(leaderboardContent, BoxLayout.Y_AXIS));
        leaderboardContent.setBackground(new Color(25, 25, 25));
        panel.add(leaderboardContent, BorderLayout.CENTER);

        JButton btnBack = createStyledButton("Back to Menu");
        btnBack.addActionListener(e -> cardLayout.show(cards, "Home"));
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(25, 25, 25));
        bottom.add(btnBack);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void updateLeaderboardPanel() {
        leaderboardContent.removeAll();
        java.util.List<DatabaseManager.HighScore> scores = DatabaseManager.getTopScores(10);
        
        for (int i = 0; i < scores.size(); i++) {
            DatabaseManager.HighScore s = scores.get(i);
            String timeStr = String.format("%02d:%02d", s.score / 60, s.score % 60);
            JLabel lbl = new JLabel((i + 1) + ". " + s.name + " - Time: " + timeStr + " (Level " + s.level + ")");
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            leaderboardContent.add(lbl);
            leaderboardContent.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        leaderboardContent.revalidate();
        leaderboardContent.repaint();
    }

    public void handleGameOver() {
        // Check for REVIVE_CROSS in quick slots or inventory
        int reviveSlotInQuick = -1;
        int reviveSlotInInv = -1;

        for (int i = 0; i < quickSlots.length; i++) {
            if (quickSlots[i] != null && quickSlots[i].getType() == Item.ItemType.REVIVE_CROSS) {
                reviveSlotInQuick = i;
                break;
            }
        }

        if (reviveSlotInQuick == -1) {
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] != null && inventory[i].getType() == Item.ItemType.REVIVE_CROSS) {
                    reviveSlotInInv = i;
                    break;
                }
            }
        }

        if (reviveSlotInQuick != -1 || reviveSlotInInv != -1) {
            // Revive triggered!
            if (reviveSlotInQuick != -1) {
                quickSlots[reviveSlotInQuick] = null;
            } else {
                inventory[reviveSlotInInv] = null;
            }
            refreshInventoryUI();

            board.removeBottomLines(15); // Clear 70% lines

            if (bossTimer != null && !bossTimer.isRunning()) bossTimer.start();
            if (bossSkillTimer != null && !bossSkillTimer.isRunning()) bossSkillTimer.start();
            if (gameRunTimer != null && !gameRunTimer.isRunning()) gameRunTimer.start();

            javax.swing.JOptionPane.showMessageDialog(this, "Thập tự hồi sinh đã kích hoạt! Cứu sống bạn và xóa 70% số hàng khối!", "REVIVED!", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (bossTimer != null) bossTimer.stop();
        if (bossSkillTimer != null) bossSkillTimer.stop();
        if (gameRunTimer != null) gameRunTimer.stop();
        
        if (timeElapsed > 0) {
            String name = javax.swing.JOptionPane.showInputDialog(this, "Game Over! Enter your name:", "High Score", javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                DatabaseManager.saveHighScore(name.trim(), timeElapsed, level);
            }
        }
        updateLeaderboardPanel();
        cardLayout.show(cards, "Leaderboard");
    }

    private JPanel createEventPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 20));

        eventTitleLabel = new JLabel("Stage Cleared!");
        eventTitleLabel.setForeground(new Color(91, 192, 222));
        eventTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));

        eventDescLabel = new JLabel("You found a treasure...");
        eventDescLabel.setForeground(Color.WHITE);
        eventDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

        btnNext = createStyledButton("Proceed deeper");
        btnNext.setPreferredSize(new Dimension(250, 40));
        btnNext.addActionListener(e -> {
            if (pendingEventAction != null) {
                pendingEventAction.run();
                pendingEventAction = null;
            }
            cardLayout.show(cards, "Game");
            level++;
            updateLevelMechanics();
            board.resumeFromEvent();
            javax.swing.SwingUtilities.invokeLater(() -> board.requestFocusInWindow());
        });
        
        btnDropChest = createStyledButton("Loot Victory Chest 🎁");
        btnDropChest.setPreferredSize(new Dimension(250, 40));
        btnDropChest.setBackground(new Color(240, 173, 78)); // Golden color
        btnDropChest.setForeground(Color.BLACK);
        btnDropChest.addActionListener(e -> openDropChest());

        btnEventChest = createStyledButton("Ancient Relic Chest 🗝️");
        btnEventChest.setPreferredSize(new Dimension(250, 40));
        btnEventChest.setBackground(new Color(153, 102, 204)); // Purple Relic color
        btnEventChest.setForeground(Color.WHITE);
        btnEventChest.addActionListener(e -> openEventChest());

        btnLockedChest = createStyledButton("🔒 Locked Vault (Requires Key)");
        btnLockedChest.setPreferredSize(new Dimension(250, 40));
        btnLockedChest.setBackground(new Color(217, 83, 79)); // Dark Red/Gold
        btnLockedChest.setForeground(Color.WHITE);
        btnLockedChest.addActionListener(e -> openLockedChest());

        btnDevilsChallenge = createStyledButton("Devil's Challenge 👿");
        btnDevilsChallenge.setPreferredSize(new Dimension(250, 40));
        btnDevilsChallenge.setBackground(new Color(180, 50, 50)); // Crimson Red
        btnDevilsChallenge.setForeground(Color.WHITE);
        btnDevilsChallenge.addActionListener(e -> showDevilsChallengeDialog());

        btnShop = createStyledButton("Merchant Shop 🛒");
        btnShop.setPreferredSize(new Dimension(250, 40));
        btnShop.setBackground(new Color(92, 184, 92)); // Green shop button
        btnShop.setForeground(Color.WHITE);
        btnShop.addActionListener(e -> showShopDialog());

        btnStatTrainer = createStyledButton("Stat Shrine ⛩️");
        btnStatTrainer.setPreferredSize(new Dimension(250, 40));
        btnStatTrainer.setBackground(new Color(91, 192, 222)); // Blue shrine button
        btnStatTrainer.setForeground(Color.BLACK);
        btnStatTrainer.addActionListener(e -> showStatTrainerDialog());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(eventTitleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(eventDescLabel, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(btnDropChest, gbc);
        panel.add(btnEventChest, gbc);
        panel.add(btnLockedChest, gbc);
        panel.add(btnDevilsChallenge, gbc);
        panel.add(btnShop, gbc);
        panel.add(btnStatTrainer, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        
        JButton btnManageInventory = createStyledButton("Manage Inventory");
        btnManageInventory.setPreferredSize(new Dimension(250, 40));
        btnManageInventory.addActionListener(e -> {
            previousCard = "Event";
            inventoryInteractive = true;
            refreshInventoryUI();
            cardLayout.show(cards, "Inventory");
        });
        panel.add(btnManageInventory, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnNext, gbc);

        return panel;
    }

    private void showShopDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Merchant Shop - Dungeon Trader", true);
        dialog.setSize(820, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(25, 25, 25));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("🧙 Merchant Shop");
        title.setForeground(new Color(240, 173, 78));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JLabel shopGoldLbl = new JLabel("Your Gold: " + playerGold + " 💰");
        shopGoldLbl.setForeground(new Color(255, 215, 0));
        shopGoldLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        shopGoldLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JPanel topContainer = new JPanel(new java.awt.GridLayout(2, 1, 0, 5));
        topContainer.setOpaque(false);
        topContainer.add(title);
        topContainer.add(shopGoldLbl);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Center split: Left = Shop Items, Right = Backpack Preview
        JPanel centerSplit = new JPanel(new java.awt.GridLayout(1, 2, 15, 0));
        centerSplit.setOpaque(false);

        // Left Panel: Shop Items
        JPanel itemsPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        itemsPanel.setOpaque(false);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(itemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)), "Goods for Sale",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));

        // Right Panel: Live Backpack Preview
        JPanel backpackPreviewPanel = new JPanel(new BorderLayout(0, 10));
        backpackPreviewPanel.setOpaque(false);
        backpackPreviewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)), "Your Backpack (12 Slots)",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));

        JPanel invGrid = new JPanel(new java.awt.GridLayout(4, 3, 5, 5));
        invGrid.setOpaque(false);
        JPanel[] shopInvSlotsUI = new JPanel[12];
        for (int i = 0; i < 12; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(40, 40, 40));
            slot.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
            shopInvSlotsUI[i] = slot;
            invGrid.add(slot);
        }
        renderShopBackpack(shopInvSlotsUI);
        backpackPreviewPanel.add(invGrid, BorderLayout.CENTER);

        // Procedurally Generated Shop Items (Random Gem Stats & Stat-Proportional Pricing)
        java.util.List<Object[]> goodsList = new java.util.ArrayList<>();
        
        // Standard Consumables (Increased economy prices & High Revive Cost)
        goodsList.add(new Object[]{"Clear Potion", Item.ItemType.CLEAR_POTION, 150, 1, "Xóa 5 hàng khối rác ở đáy"});
        goodsList.add(new Object[]{"Bomb", Item.ItemType.BOMB, 200, 1, "Gây 40 sát thương trực tiếp lên Boss"});
        goodsList.add(new Object[]{"Stat Potion", Item.ItemType.STAT_POTION, 250, 1, "+10 ATK & +5 DEF trong 20s"});
        goodsList.add(new Object[]{"Clear Debuff Potion", Item.ItemType.CLEAR_DEBUFF_POTION, 180, 1, "Xóa 1 debuff xấu nhất đang dính"});
        goodsList.add(new Object[]{"Dungeon Key", Item.ItemType.DUNGEON_KEY, 350, 1, "Dùng để mở Rương Khóa Hoàng Gia (Locked Chest)"});
        goodsList.add(new Object[]{"Revive Cross ✝️", Item.ItemType.REVIVE_CROSS, 1200 + level * 100, 1, "CỰC HIẾM: Tự động hồi sinh & xóa 70% hàng khối khi thua"});

        // Procedurally Generated Gems (Random Stats & Proportional High Pricing)
        String[] gemPrefixes = {"Common", "Rare", "Flawless", "Radiant", "Mythic"};
        Item.ItemType[] gemTypes = {Item.ItemType.GEM_ATK, Item.ItemType.GEM_DEF};
        
        for (int i = 0; i < 5; i++) {
            Item.ItemType type = gemTypes[random.nextInt(gemTypes.length)];
            int tier = random.nextInt(5); // 0..4
            String prefix = gemPrefixes[tier];
            int statVal = (tier + 1) * 3 + random.nextInt(4); // e.g. 3-6, 6-9, 9-12, 12-15, 15-18
            int price = 250 + (tier * 180) + (statVal * 30); // High price proportional to stat quality!
            
            String gemName = (type == Item.ItemType.GEM_ATK) ? prefix + " Ruby (+" + statVal + " ATK)" : prefix + " Sapphire (+" + statVal + " DEF)";
            String desc = (type == Item.ItemType.GEM_ATK) ? "Đá quý Tấn Công: +" + statVal + " ATK khi khảm" : "Đá quý Phòng Thủ: +" + statVal + " DEF khi khảm";
            goodsList.add(new Object[]{gemName, type, price, statVal, desc});
        }
        
        // Special Cursed High-Stat Gem
        int cursedStat = 18 + random.nextInt(7); // 18-24 ATK
        int cursedPrice = 1100 + cursedStat * 25;
        goodsList.add(new Object[]{"Cursed Blood Ruby (+" + cursedStat + " ATK)", Item.ItemType.GEM_ATK, cursedPrice, cursedStat, "High-Risk High-Reward: Tấn công cực lớn (+" + cursedStat + " ATK)"});

        for (Object[] item : goodsList) {
            String name = (String) item[0];
            Item.ItemType type = (Item.ItemType) item[1];
            int price = (Integer) item[2];
            int val = (Integer) item[3];
            String desc = (String) item[4];

            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.setBackground(new Color(40, 40, 40));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));

            JLabel infoLbl = new JLabel("<html><b style='color:#ffffff; font-size:11px;'>" + name + "</b> <span style='color:#ffd700;'>(" + price + "G)</span><br><span style='color:#aaaaaa; font-size:9px;'>" + desc + "</span></html>");
            row.add(infoLbl, BorderLayout.CENTER);

            JButton btnBuy = createStyledButton("Buy");
            btnBuy.setPreferredSize(new Dimension(60, 28));
            btnBuy.setFont(new Font("SansSerif", Font.BOLD, 11));
            btnBuy.addActionListener(evt -> {
                if (playerGold < price) {
                    javax.swing.JOptionPane.showMessageDialog(dialog, "Bạn không đủ Vàng!", "Không đủ tiền", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int freeIndex = -1;
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] == null) {
                        freeIndex = i;
                        break;
                    }
                }
                if (freeIndex == -1) {
                    javax.swing.JOptionPane.showMessageDialog(dialog, "Túi đồ của bạn đã đầy (12/12)!", "Túi đầy", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                playerGold -= price;
                inventory[freeIndex] = new Item(name, type, val);
                shopGoldLbl.setText("Your Gold: " + playerGold + " 💰");
                refreshInventoryUI();
                renderShopBackpack(shopInvSlotsUI);
                javax.swing.JOptionPane.showMessageDialog(dialog, "Đã mua " + name + " và thêm vào Túi đồ!", "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            });

            row.add(btnBuy, BorderLayout.EAST);
            itemsPanel.add(row);
        }

        centerSplit.add(scrollPane);
        centerSplit.add(backpackPreviewPanel);
        mainPanel.add(centerSplit, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);

        JButton btnSell = createStyledButton("Sell Items 💰");
        btnSell.setBackground(new Color(240, 173, 78));
        btnSell.setForeground(Color.BLACK);
        btnSell.addActionListener(evt -> {
            showSellItemsDialog(shopGoldLbl);
            renderShopBackpack(shopInvSlotsUI);
        });

        JButton btnClose = createStyledButton("Close Shop");
        btnClose.addActionListener(evt -> {
            dialog.dispose();
            btnShop.setVisible(false);
            btnNext.setVisible(true);
            eventDescLabel.setText("You finished shopping with the Merchant.");
        });

        bottomPanel.add(btnSell);
        bottomPanel.add(btnClose);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void renderShopBackpack(JPanel[] shopInvSlotsUI) {
        for (int i = 0; i < 12; i++) {
            shopInvSlotsUI[i].removeAll();
            if (inventory[i] != null) {
                JLabel lbl = new JLabel("<html><div style='text-align: center; color: #ffffff; font-size:9px;'>" + inventory[i].getName().replace(" ", "<br>") + "</div></html>");
                lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                shopInvSlotsUI[i].add(lbl, BorderLayout.CENTER);
                shopInvSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(91, 192, 222), 1));
            } else {
                shopInvSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
            }
            shopInvSlotsUI[i].revalidate();
            shopInvSlotsUI[i].repaint();
        }
    }

    private void showSellItemsDialog(JLabel shopGoldLbl) {
        javax.swing.JDialog sellDialog = new javax.swing.JDialog(this, "Sell Items to Merchant", true);
        sellDialog.setSize(500, 380);
        sellDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Click an item to sell it for Gold 💰");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel invGrid = new JPanel(new java.awt.GridLayout(2, 6, 5, 5));
        invGrid.setOpaque(false);
        JPanel[] sellSlotsUI = new JPanel[12];

        for (int i = 0; i < 12; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            sellSlotsUI[i] = slot;
            final int index = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (inventory[index] != null) {
                        Item itemToSell = inventory[index];
                        int sellPrice = 75; // default sell price
                        switch (itemToSell.getType()) {
                            case BOMB: sellPrice = 100; break;
                            case STAT_POTION: sellPrice = 125; break;
                            case CLEAR_DEBUFF_POTION: sellPrice = 90; break;
                            case DUNGEON_KEY: sellPrice = 175; break;
                            case REVIVE_CROSS: sellPrice = 600; break; // Sells for 600G!
                            case GEM_ATK: 
                            case GEM_DEF: 
                            case GEM_HP: 
                                int statVal = itemToSell.getValue() > 0 ? itemToSell.getValue() : 3;
                                sellPrice = 120 + statVal * 15; // Gems sell for high price based on stat quality!
                                break;
                            default: sellPrice = 75; break;
                        }

                        int choice = javax.swing.JOptionPane.showConfirmDialog(sellDialog, 
                            "Bán " + itemToSell.getName() + " với giá " + sellPrice + " Gold?", 
                            "Bán đồ", javax.swing.JOptionPane.YES_NO_OPTION);

                        if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            playerGold += sellPrice;
                            inventory[index] = null;
                            if (shopGoldLbl != null) shopGoldLbl.setText("Your Gold: " + playerGold + " 💰");
                            refreshInventoryUI();
                            renderSellGrid(sellSlotsUI);
                        }
                    }
                }
            });
            invGrid.add(slot);
        }

        renderSellGrid(sellSlotsUI);
        panel.add(invGrid, BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Done Selling");
        btnClose.addActionListener(e -> sellDialog.dispose());
        panel.add(btnClose, BorderLayout.SOUTH);

        sellDialog.add(panel);
        sellDialog.setVisible(true);
    }

    private void renderSellGrid(JPanel[] sellSlotsUI) {
        for (int i = 0; i < 12; i++) {
            sellSlotsUI[i].removeAll();
            if (inventory[i] != null) {
                JLabel lbl = new JLabel("<html><div style='text-align: center; color: #ffffff;'>" + inventory[i].getName().replace(" ", "<br>") + "</div></html>");
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                sellSlotsUI[i].add(lbl, BorderLayout.CENTER);
                sellSlotsUI[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
            } else {
                sellSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
            }
            sellSlotsUI[i].revalidate();
            sellSlotsUI[i].repaint();
        }
    }

    private void showStatTrainerDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Stat Shrine - Upgrade Stats", true);
        dialog.setSize(800, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(25, 25, 25));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("⛩️ Stat Shrine (Nâng Cấp Chỉ Số)");
        title.setForeground(new Color(91, 192, 222));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JLabel shrineGoldLbl = new JLabel("Your Gold: " + playerGold + " 💰");
        shrineGoldLbl.setForeground(new Color(255, 215, 0));
        shrineGoldLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        shrineGoldLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JPanel topContainer = new JPanel(new java.awt.GridLayout(2, 1, 0, 5));
        topContainer.setOpaque(false);
        topContainer.add(title);
        topContainer.add(shrineGoldLbl);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Center split: Left = Live Stats Panel, Right = Stat Upgrades
        JPanel centerSplit = new JPanel(new java.awt.GridLayout(1, 2, 15, 0));
        centerSplit.setOpaque(false);

        // Left Panel: Current Stats
        JPanel liveStatsPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        liveStatsPanel.setOpaque(false);
        liveStatsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)), "Your Current Stats",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));
        renderLiveShrineStats(liveStatsPanel);

        // Right Panel: Stat Upgrades list
        JPanel upgradesPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        upgradesPanel.setOpaque(false);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(upgradesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)), "Available Upgrades",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));

        Object[][] statUpgrades = {
            {"ATK Upgrade (+2 ATK)", "ATK", 75, "Tăng vĩnh viễn 2 sát thương tấn công"},
            {"DEF Upgrade (+2 DEF)", "DEF", 75, "Tăng vĩnh viễn 2 phòng thủ giảm block rác"},
            {"CRIT CHANCE (+3%)", "CRIT", 100, "Tăng vĩnh viễn 3% tỉ lệ nổ chí mạng"},
            {"CRIT DMG (+25%)", "CRIT_DMG", 100, "Tăng vĩnh viễn 25% sát thương chí mạng"},
            {"EVADE (+5%)", "EVADE", 120, "Tăng vĩnh viễn 5% tỉ lệ né đòn boss"},
            {"DEBUFF RESIST (+5%)", "RESIST", 120, "Tăng vĩnh viễn 5% tỉ lệ kháng debuff"}
        };

        for (Object[] up : statUpgrades) {
            String name = (String) up[0];
            String statType = (String) up[1];
            int price = (Integer) up[2];
            String desc = (String) up[3];

            JPanel row = new JPanel(new BorderLayout(5, 0));
            row.setBackground(new Color(40, 40, 40));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));

            JLabel infoLbl = new JLabel("<html><b style='color:#ffffff; font-size:11px;'>" + name + "</b> <span style='color:#ffd700;'>(" + price + "G)</span><br><span style='color:#aaaaaa; font-size:9px;'>" + desc + "</span></html>");
            row.add(infoLbl, BorderLayout.CENTER);

            JButton btnUpgrade = createStyledButton("Upgrade");
            btnUpgrade.setPreferredSize(new Dimension(65, 28));
            btnUpgrade.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnUpgrade.addActionListener(evt -> {
                if (playerGold < price) {
                    javax.swing.JOptionPane.showMessageDialog(dialog, "Bạn không đủ Vàng!", "Không đủ tiền", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                playerGold -= price;
                switch (statType) {
                    case "ATK": playerAtk += 2; break;
                    case "DEF": playerDef += 2; break;
                    case "CRIT": playerCritChance += 3; break;
                    case "CRIT_DMG": playerCritDmg += 25; break;
                    case "EVADE": playerEvade = Math.min(50, playerEvade + 5); break;
                    case "RESIST": playerDebuffResist = Math.min(80, playerDebuffResist + 5); break;
                }

                shrineGoldLbl.setText("Your Gold: " + playerGold + " 💰");
                refreshInventoryUI();
                renderLiveShrineStats(liveStatsPanel);
                javax.swing.JOptionPane.showMessageDialog(dialog, "Đã nâng cấp " + name + " thành công!", "Nâng cấp", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            });

            row.add(btnUpgrade, BorderLayout.EAST);
            upgradesPanel.add(row);
        }

        centerSplit.add(liveStatsPanel);
        centerSplit.add(scrollPane);
        mainPanel.add(centerSplit, BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Leave Shrine");
        btnClose.addActionListener(evt -> {
            dialog.dispose();
            btnStatTrainer.setVisible(false);
            btnNext.setVisible(true);
            eventDescLabel.setText("You left the Stat Shrine stronger than before.");
        });
        mainPanel.add(btnClose, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void renderLiveShrineStats(JPanel liveStatsPanel) {
        liveStatsPanel.removeAll();
        String totalAtkStr = playerAtk + (tempAtkBuff > 0 ? " (+" + tempAtkBuff + ")" : "");
        String totalDefStr = playerDef + (tempDefBuff > 0 ? " (+" + tempDefBuff + ")" : "");

        String[][] rows = {
            {"ATK", totalAtkStr},
            {"DEF", totalDefStr},
            {"LUCK", String.valueOf(playerLuck)},
            {"EVADE", playerEvade + "%"},
            {"CRIT", playerCritChance + "%"},
            {"CRIT DMG", playerCritDmg + "%"},
            {"RESIST", playerDebuffResist + "%"}
        };
        for (String[] r : rows) {
            JPanel p = new JPanel(new BorderLayout(5, 0));
            p.setOpaque(false);
            JLabel name = new JLabel(r[0]);
            name.setForeground(new Color(180, 210, 255));
            name.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel val = new JLabel(r[1]);
            val.setForeground(new Color(255, 220, 100));
            val.setFont(new Font("SansSerif", Font.BOLD, 12));
            p.add(name, BorderLayout.WEST);
            p.add(val, BorderLayout.EAST);
            liveStatsPanel.add(p);
        }
        liveStatsPanel.revalidate();
        liveStatsPanel.repaint();
    }

    private void recalculateEquipmentStats() {
        int baseAtk = 10;
        int baseDef = 5;
        int baseLuck = 5;

        for (Item g : weaponGems) {
            if (g != null) {
                int statVal = g.getValue() > 0 ? g.getValue() : 3;
                if (g.getType() == Item.ItemType.GEM_ATK) baseAtk += statVal;
                if (g.getType() == Item.ItemType.GEM_DEF) baseDef += Math.max(1, statVal / 2);
                if (g.getType() == Item.ItemType.GEM_HP) baseLuck += Math.max(1, statVal);
            }
        }
        for (Item g : armorGems) {
            if (g != null) {
                int statVal = g.getValue() > 0 ? g.getValue() : 2;
                if (g.getType() == Item.ItemType.GEM_DEF) baseDef += statVal;
                if (g.getType() == Item.ItemType.GEM_ATK) baseAtk += Math.max(1, statVal / 2);
                if (g.getType() == Item.ItemType.GEM_HP) baseLuck += Math.max(1, statVal);
            }
        }

        playerAtk = baseAtk;
        playerDef = baseDef;
        playerLuck = baseLuck;
    }

    private void showGemSocketDialog(String itemName) {
        boolean isWeapon = "Weapon".equalsIgnoreCase(itemName);
        Item[] targetGems = isWeapon ? weaponGems : armorGems;

        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "💎 Equipment Gem Sockets & Backpack - [" + itemName + "]", true);
        dialog.setSize(860, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 10));
        mainPanel.setBackground(new Color(25, 25, 35));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Title
        JLabel title = new JLabel(isWeapon ? "⚔️ WEAPON GEM SOCKETS (6 SLOTS)" : "🛡️ ARMOR GEM SOCKETS (6 SLOTS)");
        title.setForeground(new Color(240, 173, 78));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel infoSub = new JLabel("Click Gem in Backpack to Select → Click Socket to Attach | Click Socket with Gem to Unsocket");
        infoSub.setForeground(new Color(180, 210, 255));
        infoSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoSub.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel topHeader = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        topHeader.setOpaque(false);
        topHeader.add(title);
        topHeader.add(infoSub);
        mainPanel.add(topHeader, BorderLayout.NORTH);

        // Center Split: Left = Gem Sockets (6 Slots), Right = Live Backpack (12 Slots)
        JPanel centerSplit = new JPanel(new java.awt.GridLayout(1, 2, 15, 0));
        centerSplit.setOpaque(false);

        // Left Panel: Sockets & Equipment Visual (Iconic Circular Ring)
        JPanel socketsPanel = new JPanel(null); // Absolute layout for circular ring
        socketsPanel.setOpaque(false);
        socketsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(91, 192, 222)), "Equipment Sockets Ring",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13), new Color(91, 192, 222)
        ));

        // Center item icon
        JPanel centerItem = new JPanel(new BorderLayout());
        centerItem.setBackground(new Color(45, 45, 60));
        centerItem.setBounds(150, 135, 80, 80);
        centerItem.setBorder(BorderFactory.createLineBorder(new Color(240, 173, 78), 2));
        JLabel cLbl = new JLabel("<html><div style='text-align:center;'><b style='font-size:16px; color:#ffffff;'>" + (isWeapon ? "⚔️" : "🛡️") + "</b><br><b style='font-size:12px; color:#ffd700;'>" + itemName + "</b></div></html>");
        cLbl.setHorizontalAlignment(SwingConstants.CENTER);
        centerItem.add(cLbl, BorderLayout.CENTER);
        socketsPanel.add(centerItem);

        int radius = 110;
        int centerX = 190;
        int centerY = 175;

        JPanel[] socketSlotsUI = new JPanel[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90); // Start from top (-90 degrees)
            int x = centerX + (int)(radius * Math.cos(angle)) - 27;
            int y = centerY + (int)(radius * Math.sin(angle)) - 27;

            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(35, 35, 45));
            slot.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 50), 2, true));
            slot.setBounds(x, y, 56, 56);
            socketSlotsUI[i] = slot;
            socketsPanel.add(slot);
        }

        JLabel socketStatsSummary = new JLabel("Total Socket Stats: +0 ATK / +0 DEF");
        socketStatsSummary.setForeground(new Color(255, 215, 0));
        socketStatsSummary.setFont(new Font("SansSerif", Font.BOLD, 13));
        socketStatsSummary.setHorizontalAlignment(SwingConstants.CENTER);
        socketStatsSummary.setBounds(10, 310, 360, 25);
        socketsPanel.add(socketStatsSummary);

        // Right Panel: Live Backpack Inventory Grid
        JPanel backpackPanel = new JPanel(new BorderLayout(0, 10));
        backpackPanel.setOpaque(false);
        backpackPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 173, 78)), "Your Backpack Inventory (12 Slots)",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13), new Color(240, 173, 78)
        ));

        JPanel invGrid = new JPanel(new java.awt.GridLayout(4, 3, 6, 6));
        invGrid.setOpaque(false);
        invGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel[] backpackSlotsUI = new JPanel[12];
        for (int i = 0; i < 12; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(40, 40, 40));
            slot.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
            backpackSlotsUI[i] = slot;
            invGrid.add(slot);
        }

        backpackPanel.add(invGrid, BorderLayout.CENTER);

        // Helper renderers for live syncing
        Runnable renderAll = new Runnable() {
            @Override
            public void run() {
                // Calculate current socket stats
                int currentAtk = 0;
                int currentDef = 0;
                for (int i = 0; i < 6; i++) {
                    socketSlotsUI[i].removeAll();
                    if (targetGems[i] != null) {
                        Item gem = targetGems[i];
                        int val = gem.getValue() > 0 ? gem.getValue() : 3;
                        if (gem.getType() == Item.ItemType.GEM_ATK) currentAtk += val;
                        if (gem.getType() == Item.ItemType.GEM_DEF) currentDef += val;

                        JLabel lbl = new JLabel("<html><div style='text-align:center;'><b style='color:#5bc0de; font-size:12px;'>💎 Slot " + (i+1) + "</b><br><span style='color:#ffffff; font-size:10px;'>" + gem.getName() + "</span></div></html>");
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        socketSlotsUI[i].add(lbl, BorderLayout.CENTER);
                        socketSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(91, 192, 222), 2, true));
                    } else {
                        JLabel gemLbl = new JLabel("<html><div style='text-align:center; color:#888888;'><b style='font-size:13px;'>Slot " + (i+1) + "</b><br><span style='font-size:10px;'>(Trống)</span></div></html>");
                        gemLbl.setHorizontalAlignment(SwingConstants.CENTER);
                        socketSlotsUI[i].add(gemLbl, BorderLayout.CENTER);
                        socketSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true));
                    }
                    socketSlotsUI[i].revalidate();
                    socketSlotsUI[i].repaint();
                }

                socketStatsSummary.setText("Total Socket Stats: +" + currentAtk + " ATK / +" + currentDef + " DEF");

                // Render Backpack Slots
                for (int i = 0; i < 12; i++) {
                    backpackSlotsUI[i].removeAll();
                    if (inventory[i] != null) {
                        Item item = inventory[i];
                        boolean isGem = item.getType().name().startsWith("GEM_");
                        String colorHex = isGem ? "#ffd700" : "#ffffff";
                        String iconStr = isGem ? "💎 " : "";
                        JLabel lbl = new JLabel("<html><div style='text-align: center; color: " + colorHex + "; font-size:10px;'>" + iconStr + item.getName() + "</div></html>");
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        backpackSlotsUI[i].add(lbl, BorderLayout.CENTER);

                        if (selectedItem != null && inventory[i] == selectedItem) {
                            backpackSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2)); // Selected highlight
                        } else if (isGem) {
                            backpackSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(150, 150, 50), 1));
                        } else {
                            backpackSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));
                        }
                    } else {
                        backpackSlotsUI[i].setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 1));
                    }
                    backpackSlotsUI[i].revalidate();
                    backpackSlotsUI[i].repaint();
                }
            }
        };

        // Click listeners for Sockets
        for (int i = 0; i < 6; i++) {
            final int slotIdx = i;
            socketSlotsUI[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (selectedItem != null) {
                        // Socket selected item if it's a GEM
                        if (selectedItem.getType().name().startsWith("GEM_")) {
                            // Find selectedItem index in inventory
                            int invIdx = -1;
                            for (int k = 0; k < 12; k++) {
                                if (inventory[k] == selectedItem) {
                                    invIdx = k;
                                    break;
                                }
                            }
                            Item prevSocketGem = targetGems[slotIdx];
                            targetGems[slotIdx] = selectedItem;
                            if (invIdx != -1) {
                                inventory[invIdx] = prevSocketGem;
                            }
                            selectedItem = null;
                            recalculateEquipmentStats();
                            renderAll.run();
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(dialog, "Chỉ có thể gắn Đá Quý (Gem) vào ô này!", "Không đúng loại", javax.swing.JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        // Unsocket gem if socket contains one
                        if (targetGems[slotIdx] != null) {
                            int freeIndex = -1;
                            for (int k = 0; k < 12; k++) {
                                if (inventory[k] == null) {
                                    freeIndex = k;
                                    break;
                                }
                            }
                            if (freeIndex != -1) {
                                inventory[freeIndex] = targetGems[slotIdx];
                                targetGems[slotIdx] = null;
                                recalculateEquipmentStats();
                                renderAll.run();
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(dialog, "Túi đồ của bạn đã đầy, hãy dọn bớt ô túi để tháo đá!", "Túi đầy", javax.swing.JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            });
        }

        // Click listeners for Backpack items
        for (int i = 0; i < 12; i++) {
            final int invIdx = i;
            backpackSlotsUI[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (inventory[invIdx] != null) {
                        if (selectedItem == inventory[invIdx]) {
                            selectedItem = null; // Unselect
                        } else {
                            selectedItem = inventory[invIdx];
                        }
                    } else {
                        selectedItem = null;
                    }
                    renderAll.run();
                }
            });
        }

        renderAll.run(); // Initial render

        centerSplit.add(socketsPanel);
        centerSplit.add(backpackPanel);
        mainPanel.add(centerSplit, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setOpaque(false);

        JButton btnUnsocketAll = createStyledButton("Unsocket All Gems 🔄");
        btnUnsocketAll.setBackground(new Color(217, 83, 79));
        btnUnsocketAll.setForeground(Color.WHITE);
        btnUnsocketAll.addActionListener(e -> {
            for (int i = 0; i < 6; i++) {
                if (targetGems[i] != null) {
                    for (int k = 0; k < 12; k++) {
                        if (inventory[k] == null) {
                            inventory[k] = targetGems[i];
                            targetGems[i] = null;
                            break;
                        }
                    }
                }
            }
            recalculateEquipmentStats();
            renderAll.run();
        });

        JButton btnDone = createStyledButton("Save & Close ✅");
        btnDone.setPreferredSize(new Dimension(160, 36));
        btnDone.setBackground(new Color(92, 184, 92));
        btnDone.setForeground(Color.WHITE);
        btnDone.addActionListener(e -> {
            selectedItem = null;
            recalculateEquipmentStats();
            refreshInventoryUI();
            dialog.dispose();
        });

        bottomPanel.add(btnUnsocketAll);
        bottomPanel.add(btnDone);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("INVENTORY & STATS");
        title.setForeground(new Color(240, 173, 78));
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);



        // Stats Panel
        RoundedPanel statsPanel = new RoundedPanel(15);
        statsPanel.setBackground(new Color(35, 35, 35));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel statTitle = new JLabel("Player Stats");
        statTitle.setForeground(Color.WHITE);
        statTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        statsPanel.add(statTitle);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        String totalAtkStr = playerAtk + (tempAtkBuff > 0 ? " (+" + tempAtkBuff + ")" : "");
        String totalDefStr = playerDef + (tempDefBuff > 0 ? " (+" + tempDefBuff + ")" : "");

        String[][] statRows = {
            {" ATK",         totalAtkStr,                "(Sát thương mỗi hàng)"},
            {" DEF",         totalDefStr,                "(Giảm garbage lines nhận)"},
            {" LUCK",        String.valueOf(playerLuck), "(Tăng tỉ lệ rớt đồ hiếm)"},
            {" EVADE",       playerEvade + "%",          "(Tỉ lệ né đòn boss)"},
            {" CRIT",        playerCritChance + "%",     "(Tỉ lệ chí mạng)"},
            {" CRIT DMG",    playerCritDmg + "%",        "(Nhân sát thương crit)"},
            {" RESIST",      playerDebuffResist + "%",   "(Kháng debuff)"},
        };
        for (String[] row : statRows) {
            JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
            rowPanel.setOpaque(false);
            JLabel nameLbl = new JLabel(row[0]);
            nameLbl.setForeground(new Color(180, 210, 255));
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel valLbl = new JLabel(row[1]);
            valLbl.setForeground(new Color(255, 220, 100));
            valLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel descLbl = new JLabel(row[2]);
            descLbl.setForeground(new Color(130, 130, 130));
            descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            rowPanel.add(nameLbl, BorderLayout.WEST);
            rowPanel.add(valLbl, BorderLayout.CENTER);
            rowPanel.add(descLbl, BorderLayout.EAST);
            statsPanel.add(rowPanel);
            statsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        lblGold = new JLabel("Gold: " + playerGold);
        lblGold.setForeground(new Color(255, 215, 0));
        lblGold.setFont(new Font("SansSerif", Font.BOLD, 16));
        statsPanel.add(lblGold);
        
        // Equip Panel
        RoundedPanel equipPanel = new RoundedPanel(15);
        equipPanel.setBackground(new Color(35, 35, 35));
        equipPanel.setLayout(new BoxLayout(equipPanel, BoxLayout.Y_AXIS));
        equipPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel equipTitle = new JLabel("Equipment");
        equipTitle.setForeground(Color.WHITE);
        equipTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        equipTitle.setAlignmentX(CENTER_ALIGNMENT);
        equipPanel.add(equipTitle);
        equipPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        JPanel weaponSlot = new JPanel(new BorderLayout());
        weaponSlot.setPreferredSize(new Dimension(80, 80));
        weaponSlot.setMaximumSize(new Dimension(80, 80));
        weaponSlot.setBackground(new Color(50, 50, 50));
        weaponSlot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        JLabel wLbl = new JLabel("Weapon");
        wLbl.setForeground(Color.GRAY);
        wLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        weaponSlot.add(wLbl, BorderLayout.CENTER);
        weaponSlot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (inventoryInteractive) showGemSocketDialog("Weapon");
            }
        });
        
        JPanel armorSlot = new JPanel(new BorderLayout());
        armorSlot.setPreferredSize(new Dimension(80, 80));
        armorSlot.setMaximumSize(new Dimension(80, 80));
        armorSlot.setBackground(new Color(50, 50, 50));
        armorSlot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        JLabel aLbl = new JLabel("Armor");
        aLbl.setForeground(Color.GRAY);
        aLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        armorSlot.add(aLbl, BorderLayout.CENTER);
        armorSlot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (inventoryInteractive) showGemSocketDialog("Armor");
            }
        });
        
        equipPanel.add(weaponSlot);
        equipPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        equipPanel.add(armorSlot);
        
        // Backpack Panel
        RoundedPanel backpackPanel = new RoundedPanel(15);
        backpackPanel.setBackground(new Color(35, 35, 35));
        backpackPanel.setLayout(new BorderLayout(0, 10));
        backpackPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel backpackTitle = new JLabel("Backpack");
        backpackTitle.setForeground(Color.WHITE);
        backpackTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        backpackPanel.add(backpackTitle, BorderLayout.NORTH);
        
        JPanel gridPanel = new JPanel(new java.awt.GridLayout(2, 6, 5, 5));
        gridPanel.setOpaque(false);
        for (int i = 0; i < 12; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            backpackSlotsUI[i] = slot;
            final int index = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleInventoryClick(inventory, index, backpackSlotsUI);
                }
            });
            gridPanel.add(slot);
        }
        backpackPanel.add(gridPanel, BorderLayout.CENTER);

        // Consumables Panel
        RoundedPanel consumablesPanel = new RoundedPanel(15);
        consumablesPanel.setBackground(new Color(35, 35, 35));
        consumablesPanel.setLayout(new BoxLayout(consumablesPanel, BoxLayout.Y_AXIS));
        consumablesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel consTitle = new JLabel("Consumables");
        consTitle.setForeground(Color.WHITE);
        consTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        consTitle.setAlignmentX(CENTER_ALIGNMENT);
        consumablesPanel.add(consTitle);
        consumablesPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        for (int i = 0; i < 3; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setPreferredSize(new Dimension(60, 60));
            slot.setMaximumSize(new Dimension(60, 60));
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
            JLabel keyLbl = new JLabel("Key " + (i+1));
            keyLbl.setForeground(Color.GRAY);
            keyLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            slot.add(keyLbl, BorderLayout.CENTER);
            quickSlotsUI[i] = slot;
            final int index = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleInventoryClick(quickSlots, index, quickSlotsUI);
                }
            });
            
            consumablesPanel.add(slot);
            if (i < 2) consumablesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Trash Can Slot (Discards selected item)
        consumablesPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JPanel trashSlot = new JPanel(new BorderLayout());
        trashSlot.setPreferredSize(new Dimension(60, 50));
        trashSlot.setMaximumSize(new Dimension(60, 50));
        trashSlot.setBackground(new Color(50, 30, 30));
        trashSlot.setBorder(BorderFactory.createLineBorder(new Color(217, 83, 79), 2));
        JLabel trashLbl = new JLabel("<html><div style='text-align: center; color: #d9534f; font-weight: bold;'>TRASH</div></html>");
        trashLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trashSlot.add(trashLbl, BorderLayout.CENTER);
        trashSlot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (inventoryInteractive && selectedItem != null) {
                    selectedItem = null;
                    selectedSlotUI = null;
                    refreshInventoryUI();
                    javax.swing.JOptionPane.showMessageDialog(panel, "Đã vứt vật phẩm vào thùng rác!", "Đã vứt", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        consumablesPanel.add(trashSlot);
        
        JPanel topPanel = new JPanel(new java.awt.GridLayout(1, 3, 15, 0));
        topPanel.setOpaque(false);
        topPanel.add(statsPanel);
        topPanel.add(equipPanel);
        topPanel.add(consumablesPanel);

        JPanel mainLayout = new JPanel(new BorderLayout(0, 15));
        mainLayout.setOpaque(false);
        mainLayout.add(topPanel, BorderLayout.NORTH);
        mainLayout.add(backpackPanel, BorderLayout.CENTER);

        panel.add(mainLayout, BorderLayout.CENTER);

        JButton btnBack = createStyledButton("Close");
        btnBack.addActionListener(e -> {
            if (selectedItem != null) {
                // Try to find an empty slot in inventory to dump the item
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] == null) {
                        inventory[i] = selectedItem;
                        break;
                    }
                }
                selectedItem = null;
                selectedSlotUI = null;
                refreshInventoryUI();
            }
            cardLayout.show(cards, previousCard);
            if ("Game".equals(previousCard)) {
                if (board != null) {
                    // Do not unpause the game here, let the player press P to resume
                    javax.swing.SwingUtilities.invokeLater(() -> board.requestFocusInWindow());
                }
            }
        });
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(btnBack);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void handleInventoryClick(Item[] dataArray, int index, JPanel[] uiArray) {
        if (!inventoryInteractive) return;
        if (selectedItem == null) {
            if (dataArray[index] != null) {
                selectedItem = dataArray[index];
                dataArray[index] = null;
                selectedSlotUI = uiArray[index];
            }
        } else {
            Item temp = dataArray[index];
            dataArray[index] = selectedItem;
            selectedItem = (temp != null) ? temp : null;
            selectedSlotUI = (temp != null) ? uiArray[index] : null;
        }
        refreshInventoryUI();
    }

    private void refreshInventoryUI() {
        if (lblGold != null) {
            lblGold.setText("Gold: " + playerGold);
        }
        for (int i = 0; i < 12; i++) {
            JPanel slot = backpackSlotsUI[i];
            if (slot != null) {
                slot.removeAll();
                if (inventory[i] != null) {
                    JLabel lbl = new JLabel("<html><div style='text-align: center;'>" + inventory[i].getName().replace(" ", "<br>") + "</div></html>");
                    lbl.setForeground(Color.WHITE);
                    lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    slot.add(lbl, BorderLayout.CENTER);
                }
                slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80))); // Default
                slot.revalidate();
                slot.repaint();
            }
        }
        for (int i = 0; i < 3; i++) {
            JPanel slot = quickSlotsUI[i];
            if (slot != null) {
                slot.removeAll();
                if (quickSlots[i] != null) {
                    JLabel lbl = new JLabel("<html><div style='text-align: center;'>" + quickSlots[i].getName().replace(" ", "<br>") + "</div></html>");
                    lbl.setForeground(Color.WHITE);
                    lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    slot.add(lbl, BorderLayout.CENTER);
                } else {
                    JLabel keyLbl = new JLabel("Key " + (i+1));
                    keyLbl.setForeground(Color.GRAY);
                    keyLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    slot.add(keyLbl, BorderLayout.CENTER);
                }
                slot.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100))); // Default
                slot.revalidate();
                slot.repaint();
            }
        }
        
        if (selectedItem != null && selectedSlotUI != null) {
            selectedSlotUI.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        }
        
        // Sync game HUD slots
        for (int i = 0; i < 3; i++) {
            JPanel hudSlot = gameQuickSlotsUI[i];
            if (hudSlot == null) continue;
            // Get the itemName label (CENTER child)
            java.awt.Component[] children = hudSlot.getComponents();
            JLabel itemNameLbl = null;
            for (java.awt.Component c : children) {
                if (c instanceof JLabel && ((JLabel) c).getText() != null) {
                    String txt = ((JLabel) c).getText();
                    if (!txt.startsWith("[")) { itemNameLbl = (JLabel) c; break; }
                }
            }
            if (itemNameLbl == null) continue;
            if (quickSlots[i] != null) {
                itemNameLbl.setText(quickSlots[i].getName());
                itemNameLbl.setForeground(Color.WHITE);
                hudSlot.setBackground(new Color(40, 55, 40));
                hudSlot.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 100)));
            } else {
                itemNameLbl.setText("Empty");
                itemNameLbl.setForeground(Color.GRAY);
                hudSlot.setBackground(new Color(40, 40, 40));
                hudSlot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            }
            hudSlot.revalidate();
            hudSlot.repaint();
        }
    }

    private JPanel createGamePanel() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(new Color(25, 25, 25));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel
        RoundedPanel topPanel = new RoundedPanel(15, new BorderLayout());
        topPanel.setBackground(new Color(30, 30, 30));
        topPanel.setPreferredSize(new Dimension(800, 80));
        
        JLabel playerFx = new JLabel("<html><div style='text-align: center;'><span style='color: #5bc0de; font-family: sans-serif; font-weight: bold;'>[ YOU ]</span><br><span style='color: #888888; font-family: sans-serif;'>Player</span></div></html>");
        playerFx.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel arrow = new JLabel("→");
        arrow.setForeground(new Color(150, 150, 150));
        arrow.setFont(new Font("SansSerif", Font.BOLD, 24));
        arrow.setHorizontalAlignment(SwingConstants.CENTER);
        
        bossAnimLabel = new JLabel();
        bossAnimLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        topPanel.add(playerFx, BorderLayout.WEST);
        topPanel.add(arrow, BorderLayout.CENTER);
        topPanel.add(bossAnimLabel, BorderLayout.EAST);
        
        container.add(topPanel, BorderLayout.NORTH);

        // Center Panel container
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.weighty = 1.0;

        // Left Panel (Next Block & Controls)
        RoundedPanel leftPanel = new RoundedPanel(15);
        leftPanel.setBackground(new Color(30, 30, 30));
        leftPanel.setPreferredSize(new Dimension(150, 400));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        JLabel nextBlockLabel = new JLabel("Next block");
        nextBlockLabel.setForeground(new Color(200, 200, 200));
        nextBlockLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nextBlockLabel.setAlignmentX(CENTER_ALIGNMENT);
        nextBlockLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        nextBlockPreview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentNextPiece != null && currentNextPiece.getShape() != Shape.Tetrominoe.NoShape && !hideNextPiece) {
                    int sqSize = 18;
                    int offsetX = getWidth() / 2 - sqSize / 2;
                    int offsetY = getHeight() / 2 - sqSize / 2;
                    for (int i = 0; i < 4; i++) {
                        int x = currentNextPiece.x(i);
                        int y = -currentNextPiece.y(i);
                        drawSquare(g, offsetX + x * sqSize, offsetY + y * sqSize, currentNextPiece.getShape(), sqSize);
                    }
                }
            }
        };
        nextBlockPreview.setOpaque(false);
        nextBlockPreview.setPreferredSize(new Dimension(80, 80));
        nextBlockPreview.setMaximumSize(new Dimension(80, 80));
        
        JLabel holdBlockLabel = new JLabel("Hold block [V]");
        holdBlockLabel.setForeground(new Color(200, 200, 200));
        holdBlockLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        holdBlockLabel.setAlignmentX(CENTER_ALIGNMENT);
        holdBlockLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        holdBlockPreview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentHoldPiece != null && currentHoldPiece.getShape() != Shape.Tetrominoe.NoShape) {
                    int sqSize = 18;
                    int offsetX = getWidth() / 2 - sqSize / 2;
                    int offsetY = getHeight() / 2 - sqSize / 2;
                    for (int i = 0; i < 4; i++) {
                        int x = currentHoldPiece.x(i);
                        int y = -currentHoldPiece.y(i);
                        drawSquare(g, offsetX + x * sqSize, offsetY + y * sqSize, currentHoldPiece.getShape(), sqSize);
                    }
                }
            }
        };
        holdBlockPreview.setOpaque(false);
        holdBlockPreview.setPreferredSize(new Dimension(80, 80));
        holdBlockPreview.setMaximumSize(new Dimension(80, 80));
        
        leftPanel.add(nextBlockLabel);
        leftPanel.add(nextBlockPreview);
        leftPanel.add(holdBlockLabel);
        leftPanel.add(holdBlockPreview);
        
        // Quick Slots UI (in-game HUD)
        JLabel quickSlotsLabel = new JLabel("Items (Z, X, C)");
        quickSlotsLabel.setForeground(new Color(200, 200, 200));
        quickSlotsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        quickSlotsLabel.setAlignmentX(CENTER_ALIGNMENT);
        quickSlotsLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        
        JPanel quickSlotsPanel = new JPanel(new java.awt.GridLayout(3, 1, 0, 4));
        quickSlotsPanel.setOpaque(false);
        quickSlotsPanel.setMaximumSize(new Dimension(130, 120));
        String[] quickKeyNames = {"[Z]", "[X]", "[C]"};
        for (int i = 0; i < 3; i++) {
            JPanel slot = new JPanel(new BorderLayout(4, 0));
            slot.setPreferredSize(new Dimension(130, 36));
            slot.setBackground(new Color(40, 40, 40));
            slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            
            JLabel keyBadge = new JLabel(quickKeyNames[i]);
            keyBadge.setForeground(new Color(240, 173, 78));
            keyBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
            keyBadge.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            
            JLabel itemName = new JLabel("Empty");
            itemName.setForeground(Color.GRAY);
            itemName.setFont(new Font("SansSerif", Font.PLAIN, 11));
            
            slot.add(keyBadge, BorderLayout.WEST);
            slot.add(itemName, BorderLayout.CENTER);
            
            gameQuickSlotsUI[i] = slot;
            quickSlotsPanel.add(slot);
        }
        
        leftPanel.add(quickSlotsLabel);
        leftPanel.add(quickSlotsPanel);
        
        leftPanel.add(Box.createVerticalGlue());
        
        JButton btnInventory = createStyledButton("Inventory (I)");
        btnInventory.setAlignmentX(CENTER_ALIGNMENT);
        btnInventory.addActionListener(e -> {
            previousCard = "Game";
            inventoryInteractive = false;
            refreshInventoryUI();
            cardLayout.show(cards, "Inventory");
            if (board != null && !board.isPaused()) {
                board.togglePause();
            }
        });
        
        JButton btnMenu = createStyledButton("Flee (Menu)");
        btnMenu.setAlignmentX(CENTER_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            cardLayout.show(cards, "Home");
            if (bossTimer != null) bossTimer.stop();
        });
        
        leftPanel.add(btnInventory);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(btnMenu);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        gbc.gridx = 0;
        gbc.weightx = 0.2;
        centerContainer.add(leftPanel, gbc);

        // Middle Panel (Game Board)
        RoundedPanel middlePanel = new RoundedPanel(15, new BorderLayout());
        middlePanel.setBackground(new Color(15, 15, 15));
        middlePanel.setPreferredSize(new Dimension(350, 400));
        middlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        board = new Board(this);
        board.setBackground(new Color(15, 15, 15));
        middlePanel.add(board, BorderLayout.CENTER);
        
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        centerContainer.add(middlePanel, gbc);

        // Right Panel (Score & Enemy)
        RoundedPanel rightPanel = new RoundedPanel(15);
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setPreferredSize(new Dimension(200, 400));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        levelLabel = new JLabel("Stage: 1");
        levelLabel.setForeground(new Color(91, 192, 222));
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel scoreLabel = new JLabel("Time Survived");
        scoreLabel.setForeground(new Color(180, 180, 180));
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        scoreValueLabel = new JLabel("00:00");
        scoreValueLabel.setForeground(Color.WHITE);
        scoreValueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel bossHpLabel = new JLabel("Enemy HP");
        bossHpLabel.setForeground(new Color(180, 180, 180));
        bossHpLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        bossHpBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(20, 20, 20));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(new Color(217, 83, 79));
                double ratio = maxEnemyHp > 0 ? (enemyHp / (double) maxEnemyHp) : 0;
                int w = (int) (getWidth() * Math.max(0, ratio));
                g.fillRoundRect(0, 0, w, getHeight(), 10, 10);
            }
        };
        bossHpBar.setPreferredSize(new Dimension(150, 12));
        bossHpBar.setMaximumSize(new Dimension(150, 12));
        bossHpBar.setOpaque(false);

        JLabel attackTimerLabel = new JLabel("Enemy Attack");
        attackTimerLabel.setForeground(new Color(180, 180, 180));
        attackTimerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        attackTimerBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(20, 20, 20));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(new Color(240, 173, 78));
                double ratio = maxAttackTimer > 0 ? (attackTimer / (double) maxAttackTimer) : 0;
                int w = (int) (getWidth() * Math.max(0, ratio));
                g.fillRoundRect(0, 0, w, getHeight(), 10, 10);
            }
        };
        attackTimerBar.setPreferredSize(new Dimension(150, 12));
        attackTimerBar.setMaximumSize(new Dimension(150, 12));
        attackTimerBar.setOpaque(false);

        levelLabel.setAlignmentX(LEFT_ALIGNMENT);
        scoreLabel.setAlignmentX(LEFT_ALIGNMENT);
        scoreValueLabel.setAlignmentX(LEFT_ALIGNMENT);
        bossHpLabel.setAlignmentX(LEFT_ALIGNMENT);
        bossHpBar.setAlignmentX(LEFT_ALIGNMENT);
        attackTimerLabel.setAlignmentX(LEFT_ALIGNMENT);
        attackTimerBar.setAlignmentX(LEFT_ALIGNMENT);

        rightPanel.add(levelLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(scoreLabel);
        rightPanel.add(scoreValueLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(bossHpLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(bossHpBar);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(attackTimerLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(attackTimerBar);

        skillStatusLabel = new JLabel("");
        skillStatusLabel.setForeground(new Color(217, 83, 79));
        skillStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        skillStatusLabel.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(skillStatusLabel);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        centerContainer.add(rightPanel, gbc);
        
        container.add(centerContainer, BorderLayout.CENTER);
        
        return container;
    }
    
    private Timer gameRunTimer;
    private int timeElapsed = 0;
    
    private String previousCard = "Home";
    private boolean inventoryInteractive = false;

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        return btn;
    }

    private void resetGameMechanics() {
        if (isBossRushMode) {
            level = 5 + (bossRushCount * 5);
        } else {
            level = 1;
        }
        timeElapsed = 0;

        // Reset Player Stats to default starting values
        playerAtk = 10;
        playerDef = 2;
        playerLuck = 5;
        playerEvade = 5;
        playerCritChance = 5;
        playerCritDmg = 150;
        playerDebuffResist = 0;
        tempAtkBuff = 0;
        tempDefBuff = 0;
        playerGold = 100;

        // Reset Inventory & Quickslots
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = null;
        }
        for (int i = 0; i < quickSlots.length; i++) {
            quickSlots[i] = null;
        }
        // Starting item in quickslot 1 for a fresh run
        quickSlots[0] = new Item("Clear Potion", Item.ItemType.CLEAR_POTION, 1);

        // Reset Equipment Socket Gems
        for (int i = 0; i < weaponGems.length; i++) {
            weaponGems[i] = null;
        }
        for (int i = 0; i < armorGems.length; i++) {
            armorGems[i] = null;
        }

        // Reset Debuffs & Selections
        if (activeDebuffs != null) activeDebuffs.clear();
        hideNextPiece = false;
        selectedItem = null;
        selectedSlotUI = null;

        refreshInventoryUI();
        updateLevelMechanics();
        scoreValueLabel.setText("00:00");
        
        if (gameRunTimer != null) gameRunTimer.stop();
        gameRunTimer = new Timer(1000, e -> {
            if (board != null && !board.isPaused() && board.isStarted() && !board.isEventPaused()) {
                timeElapsed++;
                int min = timeElapsed / 60;
                int sec = timeElapsed % 60;
                scoreValueLabel.setText(String.format("%02d:%02d", min, sec));
            }
        });
        gameRunTimer.start();
    }
    
    private void updateLevelMechanics() {
        boolean isBoss = isBossRushMode || (level % 5 == 0);
        
        if (isBossRushMode) {
            level = 5 + (bossRushCount * 5);
            levelLabel.setText("👑 RUSH - Boss #" + (bossRushCount + 1));
            bossAnimLabel.setText("<html><div style='text-align: center;'><span style='font-size: 18px; font-weight: bold; color: #ff4444;'>👑 [ RUSH BOSS ]</span><br><span style='color: #ffd700; font-family: sans-serif;'>Tier " + (bossRushCount + 1) + " Overlord</span></div></html>");
            maxEnemyHp = 150 + (bossRushCount * 40);
        } else if (isBoss) {
            levelLabel.setText("Stage: " + level + " (BOSS)");
            bossAnimLabel.setText("<html><div style='text-align: center;'><span style='font-size: 20px; font-weight: bold; color: #d9534f;'>[ ENEMY ]</span><br><span style='color: #d9534f; font-family: sans-serif;'>BOSS</span></div></html>");
            maxEnemyHp = 100 + (level * 20); // Boss has more HP
        } else {
            levelLabel.setText("Stage: " + level);
            bossAnimLabel.setText("<html><div style='text-align: center;'><span style='font-size: 16px; font-weight: bold; color: #f0ad4e;'>[ ENEMY ]</span><br><span style='color: #888888; font-family: sans-serif;'>Monster</span></div></html>");
            maxEnemyHp = 30 + (level * 10); // Monsters have less HP
        }
        
        enemyHp = maxEnemyHp;
        attackTimer = 0;
        int bossTier = (level - 1) / 5;
        maxAttackTimer = Math.max(50, 150 - (bossTier * 20)); 
        
        bossHpBar.repaint();
        attackTimerBar.repaint();
        
        if (board != null) {
            board.updateSpeed(level);
        }

        if (bossTimer != null) bossTimer.stop();
        if (bossSkillTimer != null) bossSkillTimer.stop();
        
        clearAllDebuffs();
        
        bossTimer = new Timer(100, e -> {
            if (!board.isPaused() && board.isStarted() && !board.isEventPaused()) {
                attackTimer++;
                if (attackTimer >= maxAttackTimer) {
                    attackTimer = 0;
                    board.addGarbageLine();
                    for (int i = 0; i < bonusGarbageLines; i++) {
                        board.addGarbageLine();
                    }
                    bonusGarbageLines = 0; // reset after cast
                }
                attackTimerBar.repaint();
            }
        });
        bossTimer.start();
        
        if (isBoss) {
            bossSkillInterval = Math.max(2000, 10000 - (bossTier * 1000));
            bossSkillTimer = new Timer(bossSkillInterval, e -> {
                if (!board.isPaused() && board.isStarted() && !board.isEventPaused()) {
                    castBossSkill(bossTier);
                }
            });
            bossSkillTimer.start();
        }
    }

    private void clearAllDebuffs() {
        activeDebuffs.clear();
        hideNextPiece = false;
        bonusGarbageLines = 0;
        updateDebuffStatus("");
        if (nextBlockPreview != null) nextBlockPreview.repaint();
        if (board != null) {
            board.setHideGhostPiece(false);
            board.setInverseControls(false);
            board.setNoRotateCount(0);
            board.setSpeedDebuffActive(false);
            board.setMonoBlock(false, Shape.Tetrominoe.NoShape);
        }
    }
    
    private void clearOldestDebuff() {
        if (activeDebuffs.isEmpty()) return;
        String oldest = activeDebuffs.poll(); // removes head (oldest)
        switch (oldest) {
            case "BLIND":
                hideNextPiece = false;
                if (nextBlockPreview != null) nextBlockPreview.repaint();
                break;
            case "NO_SHADOW":
                board.setHideGhostPiece(false);
                break;
            case "NO_ROTATE":
                board.setNoRotateCount(0);
                break;
            case "INVERSE":
                board.setInverseControls(false);
                break;
            case "SPEED_DROP":
                board.setSpeedDebuffActive(false);
                break;
            case "SQUARE_RAIN":
                board.setMonoBlock(false, Shape.Tetrominoe.NoShape);
                break;
            case "ATK_UP":
                bonusGarbageLines = 0;
                break;
        }
        updateDebuffStatus(activeDebuffs.isEmpty() ? "" : String.join(",", activeDebuffs));
    }

    private void castBossSkill(int bossTier) {
        clearAllDebuffs();
        // 9 total skills now
        int skillOptions = Math.min(2 + bossTier, 9);
        int skill = random.nextInt(skillOptions);
        
        switch(skill) {
            case 0:
                hideNextPiece = true;
                activeDebuffs.add("BLIND");
                updateDebuffStatus("Blind (Hide Next)");
                if (nextBlockPreview != null) nextBlockPreview.repaint();
                break;
            case 1:
                updateDebuffStatus("Haste (Boss Fast Attack)");
                attackTimer = Math.max(attackTimer, maxAttackTimer - 20);
                break;
            case 2:
                board.setHideGhostPiece(true);
                activeDebuffs.add("NO_SHADOW");
                updateDebuffStatus("No Shadow");
                break;
            case 3:
                board.setNoRotateCount(3 + bossTier);
                activeDebuffs.add("NO_ROTATE");
                updateDebuffStatus("No Rotate (" + (3 + bossTier) + ")");
                break;
            case 4:
                updateDebuffStatus("Boss Heal");
                enemyHp = Math.min(maxEnemyHp, enemyHp + 30);
                bossHpBar.repaint();
                break;
            case 5:
                updateDebuffStatus("Inverse Controls!");
                board.setInverseControls(true);
                activeDebuffs.add("INVERSE");
                break;
            case 6:
                board.setSpeedDebuffActive(true);
                activeDebuffs.add("SPEED_DROP");
                updateDebuffStatus("Speed Drop!");
                break;
            case 7:
                board.setMonoBlock(true, Shape.Tetrominoe.SquareShape);
                activeDebuffs.add("SQUARE_RAIN");
                updateDebuffStatus("Square Rain");
                break;
            case 8:
                bonusGarbageLines = 2 + bossTier;
                activeDebuffs.add("ATK_UP");
                updateDebuffStatus("ATK UP (Bonus Garbage)");
                break;
        }
        
        Timer debuffResetTimer = new Timer(bossSkillInterval / 2, evt -> {
            clearAllDebuffs();
        });
        debuffResetTimer.setRepeats(false);
        debuffResetTimer.start();
    }

    public void updateScoreAndDamageBoss(int score, int linesCleared) {
        scoreValueLabel.setText(String.format("%,d", score));
        
        int totalAtk = playerAtk + tempAtkBuff;
        boolean isCrit = (random.nextInt(100) < playerCritChance);
        double critMult = isCrit ? (playerCritDmg / 100.0) : 1.0;
        int damage = (int) (linesCleared * totalAtk * critMult);
        
        enemyHp -= damage;
        
        // Delay boss attack when hit
        attackTimer = Math.max(0, attackTimer - damage);
        if (attackTimerBar != null) attackTimerBar.repaint();
        
        if (enemyHp <= 0) {
            enemyHp = 0;
            bossHpBar.repaint();
            handleStageCleared();
        } else {
            bossHpBar.repaint();
        }
    }

    private Item selectedItem = null;
    private JPanel selectedSlotUI = null;

    private void openDropChest() {
        boolean isBossStage = (level % 5 == 0);
        Item[] chestLoot = new Item[9];
        
        int minItems = isBossStage ? 5 : 2;
        int numItems = minItems + random.nextInt(isBossStage ? 5 : 4);
        
        Item.ItemType[] normalLootTypes = {
            Item.ItemType.CLEAR_POTION,
            Item.ItemType.BOMB,
            Item.ItemType.STAT_POTION,
            Item.ItemType.CLEAR_DEBUFF_POTION,
            Item.ItemType.GOLD
        };
        
        Item.ItemType[] rareLootTypes = {
            Item.ItemType.GEM_ATK,
            Item.ItemType.GEM_DEF,
            Item.ItemType.GEM_HP,
            Item.ItemType.REVIVE_CROSS,
            Item.ItemType.RANDOM_STAT_POTION,
            Item.ItemType.DUNGEON_KEY,
            Item.ItemType.GOLD
        };

        int goldAmount = isBossStage ? (250 + level * 25 + random.nextInt(100)) : (50 + level * 10 + random.nextInt(50));
        chestLoot[0] = new Item("GOLD", Item.ItemType.GOLD, goldAmount);
        
        boolean isJackpot = (random.nextInt(100) < (5 + playerLuck * 2));
        
        for (int i = 1; i < numItems; i++) {
            int slot = random.nextInt(9);
            while (chestLoot[slot] != null) {
                slot = (slot + 1) % 9;
            }
            
            Item.ItemType[] pool = (isBossStage || isJackpot) ? rareLootTypes : normalLootTypes;
            Item.ItemType lootType = pool[random.nextInt(pool.length)];
            int val = (lootType == Item.ItemType.GOLD) ? (30 + level * 5 + random.nextInt(50)) : 1;
            chestLoot[slot] = new Item(lootType.toString().replace("_", " "), lootType, val);
        }
        
        showChestLootDialog(chestLoot, false);
    }

    private void openEventChest() {
        // 25% chance of Mimic Trap
        boolean isMimic = (random.nextInt(100) < 25);
        if (isMimic) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "MIMIC TRAP! Rương giả biến thành Quái vật cắn bạn!\nĐã bị phạt thêm 6 hàng khối rác lên bàn cờ!", 
                "BẪY MIMIC", javax.swing.JOptionPane.ERROR_MESSAGE);
            for (int i = 0; i < 6; i++) {
                board.addGarbageLine();
            }
            btnEventChest.setVisible(false);
            btnNext.setVisible(true);
            eventDescLabel.setText("The Mimic attacked and fled! Proceed deeper.");
            return;
        }

        Item[] chestLoot = new Item[9];
        chestLoot[0] = new Item("GOLD VAULT", Item.ItemType.GOLD, 300 + level * 30);
        chestLoot[1] = new Item("Relic Gem", Item.ItemType.GEM_ATK, 1);
        chestLoot[2] = new Item("Ancient Shield", Item.ItemType.GEM_DEF, 1);
        chestLoot[4] = new Item("Revive Cross", Item.ItemType.REVIVE_CROSS, 1);
        chestLoot[6] = new Item("Mystery Potion", Item.ItemType.RANDOM_STAT_POTION, 1);
        
        showChestLootDialog(chestLoot, true);
    }

    private void openLockedChest() {
        int keySlot = -1;
        for (int i = 0; i < 3; i++) {
            if (quickSlots[i] != null && quickSlots[i].getType() == Item.ItemType.DUNGEON_KEY) {
                keySlot = i;
                break;
            }
        }
        int invKeySlot = -1;
        if (keySlot == -1) {
            for (int i = 0; i < 12; i++) {
                if (inventory[i] != null && inventory[i].getType() == Item.ItemType.DUNGEON_KEY) {
                    invKeySlot = i;
                    break;
                }
            }
        }

        if (keySlot == -1 && invKeySlot == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Rương Hoàng Gia này bị khóa chặt!\nBạn cần có [Dungeon Key] trong Phím tắt hoặc Túi đồ để mở.\n(Có thể tìm chìa khóa từ rương quái hoặc mua ở Merchant Shop).", 
                "Cần Chìa Khóa", javax.swing.JOptionPane.WARNING_MESSAGE);
            btnNext.setVisible(true);
            return;
        }

        // Consume 1 Dungeon Key
        if (keySlot != -1) quickSlots[keySlot] = null;
        else inventory[invKeySlot] = null;
        refreshInventoryUI();
        btnLockedChest.setVisible(false);

        Item[] chestLoot = new Item[9];
        chestLoot[0] = new Item("ROYAL GOLD VAULT", Item.ItemType.GOLD, 500 + level * 50);
        chestLoot[1] = new Item("Flawless Ruby", Item.ItemType.GEM_ATK, 1);
        chestLoot[2] = new Item("Flawless Sapphire", Item.ItemType.GEM_DEF, 1);
        chestLoot[4] = new Item("Revive Cross", Item.ItemType.REVIVE_CROSS, 1);
        chestLoot[6] = new Item("Stat Potion", Item.ItemType.STAT_POTION, 1);
        chestLoot[8] = new Item("Clear Potion", Item.ItemType.CLEAR_POTION, 1);

        showChestLootDialog(chestLoot, true);
    }

    private void autoTakeAllChestLoot(Item[] chestLoot) {
        if (selectedItem != null) {
            if (selectedItem.getType() == Item.ItemType.GOLD) {
                playerGold += selectedItem.getValue();
                selectedItem = null;
            } else {
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] == null) {
                        inventory[i] = selectedItem;
                        selectedItem = null;
                        break;
                    }
                }
            }
            selectedSlotUI = null;
        }

        for (int i = 0; i < chestLoot.length; i++) {
            Item item = chestLoot[i];
            if (item != null) {
                if (item.getType() == Item.ItemType.GOLD) {
                    playerGold += item.getValue();
                    chestLoot[i] = null;
                } else {
                    for (int invIdx = 0; invIdx < inventory.length; invIdx++) {
                        if (inventory[invIdx] == null) {
                            inventory[invIdx] = item;
                            chestLoot[i] = null;
                            break;
                        }
                    }
                }
            }
        }
        refreshInventoryUI();
    }

    private void showChestLootDialog(Item[] chestLoot, boolean isEventChest) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, isEventChest ? "Ancient Relic Chest" : "Victory Loot Chest", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel instruction = new JLabel(isEventChest ? "Event Relic Chest! Drag items into your backpack." : "Click an item to pick it up, then click an empty slot to place it.");
        instruction.setForeground(isEventChest ? new Color(153, 102, 204) : Color.WHITE);
        instruction.setFont(new Font("SansSerif", Font.BOLD, 13));
        instruction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainPanel.add(instruction, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new java.awt.GridLayout(2, 1, 0, 20));
        center.setOpaque(false);
        
        JPanel chestPanel = new JPanel(new BorderLayout());
        chestPanel.setOpaque(false);
        JLabel chestLbl = new JLabel(isEventChest ? "Relic Chest Contents (3x3)" : "Drop Chest Contents (3x3)");
        chestLbl.setForeground(isEventChest ? new Color(200, 150, 255) : Color.YELLOW);
        chestPanel.add(chestLbl, BorderLayout.NORTH);
        
        JPanel chestGrid = new JPanel(new java.awt.GridLayout(3, 3, 5, 5));
        chestGrid.setOpaque(false);
        JPanel[] chestSlotsUI = new JPanel[9];
        JPanel[] invSlotsUI = new JPanel[12];

        for (int i = 0; i < 9; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(isEventChest ? new Color(153, 102, 204) : new Color(150, 150, 50)));
            chestSlotsUI[i] = slot;
            final int index = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleLootClick(chestLoot, index, chestSlotsUI, chestLoot, chestSlotsUI, inventory, invSlotsUI, dialog);
                }
            });
            chestGrid.add(slot);
        }
        chestPanel.add(chestGrid, BorderLayout.CENTER);
        
        JPanel invPanel = new JPanel(new BorderLayout());
        invPanel.setOpaque(false);
        JLabel invLbl = new JLabel("Your Backpack (2x6)");
        invLbl.setForeground(Color.WHITE);
        invPanel.add(invLbl, BorderLayout.NORTH);
        
        JPanel invGrid = new JPanel(new java.awt.GridLayout(2, 6, 5, 5));
        invGrid.setOpaque(false);
        for (int i = 0; i < 12; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            invSlotsUI[i] = slot;
            final int index = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleLootClick(inventory, index, invSlotsUI, chestLoot, chestSlotsUI, inventory, invSlotsUI, dialog);
                }
            });
            invGrid.add(slot);
        }
        invPanel.add(invGrid, BorderLayout.CENTER);
        
        center.add(chestPanel);
        center.add(invPanel);
        mainPanel.add(center, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);

        JButton btnDiscard = createStyledButton("Discard Held Item");
        btnDiscard.setBackground(new Color(217, 83, 79));
        btnDiscard.addActionListener(e -> {
            if (selectedItem != null) {
                selectedItem = null;
                selectedSlotUI = null;
                updateLootGrid(chestLoot, chestSlotsUI);
                updateLootGrid(inventory, invSlotsUI);
                for(JPanel p : chestSlotsUI) p.setBorder(BorderFactory.createLineBorder(isEventChest ? new Color(153, 102, 204) : new Color(150, 150, 50)));
                for(JPanel p : invSlotsUI) p.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
                dialog.repaint();
                javax.swing.JOptionPane.showMessageDialog(dialog, "Đã vứt bỏ vật phẩm!", "Đã vứt", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(dialog, "Hãy click chọn 1 vật phẩm trước khi bấm Vứt!", "Chưa chọn đồ", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnClose = createStyledButton("Take All & Leave");
        btnClose.addActionListener(e -> {
            autoTakeAllChestLoot(chestLoot);
            dialog.dispose();
            
            if (isEventChest) {
                btnEventChest.setVisible(false);
                btnLockedChest.setVisible(false);
                btnNext.setVisible(true);
                eventDescLabel.setText("You looted the chest and proceeded.");
            } else {
                // Drop chest looted
                btnDropChest.setVisible(false);
                if (isBossRushMode) {
                    bossRushCount++;
                    if (bossRushCount % 3 == 0) {
                        eventTitleLabel.setText("👑 3 BOSSES DEFEATED!");
                        eventDescLabel.setText("Milestone reached! Goddess clears 7 bottom lines & Merchants arrive!");
                        btnShop.setVisible(true);
                        btnStatTrainer.setVisible(true);
                        btnNext.setVisible(true);
                        pendingEventAction = () -> { board.removeBottomLines(7); };
                    } else {
                        eventTitleLabel.setText("👑 BOSS #" + bossRushCount + " DEFEATED!");
                        eventDescLabel.setText("Boss Defeated! Prepare for the next Overlord!");
                        btnNext.setVisible(true);
                    }
                } else {
                    boolean isBossStage = (level % 5 == 0);
                    if (isBossStage) {
                        eventTitleLabel.setText("BOSS DEFEATED!");
                        eventDescLabel.setText("The Goddess heals your wounds (clears 5 bottom lines)!");
                        btnNext.setVisible(true);
                        pendingEventAction = () -> { board.removeBottomLines(5); };
                    } else {
                        // Reveal Random Event after looting drop chest
                        boolean hasEvent = (random.nextInt(100) < 70);
                        if (hasEvent) {
                            int roll = random.nextInt(5);
                            // Exclude Locked Royal Vault during the first 5 stages!
                            if (level <= 5 && roll == 1) {
                                roll = 0; // Fallback to Ancient Relic Chest if early game
                            }
                            if (roll == 0) {
                                eventDescLabel.setText("You discovered an Ancient Relic Chest!");
                                btnEventChest.setVisible(true);
                            } else if (roll == 1) {
                                eventDescLabel.setText("You stumbled upon a Locked Royal Vault! (Requires Dungeon Key)");
                                btnLockedChest.setVisible(true);
                            } else if (roll == 2) {
                                eventDescLabel.setText("A Traveling Merchant appears with rare goods!");
                                btnShop.setVisible(true);
                            } else if (roll == 3) {
                                eventDescLabel.setText("The Devil offers a dangerous bargain...");
                                btnDevilsChallenge.setVisible(true);
                            } else {
                                eventDescLabel.setText("You discovered an Ancient Stat Shrine!");
                                btnStatTrainer.setVisible(true);
                            }
                            btnNext.setVisible(true); // Always make Next button visible so player is never stuck!
                        } else {
                            eventDescLabel.setText("The path ahead is clear. Proceed deeper!");
                            btnNext.setVisible(true);
                        }
                    }
                }
            }
            refreshInventoryUI();
        });

        bottomPanel.add(btnDiscard);
        bottomPanel.add(btnClose);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        
        // Initial render
        updateLootGrid(chestLoot, chestSlotsUI);
        updateLootGrid(inventory, invSlotsUI);
        
        dialog.setVisible(true);
    }
    
    private void handleLootClick(Item[] dataArray, int index, JPanel[] uiArray, 
                                 Item[] chestLoot, JPanel[] chestSlotsUI, 
                                 Item[] inventory, JPanel[] invSlotsUI, 
                                 javax.swing.JDialog dialog) {
        if (selectedItem == null) {
            if (dataArray[index] != null) {
                if (dataArray[index].getType() == Item.ItemType.GOLD) {
                    playerGold += dataArray[index].getValue();
                    dataArray[index] = null;
                    refreshInventoryUI(); // Updates gold label in background
                } else {
                    // Pick up
                    selectedItem = dataArray[index];
                    dataArray[index] = null;
                    selectedSlotUI = uiArray[index];
                }
            }
        } else {
            // Place down
            if (dataArray[index] == null) {
                dataArray[index] = selectedItem;
                selectedItem = null;
                selectedSlotUI = null;
            } else {
                // Swap
                Item temp = dataArray[index];
                dataArray[index] = selectedItem;
                selectedItem = temp;
                selectedSlotUI = uiArray[index]; // The newly picked up item is now "selected" from this slot
            }
        }
        
        // Update both grids completely
        updateLootGrid(chestLoot, chestSlotsUI);
        updateLootGrid(inventory, invSlotsUI);
        
        // Draw the selection border
        for(JPanel p : chestSlotsUI) p.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 50)));
        for(JPanel p : invSlotsUI) p.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        
        if (selectedItem != null && selectedSlotUI != null) {
            selectedSlotUI.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        }
        
        dialog.repaint();
    }
    
    private void updateLootGrid(Item[] dataArray, JPanel[] uiArray) {
        for (int i = 0; i < dataArray.length; i++) {
            uiArray[i].removeAll();
            if (dataArray[i] != null) {
                String labelText = dataArray[i].getName().replace(" ", "<br>");
                if (dataArray[i].getType() == Item.ItemType.GOLD) {
                    labelText += "<br>(" + dataArray[i].getValue() + ")";
                }
                JLabel lbl = new JLabel("<html><div style='text-align: center;'>" + labelText + "</div></html>");
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                uiArray[i].add(lbl, BorderLayout.CENTER);
            }
            uiArray[i].revalidate();
            uiArray[i].repaint();
        }
    }

    public void useItem(int slot) {
        if (slot < 0 || slot >= 3 || quickSlots[slot] == null) return;
        Item item = quickSlots[slot];
        boolean used = false;
        
        switch (item.getType()) {
            case CLEAR_POTION:
                // Xóa 5 hàng block/rác ở đáy
                board.removeBottomLines(5);
                used = true;
                break;
                
            case BOMB:
                // Gây sát thương nhanh cho boss
                int bombDmg = 40;
                enemyHp = Math.max(0, enemyHp - bombDmg);
                if (bossHpBar != null) bossHpBar.repaint();
                if (enemyHp <= 0) handleStageCleared();
                used = true;
                break;
                
            case STAT_POTION:
                // Tăng ATK +10 và DEF +5 trong 20 giây
                tempAtkBuff += 10;
                tempDefBuff += 5;
                refreshInventoryUI();
                Timer statBuffTimer = new Timer(20000, evt -> {
                    tempAtkBuff = Math.max(0, tempAtkBuff - 10);
                    tempDefBuff = Math.max(0, tempDefBuff - 5);
                    refreshInventoryUI();
                });
                statBuffTimer.setRepeats(false);
                statBuffTimer.start();
                used = true;
                break;
                
            case RANDOM_STAT_POTION:
                // Calculate luck-based chance (Base 50% + Luck * 3%, capped at 90%)
                int luckBonus = playerLuck * 3;
                int luckyThreshold = Math.min(90, 50 + luckBonus);
                int outcomeRoll = random.nextInt(100);

                if (outcomeRoll < luckyThreshold) {
                    // GOOD EFFECT (6 Varied Possibilities!)
                    int goodRoll = random.nextInt(6);
                    if (goodRoll == 0) {
                        board.removeBottomLines(7); // Ultimate Clear
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí dọn sạch 7 hàng rác ở đáy!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else if (goodRoll == 1) {
                        tempAtkBuff += 15;
                        tempDefBuff += 10;
                        refreshInventoryUI();
                        Timer buffTimer = new Timer(30000, evt -> {
                            tempAtkBuff = Math.max(0, tempAtkBuff - 15);
                            tempDefBuff = Math.max(0, tempDefBuff - 10);
                            refreshInventoryUI();
                        });
                        buffTimer.setRepeats(false);
                        buffTimer.start();
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí ban phước +15 ATK & +10 DEF trong 30s!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else if (goodRoll == 2) {
                        enemyHp = Math.max(0, enemyHp - 60);
                        if (bossHpBar != null) bossHpBar.repaint();
                        if (enemyHp <= 0) handleStageCleared();
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí gây 60 sát thương sét đánh trực tiếp lên Boss!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else if (goodRoll == 3) {
                        int bonusGold = 150 + random.nextInt(151);
                        playerGold += bonusGold;
                        refreshInventoryUI();
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí hóa vàng + " + bonusGold + " Gold!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else if (goodRoll == 4) {
                        if (activeDebuffs != null) activeDebuffs.clear();
                        hideNextPiece = false;
                        board.setInverseControls(false);
                        board.setSpeedDebuffActive(false);
                        board.setNoRotateCount(0);
                        updateDebuffStatus("");
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí hóa giải TOÀN BỘ debuff bất lợi!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        board.removeBottomLines(4);
                        clearOldestDebuff();
                        javax.swing.JOptionPane.showMessageDialog(this, "✨ MAY MẮN (LUCK " + playerLuck + ")!\nThuốc Huyền Bí xóa 4 hàng rác & giải 1 debuff!", "Mystery Potion Result", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    // BAD / CURSED EFFECT (6 Varied Possibilities!)
                    int badRoll = random.nextInt(6);
                    if (badRoll == 0) {
                        board.addGarbageLine();
                        board.addGarbageLine();
                        board.addGarbageLine();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí biến đổi bộc phát thêm +3 hàng rác!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else if (badRoll == 1) {
                        hideNextPiece = true;
                        updateDebuffStatus("Blindness (Mù khuyết khối)");
                        Timer blindTimer = new Timer(15000, evt -> {
                            hideNextPiece = false;
                            updateDebuffStatus("");
                        });
                        blindTimer.setRepeats(false);
                        blindTimer.start();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí gây MÙ mắt (Ẩn xem trước khối trong 15s)!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else if (badRoll == 2) {
                        attackTimer = maxAttackTimer;
                        if (attackTimerBar != null) attackTimerBar.repaint();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí chọc giận Boss (Thanh tấn công của Boss bị nạp đầy)!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else if (badRoll == 3) {
                        int goldLost = Math.min(playerGold, 50 + random.nextInt(51));
                        playerGold -= goldLost;
                        refreshInventoryUI();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí làm bốc hơi - " + goldLost + " Gold!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else if (badRoll == 4) {
                        board.setInverseControls(true);
                        updateDebuffStatus("Inverse Controls (Đảo ngược phím)");
                        Timer invTimer = new Timer(10000, evt -> {
                            board.setInverseControls(false);
                            updateDebuffStatus("");
                        });
                        invTimer.setRepeats(false);
                        invTimer.start();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí làm đảo ngược phím di chuyển Trái/Phải trong 10s!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    } else {
                        board.setSpeedDebuffActive(true);
                        updateDebuffStatus("Speed Surge (Rơi cực nhanh)");
                        Timer speedTimer = new Timer(12000, evt -> {
                            board.setSpeedDebuffActive(false);
                            updateDebuffStatus("");
                        });
                        speedTimer.setRepeats(false);
                        speedTimer.start();
                        javax.swing.JOptionPane.showMessageDialog(this, "⚠️ XUI XẺO (LUCK " + playerLuck + ")!\nThuốc Huyền Bí làm khối rơi cực nhanh trong 12s!", "Mystery Potion Result", javax.swing.JOptionPane.WARNING_MESSAGE);
                    }
                }
                used = true;
                break;
                
            case CLEAR_DEBUFF_POTION:
                // Xóa 1 debuff cũ nhất
                clearOldestDebuff();
                used = true;
                break;
                
            case REVIVE_CROSS:
                // Không thể bấm dùng - tự động khi chết (handled in handleGameOver)
                // Không tiêu thụ
                break;
                
            default:
                break;
        }
        
        if (used) {
            quickSlots[slot] = null;
            refreshInventoryUI();
        }
    }

    private void showDevilsChallengeDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Devil's Challenge 👿", true);
        dialog.setSize(650, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(25, 15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Devil's Challenge 👿 (Thử Thách Ác Quỷ)");
        title.setForeground(new Color(217, 83, 79));
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JLabel subTitle = new JLabel("Accept a Curse on your next stage for legendary rewards!");
        subTitle.setForeground(new Color(200, 180, 180));
        subTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        JPanel topContainer = new JPanel(new java.awt.GridLayout(2, 1, 0, 5));
        topContainer.setOpaque(false);
        topContainer.add(title);
        topContainer.add(subTitle);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        JPanel dealsPanel = new JPanel(new java.awt.GridLayout(3, 1, 0, 10));
        dealsPanel.setOpaque(false);

        Object[][] deals = {
            {"Blind Curse", "BLIND", "Mù màu (Dính Mù che khối tiếp theo ở màn tới)", 250, Item.ItemType.STAT_POTION, "Stat Potion"},
            {"Haste Speed Drop", "SPEED_DROP", "Rơi siêu tốc (Khối rơi cực nhanh ở màn tới)", 300, Item.ItemType.GEM_ATK, "Rare ATK Gem"},
            {"Heavy Garbage Curse", "ATK_UP", "Phạt thêm +3 hàng rác cho mỗi đòn đánh quái", 400, Item.ItemType.REVIVE_CROSS, "Revive Cross"}
        };

        for (Object[] deal : deals) {
            String dealName = (String) deal[0];
            String debuffKey = (String) deal[1];
            String curseDesc = (String) deal[2];
            int rewardGold = (Integer) deal[3];
            Item.ItemType itemType = (Item.ItemType) deal[4];
            String itemName = (String) deal[5];

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(new Color(45, 25, 25));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 50, 50)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));

            JLabel info = new JLabel("<html><b style='color:#ff6666; font-size:12px;'>" + dealName + "</b><br>"
                + "<span style='color:#ddaaaa; font-size:10px;'>" + curseDesc + "</span><br>"
                + "<span style='color:#ffd700; font-size:10px;'>Phần thưởng: +" + rewardGold + " 💰 + " + itemName + "</span></html>");
            row.add(info, BorderLayout.CENTER);

            JButton btnAccept = createStyledButton("Accept Curse 👿");
            btnAccept.setPreferredSize(new Dimension(140, 35));
            btnAccept.setBackground(new Color(180, 40, 40));
            btnAccept.setForeground(Color.WHITE);
            btnAccept.addActionListener(evt -> {
                playerGold += rewardGold;
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] == null) {
                        inventory[i] = new Item(itemName, itemType, 1);
                        break;
                    }
                }
                
                activeDebuffs.add(debuffKey);
                if (debuffKey.equals("BLIND")) {
                    hideNextPiece = true;
                } else if (debuffKey.equals("SPEED_DROP")) {
                    board.setSpeedDebuffActive(true);
                } else if (debuffKey.equals("ATK_UP")) {
                    bonusGarbageLines = 3;
                }

                refreshInventoryUI();
                dialog.dispose();
                btnDevilsChallenge.setVisible(false);
                btnNext.setVisible(true);
                eventDescLabel.setText("You accepted the Devil's Curse (" + dealName + ") for great riches!");
                javax.swing.JOptionPane.showMessageDialog(this, "Đã chấp nhận Thử Thách Ác Quỷ! Nhận +" + rewardGold + " 💰 + " + itemName, "Chấp Nhận Thử Thách", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            });

            row.add(btnAccept, BorderLayout.EAST);
            dealsPanel.add(row);
        }

        mainPanel.add(dealsPanel, BorderLayout.CENTER);

        JButton btnDecline = createStyledButton("Decline & Leave");
        btnDecline.addActionListener(evt -> {
            dialog.dispose();
            btnDevilsChallenge.setVisible(false);
            btnNext.setVisible(true);
            eventDescLabel.setText("You refused the Devil's Bargain.");
        });
        mainPanel.add(btnDecline, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    public void showMasterControlPanel() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Master Control Panel (Cheat & Debug Console)", true);
        dialog.setSize(680, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("MASTER CONTROL PANEL (F12)");
        title.setForeground(new Color(255, 215, 0));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // TAB 1: Cheats & Stats
        JPanel tabStats = new JPanel(new java.awt.GridLayout(4, 2, 10, 10));
        tabStats.setBackground(new Color(30, 30, 45));
        tabStats.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton btnAddGold = createStyledButton("+5,000 Gold 💰");
        btnAddGold.setBackground(new Color(240, 173, 78));
        btnAddGold.setForeground(Color.BLACK);
        btnAddGold.addActionListener(e -> {
            playerGold += 5000;
            refreshInventoryUI();
            javax.swing.JOptionPane.showMessageDialog(dialog, "Đã thêm +5,000 Gold!", "Cheat Gold", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnMaxStats = createStyledButton("MAX ALL STATS 🔥");
        btnMaxStats.setBackground(new Color(217, 83, 79));
        btnMaxStats.addActionListener(e -> {
            playerAtk = 99;
            playerDef = 50;
            playerLuck = 50;
            playerEvade = 50;
            playerCritChance = 100;
            playerCritDmg = 300;
            playerDebuffResist = 80;
            refreshInventoryUI();
            javax.swing.JOptionPane.showMessageDialog(dialog, "Đã Max toàn bộ Stats!", "Max Stats", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClearBoard = createStyledButton("Heal Board (Clear 10 Lines) 🧹");
        btnClearBoard.setBackground(new Color(92, 184, 92));
        btnClearBoard.addActionListener(e -> {
            board.removeBottomLines(10);
            javax.swing.JOptionPane.showMessageDialog(dialog, "Đã xóa 10 hàng rác ở đáy bàn cờ!", "Heal Board", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClearDebuffs = createStyledButton("Clear All Debuffs 🛡️");
        btnClearDebuffs.setBackground(new Color(91, 192, 222));
        btnClearDebuffs.setForeground(Color.BLACK);
        btnClearDebuffs.addActionListener(e -> {
            clearAllDebuffs();
            javax.swing.JOptionPane.showMessageDialog(dialog, "Đã xóa toàn bộ Debuff!", "Clear Debuffs", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        tabStats.add(btnAddGold);
        tabStats.add(btnMaxStats);
        tabStats.add(btnClearBoard);
        tabStats.add(btnClearDebuffs);
        tabbedPane.addTab("Cheats & Stats", tabStats);

        // TAB 2: Stage & Combat
        JPanel tabStage = new JPanel(new java.awt.GridLayout(3, 2, 10, 10));
        tabStage.setBackground(new Color(30, 30, 45));
        tabStage.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton btnKillEnemy = createStyledButton("Insta-Kill Boss / Monster ☠️");
        btnKillEnemy.setBackground(new Color(217, 83, 79));
        btnKillEnemy.addActionListener(e -> {
            enemyHp = 0;
            updateScoreAndDamageBoss(board.getScore() + 1000, 4);
            dialog.dispose();
        });

        JButton btnSkipLevel = createStyledButton("Jump +5 Stages ⏩");
        btnSkipLevel.setBackground(new Color(153, 102, 204));
        btnSkipLevel.addActionListener(e -> {
            level += 5;
            updateLevelMechanics();
            refreshInventoryUI();
            javax.swing.JOptionPane.showMessageDialog(dialog, "Đã nhảy tới Stage " + level + "!", "Jump Level", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        tabStage.add(btnKillEnemy);
        tabStage.add(btnSkipLevel);
        tabbedPane.addTab("Combat & Stage", tabStage);

        // TAB 3: Item Spawner
        JPanel tabItems = new JPanel(new java.awt.GridLayout(3, 2, 10, 10));
        tabItems.setBackground(new Color(30, 30, 45));
        tabItems.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Item[] spawnableItems = {
            new Item("Dungeon Key", Item.ItemType.DUNGEON_KEY, 1),
            new Item("Revive Cross", Item.ItemType.REVIVE_CROSS, 1),
            new Item("Flawless Ruby", Item.ItemType.GEM_ATK, 1),
            new Item("Flawless Sapphire", Item.ItemType.GEM_DEF, 1),
            new Item("Clear Potion", Item.ItemType.CLEAR_POTION, 1),
            new Item("Stat Potion", Item.ItemType.STAT_POTION, 1)
        };

        for (Item item : spawnableItems) {
            JButton btnSpawn = createStyledButton("Spawn " + item.getName());
            btnSpawn.setBackground(new Color(60, 60, 80));
            btnSpawn.addActionListener(e -> {
                for (int i = 0; i < inventory.length; i++) {
                    if (inventory[i] == null) {
                        inventory[i] = item;
                        refreshInventoryUI();
                        javax.swing.JOptionPane.showMessageDialog(dialog, "Đã thêm " + item.getName() + " vào Túi đồ!", "Spawn Item", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
                javax.swing.JOptionPane.showMessageDialog(dialog, "Túi đồ đã đầy!", "Túi đầy", javax.swing.JOptionPane.WARNING_MESSAGE);
            });
            tabItems.add(btnSpawn);
        }
        tabbedPane.addTab("Item Spawner", tabItems);

        // TAB 4: Event Force Trigger
        JPanel tabEvents = new JPanel(new java.awt.GridLayout(3, 2, 10, 10));
        tabEvents.setBackground(new Color(30, 30, 45));
        tabEvents.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton btnTriggerShop = createStyledButton("Open Shop 🛒");
        btnTriggerShop.addActionListener(e -> { dialog.dispose(); showShopDialog(); });

        JButton btnTriggerShrine = createStyledButton("Open Stat Shrine ⛩️");
        btnTriggerShrine.addActionListener(e -> { dialog.dispose(); showStatTrainerDialog(); });

        JButton btnTriggerVault = createStyledButton("Open Royal Vault 🔒");
        btnTriggerVault.addActionListener(e -> { dialog.dispose(); openLockedChest(); });

        JButton btnTriggerDevil = createStyledButton("Open Devil's Challenge 👿");
        btnTriggerDevil.addActionListener(e -> { dialog.dispose(); showDevilsChallengeDialog(); });

        tabEvents.add(btnTriggerShop);
        tabEvents.add(btnTriggerShrine);
        tabEvents.add(btnTriggerVault);
        tabEvents.add(btnTriggerDevil);
        tabbedPane.addTab("Force Events", tabEvents);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Close Debug Panel");
        btnClose.addActionListener(e -> dialog.dispose());
        mainPanel.add(btnClose, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    public void showControlSettingsDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Controller & Key Bindings", true);
        dialog.setSize(650, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(25, 25, 35));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("🎮 Controller & Key Bindings Settings");
        title.setForeground(new Color(91, 192, 222));
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new java.awt.GridLayout(8, 2, 10, 8));
        centerPanel.setBackground(new Color(35, 35, 45));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[][] mappings = {
            {"Move Left / Right", "D-Pad Left/Right | Left Stick | Arrow Left/Right | A / D | Numpad 4/6"},
            {"Soft Drop", "D-Pad Down | Left Stick Down | Arrow Down | S | Numpad 2"},
            {"Rotate Block", "Button A / X | Arrow Up | W | J | Numpad 8"},
            {"Hard Drop (Instant)", "Button Y / Start | Space | Enter | K"},
            {"Hold Piece", "Button LB / L1 | V | L | Left Shift"},
            {"Use Quickslots (Z / X / C)", "Button RB / R1 / Triggers | Keys Z / X / C | U / I / O"},
            {"Pause Game", "Button Start / Select | P | Esc"},
            {"Master Control Console", "F12 | Ctrl + Shift + C"}
        };

        for (String[] map : mappings) {
            JLabel lblAction = new JLabel("  " + map[0]);
            lblAction.setForeground(new Color(240, 173, 78));
            lblAction.setFont(new Font("SansSerif", Font.BOLD, 12));

            JLabel lblKeys = new JLabel(map[1]);
            lblKeys.setForeground(Color.WHITE);
            lblKeys.setFont(new Font("SansSerif", Font.PLAIN, 11));

            centerPanel.add(lblAction);
            centerPanel.add(lblKeys);
        }

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JButton btnClose = createStyledButton("Close Settings");
        btnClose.addActionListener(e -> dialog.dispose());
        mainPanel.add(btnClose, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void handleStageCleared() {
        if (bossTimer != null) bossTimer.stop();
        board.pauseForEvent();
        
        btnDropChest.setVisible(true);
        btnEventChest.setVisible(false);
        btnLockedChest.setVisible(false);
        btnDevilsChallenge.setVisible(false);
        btnShop.setVisible(false);
        btnStatTrainer.setVisible(false);
        btnNext.setVisible(false);
        
        boolean isBossStage = (level % 5 == 0);
        if (isBossStage) {
            eventTitleLabel.setText("BOSS DEFEATED!");
            eventDescLabel.setText("Boss Defeated! Claim your legendary Boss loot drop.");
        } else {
            eventTitleLabel.setText("Stage " + level + " Cleared!");
            eventDescLabel.setText("Monster Defeated! Claim your victory loot drop.");
        }
        
        cardLayout.show(cards, "Event");
    }

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        EventQueue.invokeLater(() -> {
            var game = new Tetris();
            game.setVisible(true);
        });
    }
}
