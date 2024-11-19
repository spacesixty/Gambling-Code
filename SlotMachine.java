import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/*
 * Make a pity system where after 3 attempts with no odd or even it sets the closest to only 7
 * When it reaches 5 attempts it locks the last one to 7 as well until jackpot or odd/even
 * 
 */

public class SlotMachine extends JFrame {
    private JLabel[] slotLabels;
    private JButton spinButton;
    private ImageIcon[] icons;
    private ImageIcon loading;
    private JLabel numbers;
    private Random random;
    private Timer timer;
    private int spinCount;
    private int speed;
    private int slot1 = 0;
    private int slot2 = 0;
    private int slot3 = 0;
    private Clip spinClip;
    boolean AllO = false;
    boolean AllE = false;
    int pity = 2;
    boolean jackpot = false;

    public SlotMachine() {
        setTitle("Slot Machine");
        setSize(600, 350);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the slot labels
        slotLabels = new JLabel[3];
        JPanel slotPanel = new JPanel();
        slotPanel.setLayout(new GridLayout(1, 4));

        // Load images for the slot machine
        loading = new ImageIcon("animated.gif");
        icons = new ImageIcon[7];
        icons[0] = new ImageIcon("1.png");
        icons[1] = new ImageIcon("2.png");
        icons[2] = new ImageIcon("3.png");
        icons[3] = new ImageIcon("4.png");
        icons[4] = new ImageIcon("5.png");
        icons[5] = new ImageIcon("6.png");
        icons[6] = new ImageIcon("7.png");

        // Create labels for the slots
        for (int i = 0; i < slotLabels.length; i++) {
            slotLabels[i] = new JLabel();
            slotLabels[i].setHorizontalAlignment(JLabel.CENTER);
            slotLabels[i].setBorder(new LineBorder(Color.BLACK, 2));
            slotPanel.add(slotLabels[i]);
        }
        slotLabels[0].setIcon(icons[0]);
        slotLabels[1].setIcon(icons[0]);
        slotLabels[2].setIcon(icons[0]);

        add(slotPanel, BorderLayout.CENTER);

        // Spin button
        numbers = new JLabel(slot1 + "; " + slot2 + "; " + slot3);
        spinButton = new JButton("Spin");
        spinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSpinning();
            }
        });
        add(spinButton, BorderLayout.SOUTH);

        random = new Random();
        setVisible(true);
        
        // Add global key listener
        
    }

    private void startSpinning() {
        spinCount = 0; // Reset spin count
        speed = 70; // Starting speed (ms)
        spinButton.setEnabled(false); // Disable button during spin
        

        playAudio("spinning2.wav"); // Play audio

        // Timer to change icons
        timer = new Timer(speed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < slotLabels.length; i++) {
                    slotLabels[i].setIcon(loading);
                }

                // Adjust speed for a slowing effect
                spinCount++;
                if (spinCount < 30) { // Keep spinning for 30 ticks
                    if(AllE) {
                        speed = Math.max(10, 0 - 2); // Decrease speed, but not less than 30ms
                        timer.setDelay(speed);
                    }
                    else{
                        speed = Math.max(30, speed - 2); // Decrease speed, but not less than 30ms
                        timer.setDelay(speed);
                    }
                } else {
                    timer.stop();
                    stopAudio(); // Stop audio when finished
                    showFinalResult();
                }
            }
        });
        timer.start();
    }

    private void showFinalResult() {
        for (int i = 0; i < slotLabels.length; i++) {
            final int slotIndex = i; // Create a final variable for the current slot index
            int finalIndex;
            if (((AllE) != true && (AllO == true))){
                finalIndex = random.nextInt((7-4)) + 3;
            }
            else {
                if (jackpot == true){
                }
                finalIndex = random.nextInt(icons.length);
            }
            
            numbers.setText(slot1 + "; " + slot2 + "; " + slot3);
            int delay = 85; // Delay for each slot (1 second between each)

            // Use a timer to update the final result for each slot after a delay
            Timer delayTimer = new Timer(delay * (i + 1), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    slotLabels[slotIndex].setIcon(icons[finalIndex]);
                }
            });
            
            if (i == 0) {
                slot1 = finalIndex + 1;
            } else if (i == 1) {
                slot2 = finalIndex + 1;
            } else if (i == 2) {
                slot3 = finalIndex + 1;
            }
            

            delayTimer.setRepeats(false);
            delayTimer.start();
        }
        if (spinClip != null && !spinClip.isRunning()) {
            playAudio("land.wav");
        }

        if ((slot1 == slot2) && (slot2 == slot3)) {
            playAudio("jackpot.wav");
            AllE = false;
            AllO = false;
            jackpot = true;
            spinButton.setText("JACKPOT");
        } else if ((slot1 % 2 == 0) && (slot2 % 2 == 0) && (slot3 % 2 == 0)) {
            AllO = false;
            AllE = true;
            spinButton.setText("All Even! Speed Increased!");
            
        } else if ((slot1 % 2 == 1) && (slot2 % 2 == 1) && (slot3 % 2 == 1)) {
            AllO = true;
            AllE = false;
            spinButton.setText("All Odds! Jackpot Chance Increased!");
            
        } else {
            spinButton.setText("No match");
        }
        spinButton.setEnabled(true); // Re-enable button after spin
    }

    private void playAudio(String filePath) {
        try {
            if (spinClip != null && spinClip.isRunning()) {
                spinClip.stop(); // Stop the currently playing clip
                spinClip.close(); // Close the clip before opening a new one
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            spinClip = AudioSystem.getClip();
            spinClip.open(audioInputStream);
            spinClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {
        if (spinClip != null && spinClip.isRunning()) {
            spinClip.stop();
            spinClip.close();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SlotMachine());
    }
}
