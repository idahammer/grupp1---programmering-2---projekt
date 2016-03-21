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

public class Screen2GUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Screen2GUI frame = new Screen2GUI();
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
	public Screen2GUI() {
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
				lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/pil2.gif")));
				lblNewLabel.setBounds(0, 0, (int)width, (int)height);
				contentPane.add(lblNewLabel);
		

		// Start screen pic
				JLabel lblNewLabel2 = new JLabel("");
				lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
				lblNewLabel2.setBounds(0, 0, 1080, 1920);
				contentPane.add(lblNewLabel2);
			
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
				@Override
				public boolean dispatchKeyEvent(KeyEvent e) {
					
		//picture after klick
					lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/medea2whatDOweDO.png"))); 
					System.out.println("Got key event!");
					return false;
				}
			});

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(KeyEvent e) {
						lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("null")));
						System.out.println("Got key event!");
						return false;
					}
				});
		
		
		

		//JLabel lblNewLabel2 = new JLabel("");
		//lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/medea2jpg.jpg")));
		//lblNewLabel2.setBounds(6, 19, (int)width, (int)height);
		//contentPane.add(lblNewLabel2);
		
		

		
		
		//JLabel lblNewLabel = new JLabel("");
		//lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/wr_3-01.jpg")));
		//lblNewLabel.setBounds(0, 0, (int)width, (int)height);
		//contentPane.add(lblNewLabel);
	}
}
