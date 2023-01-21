// Class that converts the given number to words
// Calls onto the Text_to_Speech_Class to voice those words

public class Number_To_Text {

	private final static String[] numbers = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
			"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
	static final String tens[] = {"ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty","ninety"};
    static final String thousands[] = {"thousand", "million", "billion", "trillion"};
    
	Number_To_Text(String num)
	{
		Text_To_Speech.playSound(numToText(num));
	}
    
	private  String numToText(String num)
	{
		String total = "";
		String decimals = "";
		boolean isNegative = false;
		
		// integer that tracks the magnitude of the number (0 - hundred, 1 - thousand, 2 - million)
		int magnitude = 0;
		
		// deletes the minus sign out of the answer, due to be added later
		if(num.charAt(0) == '-')
		{
			num = num.substring(1);
			isNegative = true;
		}
			
		// separates the whole numbers from the decimals
		if(num.indexOf('.') != -1)
		{
			decimals = num.substring(num.indexOf('.') + 1, num.length());
			num = num.substring(0, num.indexOf('.'));
		}
		
		for(int i = num.length()-1; i >= 0; i--)
		{	
			boolean threeNums = false, twoNums = false;
			
			// takes care of the magnitude of the three numbers (thousands, millions, etc.)
			if(magnitude > 0)
			{
				boolean out = false;
				// if the third character is within the string
				if(i-2 >= 0)
					if(num.charAt(i-2) == '0' && num.charAt(i-1) == '0' && num.charAt(i) == '0')
						out = true;
				
				if(!out)
					total = thousands[magnitude - 1] + " " + total;
			}
			magnitude++;
			
			// if the second number exists in a string (like 1 in 19, 3 in 30, etc.)
			if(i-1 >= 0)
			{
				twoNums = true;
				int number1 = Integer.parseInt(Character.toString(num.charAt(i-1)));
				int number2 = Integer.parseInt(Character.toString(num.charAt(i)));
				
				// inserts a ten-type value when cases like 20, 30, or 90 occur
				if(number1 != 0 && number2 == 0)
					total = tens[number1-1] + " " + total;
				else if(number1 != 0 && number2 != 0)
				{
					if(number1 * 10 + number2 <= 19)// if the number is unique
						total = numbers[number1 * 10 + number2] + " " + total;
					else // split the number into a tens and a num
						total = tens[number1 - 1] + " " + numbers[number2] + " " + total;
				}
				else if(number1 == 0 && number2 != 0)
					total = numbers[number2] + " " + total;
			}
			else
			{
				int current = Integer.parseInt(Character.toString(num.charAt(i)));
				total = numbers[current] + " " + total;
			}
			
			// if the third number exists in a string ( 1 in 120, 3 in 340, etc)
			if(i-2 >= 0)
			{
				threeNums = true;
				
				int current = Integer.parseInt(Character.toString(num.charAt(i-2)));
				
				if(current != 0)
					total = numbers[current] + " hundred " + total;
			}
			
			if(threeNums)
				i -= 2;
			else if(twoNums)
				i--;
		}
		
		// adds all the decimal values, if present, to the end of the string
		if(decimals.length() > 0)
		{
			total += "point ";
			for(int i = 0; i < decimals.length(); i++)
			{
				if(decimals.charAt(i) == '-')
				{
					total += "negative";
					continue;
				}
				
				if(decimals.charAt(i) != 'E') // goes up to 'E' for the decimal places
					total += numbers[Integer.parseInt(Character.toString(decimals.charAt(i)))] + " ";
				else
				{
					total += "E ";
					++i;
					if(i+1 < decimals.length())
					{
						int number1 = Integer.parseInt(Character.toString(decimals.charAt(i)));
						int number2 = Integer.parseInt(Character.toString(decimals.charAt(i + 1)));
						
						// inserts a ten-type value when cases like 20, 30, or 90 occur
						if(number1 != 0 && number2 == 0)
							total += tens[number1-1] + " ";
						else if(number1 != 0 && number2 != 0)
						{
							if(number1 * 10 + number2 <= 19)// if the number is unique
								total += numbers[number1 * 10 + number2] + " ";
							else // split the number into a tens and a num
								total += tens[number1 - 1] + " " + numbers[number2] + " ";
						}
					}
					else
						total += numbers[Integer.parseInt(Character.toString(decimals.charAt(i)))] + " ";
					
					break;
				}
			}
		} 
		
		if(isNegative)
			total = "negative " + total;
		
		return total;
	}
}