package se.mah.kd405a_group1.medea;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class SoundcloudPlayer {
	static final String clientId = "207f6c2075addf48873c9b0f281de38a";
	static final String clientSecret = "9a11033e386b15d510baddd16271e908";


	public SoundcloudPlayer(String trackURI) {
		try {
			// Get user info.
			// getResource("/users/medea-vox");
			
			// Get tracks.
			// getResource("/users/medea-vox/tracks");

			// Get track info.
			SoundcloudTrack track = new SoundcloudTrack(getResource(trackURI + "?client_id=" + clientId));
			
			// Play track.
			track.play();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Represents a Soundcloud track.
	 */
	class SoundcloudTrack {
		/** URL to the track. */
		public String url;
		
		/** Title of the track. */
		public String title;
		
		/** Create/modify date */
		public String date;
		
		/** Description of track */
		public String description;
		
		/** URL to thte waveform image */
		public String waveformUrl;
		
		/**
		 * Track constructor.
		 * @param jsonTrack Track info.
		 */
		public SoundcloudTrack(JSONObject jsonTrack) {
			this.url = jsonTrack.getString("download_url");
			this.title = jsonTrack.getString("title");
			this.date = jsonTrack.getString("last_modified");
			this.description = jsonTrack.getString("description");
			this.waveformUrl = jsonTrack.getString("waveform_url");
		}
		
		/**
		 * Retries the URL of the stream from the track info.
		 * @return A URL object of the stream.
		 * @throws Exception
		 */
		private URL getURL() throws Exception {
			// Get download URI.
			// JSONObject jsonDownload = new JSONObject(new JSONTokener(new
			// URL(track.url).openStream()));
			// JSONObject jsonDownload = getResource(track.url"https://api.soundcloud.com/tracks/249648982/download?client_id=" + clientId);
			JSONObject jsonDownload = getResource(this.url + "?client_id=" + clientId);
			String downloadURL = jsonDownload.getString("location");

			// Play stream.
			return new URL(downloadURL);
		}

		/**
		 * Plays a Soundcloud track from a URL
		 * @param url URL to track that should be played.
		 */
		public void play() {
			// Create a new thread for playback.
			new Thread(new Runnable() {
				public void run() {
					try {
						// Ugly way of looping audio.
						while (true) {
							/*
							// Open audio stream.
							InputStream inputStream = url.openStream();
							BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
							AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
							*/

							
							// Open audio stream.
							MpegAudioFileReader mpgFileReader = new MpegAudioFileReader();
							InputStream is = getURL().openStream();
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
						}
					} catch (Exception e) {
						// Unexpected error.
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}).start();
		}

		/**
		 * Plays a raw stream.
		 * @param targetFormat Output format.
		 * @param inputStream Stream containing raw data.
		 * @throws IOException 
		 * @throws LineUnavailableException
		 */
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
	}

	/**
	 * Get Soundcloud resource.
	 * @param resourceUri URI to the resource.
	 * @return Resource as a JSON object.
	 */
	public JSONObject getResource(String resourceUri) throws Exception {
		JSONObject jsonResource = new JSONObject();

		// Create API wrapper for Soundcloud.
		final ApiWrapper wrapper = new ApiWrapper(clientId, clientSecret, null, null);
		
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
	 * TODO: Remove this.
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
