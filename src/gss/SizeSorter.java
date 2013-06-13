package gss;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class SizeSorter extends JFrame
        implements
        ActionListener
{
    private static final long  serialVersionUID = 1L;
    private MessageLoader      loader;

    private JTextField         usernameField    = new JTextField(20);
    private JPasswordField     passwordField    = new JPasswordField(20);
    final private JProgressBar progressBar      = new JProgressBar(0, 100);

    private JLabel totalMessagesLabel;
    private JLabel currentMessageLabel;
    private JLabel totalSizeLabel;
    
    private JButton startButton;

    public static void main(String[] args)
    {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                new SizeSorter();
            }
        });
    }

    public SizeSorter()
    {
        super("Gmail Size Sorter");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        this.setLayout(new GridLayout(0, 2));
        this.setSize(300, 200);
        this.setLocationRelativeTo(null);

        totalMessagesLabel = new JLabel();
        currentMessageLabel = new JLabel();
        totalSizeLabel = new JLabel();
        
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

		this.getRootPane().setDefaultButton(startButton);

        progressBar.setStringPainted(true);
        showProgress(false);

        this.add(new JLabel("Username: ", JLabel.RIGHT));
        this.add(usernameField);
        this.add(new JLabel("Password: ", JLabel.RIGHT));
        this.add(passwordField);
        this.add(new JLabel("Total Messages: ", JLabel.RIGHT));
        this.add(totalMessagesLabel);
        this.add(new JLabel("Current Messages: ", JLabel.RIGHT));
        this.add(currentMessageLabel);
        this.add(new JLabel("Total Size: ", JLabel.RIGHT));
        this.add(totalSizeLabel);
        this.add(progressBar);
        this.add(startButton);
        
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().compareTo("start") == 0)
        {
            loader = new MessageLoader(this, usernameField.getText(), new String(
                    passwordField.getPassword()));
            passwordField.setText("");
            loader.start();
        }
    }
    
    public void setTotalMessages(int totalMessages)
    {
        totalMessagesLabel.setText(Integer.toString(totalMessages));
    }
    
    public void setCurrentMessage(int currentMessage)
    {
        currentMessageLabel.setText(Integer.toString(currentMessage));
    }
    
    public void setTotalSize(String totalSize)
    {
        totalSizeLabel.setText(totalSize);
    }
    
    public void setInputEnabled(boolean enabled)
    {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        startButton.setEnabled(enabled);
    }
    
    public void showProgress(boolean show)
    {
        progressBar.setVisible(show);
    }
    
    public void setProgress(int progress)
    {
        progressBar.setValue(progress);
    }
    
    public void setStatus(String status)
    {
        startButton.setText(status);
    }
}
