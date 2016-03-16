package se.mah.kd405a_group1.medea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ImageIcon;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.JSONTokener;

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
		double width = 400;// screenSize.getWidth();
		double height = 400;// screenSize.getHeight();
		System.out.println("JFrame Width: " + width + " Height: " + height);
		this.setBounds(0, 0, (int) width, (int) height);
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.PINK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/wr_2-01.jpg")));
		lblNewLabel.setBounds(0, 0, (int) width, (int) height);
		contentPane.add(lblNewLabel);

		// playSound("MEDEA-VOX-160301-EXTREMIST-COMMUNICATION.mp3");
		// testPlay(Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/MEDEA-VOX-160301-EXTREMIST-COMMUNICATION.mp3"));
		try {
			// getResource("/users/medea-vox");
			//getResource("/users/medea-vox/tracks");
			
			String clientId = "?client_id=207f6c2075addf48873c9b0f281de38a";

			// Get track info.
			JSONObject jsonTrack = getResource("/users/medea-vox/tracks/249648982/" + clientId);
			SoundcloudTrack track = new SoundcloudTrack();
			track.url = jsonTrack.getString("download_url");
			track.title = jsonTrack.getString("title");
			track.date = jsonTrack.getString("last_modified");
			track.description = jsonTrack.getString("description");
			track.waveformUrl = jsonTrack.getString("waveform_url");

			// Get download URI.
			//JSONObject jsonDownload = new JSONObject(new JSONTokener(new URL(track.url).openStream()));
			//JSONObject jsonDownload = getResource(track.url"https://api.soundcloud.com/tracks/249648982/download" + clientId);
			JSONObject jsonDownload = getResource(track.url + clientId);
			String downloadURL = jsonDownload.getString("location");
			
			// Play stream.
			testPlay(new URL(downloadURL));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void testPlay(URL url) {
		new Thread(new Runnable() {
			public void run() {
				try {
					// Ugly way of looping audio.
					while (true) {
						/*// Open audio stream.
						InputStream inputStream = url.openStream();
						BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
						AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);*/

						// Open stream.
						MpegAudioFileReader mpgFileReader = new MpegAudioFileReader();
						InputStream is = url.openStream();
						AudioInputStream inputStream = mpgFileReader.getAudioInputStream(is);

						// Setup format.
						AudioFormat baseFormat = inputStream.getFormat();
						AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
								baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
								baseFormat.getSampleRate(), false);
						
						// Open input stream.
						AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);

						// Play audio.
						rawplay(targetFormat, audioInputStream);

						// Close stream.
						audioInputStream.close();

						// Stop if thread is interrupted.
						if (Thread.interrupted()) {
							break;
						}
						try {
							Thread.sleep(1);
						} catch (InterruptedException iex) {
							break;
						}
						return;
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void rawplay(AudioFormat targetFormat, AudioInputStream inputStream)
			throws IOException, LineUnavailableException {
		byte[] audioBuffer = new byte[4096];

		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			int bytesRead = 0;
			int bytesWritten = 0;

			// Start audio output.
			line.start();

			while (bytesRead != -1) {
				// Read audio buffer.
				bytesRead = inputStream.read(audioBuffer, 0, audioBuffer.length);

				// Only write out data if available.
				if (bytesRead != -1) {
					// Write to output.
					bytesWritten = line.write(audioBuffer, 0, bytesRead);
				}
			}

			// Stop and close output and stream.
			line.drain();
			line.stop();
			line.close();
			inputStream.close();
		}
	}

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
	
	class SoundcloudTrack {
		public String url;
		public String title;
		public String date;
		public String description;
		public String waveformUrl;
		
		public SoundcloudTrack(JSONObject jsonTrack) {
		}
	}

	/**
	 * Get Soundcloud resource.
	 * @param resourceUri URI to the resource.
	 * @return Resource as a JSON object.
	 */
	public JSONObject getResource(String resourceUri) throws Exception {
		JSONObject jsonResource = new JSONObject();

		// Create API wrapper for Soundcloud.
		final ApiWrapper wrapper = new ApiWrapper("207f6c2075addf48873c9b0f281de38a",
				"9a11033e386b15d510baddd16271e908", null, null);
		
		// No need for login.
		// wrapper.login("username", "password");
		
		// Request resource.
		final Request resource = Request.to(resourceUri);
		
		// Debug.
		//System.out.println("GET " + resource);

		// Get resource.
		try {
			HttpResponse response = wrapper.get(resource);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// Resource found (Status: 200).
				jsonResource = Http.getJSON(response);

				// Debug.
				//System.out.println(jsonResource.toString());
			} else if(response.getStatusLine().getStatusCode() == 302) {
				// Resource found (Status: 302).
				jsonResource = Http.getJSON(response);

				// Debug.
				//System.out.println(jsonResource.toString());
			} else {
				// Resource not found.
				// Debug.
				System.err.println("Invalid status received: " + response.getStatusLine());
			}
		} finally {
			// Debug.
			// Serialize wrapper state again (token might have been refreshed).
			System.out.println(wrapper.toString());
		}
		
		return jsonResource;
	}

	/**
	 * NOT USED ANYMORE.
	 * @param url
	 */
	public static synchronized void playSound(final String url) {
		new Thread(new Runnable() {
			public void run() {
				try {
					// Open audio stream.
					BigClip clip = new BigClip(AudioSystem.getClip());
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(
							Screen1GUI.class.getResource("/se/mah/kd405a_group1/medea/res/" + url));
					clip.open(inputStream);

					// Enable looping.
					clip.loop(Clip.LOOP_CONTINUOUSLY);

					// Start clip.
					clip.start();
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException iex) {
							iex.printStackTrace();
							return;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

}
