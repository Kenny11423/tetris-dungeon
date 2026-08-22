package com.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;
import com.tetris.Shape.Tetrominoe;

public class Board extends JPanel {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private final int INITIAL_DELAY = 100;
    private int periodInterval = 1000;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isEventPaused = false;
    private int score = 0;
    private int curX = 0;
    private int curY = 0;
    private Tetris parent;
    private Shape curPiece;
    private Tetrominoe[] board;

    // Debuff flags
    private boolean hideGhostPiece = false;
    private boolean inverseControls = false;
    private int noRotateCount = 0;
    private boolean speedDebuffActive = false;
    private int normalPeriodInterval = 600;
    
    private boolean monoBlockActive = false;
    private Tetrominoe monoBlockType = Tetrominoe.SquareShape;

    public Board(Tetris parent) {
        this.parent = parent;
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        addKeyListener(new TAdapter());
    }

    public void setHideGhostPiece(boolean hide) { this.hideGhostPiece = hide; repaint(); }
    public void setInverseControls(boolean inverse) { this.inverseControls = inverse; }
    public void setNoRotateCount(int count) { this.noRotateCount = count; }
    
    public void setMonoBlock(boolean active, Tetrominoe type) {
        this.monoBlockActive = active;
        this.monoBlockType = type;
        if (active && nextPiece != null) {
            nextPiece.setShape(type);
            parent.updateNextPiece(nextPiece);
        }
    }
    
    public void setSpeedDebuffActive(boolean active) { 
        this.speedDebuffActive = active; 
        if (timer != null) {
            timer.setDelay(active ? normalPeriodInterval / 3 : normalPeriodInterval);
        }
    }

    private int squareSize() {
        return Math.min((int) getSize().getWidth() / BOARD_WIDTH,
                        (int) getSize().getHeight() / BOARD_HEIGHT);
    }
    
    private Tetrominoe shapeAt(int x, int y) { 
        return board[(y * BOARD_WIDTH) + x]; 
    }

    public boolean isStarted() { return isStarted; }
    public boolean isPaused() { return isPaused; }
    public boolean isEventPaused() { return isEventPaused; }

    private Shape nextPiece;
    private Shape holdPiece;
    private boolean hasHeld = false;

    public Shape getNextPiece() { return nextPiece; }

    public void start() {
        curPiece = new Shape();
        nextPiece = new Shape();
        nextPiece.setRandomShape();
        holdPiece = new Shape();
        holdPiece.setShape(Tetrominoe.NoShape);
        parent.updateHoldPiece(holdPiece);
        
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

        clearBoard();
        isStarted = true;
        isFallingFinished = false;
        isEventPaused = false;
        hasHeld = false;
        score = 0;

        newPiece();
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(periodInterval, new GameCycle());
        timer.setInitialDelay(INITIAL_DELAY);
        timer.start();
    }

    public void updateSpeed(int level) {
        int bossTier = (level - 1) / 5;
        normalPeriodInterval = Math.max(150, 1000 - (bossTier * 100));
        periodInterval = normalPeriodInterval;
        if (timer != null && !speedDebuffActive) {
            timer.setDelay(periodInterval);
        } else if (timer != null && speedDebuffActive) {
            timer.setDelay(normalPeriodInterval / 3);
        }
    }

    public void togglePause() {
        if (!isStarted || isEventPaused) {
            return;
        }
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }
    
    public void pauseForEvent() {
        isEventPaused = true;
        if (timer != null) {
            timer.stop();
        }
    }
    
    public void resumeFromEvent() {
        isEventPaused = false;
        if (!isPaused && timer != null) {
            timer.start();
        }
    }
    
    public void removeBottomLines(int count) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (i < BOARD_HEIGHT - count) {
                    board[(i * BOARD_WIDTH) + j] = shapeAt(j, i + count);
                } else {
                    board[(i * BOARD_WIDTH) + j] = Tetrominoe.NoShape;
                }
            }
        }
        repaint();
    }

    public void reset() {
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        var size = getSize();
        int sqSize = squareSize();
        int boardWidthPx = BOARD_WIDTH * sqSize;
        int boardHeightPx = BOARD_HEIGHT * sqSize;
        
        int boardLeft = (size.width - boardWidthPx) / 2;
        int boardTop = size.height - boardHeightPx;

        // Draw board background
        g.setColor(new Color(20, 20, 20));
        g.fillRect(boardLeft, boardTop, boardWidthPx, boardHeightPx);

        // Draw grid lines
        g.setColor(new Color(40, 40, 40));
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(boardLeft, boardTop + i * sqSize, boardLeft + boardWidthPx, boardTop + i * sqSize);
        }
        for (int j = 0; j <= BOARD_WIDTH; j++) {
            g.drawLine(boardLeft + j * sqSize, boardTop, boardLeft + j * sqSize, boardTop + boardHeightPx);
        }

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoe.NoShape) {
                    drawSquare(g, boardLeft + j * sqSize, boardTop + i * sqSize, shape, sqSize);
                }
            }
        }

        if (curPiece != null && curPiece.getShape() != Tetrominoe.NoShape) {
            if (!hideGhostPiece) {
                int ghostY = curY;
                while (ghostY > 0 && canMove(curPiece, curX, ghostY - 1)) {
                    ghostY--;
                }
                
                for (int i = 0; i < 4; i++) {
                    int x = curX + curPiece.x(i);
                    int y = ghostY - curPiece.y(i);
                    drawGhostSquare(g, boardLeft + x * sqSize, boardTop + (BOARD_HEIGHT - y - 1) * sqSize, curPiece.getShape(), sqSize);
                }
            }
            
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, boardLeft + x * sqSize, boardTop + (BOARD_HEIGHT - y - 1) * sqSize, curPiece.getShape(), sqSize);
            }
        }
        
        if (isEventPaused) {
        } else if (isPaused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(boardLeft, boardTop, boardWidthPx, boardHeightPx);
            g.setColor(Color.WHITE);
            g.drawString("PAUSED", boardLeft + boardWidthPx/2 - 25, boardTop + boardHeightPx/2);
        } else if (!isStarted) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(boardLeft, boardTop, boardWidthPx, boardHeightPx);
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", boardLeft + boardWidthPx/2 - 35, boardTop + boardHeightPx/2);
        }
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!canMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }
        tryMove(curPiece, curX, newY);
        pieceDropped();
    }

    private int lockGraceTicks = 0;

    private void oneLineDown() {
        if (!canMove(curPiece, curX, curY - 1)) {
            lockGraceTicks++;
            if (lockGraceTicks >= 2) { // Wait 1 extra tick before locking
                pieceDropped();
                lockGraceTicks = 0;
            }
        } else {
            tryMove(curPiece, curX, curY - 1);
            lockGraceTicks = 0;
        }
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();
        
        if (noRotateCount > 0) {
            noRotateCount--;
            parent.updateDebuffStatus(noRotateCount > 0 ? "No Rotate (" + noRotateCount + ")" : "");
        }

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void newPiece() {
        curPiece.setShape(nextPiece.getShape());
        
        if (monoBlockActive) {
            nextPiece.setShape(monoBlockType);
        } else {
            nextPiece.setRandomShape();
        }
        parent.updateNextPiece(nextPiece);
        
        hasHeld = false;
        
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoe.NoShape);
            if (timer != null) timer.stop();
            isStarted = false;
            parent.handleGameOver();
        }
    }

    private boolean canMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape) {
                return false;
            }
        }
        return true;
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        if (!canMove(newPiece, newX, newY)) {
            return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        
        lockGraceTicks = 0; // Reset grace period on successful move/rotate
        
        repaint();

        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {
            score += numFullLines * 100;
            parent.updateScoreAndDamageBoss(score, numFullLines);
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }
    
    public void addGarbageLine() {
        // Move everything up
        for (int i = BOARD_HEIGHT - 1; i > 0; i--) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[(i * BOARD_WIDTH) + j] = shapeAt(j, i - 1);
            }
        }
        // Add random garbage line at bottom
        for (int j = 0; j < BOARD_WIDTH; j++) {
            if (Math.random() > 0.3) {
                board[j] = Tetrominoe.SquareShape; 
            } else {
                board[j] = Tetrominoe.NoShape;
            }
        }
        repaint();
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape, int sqSize) {
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
        g.drawLine(x + 1, y + sqSize - 1,
                x + sqSize - 1, y + sqSize - 1);
        g.drawLine(x + sqSize - 1, y + sqSize - 1,
                x + sqSize - 1, y + 1);
    }

    private void drawGhostSquare(Graphics g, int x, int y, Tetrominoe shape, int sqSize) {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        var color = colors[shape.ordinal()];

        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g.fillRect(x + 1, y + 1, sqSize - 2, sqSize - 2);

        g.setColor(color.darker());
        g.drawRect(x + 1, y + 1, sqSize - 2, sqSize - 2);
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private void update() {
        if (isPaused || isEventPaused) {
            return;
        }
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    public void holdPieceAction() {
        if (!isStarted || isEventPaused || curPiece.getShape() == Tetrominoe.NoShape || hasHeld) {
            return;
        }

        if (holdPiece.getShape() == Tetrominoe.NoShape) {
            holdPiece.setShape(curPiece.getShape());
            newPiece();
            hasHeld = true; // wait, newPiece sets it to false, so we must set it to true AFTER newPiece()
        } else {
            Shape temp = new Shape();
            temp.setShape(curPiece.getShape());
            curPiece.setShape(holdPiece.getShape());
            holdPiece.setShape(temp.getShape());
            
            curX = BOARD_WIDTH / 2 + 1;
            curY = BOARD_HEIGHT - 1 + curPiece.minY();
            
            if (!canMove(curPiece, curX, curY)) {
                // If it can't spawn, game over
                curPiece.setShape(Tetrominoe.NoShape);
                if (timer != null) timer.stop();
                isStarted = false;
                parent.handleGameOver();
                return;
            }
        }
        parent.updateHoldPiece(holdPiece);
        hasHeld = true;
        lockGraceTicks = 0;
        repaint();
    }

    private void tryRotate(Shape newPiece, int currentX, int currentY) {
        // Normal rotation
        if (tryMove(newPiece, currentX, currentY)) return;
        
        // Basic Wall Kicking (SRS-lite)
        // Try shifting left or right by 1 or 2 blocks, or up by 1 block
        if (tryMove(newPiece, currentX - 1, currentY)) return;
        if (tryMove(newPiece, currentX + 1, currentY)) return;
        if (tryMove(newPiece, currentX, currentY - 1)) return;
        if (tryMove(newPiece, currentX - 2, currentY)) return;
        if (tryMove(newPiece, currentX + 2, currentY)) return;
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || isEventPaused || curPiece == null || curPiece.getShape() == Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == KeyEvent.VK_P) {
                togglePause();
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    if (inverseControls) tryMove(curPiece, curX + 1, curY);
                    else tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    if (inverseControls) tryMove(curPiece, curX - 1, curY);
                    else tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                    if (noRotateCount <= 0) {
                        tryRotate(curPiece.rotateRight(), curX, curY);
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_D:
                    oneLineDown();
                    break;
                case KeyEvent.VK_C:
                    holdPieceAction();
                    break;
                case KeyEvent.VK_1:
                    parent.useItem(0);
                    break;
                case KeyEvent.VK_2:
                    parent.useItem(1);
                    break;
                case KeyEvent.VK_3:
                    parent.useItem(2);
                    break;
            }
        }
    }
}
