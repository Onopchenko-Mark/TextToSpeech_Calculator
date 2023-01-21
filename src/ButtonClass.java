import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

public class ButtonClass extends JButton
{	
	private static final Font smallFont = new Font("Verdana", Font.BOLD, 24);
	private static final Color numbersBgColor = new Color(48, 50, 52);
	private static final Color specialBgColor = new Color(175, 143, 233);
	
	private float alphaValue;

	public void setOpacity(float opacity) 
	{
		this.setBackground(
				new Color(
				(float)this.getBackground().getRed() / 255, 
				(float)this.getBackground().getGreen() / 255, 
				(float)this.getBackground().getBlue() / 255,
				opacity));
		
		CalculatorClass.buttonPanel.repaint();
	}
	 
	ButtonClass(String name)
	{
		this.setText(name);
		this.setFont(ButtonClass.smallFont);
		UIManager.getDefaults().put("Button.disabledText",Color.WHITE);
		this.setEnabled(false);
		
		// Colors the buttons differently depending on them being a number or an operation-related (+ - * /) button
		try
		{
			Integer.parseInt(this.getText());
			
			this.setBackground(ButtonClass.numbersBgColor);
			alphaValue = 0.4f;
		}
		catch(Exception e)
		{
			if(this.getText().equals("=") || this.getText().equals("mode") || this.getText().equals("deg") || this.getText().equals("2nd"))
			{
				this.setBackground(new Color(255, 140, 0));
				alphaValue = 0.8f;
			}
			else
			{
				this.setBackground(ButtonClass.specialBgColor);
				alphaValue = 0.3f;
			}
		}
		
		this.setOpacity(alphaValue);
		this.setBorder(BorderFactory.createLoweredBevelBorder());
		
		this.addMouseListener(new MouseAdapter() 
		{
			// A hover effect over a button in the calculator
		    public void mouseEntered(MouseEvent evt) 
		    {
		    	setBorder(BorderFactory.createLineBorder(Color.WHITE));
		    	setOpacity(alphaValue + 0.2f);
		    }

		    public void mouseExited(MouseEvent evt) 
		    {
		    	setBorder(BorderFactory.createLoweredBevelBorder());
		    	setOpacity(alphaValue);
		    }
		    
		    @Override
            public void mousePressed(MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON1)
                {
                	// switches between the radians and degrees modes
                	if(getText().contentEquals("deg"))
                		setText("rad");
                	else if(getText().contentEquals("rad"))
                		setText("deg");
                	
                	// Sends the this.getText() of the button that was pressed to the CalculatorClass
            		CalculatorClass.updateForButtons(getText());
            		CalculatorClass.buttonPanel.repaint();
            		
            		// Stops all sounds when a button is pressed
            		Text_To_Speech.stopSounds();
                } 
                // plays a sound effect after the user right-clicks a button in accessibility mode
                else if (arg0.getButton() == MouseEvent.BUTTON3 && Intro.getAccessibilityMode()) 
                {
                	Text_To_Speech.stopSounds();
                	Text_To_Speech.playSound(getText()); // play the sound 
                }
            }
		});	
	}
	
	public JComponent getButton()
	{
		return this;
	}
}