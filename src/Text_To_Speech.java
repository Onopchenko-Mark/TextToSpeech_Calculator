import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Text_To_Speech {

	private static Clip sound;
	private static String soundName;
	private static Thread soundThread;
	
	public static String getFilePath()
	{
		return System.getProperty("user.dir") + "\\sound\\";
	}
	
	public static void playSound(String text)
	{
		ArrayList <String> words = new ArrayList<String>();
		
		// finds all the words in the string and stores them in a list
		while(text.length() > 0)
		{
			try 
			{
				words.add(text.substring(0, text.indexOf(' ')));
			}
			catch (Exception e)
			{
				words.add(text);
				break;
			}
			
			text = text.substring(text.indexOf(' ') + 1, text.length());
		}
	
		soundThread = new Thread()
		{
			@Override
			public void run()
			{
				stopSounds();
				for(String word : words)
				{
					soundName = createSoundFile(word);
				
					try 
					{
						sound = AudioSystem.getClip();
						AudioInputStream ais = AudioSystem.getAudioInputStream(new File(getFilePath() + soundName + ".wav"));
						sound.open(ais);
						sound.start(); // play the sound
						sound.setFramePosition(0);
					} 
					catch (Exception e) 
					{
						System.out.println(getClass() + ": Failed to load a soundfile at the adress of " + 
								getFilePath() + soundName + ".wav");
					}
					try {
						// gives a longer pause to 'E'
						if(word.contentEquals("E"))
							Thread.sleep(3000);
						else
							Thread.sleep(700);
						
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		soundThread.start();
	}
	
	public static void stopSounds()
	{
		try
		{
			sound.isRunning();
		}
		catch (java.lang.NullPointerException e)
		{
			return;
		}
		
		sound.setFramePosition(0); // rewind to the beginning
		sound.stop();
		sound.close();
	}
	
	public static boolean getThreadStatus()
	{
		return soundThread.isAlive();
	}
	
	// This method ensures that while the tutorial or 
	public static boolean stopRecordingSpeech()
	{
		if((soundName.contentEquals("intro") || soundName.contentEquals("tutorial") 
				|| soundName.contentEquals("start")
				|| soundName.contentEquals("stop")) && sound.isRunning())
			return true;
		
		return false;
	}
	
	// returns a soundFile specific to the button's this.getText()
	private static String createSoundFile(String text)
	{	
		switch(text)
		{
			case "1":
				return "one";
				
			case "2":
				return "two";
				
			case "3":
				return "three";
				
			case "4":
				return "four";
				
			case "5":
				return "five";
				
			case "6":
				return "six";
			
			case "7":
				return "seven";
				
			case "8":
				return "eight";
				
			case "9":
				return "nine";
				
			case "0":
				return "zero";
				
			case ".":
				return "point";
				
			case "C":
				return "clear";
				
			case "DEL":
				return "delete";
				
			case "EXP":
				return "exp";
				
			case "e":
				return "euler";
				
			case "x^y":
				return "power";
				
			case "√":
				return "sqrt";
				
			case "mode":
				return "mode";
				
			case "(":
				return "open_bracket";
			
			case ")":
				return "closed_bracket";
				
			case "sin":
				return "sin";
				
			case "cos":
				return "cos";
				
			case "tan":
				return "tan";
				
			case "x!":
				return "factorial";
				
			case "|x|":
				return "abs";
				
			case "ln(x)":
				return "ln";
				
			case "lg(x)":
				return "log";
				
			case "π":
				return "pi";
				
			case "deg":
				return "degrees";
				
			case "rad":
				return "radians";
				
			case "2nd":
				return "2nd";
				
			case "x":
				return "multiply";
				
			case "÷":
				return "divide";
			
			case "+":
				return "plus";
				
			case "-":
				return "minus";
				
			case "=":
				return "equals";
		}
		return text;
	}
}
