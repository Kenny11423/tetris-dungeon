package com.tetris;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
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
    private Runnable pendingEventAction;
    private Random random = new Random();

    private Shape currentNextPiece;
    private JPanel nextBlockPreview;

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
            board.requestFocusInWindow();
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
            JLabel lbl = new JLabel((i + 1) + ". " + s.name + " - Score: " + s.score + " (Level " + s.level + ")");
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
        
        int s = Integer.parseInt(scoreValueLabel.getText().replace(",", ""));
        if (s > 0) {
            String name = javax.swing.JOptionPane.showInputDialog(this, "Game Over! Enter your name:", "High Score", javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                DatabaseManager.saveHighScore(name.trim(), s, level);
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

        JButton btnNext = createStyledButton("Proceed deeper");
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
            board.requestFocusInWindow();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(eventTitleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(eventDescLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnNext, gbc);

        return panel;
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
        
        leftPanel.add(nextBlockLabel);
        leftPanel.add(nextBlockPreview);
        leftPanel.add(Box.createVerticalGlue());
        
        JButton btnMenu = createStyledButton("Flee (Menu)");
        btnMenu.setAlignmentX(CENTER_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            cardLayout.show(cards, "Home");
            if (bossTimer != null) bossTimer.stop();
        });
        
        leftPanel.add(btnMenu);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

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

        JLabel scoreLabel = new JLabel("Score");
        scoreLabel.setForeground(new Color(180, 180, 180));
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        scoreValueLabel = new JLabel("0");
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
        updateLevelMechanics();
        scoreValueLabel.setText("0");
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
        hideNextPiece = false;
        bonusGarbageLines = 0;
        updateDebuffStatus("");
        if (nextBlockPreview != null) nextBlockPreview.repaint();
        if (board != null) {
            board.setHideGhostPiece(false);
            board.setInverseControls(false);
            board.setNoRotateCount(0);
            board.setSpeedDebuffActive(false);
        }
    }

    private void castBossSkill(int bossTier) {
        clearAllDebuffs();
        int skillOptions = Math.min(2 + bossTier, 6);
        int skill = random.nextInt(skillOptions);
        
        switch(skill) {
            case 0:
                hideNextPiece = true;
                updateDebuffStatus("Blind (Hide Next)");
                if (nextBlockPreview != null) nextBlockPreview.repaint();
                break;
            case 1:
                updateDebuffStatus("Haste (Boss Fast Attack)");
                attackTimer = Math.max(attackTimer, maxAttackTimer - 20);
                break;
            case 2:
                board.setHideGhostPiece(true);
                updateDebuffStatus("No Shadow");
                break;
            case 3:
                board.setNoRotateCount(3 + bossTier);
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
        
        if (enemyHp <= 0) {
            enemyHp = 0;
            bossHpBar.repaint();
            handleStageCleared();
        } else {
            bossHpBar.repaint();
        }
    }

    private void handleStageCleared() {
        if (bossTimer != null) bossTimer.stop();
        board.pauseForEvent();
        
        boolean nextIsBoss = ((level + 1) % 5 == 0);
        
        if (nextIsBoss) {
            eventDescLabel.setText("A terrifying presence approaches... The goddess heals your wounds! (Bottom lines cleared)");
            pendingEventAction = () -> {
                board.removeBottomLines(5); // Heal event removes garbage/blocks
            };
        } else {
            int eventType = random.nextInt(3);
            if (eventType == 0) {
                eventDescLabel.setText("You found a hidden stash! Score +500");
                pendingEventAction = () -> {
                    int s = Integer.parseInt(scoreValueLabel.getText().replace(",", ""));
                    scoreValueLabel.setText(String.format("%,d", s + 500));
                };
            } else if (eventType == 1) {
                eventDescLabel.setText("The dungeon shifts... Speed increases slightly for the next room!");
                pendingEventAction = () -> {};
            } else {
                eventDescLabel.setText("You safely proceed to the next room.");
                pendingEventAction = () -> {};
            }
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
