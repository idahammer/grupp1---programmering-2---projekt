package se.mah.kd405a_group1.medea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import javax.swing.ImageIcon;

public class Screen3GUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Screen3GUI frame = new Screen3GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Screen3GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		System.out.println("JFrame Width: "+width+" Height: "+height);
		this.setBounds(0, 0, (int)width, (int)height); 
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.PINK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Screen3GUI.class.getResource("/se/mah/kd405a_group1/medea/res/pil2.gif")));
		lblNewLabel.setBounds(43, 107, 269, 195);
		contentPane.add(lblNewLabel);
		
		// Start screen pic
		JLabel lblNewLabel2 = new JLabel("");
		lblNewLabel2.setIcon(new ImageIcon(Screen3GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart1.png")));
		lblNewLabel2.setBounds(-849, 27, 2129, 800);
		contentPane.add(lblNewLabel2);
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				
				lblNewLabel2.setIcon(new ImageIcon(Screen3GUI.class.getResource("/se/mah/kd405a_group1/medea/res/Medea3jpg.jpg"))); //byt till bild
				System.out.println("Got key event!");
				return false;
			}
		});

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
				@Override
				public boolean dispatchKeyEvent(KeyEvent e) {
					lblNewLabel.setIcon(new ImageIcon(Screen3GUI.class.getResource("null"))); // tar bort pilen
					System.out.println("Got key event!");
					return false;
				}
			});
		
		//JLabel lblNewLabel = new JLabel("New label");
		//lblNewLabel.setIcon(new ImageIcon(Screen3GUI.class.getResource("/se/mah/kd405a_group1/medea/res/Medea3jpg.jpg")));
		//lblNewLabel.setBounds(0, 0, 1291, 800);
		//contentPane.add(lblNewLabel);
		
	
	}
}
