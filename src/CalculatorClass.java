import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
 
public class CalculatorClass 
{
	private final Color bgColor = new Color(0.031f, 0.00784f, 0.14118f, 1.f);
	private final static Font titleFont = new Font("Verdana", Font.BOLD, 60);
	private final static Font smallFont = new Font("Verdana", Font.BOLD, 36);
	private static final String[][] standartModeButtons = {
			{"C", "DEL", "√", "÷"}, 
			{"7", "8", "9", "x"}, 
			{"4", "5", "6", "-"}, 
			{"1", "2", "3", "+"}, 
			{"mode", "0", ".", "="}};
	private static final String[][] expandedModeButtons = {
			{"2nd", "sin", "cos", "tan", "π"}, 
			{"deg", "ln(x)", "log(x)", "(", ")"},
			{"EXP", "C", "DEL", "√", "÷"},
			{"x^y", "7", "8", "9", "x"}, 
			{"x^2", "4", "5", "6", "-"}, 
			{"x!", "1", "2", "3", "+"}, 
			{"mode", "e", "0", ".", "="}};
	private static final char[] operatorsPriority = {'$', 'c', 't', 'S', 'C', 'T', 'l', 'L', '√', '^', '!', '/', '*', '-', '+'};
	private static ArrayList<Integer> operatorsIndexes;
	private static ArrayList<Character> operatorsNames;
	
	private static JFrame f;
	
	private static JLabel speechField; // this input field is only enabled in accessibility mode and it displays user's speech
	private static JLabel inputField;
	private static JLabel outputField;
	
	public static JPanel buttonPanel;
	
	private static boolean accessibilityMode = Intro.getAccessibilityMode();
	private static boolean degrees = false;
	private static boolean scientificMode = false;
	private static boolean arcMode = false; // mode that is responsible for the arcsin, arccos and arctan
	
	CalculatorClass()
	{
		EventQueue.invokeLater(new Runnable() 
		{
            @Override
            public void run() {
            	if(accessibilityMode)
            		loadSpeechClass.start();
            	
            	initializeFrame();
            }
		});
	}
	
	// A thread that loads the resource-heavy Speech_To_Text class
	Thread loadSpeechClass = new Thread()
	{
		@Override
		public void run()
		{
			new Speech_To_Text();
		}
	};
	
	// Initializes the calculator frame
	void initializeFrame()
	{
		f = new JFrame("Calculator");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setBackground(bgColor);
		
		// Dynamically scales the application depending on the screen resolution
		int height = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.5);
		int width = (int)(height / 0.9);
		
		f.setPreferredSize(new Dimension(width, height));
		f.add(initializeComponents());
		f.addKeyListener(l);
		f.setFocusable(true);
		f.requestFocus();
        f.setFocusTraversalKeysEnabled(false);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setResizable(false);
		
		if(accessibilityMode)
			f.setAlwaysOnTop(true);
		
		f.setVisible(true);
	}
	
	//private static JScrollPane inputFieldScroll;
	//private static JScrollPane outputFieldScroll;
	Component initializeComponents()
	{
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new GridLayout(2,1));
		buttonPanel = new JPanel();
		
		// when accessibility mode is enabled, the speechField line is added to the calculator's interface
		if(accessibilityMode)
		{
			topPanel.setLayout(new GridLayout(3,1));
			speechField = new JLabel(" ", SwingConstants.RIGHT);
			speechField.setFont(titleFont);
			speechField.setOpaque(true);
			speechField.setBackground(bgColor);
			speechField.setForeground(Color.WHITE);
			
			JScrollPane speechFieldScroll = new JScrollPane(speechField, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			speechFieldScroll.setBorder(BorderFactory.createEmptyBorder());
			topPanel.add(speechFieldScroll);
		}
		
		inputField = new JLabel("0", SwingConstants.RIGHT);
		inputField.setFont(titleFont);
		inputField.setOpaque(true);
		inputField.setBackground(bgColor);
		inputField.setForeground(Color.WHITE);
		
		outputField = new JLabel(" ", SwingConstants.RIGHT);
		outputField.setFont(smallFont);
		outputField.setOpaque(true);
		outputField.setBackground(bgColor);
		outputField.setForeground(Color.WHITE);
		
		JScrollPane inputFieldScroll = new JScrollPane(inputField, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		inputFieldScroll.setBorder(BorderFactory.createEmptyBorder());
		JScrollPane outputFieldScroll = new JScrollPane(outputField, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputFieldScroll.setBorder(BorderFactory.createEmptyBorder());
		
		
		topPanel.add(inputFieldScroll);
		topPanel.add(outputFieldScroll);
		
		// fills the buttonPanel with calculator buttons
		setCalculatorMode();
		
		buttonPanel.setBackground(bgColor);

		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		
		return mainPanel;
	}
	
	// Allows the user to enter keyboard inputs as calculator prompts
	KeyListener l = new KeyListener()
	{
		
		@Override
		public void keyTyped(KeyEvent e) 
		{
			// Invoked when a key is typed. Uses KeyChar, char output
			
		}

		@Override
		public void keyPressed(KeyEvent e) // Invoked when a physical key is pressed down. Uses KeyCode, int output
		{
			//updateOutputField(outputFieldBuffer("tan(2"));
			if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			{
				setAnswerMode(false);
				deleteCharacter();
			}	
			else if(e.getKeyCode() == KeyEvent.VK_ENTER)
				updateForButtons("=");
			else if(e.getKeyCode() == KeyEvent.VK_SPACE)
				Text_To_Speech.stopSounds();
			else if(validInput(e.getKeyCode()))
			{
				setAnswerMode(false);
				addCharacter(Character.toString(e.getKeyChar()));
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// called whenever a button is released
		}
	};
	
	static public void addCharacter(String text)
	{
		char previousChar = inputField.getText().charAt(inputField.getText().length()-1);
		
		try
		{ 	// Identifies the user's character as either being a number or an operator
			Integer.parseInt(text);

			// a number had been entered
			
			// either replaces the initial 0 with a number or appends the number to the string
			if(inputField.getText().contentEquals("0"))
				inputField.setText(text);
			else if(previousChar == ')' || previousChar == '!' || previousChar == 'e' || previousChar == 'π')
				inputField.setText(inputField.getText() + "*" + text);
			else
				inputField.setText(inputField.getText() + text);
		}
		catch(Exception exception) // an operator had been entered
		{
			// If the input line is empty and an operator is entered
			if(inputField.getText().contentEquals("0"))
			{
				switch(text)
				{
					case "+": 
					case "*":
					case "/":
					case ".":
					case "^":
					case "^2":
					case "!":
						inputField.setText("0" + text);
					break;
					
					default:
						inputField.setText(text);
				}
				updateOutputField(outputFieldBuffer(inputField.getText()));
				return;
			}
			
			// when there are already numbers/operators entered in the input line of the calculator
			switch(text)
			{
				case "+": 
				case "-":
				case "*":
				case "/":
				case "^":
				case "^2":
				case "!":
					try // checks to see if the last character entered was an operator or a number
					{
						Integer.parseInt(Character.toString(previousChar));
						
						inputField.setText(inputField.getText() + text);
					}
					catch(Exception e)
					{ 	// if the last character in the calculator string was an operator 
						//(with an exception of the closed bracket, factorial, e or pi), a new operator replaces the previous one
						if(previousChar == ')' || previousChar == '!' || previousChar == 'e' || previousChar == 'π')
							inputField.setText(inputField.getText() + text);
						else if(previousChar == '(')
						{
							if(text.contentEquals("-"))
								inputField.setText(inputField.getText() + text);
							else
								inputField.setText(inputField.getText() + "0" + text);
						}	
						else 
						{
							deleteCharacter();
							inputField.setText(inputField.getText() + text);
						}
					}
				break;
				
				case ")":
					try // checks to see if the last character entered was an operator or a number
					{
						if(previousChar != ')' && previousChar != 'π' && previousChar != 'e')
							Integer.parseInt(Character.toString(previousChar));
						
						inputField.setText(inputField.getText() + ")");
					}
					catch(Exception e) // if the last character entered was an operator [ex. (5+ ], end the bracket with a zero [ex. (5+0)]
					{
						inputField.setText(inputField.getText() + "0)");
					}
				break;
				
				case "(":
					try
					{
						Integer.parseInt(Character.toString(previousChar));
						inputField.setText(inputField.getText() + "*(");
					}
					catch (Exception e)
					{
						if(previousChar != ')')
							inputField.setText(inputField.getText() + "(");
						else
							inputField.setText(inputField.getText() + "*(");
					}
				break;
				
				case ".": // Ensures that two dots cannot be placed after the same number
					if(!inputField.getText().contains("."))
					{
						inputField.setText(inputField.getText() + ".");
						break;
					}
						
					try 
					{
						Integer.parseInt(Character.toString(previousChar));
					}
					catch (Exception e)
					{
						if(previousChar != '.') // if the previous character is an operator, add (0.)
							inputField.setText(inputField.getText() + "0.");
						
						break; // if the previous character IS a dot operator, simply do nothing
					}
					
					// This loop starts from the end of the text string and tries 
					// to find out if the number already has a dot inside of it
					for(int i = inputField.getText().length() - 1; i >= 0; i--)
					{
						if(inputField.getText().charAt(i) == '.')
							break;
						
						// if an operator is reached and the number has no dot in it, add a dot
						try 
						{
							Integer.parseInt(Character.toString(inputField.getText().charAt(i)));
						}
						catch(Exception e)
						{
							inputField.setText(inputField.getText() + ".");
							break;
						}
					}
				break;
				
				default: // case for square root, pi, euler's number, trigonometric and logarithmic functions
					try
					{
						if(previousChar != ')' && previousChar != 'π' && previousChar != 'e')
							Integer.parseInt(Character.toString(previousChar));
						
						inputField.setText(inputField.getText() + "*" + text);
					}
					catch(Exception e)
					{
						inputField.setText(inputField.getText() + text);
					}	
			}
		}
		updateOutputField(outputFieldBuffer(inputField.getText()));
	}
	
	// deletes the last character entered by the user
		static void deleteCharacter()
		{
			String bufferedText = inputField.getText();
			// The algorithm of deletion is as follows:
			/*
			 * convert all trigonometric and logarithmic functions plus the *first bracket* into one-character symbols
			 * delete one character
			 * expand the one-character symbols into trigonometric and logarithmic functions
			 * 
			 * The reason why these lines of code are different from outputFieldBuffer()
			 * is because there, the first bracket of the trigonometric ratio is excluded
			 */
			bufferedText = bufferedText.replace("arcsin(", "$");
			bufferedText = bufferedText.replace("arccos(", "c");
			bufferedText = bufferedText.replace("arctan(", "t");
			bufferedText = bufferedText.replace("sin(", "S");
			bufferedText = bufferedText.replace("cos(", "C");
			bufferedText = bufferedText.replace("tan(", "T");
			bufferedText = bufferedText.replace("log(", "L");
			bufferedText = bufferedText.replace("ln(", "l");
			
			
			bufferedText = bufferedText.length() > 1 ? bufferedText.substring(0, bufferedText.length()-1) : "0";
			
			bufferedText = bufferedText.replace("c", "arccos(");
			bufferedText = bufferedText.replace("$", "arcsin(");
			bufferedText = bufferedText.replace("t", "arctan(");
			bufferedText = bufferedText.replace("S", "sin(");
			bufferedText = bufferedText.replace("C", "cos(");
			bufferedText = bufferedText.replace("T", "tan(");
			bufferedText = bufferedText.replace("l", "ln(");
			bufferedText = bufferedText.replace("L", "log(");
			
			inputField.setText(bufferedText);
			updateOutputField(outputFieldBuffer(inputField.getText()));
		}
		
	// Function that converts all the functions of the input string into singular characters for later processing by the calculate() function
	// and future output
	static String outputFieldBuffer(String text)
	{
		text = text.replace("arcsin", "$");
		text = text.replace("arccos", "c");
		text = text.replace("arctan", "t");
		text = text.replace("sin", "S");
		text = text.replace("cos", "C");
		text = text.replace("tan", "T");
		text = text.replace("log", "L");
		text = text.replace("ln", "l");
		
		return text;
	}
	
	static void updateOutputField(String text)
	{
		if(text.contentEquals("0"))
		{
			outputField.setText(" ");
			return;
		}
		
		// Evaluate the answer
		// if there are any operators, calculate the result
		while(findOperators(text))
		{
			text = brackets(text);
			/*
				Two types of errors may occur during the calculations
				Calculation_Error - if an operation that is performed is mathematically illegal (sqrt(-1), dividing by 0, etc.)
				In such a case the user will be notified that the operation entered is illogical
				Processing_Error - occurs mainly during the process of entering an expression.
				For example, before an expression such as 'sin(90)' could be entered, there will be an instance where 'sin(' is
				all that had been entered by the user so far, calling for a processing error to occur
			*/
			if(text.contentEquals("Calculation_Error"))
			{
				outputField.setText("Error");
				return;
			}
			else if(text.contentEquals("Processing_Error"))
			{
				outputField.setText(" ");
				return;
			}
		}
		
		// I 'pi' or 'e' are alone in the input string, no operations are performed on them, therefore, their true value never gets displayed
		text = text.replaceAll("π", String.valueOf(Math.PI));
		text = text.replaceAll("e", String.valueOf(Math.E));
		
		
		outputField.setText("= " + text);
	}
	
	static boolean findOperators(String text)
	{
		// Stores the locations of all the operators in the text and their symbols
		operatorsIndexes = new ArrayList<Integer>();
		operatorsNames = new ArrayList<Character>();
		
		boolean hasOperators = false;
		
		if(text.length() == 0)
			return false;
		
		for(int i = 0; i < text.length(); i++)
		{
			try 
			{
				Integer.parseInt(Character.toString(text.charAt(i)));
			}
			catch (Exception e)
			{
				if(text.charAt(i) != '.' && text.charAt(i) != 'E' && text.charAt(i) != '-' && text.charAt(i) != 'π' && text.charAt(i) != 'e')
				{
					operatorsIndexes.add(i);
					operatorsNames.add(text.charAt(i));
					hasOperators = true;
					continue;
				}
				
				if(text.charAt(i) == '-' && i != 0)
				{
					try
					{
						Integer.parseInt(Character.toString(text.charAt(i-1)));
					}
					catch(Exception e2)
					{
						// if the minus is behind another operator like a square root or in the beginning of the string, it does not count
						continue;
					}
						
					operatorsIndexes.add(i);
					operatorsNames.add(text.charAt(i));
					hasOperators = true;
				}
			}
		}
		return hasOperators;
	}
	
	static String brackets(String initialText)
	{
		String textInBrackets;
		
		findOperators(initialText);
		
		// tries to find the brackets in the input string
		boolean openBracket = operatorsNames.indexOf('(') == -1 ? false : true;
		boolean closedBracket = operatorsNames.indexOf(')') == -1 ? false : true;
		
		//if there are no brackets, calculate the whole string
		if(!openBracket && !closedBracket)
		{
			// picks the highest priority operator, scans through the list to find the numbers surrounding it
			for(char i : operatorsPriority)
			{
				if(operatorsIndexes.size() > 0)
					initialText = compute(initialText, i);
				else
					break;
				
				if(initialText.contentEquals("Calculation_Error"))
					return "Calculation_Error";
				else if(initialText.contentEquals("Processing_Error"))
					return "Processing_Error";
			}
			return initialText;
		}
		else if(openBracket && !closedBracket) // if only the closed bracket exists, the open bracket is implied to be at the beginning of the string
		{
			initialText = initialText +  ")";
			findOperators(initialText);
		}
		else if(!openBracket && closedBracket) // if only the open bracket exists, the closed bracket is implied to be at the end of the string
		{
			initialText = "(" + initialText;
			findOperators(initialText);
		}
		
		int openBracketIndex = operatorsIndexes.get(operatorsNames.indexOf('('));
		int closedBracketIndex = operatorsIndexes.get(operatorsNames.indexOf(')'));
		
		// if the brackets are right next to each other, return an error
		if(openBracketIndex + 1 == closedBracketIndex)
			return "Processing_Error";
		else
			textInBrackets = initialText.substring(openBracketIndex + 1, closedBracketIndex);
		 
		// the 'brackets' function calls on itself to check for more brackets
		StringBuffer buf = new StringBuffer(initialText);
		String finalResult = brackets(textInBrackets);
		if(finalResult.contentEquals("Error"))
			return "Error";
		
		buf.replace(openBracketIndex, closedBracketIndex + 1, finalResult);
		return buf.toString();
	}
	
	// An enhanced switch statement is used, it is a preview feature in Java 13
	static String compute(String text, char operator)
	{
		//while(true)
		//{
			int arrIndex = operatorsNames.indexOf(operator);
			
			// checks that the operator is inside the string and is not the last character
			if(arrIndex == -1)// if the operator is not in the string, exit the "compute"
				return text;
			else if(operatorsIndexes.get(arrIndex) == text.length()-1 && operator != '!')
			{
				operatorsIndexes.remove(arrIndex);
				operatorsNames.remove(arrIndex);
				text = text.substring(0, text.length()-1);
				return text;
			}
			
			try 
			{
				int start, end;
				double x = 0, y = 0;
				
				// if the operator is the first one, start from the beginning of the string`
				if(arrIndex == 0)
					start = 0;
				else // start with the last available operator
					start = operatorsIndexes.get(arrIndex - 1) + 1;
				
				// if the operator is the last one, end with the end of the string
				if(arrIndex + 1 == operatorsIndexes.size())
					end = text.length();
				else // end with the next available operator
					end = operatorsIndexes.get(arrIndex + 1);

				// when the operator is a square root, a trig function or a log function, whatever number is in front of it doesn't matter
				try{x = Double.parseDouble(text.substring(start, operatorsIndexes.get(arrIndex)));}
				catch(Exception e)
				{
					// special cases for π and e that have a special value attached to them
					if(text.charAt(start) == 'e')
						x = Math.E;
					else if(text.charAt(start) == 'π')
						x = Math.PI;
				}
				
				// take the number present after the operator as the second number
				// in case of functions like a factorial, the second number does not matter
				try{y = Double.parseDouble(text.substring(operatorsIndexes.get(arrIndex) + 1, end));}
				catch(Exception e)
				{
					// special cases for π and e that have a special value attached to them
					if(text.charAt(end - 1) == 'e')
						y = Math.E;
					else if(text.charAt(end - 1) == 'π')
						y = Math.PI;
				}
				
				double numberAnswer;
				switch (operator)
				{
					case '+':
						numberAnswer = x+y;
					break;
					
					case '-':
						numberAnswer = x-y;
					break;
						
					case '*':
						numberAnswer = x*y;
					break;
						
					case '/':
						if(y == 0) // cannot divide by 0
							return "CError";
						
						numberAnswer = x/y;
					break;
						
					case '^':
						numberAnswer = Math.pow(x,y);
					break;
					
					case '√':
						if(y < 0) // cannot get a square root of a negative number
							return "Calculation_Error";
						
						numberAnswer = Math.sqrt(y);
					break;
					
					case '!':
						if(x < 0)
							return "Calculation_Error";
						
						// the decimals of the variable are rounded up into a whole number
						// because implementing the true definition of a factorial using a 
						// Gamma function is too complex for the purposes of this project
							x = Math.round(x);
							
							numberAnswer = 1;  
  
							for(int i = 2; i <= x; i++) 
								numberAnswer *= i;    
						break;
						
						case 'S': // sin
						// For whatever reason, Math.toRadians() converts from radians to degrees and not the other way around
						numberAnswer = degrees ? Math.sin(Math.toRadians(y)) : Math.sin(y);
					break;
					
					case 'C': // cos
						numberAnswer = degrees ? Math.cos(Math.toRadians(y)) : Math.cos(y);
					break;
					
					case 'T': // tan
						numberAnswer = degrees ? Math.tan(Math.toRadians(y)) : Math.tan(y);
					break;
					
					case '$': // arcsin
						if(y < -1 || y > 1)
							return "Calculation_Error";
						
						numberAnswer = degrees ? Math.toRadians(Math.asin(y)) : Math.asin(y);
					break;
					
					case 'c': // arccos
						if(y < -1 || y > 1)
							return "Calculation_Error";
						
						numberAnswer = degrees ? Math.toRadians(Math.acos(y)) : Math.acos(y);
					break;
					
					case 't': // arctan
						numberAnswer = degrees ? Math.toRadians(Math.atan(y)) : Math.atan(y);
					break;
					
					case 'L': // log(x)
						if(y <= 0)
							return "Calculation_Error";
						
						numberAnswer = Math.log10(y);
					break;
					
					case 'l': // ln(x)
						if(y <= 0)
							return "Calculation_Error";
						
						numberAnswer = Math.log(y);
					break;
					
					default:
						return "Calculation_Error";
				};

				// Adjustment for floating-point 
				if(numberAnswer >= 0 && numberAnswer < 1E-14)
					numberAnswer = 0;
				
				String textAnswer = String.valueOf(numberAnswer);
				// removes the standart ".0" associated with a double as a result
				if(textAnswer.endsWith(".0"))
					textAnswer = textAnswer.substring(0, textAnswer.length()-2);
				
				StringBuilder temp = new StringBuilder(text);
				temp.replace(start, end, textAnswer);
				text = temp.toString();
			}
			catch(Exception e)
			{
				return "Processing_Error";
			}
			
			// Revalidates the modified string
			findOperators(text);
			return compute(text, operator);
		//}
	}
	
	static void updateForButtons(String text)
	{
		if(!text.contentEquals("="))
			setAnswerMode(false);
		
		switch(text)
		{
			case "=":
				// if the equal sign is pressed, enter the 'answer' mode
				setAnswerMode(true);
				
				if(accessibilityMode && !outputField.getText().contentEquals(" "))
				{
					// Crops the answer to be read out loud
					String answer = outputField.getText().substring(2);
					if(answer.indexOf('.') != -1 && answer.indexOf('.') + 3 <= answer.length()) 
					{
						if(answer.contains("E")) // crops all the decimal places except the first four + leaves 'E'
							answer = answer.substring(0, answer.indexOf('.') + 5) + answer.substring(text.indexOf('E')); 
						else // crops all the decimal places except the first four
							answer = answer.substring(0, answer.indexOf('.') + 5); 
					}
					new Number_To_Text(answer);
				}
			break;
			
			case "DEL": // deletes the last character entered
				deleteCharacter();
			break;
			
			case "C": // clears the input line
				speechField.setText(" ");
				inputField.setText("0");
				outputField.setText(" ");
				Speech_To_Text.clearSpeechRecognitionResult();
			break;
			
			case "mode": // switches the calculator modes
				scientificMode = !scientificMode;
				setCalculatorMode();
			break;
				
			case "x":
				addCharacter("*");
			break;
			
			case "÷":
				addCharacter("/");
			break;	
			
			case "x^y":
				addCharacter("^");
			break;
			
			case "x^2":
				addCharacter("^2");
			break;
				
			case "EXP":
				addCharacter("e^");
			break;
			
			case "x!":
				addCharacter("!");
			break;
			
			case "2nd":
				arcMode = !arcMode;
				setCalculatorMode();
			break;
			
			case "deg":
			case "rad":
				degrees = !degrees;
			break;
			
			case "sin":
			case "cos":
			case "tan":
			case "arcsin":
			case "arccos":
			case "arctan":
				addCharacter(text + "(");
			break;
			
			case "log(x)":
			case "ln(x)": // adds 'log(' and 'ln(' accordingly
				addCharacter(text.substring(0, text.length()-2));
			break;
			
			default: 
			    addCharacter(text);
		}
			
		f.requestFocus();
	}
	
	// makes the answer box bigger and the input box smaller when the 'equals' sign is pressed and vice versa
	public static void setAnswerMode(boolean answerMode)
	{
		if(answerMode)
		{
			outputField.setFont(titleFont);
			inputField.setFont(smallFont);
		}
		else
		{
			outputField.setFont(smallFont);
			inputField.setFont(titleFont);
		}
	}
	
	// Function responsible for button render depending on the calculator mode currently set (normal or scientific)
	static void setCalculatorMode()
	{
		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.BOTH;
		
		String[][] copiedArray;
		buttonPanel.removeAll();
		
		// if the calculator is currently in standard mode
		if(!scientificMode)
		{
			buttonPanel.setLayout(new GridLayout(5, 4));
			copiedArray = new String[5][4];
			
			for(int i = 0; i < standartModeButtons.length; i++)
				  for(int j = 0; j < standartModeButtons[i].length; j++)
					  copiedArray[i][j] = standartModeButtons[i][j];
		}
		else // if the calculator is currently in scientific mode
		{
			buttonPanel.setLayout(new GridLayout(7, 5));
			copiedArray = new String[7][5];

			for(int i = 0; i < expandedModeButtons.length; i++)
				  for(int j = 0; j < expandedModeButtons[i].length; j++)
					  copiedArray[i][j] = expandedModeButtons[i][j];
			
			if(arcMode)
			{
				java.util.List<String> copiedList = Arrays.asList(copiedArray[0]);
				copiedList.set(copiedList.indexOf("sin"), "arcsin");
				copiedList.set(copiedList.indexOf("cos"), "arccos");
				copiedList.set(copiedList.indexOf("tan"), "arctan");
					
				copiedArray[0] = Arrays.copyOf((String[]) copiedList.toArray(), expandedModeButtons[0].length);
			}
		}
		
		for(g.gridy = 0; g.gridy < copiedArray.length; g.gridy++)
			for(g.gridx = 0; g.gridx < copiedArray[0].length; g.gridx++)   
				buttonPanel.add(new ButtonClass(copiedArray[g.gridy][g.gridx]).getButton(), g);
				
		f.revalidate();
		f.repaint();
	}
	
	// SETTERS //
	public static void setInputFieldText(String text)
	{
		inputField.setText(text);
	}
	
	public static void setOutputFieldText(String text)
	{
		outputField.setText(text);
	}
	
	public static void setSpeechFieldText(String text)
	{
		speechField.setText(text);
	}
	
	private final int[] codes = {45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 61, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 109, 110, 111, 151, 521};
	// Checks if the user's input is within a valid range of characters
	boolean validInput(int code)
	{
		for(int i : codes)
			if(code == i)
				return true;
		
			return false;
	}
}