package gui;
//Larping Studio by ViveTheJoestar
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class Progress {
	public static int fileCnt = -1;
	private static JDialog dialog;
	private static JLabel label;
	private static JProgressBar bar;
	
	public static void updateProgBar(int max) {
		if (bar != null) {
			bar.setMaximum(max);
			fileCnt++;
			bar.setValue(fileCnt);
			bar.setVisible(true);
			int percentage = (int) ((fileCnt * 1.0 / max) * 100);
			if (percentage > 98) dialog.dispose();
		}
	}
	public static void updateProgLabel(String labelText) {
		if (label != null) {
			if (labelText.equals("")) dialog.dispose();
			label.setText("Working on: " + labelText);
		}
	}
	public static void setUI(boolean[] flags, File currFile, int index, Color bgColor, Font barFont, Image img, JFrame frame) {
		//Early returns
		boolean unpackDump = currFile.getName().endsWith(".dump") && flags[1];
		if (!(!unpackDump ^ flags[0])) {
			Program.setBtnFunction(currFile, flags, index);
			return;
		}
		frame.setEnabled(false);
		//Set progress bar properties (must be done before declaring)
		UIManager.put("ProgressBar.background", bgColor);
		UIManager.put("ProgressBar.foreground", new Color(16, 217, 91));
		UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
		UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		//Set components
		bar = new JProgressBar();
		dialog = new JDialog();
		label = new JLabel();
		Box barBox = Box.createHorizontalBox(), labelBox = Box.createHorizontalBox();
		Dimension minSize = new Dimension(600, 300);
		JPanel panel = new JPanel();
		//Set component properties
		bar.setBorderPainted(true);
		bar.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY));
		bar.setFont(barFont);
		bar.setStringPainted(true);
		bar.setVisible(false);
		label.setFont(new Font("Tahoma", Font.ITALIC, 18));
		label.setForeground(new Color(138, 251, 180));
		panel.setBackground(bgColor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//Add components
		barBox.add(Box.createHorizontalGlue());
		barBox.add(bar);
		barBox.add(Box.createHorizontalGlue());
		labelBox.add(Box.createHorizontalGlue());
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		panel.add(Box.createVerticalGlue());
		panel.add(labelBox);
		panel.add(new JLabel(" "));
		panel.add(barBox);
		panel.add(Box.createVerticalGlue());
		dialog.add(panel);
		//Set frame properties
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setIconImage(img);
		dialog.setMinimumSize(minSize);
		dialog.setLocationRelativeTo(null);
		dialog.setTitle(Program.TITLE);
		dialog.setVisible(true);
		//Set worker
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Program.setBtnFunction(currFile, flags, index);
				frame.setEnabled(true);
				return null;
			}			
		};
		worker.execute();
		//Add window listener
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (!frame.isEnabled()) {
					String msg = "Aborting current progress... is this OK?";
					Program.tk.beep();
					int result = JOptionPane.showConfirmDialog(null, msg, Program.TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) System.exit(-1);
				}
			}
		});
	}
}