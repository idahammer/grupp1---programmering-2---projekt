package se.mah.kd405a_group1.medea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;

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
		
<<<<<<< HEAD
		//JLabel lblNewLabel2 = new JLabel("");
		//lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/medea2jpg.jpg")));
		//lblNewLabel2.setBounds(6, 19, (int)width, (int)height);
		//contentPane.add(lblNewLabel2);
		
		// Start screen pic
				JLabel lblNewLabel2 = new JLabel("");
				lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
				lblNewLabel2.setBounds(-831, 6, 2249, 800);
				contentPane.add(lblNewLabel2);
=======
		JLabel lblNewLabel2 = new JLabel("");
		lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MEDEA2FINALIHOPE.png")));
		lblNewLabel2.setBounds(6, 19, (int)width, (int)height);
		contentPane.add(lblNewLabel2);
>>>>>>> origin/master
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(34, 19, 280, 244);
		contentPane.add(lblNewLabel);
		
		//JLabel lblNewLabel = new JLabel("");
		//lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/wr_3-01.jpg")));
		//lblNewLabel.setBounds(0, 0, (int)width, (int)height);
		//contentPane.add(lblNewLabel);
	}
}
