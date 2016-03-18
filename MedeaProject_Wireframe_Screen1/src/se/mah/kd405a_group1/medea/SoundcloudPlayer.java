package se.mah.kd405a_group1.medea;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class SoundcloudPlayer {
	static final String clientId = "207f6c2075addf48873c9b0f281de38a";
	static final String clientSecret = "9a11033e386b15d510baddd16271e908";

	String user;
	
	/** Contains the available tracks on soundcloud */
	ArrayList<Track> tracks;

	public SoundcloudPlayer(String user) {
		// Set username.
		this.user = user;
		
		// Create list for tracks.
		this.tracks = new ArrayList<Track>();

		try {
			// Get user info.
			// getResource("/users/" + this.user);
			
			// Get tracks.
			JSONObject jsonTracks = getResource("/users/" + this.user + "/tracks" + "?client_id=" + clientId, true);
			JSONArray tracks = jsonTracks.getJSONArray("array");

			// Add tracks to list.
			Iterator<Object> it = tracks.iterator();
			while(it.hasNext()){
				JSONObject jsonTrack = (JSONObject)(it.next());
				this.tracks.add(new Track(jsonTrack));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		/*// Debug
		System.out.println("Available tracks:");
		for(Track track : this.tracks) {
			System.out.println(track.title);
		}*/
	}

	/**
	 * Number of available tracks.
	 * @return Number of available tracks.
	 */
	public int getNumberOfTracks() {
		return tracks.size();
	}
	
	/**
	 * Returns a track object..
	 * @param index Index of the track.
	 * @return Requested track.
	 */
	public Track getTrack(int index) {
		return tracks.get(index);
	}
	
	/**
	 * Play a track from soundcloud.
	 * @param trackURI URI of the track.
	 */
	public void playTrack(String trackURI) {
		try {
			// Get track info.
			Track track = new Track(getResource(trackURI + "?client_id=" + clientId, false));
			
			// Play track.
			track.play();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Represents a Soundcloud track.
	 */
	public class Track {
		/** URL to the track. */
		private String url;
		
		/** Title of the track. */
		private String title;
		
		/** Create/modify date */
		private String date;
		
		/** Description of track */
		private String description;
		
		/** URL to the artwork image */
		private String artworkUrl;
		
		/** URL to the waveform image */
		private String waveformUrl;
		
		/** Current play position in ms */
		private int currentPos;
		
		/** Track duration in ms */
		private int duration; 
		
		/**
		 * Track constructor.
		 * @param jsonTrack Track info.
		 */
		public Track(JSONObject jsonTrack) {
			this.url = jsonTrack.getString("download_url");
			this.title = jsonTrack.getString("title");
			this.date = jsonTrack.getString("last_modified");
			this.description = jsonTrack.getString("description");
			this.waveformUrl = jsonTrack.getString("waveform_url");
			this.currentPos = 0;
			this.duration = jsonTrack.getInt("duration");

			// Get artwork URL.
			if(!jsonTrack.isNull("artwork_url")) {
				this.artworkUrl = jsonTrack.getString("artwork_url");
			} else {
				this.artworkUrl = "";
			}
		}

		/**
		 * Title of the track.
		 * @return Title of the track.
		 */
		public String getTitle() {
			return this.title;
		} 
		
		/**
		 * Create/modify date of the track.
		 * @return Create/modify date of the track.
		 */
		public String getDate() {
			return this.date;
		} 
		
		/**
		 * Description of the track.
		 * @return Description of the track.
		 */
		public String getDescription() {
			return this.description;
		} 
		
		/**
		 * URL to a image of the tracks waveform.
		 * @return URL to a image of the tracks waveform.
		 */
		public String getWaveformURL() {
			return this.waveformUrl;
		} 
		
		/**
		 * Current play position in ms.
		 * @return Current play position in ms
		 */
		public int getCurrentPos() {
			return this.currentPos;
		}

		/**
		 * Track duration in ms.
		 * @return Track duration in ms.
		 */
		public int getDuration() {
			return this.duration;
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
			JSONObject jsonDownload = getResource(this.url + "?client_id=" + clientId, false);
			String downloadURL = jsonDownload.getString("location");

			// Play stream.
			return new URL(downloadURL);
		}

		/**
		 * Plays a Soundcloud track from a URL
		 * @param url URL to track that should be played.
		 */
		public void play() {
			new PlaybackThread().start();
		}
		
		/**
		 * Handels track playback.
		 */
		private class PlaybackThread extends Thread {
			/**
			 * Handels playback.
			 */
			@Override
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
						AudioInputStream inputStream = mpgFileReader.getAudioInputStream(new BufferedInputStream(is));

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

						// Update current playback position.
						currentPos = (int)getMicrosecondPosition(line);
						
						/*// Debug
						System.out.println("current pos: " + currentPos / 1000);*/
					}

					// Stop and close output and stream.
					line.drain();
					line.stop();
					line.close();
					inputStream.close();
				}
			}

			/**
			 * Returns a audio line in the format specified.
			 * @param audioFormat Format of the audio line.
			 * @return The audio line.
			 * @throws LineUnavailableException
			 */
			private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
				SourceDataLine res = null;
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
				res = (SourceDataLine) AudioSystem.getLine(info);
				res.open(audioFormat);
				return res;
			}

			int framePosition = 0;

		    private long convertFramesToMilliseconds(SourceDataLine dataLine, int frames) {
		        return (frames / (long) dataLine.getFormat().getSampleRate()) * 1000;
		    }

		    public long getMicrosecondPosition(SourceDataLine dataLine) {
		    	return convertFramesToMilliseconds(dataLine, getFramePosition(dataLine));
		    }

		    public int getFramePosition(SourceDataLine dataLine) {
				return framePosition + dataLine.getFramePosition();
		    }
		}
	}

	/**
	 * Get Soundcloud resource.
	 * @param resourceUri URI to the resource.
	 * @return Resource as a JSON object.
	 */
	private JSONObject getResource(String resourceUri, boolean isArray) throws Exception {
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
				if(isArray) {
					// Soundcloud returns invalid JSON for arrays - so fix it!
					jsonResource = new JSONObject(new JSONTokener("{\"array\":" + Http.getString(response) + "}"));
				} else {
					jsonResource = Http.getJSON(response);
				}

				/*// Debug
				System.out.println(Http.formatJSON(jsonResource.toString()));*/
			} else if(response.getStatusLine().getStatusCode() == 302) {
				// Resource found (Status: 302).
				jsonResource = Http.getJSON(response);

				/*// Debug.
				System.out.println(Http.formatJSON(jsonResource.toString()));*/
			} else {
				// Resource not found.
				// Debug.
				System.err.println("Invalid status received: " + response.getStatusLine());
			}
		} finally {
			/*// Debug.
			// Serialize wrapper state again (token might have been refreshed).
			System.out.println(wrapper.toString());*/
		}
		
		return jsonResource;
	}

	/**
	 * NOT USED ANYMORE.
	 * TODO: Remove this.
	 * @param url
	 */
	private static synchronized void playSound(final String url) {
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
