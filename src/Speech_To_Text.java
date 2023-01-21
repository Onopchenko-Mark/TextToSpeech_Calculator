import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class Speech_To_Text {
	
	private final static HashMap<String, Integer> numsToSymbols = new HashMap<>();
	private final static HashMap<String, Integer> magnitudesToSymbols = new HashMap<>();
	private final static HashMap<String, String> operatorsToSymbols = new HashMap<>();
	
	private static LiveSpeechRecognizer recognizer;
	private static boolean ignoreSpeechRecognitionResults = true;
	
	// Stores words from LiveSpeechRecognizer just like they are pronounced
	private static String speechRecognitionResult = "";
	// Stores the mathematical expression itself
	private ArrayList<String> finalText;
	// String used for conversion between speechRecognitionResult and finalText
	private String currentNum;
	
	Speech_To_Text() {
		// Configuration
		Configuration configuration = new Configuration();
		
		// Load english language model for speech recognition
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		
		// Grammar (only particular words get recognized)
		configuration.setGrammarPath("resource:/grammars");
		configuration.setGrammarName("grammar");
		configuration.setUseGrammar(true);
		
		try 
		{
			recognizer = new LiveSpeechRecognizer(configuration);
		} 
		catch (IOException ex) 
		{
			System.out.println(getClass() + ": Microphone is not available.");
			return;
		}
		
		// Fill the hashmaps with elements
		createNumsToSymbols();
		createMagnitudesToSymbols();
		createOperatorsToSymbols();
		
		speechRecognition.start();
	}

	private Thread speechRecognition = new Thread()
	{
		@Override
		public void run()
		{
			System.out.println("Loading Speech Recognizer...");
			
			//Start Recognition
			recognizer.startRecognition(true);
			
			while (true) 
			{
				SpeechResult speechResult = recognizer.getResult();
				
				//Check if we ignore the speech recognition results
				//Check the result
				if (speechResult == null)
					continue;
				
				// Get the hypothesis
				String hypothesis = speechResult.getHypothesis();
				
				if(hypothesis.contains(" "))
					hypothesis = hypothesis.substring(0, hypothesis.indexOf(" "));
				
				// When the introductory message is playing, stop recording speech
				if(Text_To_Speech.stopRecordingSpeech())
					continue;
				
				switch(hypothesis)
				{
					case "tutorial":
						Text_To_Speech.playSound("tutorial");
						ignoreSpeech();
						speechResult = null;
					break;
					
					case "start":
						System.out.println("Recording Started");
						Text_To_Speech.playSound("start");
						ignoreSpeechRecognitionResults = false;
						speechResult = null;
					continue;
					
					case "calculate":
						CalculatorClass.updateForButtons("=");
						
					try {Thread.sleep(2000);} 
					catch (InterruptedException e) {}
						
					ignoreSpeech();
					
					CalculatorClass.setAnswerMode(false);
					speechResult = null;
					continue;
						
					case "stop":
						System.out.println("Recording Stopped");
						Text_To_Speech.playSound("stop");
						ignoreSpeechRecognitionResults = true;
					break;
					
					case "read":
						Text_To_Speech.playSound(speechRecognitionResult);
						ignoreSpeech();
						speechResult = null;
					continue;
				}
				
				// Print the hypothesis, if the results are being recognized
				if(hypothesis.contentEquals("") || ignoreSpeechRecognitionResults)
					continue;
				
				switch(hypothesis)
				{
					case "sign":
						speechRecognitionResult += "sin ";
					break;
					
					case "coast":
						speechRecognitionResult += "cos ";
					break;
					
					case "tangent":
						speechRecognitionResult += "tan ";
					break;
					
					case "natural":
						speechRecognitionResult += "ln ";
					break;
					
					case "pie":
						speechRecognitionResult += "pi ";
					break;
					
					case "east":
						speechRecognitionResult += "e ";
					break;
					
					default:
						if(hypothesis.contentEquals("erase"))
						{
							// deletes the space after the last word entered
							speechRecognitionResult = speechRecognitionResult.substring(0, speechRecognitionResult.length()-1);
							// deletes the last word entered
							if(speechRecognitionResult.contains(" "))
								speechRecognitionResult = speechRecognitionResult.substring(0, speechRecognitionResult.lastIndexOf(" ") + 1);
							else
								speechRecognitionResult = "";
							
							CalculatorClass.deleteCharacter();
						}
						else if(hypothesis.contentEquals("clear"))
							speechRecognitionResult = "";
						else
							speechRecognitionResult += hypothesis + " ";
				}
				
				CalculatorClass.setInputFieldText("0");
				CalculatorClass.setOutputFieldText(" ");
				
				convert(speechRecognitionResult);
				CalculatorClass.setSpeechFieldText(speechRecognitionResult);
				for(String i : finalText)
					CalculatorClass.addCharacter(i);
				
			}
		}
	};
	
	private void ignoreSpeech()
	{
		while(Text_To_Speech.getThreadStatus())
		{
			ignoreSpeechRecognitionResults = true;
			
			try {Thread.sleep(500);} 
			catch (InterruptedException e) {}
		}
		
		ignoreSpeechRecognitionResults = false;
	}

	private void convert(String initialText)
	{
		finalText = new ArrayList<String>();
		currentNum = "";
		
		while(!initialText.isEmpty())
		{
			// The word is taken from the original String and stored separately
			String word = initialText.substring(0, initialText.indexOf(' '));
			// The word is deleted from the original String
			initialText = initialText.substring(initialText.indexOf(' ') + 1);
			
			if(numsToSymbols.containsKey(word))
			{
				if(!currentNum.isEmpty())
					currentNum += "+";
				
				currentNum += numsToSymbols.get(word);
				
				if(initialText.isEmpty())
					finalText.add(CalculatorClass.brackets(currentNum));
			}
			else if(magnitudesToSymbols.containsKey(word))
			{
				if(!currentNum.isEmpty())
					currentNum += "*";
				
				currentNum += magnitudesToSymbols.get(word);
			}
			else if(operatorsToSymbols.containsKey(word))
			{
				if(!currentNum.isEmpty())
				{
					finalText.add(CalculatorClass.brackets(currentNum));
					currentNum = "";
				}
					
				finalText.add(operatorsToSymbols.get(word));
			}
			
			// Checks for compound phrases
			if(!initialText.isEmpty())
				compoundPhrases(initialText, word);
		}
	}
	
	/*
	 This function specifically checks for compound phrases
	 being requested by the user such as 'square root', '___ bracket', etc. 
	 (this is the only time when the program needs to know the next word
	 which comes after the currently selected one)
	 */
	private void compoundPhrases(String initialText, String currentWord)
	{
		String nextWord = initialText.substring(0, initialText.indexOf(' '));
		String compoundSymbol = "";
		boolean isCompound = false;
	
		// if the phrase "___ bracket" is said by the user, add the proper bracket character to string
		if((currentWord.contentEquals("open") || currentWord.contentEquals("closed")) && 
			nextWord.contentEquals("bracket"))
		{
			isCompound = true;
			
			if(currentWord.contentEquals("open"))
			{
				speechRecognitionResult.replace("open bracket", "open_bracket");
				compoundSymbol = "(";
			}
			else
			{
				speechRecognitionResult.replace("closed bracket", "closed_bracket");
				compoundSymbol = ")";
			}
				
		}
		else if(currentWord.contentEquals("square") && nextWord.contentEquals("root"))
		{
			isCompound = true;
			speechRecognitionResult.replace("square root", "sqrt");
			compoundSymbol = "√";
		}
		else if(currentWord.contentEquals("arc"))
		{
			isCompound = true;
			
			if(nextWord.contentEquals("sign"))
				compoundSymbol = "arcsin(";
			else if(nextWord.contentEquals("coast"))
				compoundSymbol = "arccos(";
			else if(nextWord.contentEquals("tangent"))
				compoundSymbol = "arctan(";
			else
				isCompound = false;
		}
		
		if(!isCompound)
			return;
		
		// deletes two words at the same time
		initialText = initialText.substring(initialText.indexOf(' ') + 1);
					
		if(!currentNum.isEmpty())
		{
			finalText.add(CalculatorClass.brackets(currentNum));
			currentNum = "";
		}
		finalText.add(compoundSymbol);
	}
	
	public static void clearSpeechRecognitionResult()
	{
		speechRecognitionResult = "";
	}
	
	private static void createNumsToSymbols()
	{
		numsToSymbols.put("zero", 0);
		numsToSymbols.put("one", 1);
		numsToSymbols.put("two", 2);
		numsToSymbols.put("three", 3);
		numsToSymbols.put("four", 4);
		numsToSymbols.put("five", 5);
		numsToSymbols.put("six", 6);
		numsToSymbols.put("seven", 7);
		numsToSymbols.put("eight", 8);
		numsToSymbols.put("nine", 9);
		numsToSymbols.put("ten", 10);
		numsToSymbols.put("eleven", 11);
		numsToSymbols.put("twelve", 12);
		numsToSymbols.put("thirteen", 13);
		numsToSymbols.put("fourteen", 14);
		numsToSymbols.put("fifteen", 15);
		numsToSymbols.put("sixteen", 16);
		numsToSymbols.put("seventeen", 17);
		numsToSymbols.put("eighteen", 18);
		numsToSymbols.put("nineteen", 19);
		numsToSymbols.put("twenty", 20);
		numsToSymbols.put("thirty", 30);
		numsToSymbols.put("fourty", 40);
		numsToSymbols.put("fifty", 50);
		numsToSymbols.put("sixty", 60);
		numsToSymbols.put("seventy", 70);
		numsToSymbols.put("eighty", 80);
		numsToSymbols.put("ninety", 90);
	}
	
	private static void createMagnitudesToSymbols()
	{
		magnitudesToSymbols.put("hundred", 100);
		magnitudesToSymbols.put("thousand", 1000);
		magnitudesToSymbols.put("million", 1000000);
		magnitudesToSymbols.put("billion", 1000000000);
	}
	
	private static void createOperatorsToSymbols()
	{
		operatorsToSymbols.put("plus", "+");
		operatorsToSymbols.put("minus", "-");
		operatorsToSymbols.put("multiply", "*");
		operatorsToSymbols.put("times", "*");
		operatorsToSymbols.put("divided", "/");
		operatorsToSymbols.put("over", "/");
		operatorsToSymbols.put("point", ".");
		operatorsToSymbols.put("pi", "π");
		operatorsToSymbols.put("e", "e"); // Euler's number
		operatorsToSymbols.put("sin", "sin("); // sin
		operatorsToSymbols.put("cos", "cos("); // cos
		operatorsToSymbols.put("tan", "tan(");
		operatorsToSymbols.put("log", "log(");
		operatorsToSymbols.put("ln", "ln("); //ln(x)
		operatorsToSymbols.put("power", "^");
		operatorsToSymbols.put("squared", "^2");
		operatorsToSymbols.put("factorial", "!");
	}
}