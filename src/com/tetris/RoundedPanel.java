package com.tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private int radius;
    private Color borderColor;

    public RoundedPanel(int radius, LayoutManager layout) {
        super(layout);
        this.radius = radius;
        this.borderColor = new Color(50, 50, 50);
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this(radius, null);
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
}
