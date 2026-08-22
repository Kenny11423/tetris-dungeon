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
    private boolean hideNextPiece = false;

    private JPanel bossHpBar;
    private JPanel attackTimerBar;
    private JLabel levelLabel;
    private JLabel bossAnimLabel;
    
    // Event Panel components
    private JLabel eventTitleLabel;
    private JLabel eventDescLabel;
    private JButton btnChest;
    private JButton btnNext;
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
    
    // Debuff tracking - ordered by when applied (oldest first)
    private java.util.LinkedList<String> activeDebuffs = new java.util.LinkedList<>();
    private Random random = new Random();

    private Shape currentNextPiece;
    private JPanel nextBlockPreview;
    
    private Shape currentHoldPiece;
    private JPanel holdBlockPreview;

    public void updateDebuffStatus(String text) {
        if (skillStatusLabel != null) {
            skillStatusLabel.setText(text);
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
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("Tetris Dungeon");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));

        JButton btnPlay = createStyledButton("Enter Dungeon");
        btnPlay.setPreferredSize(new Dimension(200, 40));
        btnPlay.addActionListener(e -> {
            cardLayout.show(cards, "Game");
            board.start(); // Fully reset board
            resetGameMechanics();
            javax.swing.SwingUtilities.invokeLater(() -> board.requestFocusInWindow());
        });

        JButton btnLeaderboard = createStyledButton("Leaderboard");
        btnLeaderboard.setPreferredSize(new Dimension(200, 40));
        btnLeaderboard.addActionListener(e -> {
            updateLeaderboardPanel();
            cardLayout.show(cards, "Leaderboard");
        });

        JButton btnQuit = createStyledButton("Quit");
        btnQuit.setPreferredSize(new Dimension(200, 40));
        btnQuit.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(btnPlay, gbc);

        gbc.gridy = 2;
        panel.add(btnLeaderboard, gbc);
        
        gbc.gridy = 3;
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
        
        btnChest = createStyledButton("Open Chest");
        btnChest.setPreferredSize(new Dimension(250, 40));
        btnChest.setBackground(new Color(240, 173, 78)); // Golden color
        btnChest.setForeground(Color.BLACK);
        btnChest.addActionListener(e -> openChest());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(eventTitleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(eventDescLabel, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(btnChest, gbc);

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

    private void showGemSocketDialog(String itemName) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, itemName + " Gems", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel(null); // Absolute layout for circular arrangement
        panel.setBackground(new Color(30, 30, 30));
        
        JLabel title = new JLabel(itemName + " - Gem Sockets");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBounds(100, 10, 200, 30);
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panel.add(title);
        
        // Center item icon
        JPanel centerItem = new JPanel(new BorderLayout());
        centerItem.setBackground(new Color(60, 60, 60));
        centerItem.setBounds(160, 160, 80, 80);
        JLabel cLbl = new JLabel(itemName);
        cLbl.setForeground(Color.WHITE);
        cLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        centerItem.add(cLbl, BorderLayout.CENTER);
        panel.add(centerItem);
        
        int radius = 100;
        int centerX = 200;
        int centerY = 200;
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90); // Start from top (-90 degrees)
            int x = centerX + (int)(radius * Math.cos(angle)) - 25;
            int y = centerY + (int)(radius * Math.sin(angle)) - 25;
            
            JPanel gemSlot = new JPanel();
            gemSlot.setBackground(new Color(40, 40, 40));
            gemSlot.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 50), 2, true));
            gemSlot.setBounds(x, y, 50, 50);
            
            JLabel gemLbl = new JLabel(String.valueOf(i+1));
            gemLbl.setForeground(Color.GRAY);
            gemSlot.add(gemLbl);
            
            panel.add(gemSlot);
        }
        
        JButton btnClose = createStyledButton("Close");
        btnClose.setBounds(150, 330, 100, 30);
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose);
        
        dialog.add(panel);
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
        
        String[][] statRows = {
            {" ATK",         "10",    "(Sát thương mỗi hàng)"},
            {" DEF",         "5",     "(Giảm garbage lines nhận)"},
            {" LUCK",        "2",     "(Tăng tỉ lệ rớt đồ hiếm)"},
            {" EVADE",       "0%",    "(Tỉ lệ né đòn boss)"},
            {" CRIT",        "5%",    "(Tỉ lệ chí mạng)"},
            {" CRIT DMG",    "150%",  "(Nhân sát thương crit)"},
            {" RESIST",      "0%",    "(Kháng debuff)"},
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
        
        JLabel holdBlockLabel = new JLabel("Hold block");
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
        JLabel quickSlotsLabel = new JLabel("Items (1,2,3)");
        quickSlotsLabel.setForeground(new Color(200, 200, 200));
        quickSlotsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        quickSlotsLabel.setAlignmentX(CENTER_ALIGNMENT);
        quickSlotsLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        
        JPanel quickSlotsPanel = new JPanel(new java.awt.GridLayout(3, 1, 0, 4));
        quickSlotsPanel.setOpaque(false);
        quickSlotsPanel.setMaximumSize(new Dimension(130, 120));
        for (int i = 0; i < 3; i++) {
            JPanel slot = new JPanel(new BorderLayout(4, 0));
            slot.setPreferredSize(new Dimension(130, 36));
            slot.setBackground(new Color(40, 40, 40));
            slot.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            
            JLabel keyBadge = new JLabel("[" + (i + 1) + "]");
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
        level = 1;
        timeElapsed = 0;
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
        boolean isBoss = (level % 5 == 0);
        
        if (isBoss) {
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
        
        int damage = linesCleared * 10;
        enemyHp -= damage;
        
        // Delay boss attack when hit
        attackTimer = Math.max(0, attackTimer - (linesCleared * 10));
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

    private void openChest() {
        btnChest.setVisible(false);
        btnNext.setVisible(true);
        
        Item[] chestLoot = new Item[9];
        Item.ItemType[] possibleTypes = {
            Item.ItemType.CLEAR_POTION,
            Item.ItemType.BOMB,
            Item.ItemType.STAT_POTION,
            Item.ItemType.RANDOM_STAT_POTION,
            Item.ItemType.CLEAR_DEBUFF_POTION,
            Item.ItemType.GEM_ATK,
            Item.ItemType.GEM_DEF,
            Item.ItemType.REVIVE_CROSS,
            Item.ItemType.GOLD
        };
        
        // Randomize loot (0 to 9 items)
        int numItems = random.nextInt(6); // 0 to 5 items usually
        for (int i = 0; i < numItems; i++) {
            int slot = random.nextInt(9);
            if (chestLoot[slot] == null) {
                Item.ItemType lootType = possibleTypes[random.nextInt(possibleTypes.length)];
                int value = 1;
                if (lootType == Item.ItemType.GOLD) {
                    value = 50 + random.nextInt(151); // 50 to 200 gold
                }
                chestLoot[slot] = new Item(lootType.toString().replace("_", " "), lootType, value);
            }
        }
        
        showChestLootDialog(chestLoot);
    }

    private void showChestLootDialog(Item[] chestLoot) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Treasure Chest", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel instruction = new JLabel("Click an item to pick it up, then click an empty slot to place it.");
        instruction.setForeground(Color.WHITE);
        instruction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainPanel.add(instruction, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new java.awt.GridLayout(2, 1, 0, 20));
        center.setOpaque(false);
        
        // Chest Grid (3x3)
        JPanel chestPanel = new JPanel(new BorderLayout());
        chestPanel.setOpaque(false);
        JLabel chestLbl = new JLabel("Chest Contents (3x3)");
        chestLbl.setForeground(Color.YELLOW);
        chestPanel.add(chestLbl, BorderLayout.NORTH);
        
        JPanel chestGrid = new JPanel(new java.awt.GridLayout(3, 3, 5, 5));
        chestGrid.setOpaque(false);
        JPanel[] chestSlotsUI = new JPanel[9];
        JPanel[] invSlotsUI = new JPanel[12]; // Pre-declare for listener reference

        for (int i = 0; i < 9; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(new Color(50, 50, 50));
            slot.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 50)));
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
        
        // Player Inventory Grid (2x6)
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
        
        JButton btnClose = createStyledButton("Take All & Leave");
        btnClose.addActionListener(e -> {
            selectedItem = null;
            selectedSlotUI = null;
            dialog.dispose();
            eventDescLabel.setText("You looted the chest and proceeded.");
            refreshInventoryUI();
        });
        mainPanel.add(btnClose, BorderLayout.SOUTH);
        
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
                // Xóa 3 hàng block/rác ở đáy
                board.removeBottomLines(3);
                used = true;
                break;
                
            case BOMB:
                // Gây sát thương nhanh cho boss
                int bombDmg = 30;
                enemyHp = Math.max(0, enemyHp - bombDmg);
                if (bossHpBar != null) bossHpBar.repaint();
                if (enemyHp <= 0) handleStageCleared();
                used = true;
                break;
                
            case STAT_POTION:
                // Tăng ATK tạm thời (tăng dame per line x2 trong 30 giây)
                // TODO: implement proper stat buff system later
                // For now: clear 2 hàng + tăng sát thương tạm thời
                board.removeBottomLines(2);
                used = true;
                break;
                
            case RANDOM_STAT_POTION:
                // Ngẫu nhiên buff hoặc debuff
                int roll = random.nextInt(10);
                if (roll < 6) {
                    // Lucky: xóa 3 hàng + xóa oldest debuff
                    board.removeBottomLines(3);
                    clearOldestDebuff();
                } else if (roll < 8) {
                    // Neutral: xóa 1 hàng
                    board.removeBottomLines(1);
                } else {
                    // Unlucky: boss tấn công nhanh hơn
                    attackTimer = Math.max(0, attackTimer - 30);
                    if (attackTimerBar != null) attackTimerBar.repaint();
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

    private void handleStageCleared() {
        if (bossTimer != null) bossTimer.stop();
        board.pauseForEvent();
        
        btnChest.setVisible(true);
        btnNext.setVisible(false); // Wait until chest is opened
        
        boolean nextIsBoss = ((level + 1) % 5 == 0);
        
        if (nextIsBoss) {
            eventDescLabel.setText("A terrifying presence approaches... Boss Room Next!");
            pendingEventAction = () -> {
                board.removeBottomLines(5); // Heal event
            };
        } else {
            eventDescLabel.setText("Stage Cleared! You found a treasure chest.");
            pendingEventAction = () -> {};
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
