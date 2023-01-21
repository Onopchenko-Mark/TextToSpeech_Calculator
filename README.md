# TextToSpeech_Calculator
This program is a GUI calculator capable of text-to-speech mathematical calculations
This application has two modes: standard and accessible  

## Standard  
Functions like a normal calculator, respects the order of operations and calculates in-line for readability  
  
## Accessible  
All the features of the standart mode are preserved and two more are added:  
1. When hovering over a button, the user can right-click on it and its contents will be spoken out loud  
2. When the user presses the **equals sign**, the answer will be read out loud  
3. The user may use the following voice commands to operate the calculator:  
tutorial - lists all the commands below  
start - starts voice recording  
calculate - calculates and reads result  
erase - deletes the last word entered by the user  
clear - clears all input lines  
read - reads the words spoken by the user thus far  
stop - stops voice recording  

## Accessibility Mode Instructions
All of the available phrases and mathematical expressions can be found in the **resources/grammars/grammar.gram** file, which can be opened as text  
Please speak slowly, clearly, one word at a time for the best voice recognition accuracy.  
You may also use headphones for best experience, although it is not necessary for the program to function.  


## Installation Steps
Extract the **bin** folder in its entirety onto your machine, navigate to **calculator.exe** and launch the application. Enjoy!

## Source Code
The source code of the program can be found in the **src** folder, with the **main** located in the **Intro** class  

## Resources
The project was made using the CMU Sphinx, an open-source speech recognition library written in Java.  
https://cmusphinx.github.io/  
The library files used in the project can be found in **resources/libraries** folder of the repository.  

## Important Note
Some mathematical expressions did not have a phonetic transcription attached to them, so some reformat of words was made.  
cosine - coast  
e - east  
lnx - natural  
raised to the power of - power  
