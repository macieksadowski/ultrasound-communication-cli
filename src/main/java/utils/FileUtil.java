package utils;

import java.io.File;
import java.io.FileWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class FileUtil {
    public static boolean saveToAudioFile(String name, AudioFileFormat.Type fileType, AudioInputStream audioInputStream) {
        System.out.println("Saving...");
        if (null == name || null == fileType || audioInputStream == null) {
            return false;
        }
        File myFile = new File(name + "." + fileType.getExtension());
        try {
            audioInputStream.reset();
        } catch (Exception e) {
            return false;
        }
        int i = 0;
        while (myFile.exists()) {
            String temp = "" + i + myFile.getName();
            myFile = new File(temp);
        }
        try {
            AudioSystem.write(audioInputStream, fileType, myFile);
        } catch (Exception ex) {
            return false;
        }
        System.out.println("Saved " + myFile.getAbsolutePath());
        return true;
    }
    
    public static boolean saveToFile(String name, short[] data) {
    	//System.out.println("Saving...");
        if (null == name || null == data) {
            return false;
        }
        File myFile = new File(name + ".csv");
        int i = 0;
        while (myFile.exists()) {
            String temp = "" + i + myFile.getName();
            myFile = new File(temp);
            i++;
        }
        try {
        	FileWriter writer = new FileWriter(myFile);
        	int len = data.length;
        	for (int j = 0; j < len; j++) {
        	   writer.write(data[j] + "," + "");
        	}
        	writer.close();
            
        } catch (Exception ex) {
            return false;
        } 
        //System.out.println("Saved " + myFile.getAbsolutePath());
        return true;
    }
    
	public static short[] ByteToShort(byte[] input) {
		int short_index, byte_index;
		int iterations = input.length / 2;
		
		short[] buffer = new short[input.length / 2];
		
		short_index = byte_index = 0;
		
		for(;short_index != iterations;) {
			buffer[short_index] = (short) (((input[byte_index] & 0x00FF) | (input[byte_index + 1] << 8)));
			
			++short_index;
			byte_index += 2;
		}
		
		return buffer;
	}
}
