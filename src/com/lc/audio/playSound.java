package com.lc.audio;

public class playSound extends Thread {

	/**
	 * This is a working buffer used to transfer the data between the
	 * AudioInputStream and the SourceDataLine. The size is rather arbitrary.
	 */
	byte playBuffer[] = new byte[16384];

	public void run() {
		try {

			/**
			 * Open and start the SourceDataLine
			 */
			Synthesize.sourceDataLine.open(Synthesize.audioFormat);
			Synthesize.sourceDataLine.start();

			int cnt;
			/**
			 * Transfer the audio data to the speakers
			 */
			while ((cnt = Synthesize.audioInputStream.read(playBuffer, 0, playBuffer.length)) != -1) {

				/**
				 * Keep looping until the input read method returns -1 for empty
				 * stream.
				 */
				if (cnt > 0) {
					/**
					 * Write data to the internal buffer of the data line where
					 * it will be delivered to the speakers in real time
					 */
					Synthesize.sourceDataLine.write(playBuffer, 0, cnt);
				}
			}

			/**
			 * Block and wait for internal buffer of the SourceDataLine to
			 * become empty.
			 */
			Synthesize.sourceDataLine.drain();

			/**
			 * Finish with the SourceDataLine
			 */
			Synthesize.sourceDataLine.stop();
			Synthesize.sourceDataLine.close();

		} catch (Exception e) {
		}

	}
}
