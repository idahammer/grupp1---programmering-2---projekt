//SCREEN 2

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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.Firebase.AuthResultHandler;

import javax.swing.ImageIcon;

public class Screen2GUI extends JFrame {

	private JPanel contentPane;
	JLabel lblNewLabel;
	JLabel lblNewLabel2;
	JLabel lblStatus;

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
		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		System.out.println("JFrame Width: "+width+" Height: "+height);
		this.setBounds(0, 0, (int)width, (int)height); 
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
				
		// Debug error label.
		lblStatus = new JLabel("STATUS");
		lblStatus.setVisible(false);
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setOpaque(true);
		lblStatus.setBackground(Color.GRAY);
		lblStatus.setForeground(Color.BLACK);
		lblStatus.setBounds(0, 0, (int)width, 300);
		contentPane.add(lblStatus);

		lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/arrowz2.gif")));
		lblNewLabel.setBounds(260, 280, (int)width, (int)height);
		contentPane.add(lblNewLabel);
		

		// Start screen pic
		lblNewLabel2 = new JLabel("");
		lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
		lblNewLabel2.setBounds(0, 0, (int)width, (int)height);
		contentPane.add(lblNewLabel2);

		// Main.
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Connect to firebase.
				connectFirebase("medea2");
			}
		}).start();
	}

	/** Draw error message on screen */
	private void onError(String error) {
		System.out.println(error);
		lblStatus.setVisible(true);
		lblStatus.setText(error);
		
	}

	/**
	 * Called when screen is activated.
	 */
	private void activateScreen() {
		//picture after klick
		lblNewLabel.setIcon(null);
		lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/medea2whatDOweDO.png")));
	}
	
	/**
	 * Called when screen is deactivated.
	 */
	private void deactivateScreen() {
		lblNewLabel.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/arrowz2.gif")));
		lblNewLabel2.setIcon(new ImageIcon(Screen2GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
	}
	
	private void connectFirebase(String screenName) {
		// Connect to Firebase.
		Firebase fbRef = new Firebase("https://kd401ag1.firebaseio.com/");
		fbRef.authAnonymously(new AuthResultHandler() {
			
			@Override
			public void onAuthenticationError(FirebaseError error) {
			    System.out.println("Login Failed! " + error.toString());
			}
			
			@Override
			public void onAuthenticated(AuthData authData) {
			    // Read value from firebase.
				fbRef.child("screens").addValueEventListener(new ValueEventListener() {
					/**
					 * Called when data is changed.
					 */
				    @Override
				    public void onDataChange(DataSnapshot snapshot) {
				        if(snapshot.child(screenName).getValue(String.class).equals("active")) {
				        	activateScreen();
				        } else {
				        	deactivateScreen();
				        }
				    }
				    
				    /**
				     * Called when canceling.
				     */
				    @Override
				    public void onCancelled(FirebaseError firebaseError) {
				        System.out.println("The read failed: " + firebaseError.getMessage());
				    }
				});
			}
		});
	}
}
