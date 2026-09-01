package com.tetris;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.swing.SwingUtilities;

public class GamepadManager {

    private final Tetris tetris;
    private final Board board;
    private boolean running = true;
    private Thread pollingThread;
    private static GamepadManager instance;

    public GamepadManager(Tetris tetris, Board board) {
        this.tetris = tetris;
        this.board = board;
        instance = this;
    }

    public static GamepadManager getInstance() {
        return instance;
    }

    public void startPolling() {
        pollingThread = new Thread(() -> {
            while (running) {
                File devJoystick = findJoystickDevice();
                if (devJoystick != null && devJoystick.canRead()) {
                    pollDevice(devJoystick);
                } else {
                    try {
                        Thread.sleep(2000); // Check for connected gamepad every 2 sec
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.setName("GamepadPollingThread");
        pollingThread.start();
    }

    public void stopPolling() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private File findJoystickDevice() {
        for (int i = 0; i < 4; i++) {
            File f = new File("/dev/input/js" + i);
            if (f.exists() && f.canRead()) {
                return f;
            }
        }
        return null;
    }

    private void pollDevice(File dev) {
        try (FileInputStream fis = new FileInputStream(dev)) {
            byte[] buffer = new byte[8];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            long lastAxisTime = 0;

            while (running) {
                int bytesRead = fis.read(buffer);
                if (bytesRead < 8) break;

                bb.rewind();
                int time = bb.getInt();
                short value = bb.getShort();
                byte type = bb.get();
                byte number = bb.get();

                int eventType = type & 0x7F;

                if (eventType == 1 && value == 1) {
                    // Button Pressed Event
                    SwingUtilities.invokeLater(() -> handleButtonPress(number));
                } else if (eventType == 2) {
                    // Axis Motion Event
                    long now = System.currentTimeMillis();
                    if (now - lastAxisTime > 120) { // Throttle axis repeat rate to 120ms
                        if (handleAxisMotion(number, value)) {
                            lastAxisTime = now;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Controller disconnected or read error
        }
    }

    private void handleButtonPress(int buttonNumber) {
        switch (buttonNumber) {
            case 0: // A (Xbox) / Cross (PS)
            case 1: // B (Xbox) / Circle (PS)
            case 2: // X (Xbox) / Square (PS)
                board.rotatePiece();
                break;
            case 3: // Y (Xbox) / Triangle (PS)
                board.hardDrop();
                break;
            case 4: // LB / L1
                board.holdPieceAction();
                break;
            case 5: // RB / R1
                tetris.useItem(0);
                break;
            case 6: // Select / Back / Share
                tetris.useItem(1);
                break;
            case 7: // Start / Options
                board.togglePause();
                break;
            case 8: // Xbox Logo / PS Home
            case 9: // L3
                tetris.useItem(2);
                break;
        }
    }

    private boolean handleAxisMotion(int axisNumber, short value) {
        if (axisNumber == 0 || axisNumber == 6) { // X Axis (Left Stick or D-Pad Left/Right)
            if (value < -15000) {
                board.moveLeft();
                return true;
            } else if (value > 15000) {
                board.moveRight();
                return true;
            }
        } else if (axisNumber == 1 || axisNumber == 7) { // Y Axis (Left Stick or D-Pad Up/Down)
            if (value > 15000) {
                board.softDrop();
                return true;
            } else if (value < -15000) {
                board.rotatePiece();
                return true;
            }
        }
        return false;
    }
}
