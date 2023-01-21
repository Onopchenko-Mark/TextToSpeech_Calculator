import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;


public class Intro {
	private final static Font titleFont = new Font("Verdana", Font.BOLD, 70);
	private final static Font smallFont = new Font("Verdana", Font.BOLD, 33);
	private static boolean accessibilityMode;
	private JFrame f;
	
	
	public static boolean getAccessibilityMode()
	{
		return accessibilityMode;
	}
	
	public static void main(String[] args) 
	{
		new Intro();
	}
	
	Intro()
	{
		initializeFrame();
		Text_To_Speech.playSound("intro");
	}

	private void initializeFrame()
	{
		f = new JFrame("text");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setBackground(new Color(0.031f, 0.00784f, 0.14118f, 1.f));
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		f.setPreferredSize(new Dimension((int) (size.getWidth() / 1.5), (int)(size.getHeight() / 2.6)));
		f.add(initializeComponents());
		f.pack();
		f.setLocationRelativeTo(null);
		f.setResizable(false);
		f.setVisible(true);
		
		f.addMouseListener(new MouseAdapter() 
		{
			@Override
            public void mousePressed(MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON3)
                {
                	accessibilityMode = true;
                	
                	Text_To_Speech.playSound("accessibilityMode");
                	new CalculatorClass();
                	f.dispose();
                }
			 }
		});
	}
	
	private Component initializeComponents()
	{
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
		title.setFont(titleFont);
		title.setForeground(Color.WHITE);
		
		JLabel text = new JLabel("<html>Please turn on volume for the Accessibility Mode<br>"
				+ "Press <font color='red'>Right Mouse Button</font> for Accessibility Mode<br>"
				+ "Press <font color='red'>Continue</font> to enter Standard Mode</html>");
		text.setHorizontalAlignment(SwingConstants.CENTER);
		text.setFont(smallFont);
		text.setForeground(Color.WHITE);
		
		JButton button = new JButton ("Continue");
		button.addActionListener(e -> {
			accessibilityMode = false;
			Text_To_Speech.stopSounds();
        	new CalculatorClass();
        	f.dispose();
		});
		
		button.setFont(smallFont);
		button.setBackground(Color.BLACK);
		button.setForeground(Color.WHITE);
		
		mainPanel.add(title, BorderLayout.NORTH);
		mainPanel.add(text, BorderLayout.CENTER);
		mainPanel.add(button, BorderLayout.SOUTH);
		mainPanel.setBackground(new Color(0.031f, 0.00784f, 0.14118f, 1.f));
		
		return mainPanel;
	}
}
