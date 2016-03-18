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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class Screen1GUI extends JFrame {

	private JPanel contentPane;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		System.out.println("JFrame Width: " + width + " Height: " + height);
		this.setBounds(0, 0, (int) width, (int) height);
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.PINK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		/*JLabel lblNewLabel2 = new JLabel("");
		lblNewLabel2.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MedeaStart.png")));
		lblNewLabel2.setBounds(0, 0, (int)width, (int)height);
		contentPane.add(lblNewLabel2);*/
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/pil.gif")));
		lblNewLabel.setBounds(0, 0, (int)width, (int)height);
		contentPane.add(lblNewLabel);

		JLabel lblNewLabel2 = new JLabel("");
		lblNewLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel2.setBounds(0, 0, (int)width, (int)height);
		contentPane.add(lblNewLabel2);
		
		// Test soundcloud player.
		SoundcloudPlayer soundcloudPlayer = new SoundcloudPlayer("medea-vox");
		//soundcloudPlayer.playTrack("/users/medea-vox/tracks/249648982/");
		
		// Get a track.
		SoundcloudPlayer.Track track = soundcloudPlayer.getTrack(1);
		
		// Play track.
		track.play();
		try {
			// Fetch the audio waveform image.
			lblNewLabel.setIcon(new ImageIcon(new URL(track.getWaveformURL())));
			lblNewLabel2.setText("Time:" + (track.getCurrentPos() / 1000) + "/" + (track.getDuration() / 1000));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				DecimalFormat decimalFormat = new DecimalFormat("00");
				while(true) {
					int c = track.getCurrentPos();
					int sc = c / 1000;
					int mc = (sc > 0) ? sc / 1000 : 0;
					int hc = (mc > 0) ? mc / 60 : 0;
					int d = track.getDuration();
					int sd = d / 1000;
					int md = (sd > 0) ? sd / 1000 : 0;
					int hd = (md > 0) ? md / 60 : 0;
					lblNewLabel2.setText("Time - " + ((hd > 0) ? decimalFormat.format(hc) : "") + ":" + decimalFormat.format(mc) +
							":" + decimalFormat.format(sc) + "/" + ((hd > 0) ? decimalFormat.format(hd) : "") + ":" +
							decimalFormat.format(md) + ":" + decimalFormat.format(sd));
					try {
						Thread.sleep(900);
					} catch(InterruptedException ex) {
						break;
					}
				}
			}
			
		}).start();
	}
}
