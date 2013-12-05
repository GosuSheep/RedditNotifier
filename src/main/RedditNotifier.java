package main;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;

import com.omrlnr.jreddit.User;
import common.GosuFrame;
import common.SwingOps;

public class RedditNotifier implements WindowListener,MouseListener{

	int delay = 30;
	Thread thread;
	private static JTextField delayField;
	private static JTextField userField;
	private static JPasswordField passwordField;
	private static JTextField messageField;
	private GosuFrame frame;
	private TrayIcon trayIcon;
	Image hasMailImage;
	Image noMailImage;
	Image errorImage;
	
	public static void main(String []args){
		new RedditNotifier();
	}
	
	public RedditNotifier(){
		SwingOps.setSystemLookAndFeel();
		frame = new GosuFrame("Reddit Notifier",240,225);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.addWindowListener(this);
		delayField = new JTextField("30");
		delayField.setBounds(10, 11, 40, 20);
		JButton changeButton = new JButton("Change Delay");
		changeButton.setBounds(116, 11, 99, 20);
		SwingOps.addAction(changeButton, this, "rerun");
		
		JPanel midPanel = new JPanel();
		midPanel.setLayout(null);
		midPanel.add(delayField);
		midPanel.add(changeButton);
		userField = new JTextField("");
		userField.setBounds(70, 42, 99, 20);
		passwordField = new JPasswordField("");
		passwordField.setBounds(70, 73, 99, 20);
		JLabel label = new JLabel("Username");
		label.setBounds(10, 42, 60, 20);
		midPanel.add(label);
		midPanel.add(userField);
		JLabel label_1 = new JLabel("Password");
		label_1.setBounds(10, 73, 60, 20);
		midPanel.add(label_1);
		midPanel.add(passwordField);
		
		JButton parseButton = new JButton("Parse Now");
		parseButton.setBounds(10, 102, 205, 23);
		SwingOps.addAction(parseButton, this, "parse");
		midPanel.add(parseButton);
		JButton redditButton = new JButton("Go to Reddit");
		redditButton.setBounds(10, 136, 205, 23);
		SwingOps.addAction(redditButton,this,"reddit");
		midPanel.add(redditButton);
		
		messageField = new JTextField("Timer not started...");
		messageField.setEditable(false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(mainPanel);
		mainPanel.add(midPanel,BorderLayout.CENTER);
		
		JLabel lblNewLabel = new JLabel("Seconds");
		lblNewLabel.setBounds(60, 14, 46, 14);
		midPanel.add(lblNewLabel);
		mainPanel.add(messageField,BorderLayout.SOUTH);
		
		SystemTray tray = SystemTray.getSystemTray();
		MenuItem openItem = new MenuItem("Open");
		openItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(true);
			}
		});
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		PopupMenu popup = new PopupMenu();
		popup.add(openItem);
		popup.add(exitItem);
		try {
			noMailImage = ImageIO.read(new File("reddit_no_mail.png"));
			hasMailImage = ImageIO.read(new File("reddit_has_mail.png"));
			errorImage = ImageIO.read(new File("reddit_error.png"));
			trayIcon = new TrayIcon(noMailImage);
			trayIcon.setPopupMenu(popup);
			trayIcon.addMouseListener(this);
			tray.add(trayIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		frame.setVisible(true);
	}
	
	public void rerun(){
		if (thread!=null)
			thread.interrupt();
		try{
			delay = Integer.parseInt(delayField.getText());
		}catch (Exception e){
			delayField.setText(String.valueOf(delay));
		}
		final ActionListener performer = new ActionListener(){
			public void actionPerformed(ActionEvent event){
				parse();
			}
		};
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				new Timer(delay*1000,performer).start();
				log("Timer started with "+delay+" delay");
			}
		};
		thread = new Thread(runnable);
		thread.start();
	}
	
	public void log(String text){
		messageField.setText(text+" ["+System.currentTimeMillis()+"]");
	}
	
	public void parse(){
		log("Parsing...");
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				User user = new User(userField.getText(),new String(passwordField.getPassword()));
				try {
					user.connect();
					if(user.hasMail()){
						log("NEW MAIL");
						trayIcon.displayMessage("New Reddit Mail!", "", MessageType.INFO);
						trayIcon.setImage(hasMailImage);
					}else{
						log("No new mail");
						trayIcon.setImage(noMailImage);
					}
				} catch (Exception e) {
					e.printStackTrace();
					log("Unable to connect...");
					trayIcon.displayMessage("Unable to connect!", "", MessageType.INFO);
					trayIcon.setImage(errorImage);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	public void reddit(){
		try {
			java.awt.Desktop.getDesktop().browse(new URI("http://www.reddit.com/message/unread/"));
		} catch (Exception e) {
			log("Unable to launch reddit...");
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		trayIcon.displayMessage("Minimized", "This program isn't closed. Right click here and click 'Exit' to exit.", MessageType.INFO);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		frame.setVisible(false);
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getClickCount() >=2){
			frame.setVisible(true);
			frame.setExtendedState(JFrame.NORMAL);
			frame.toFront();
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
