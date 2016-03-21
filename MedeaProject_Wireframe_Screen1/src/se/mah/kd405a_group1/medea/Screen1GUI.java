//SCREEN 1

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.Firebase.AuthResultHandler;

import javax.swing.Icon;

import javax.swing.ImageIcon;

import javax.swing.SwingConstants;
import javax.swing.JButton;

public class Screen1GUI extends JFrame  {

	private JPanel contentPane;
	JLabel lblNewLabel;
	JLabel lblNewLabel2;
	SoundcloudPlayer soundcloudPlayer;
	SoundcloudPlayer.Track soundcloudTrack;
	
	boolean firstTimePlayAudio = true;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Screen1GUI frame = new Screen1GUI();
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
	public Screen1GUI() {
		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = 1080;// screenSize.getWidth();
		double height = 1920;// screenSize.getHeight();
		System.out.println("JFrame Width: " + width + " Height: " + height);
		this.setBounds(0, 0, (int) width, (int) height);
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//arrow gif
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/arrowz2.gif")));
		lblNewLabel.setBounds(260, 280, (int)width, (int)height);
		contentPane.add(lblNewLabel);

		
		// Start screen pic
		lblNewLabel2 = new JLabel("");
		lblNewLabel2.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
		lblNewLabel2.setBounds(0, 0, (int)width, (int)height);
		contentPane.add(lblNewLabel2);
		
		// Main.
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Connect to firebase.
				connectFirebase("medea1");
			}
		}).start();
	}

	/**
	 * Called when screen is activated.
	 */
	private void activateScreen() {
		lblNewLabel.setIcon(null);
		lblNewLabel2.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/podcast.jpg"))); //byt till podcastbild
		
		// Play track.
		if(firstTimePlayAudio) {
			// Create soundcloud player.
			soundcloudPlayer = new SoundcloudPlayer("medea-vox");
			//soundcloudPlayer.playTrack("/users/medea-vox/tracks/249648982/");

			// Get a track.
			soundcloudTrack = soundcloudPlayer.getTrack(1);
			
			// Play track.
			soundcloudTrack.play();
			
			firstTimePlayAudio = false;
		}
	}
	
	/**
	 * Called when screen is deactivated.
	 */
	private void deactivateScreen() {
		lblNewLabel.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
		lblNewLabel2.setIcon(null);
		
		// Stop playback.
		if(soundcloudTrack != null) {
			soundcloudTrack.stop();
		}
		soundcloudTrack = null;
		soundcloudPlayer = null;
		
		firstTimePlayAudio = true;
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
	

