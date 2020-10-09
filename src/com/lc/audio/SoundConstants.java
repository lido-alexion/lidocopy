package com.lc.audio;

public class SoundConstants {

	/**
	 * The higher the sampling rate, the more samples are required for a fixed
	 * amount of time, the more memory is required, and the more computational
	 * demands are placed on the computer to be able to handle the audio data in
	 * real time.
	 */
	static float sampleRate = 16000.0F; // Allowable 8000, 11025, 16000, 22050,
	// 44100

	/**
	 * an 8-bit sample can record a dynamic volume range of only 127 to 1.
	 * 16-bit signed samples can record a dynamic volume range of 32,767 to 1
	 */
	static int sampleSizeInBits = 16; // Allowable 8, 16

	/**
	 * channels: monaural (one channel) and stereo (two channel) sound
	 */
	static int channels = 1; // Allowable 1, 2

	/**
	 * Java allows for the use of either signed or unsigned audio data. However,
	 * because Java does not support unsigned integer types (as does C and C++),
	 * extra work is required to create synthetic sound for unsigned data.
	 */
	static boolean signed = true; // Allowable true, false

	/**
	 * bigEndian : true means bigEndian (MSB first) and false means
	 * littleEndian(LSB first) However, Java stores everything in bigEndian
	 * format, so extra work is required to use littleEndian
	 */
	static boolean bigEndian = true; // Allowable true, false

	/**
	 * An audio data buffer for synthetic data : byte array with a length of
	 * 64000 bytes. Each of the synthetic sound data generators deposits the
	 * synthetic sound data in this array when it is invoked.
	 * 
	 * At 16-bits per sample and 16000 samples per second, this array can
	 * contain two seconds of monaural (one-channel) data or one second of
	 * stereo (two-channel) data.
	 * 
	 * You can change the length of the audio data by changing the size of this
	 * array. However, for some reasons, you should make the size of the array
	 * an even multiple of four
	 */
	static byte audioData[] = new byte[16000 * 4];

	/**
	 * number of bytes per sample: Each channel requires two 8-bit bytes per
	 * 16-bit sample.
	 * 
	 * For one-channel (monaural) data, the value of bytesPerSamp is set to 2.
	 * (for stereo data, the number of bytes per sample would be 4
	 */
	static int bytesPerSamp;

	/**
	 * length of the audio data in samples: computes and saves the required
	 * length of the synthetic sound data in samples by dividing the length of
	 * the audioData array (byteLength, see Listing 14) by the number of bytes
	 * per sample (bytesPerSamp).
	 */
	static int sampLength;

	/**
	 * Why was a frequency 950 Hz used?
	 * 
	 * The frequency of 950 Hz was chosen because it is well within the spectral
	 * hearing range of most people, and it is within the spectral reproduction
	 * range of most computer speakers. However, the choice was arbitrary. Any
	 * other frequency that meets the above requirements should work just as
	 * well.
	 */

	static double freq = 950.0;

}
