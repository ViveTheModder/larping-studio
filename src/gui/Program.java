package gui;
//Larping Studio by ViveTheJoestar
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.Bt4CharaPak;
import cmd.Bt4CharaPck;
import cmd.Main;

public class Program {
	private static File lastDir;
	private static final String TITLE = "Larping Studio";
	
	public static void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setUI();
		}
		catch (Exception e) {
			err(e);
		}
	}
	
	private static File getDirFromChooser(boolean isFile) {
		File dir = null;
		int mode = isFile ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY;
		FileNameExtensionFilter ff = new FileNameExtensionFilter("PAK File (.PAK)", "pak");
		String title = isFile ? "Open File" : "Open Folder";
		JFileChooser fc = new JFileChooser();
		if (lastDir != null) fc.setCurrentDirectory(lastDir);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setDialogTitle(title);
		if (isFile) fc.setFileFilter(ff);
		fc.setFileSelectionMode(mode);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.OPEN_DIALOG) {
			dir = fc.getSelectedFile();
			lastDir = dir;
		}
		return dir;
	}
	private static void err(Exception e) {
		String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
		JOptionPane.showMessageDialog(null, msg, TITLE, JOptionPane.ERROR_MESSAGE);
	}
	private static void setBtnFunction(File currFile, boolean[] flags, int index) {
		try {
			flags[0] = index % 2 == 0 ? true : false;
			flags[1] = index < 2 ? true : false;
			String arg = flags[1] ? "-u" : "-r";
			String msg = null;
			if (currFile != null) {
				if (flags[0]) {
					if (currFile.isFile()) {
						Bt4CharaPak pak = new Bt4CharaPak(currFile);
						if (pak.isValid())
							msg = Main.chooseOptionFromArg(pak, arg);
						else if (currFile.getName().toLowerCase().endsWith(".pck")) {
							Bt4CharaPck pck = new Bt4CharaPck(currFile);
							msg = Main.chooseOptionFromArg(pck, arg);
						}
					}
				}
				else {
					if (currFile.isDirectory()) {
						File[] pakFiles = currFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".pak"));
						Bt4CharaPak[] paks = new Bt4CharaPak[pakFiles.length];
						for (int pakCnt = 0; pakCnt < paks.length; pakCnt++) {
							paks[pakCnt] = new Bt4CharaPak(pakFiles[pakCnt]);
							msg = Main.chooseOptionFromArg(paks[pakCnt], arg);
						}
					}
				}
			}
			if (msg != null) JOptionPane.showMessageDialog(null, msg, TITLE, JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e) {
			err(e);
		}
	}
	private static void setUI() {
		final boolean[] flags = new boolean[2];
		String[] btnText = { "Unpack", "Unpack All", "Repack", "Repack All" };
		String[] btnTips = { 
			"Unpacks a given BT4 character costume file.", 
			"Unpacks all the BT4 character costume files provided in a folder.",
			"Repacks a given BT4 character costume file, if already unpacked.", 
			"Repacks all the BT4 character costume files provided in a folder, if already unpacked."
		};
		//Set components
		Color bgColor = new Color(34, 31, 30);
		Color unpackBtnColor = new Color(109, 103, 202);
		Color repackBtnColor = new Color(146, 152, 53);
		Dimension minSize = new Dimension(500, 500);
		Font btnFont = new Font("Tahoma", Font.BOLD, 32);
		JButton[] btns = new JButton[btnText.length];
		JFrame frame = new JFrame(TITLE);
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(2, 2));
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image img = tk.getImage(ClassLoader.getSystemResource("img/icon.png"));
		//Set component properties
		gridPanel.setBackground(bgColor);
		//Add components
		for (int btnCnt = 0; btnCnt < btns.length; btnCnt++) {
			final int index = btnCnt;
			Box btnBox = Box.createHorizontalBox();
			btns[btnCnt] = new JButton(btnText[btnCnt]);
			btns[btnCnt].setContentAreaFilled(false);
			btns[btnCnt].setForeground(Color.WHITE);
			btns[btnCnt].setFont(btnFont);
			btns[btnCnt].setToolTipText(btnTips[btnCnt]);
			btns[btnCnt].setOpaque(true);
			if (btnCnt < 2) btns[btnCnt].setBackground(unpackBtnColor);
			else btns[btnCnt].setBackground(repackBtnColor);
			btnBox.add(Box.createHorizontalGlue());
			btnBox.add(btns[btnCnt]);
			btnBox.add(Box.createHorizontalGlue());
			gridPanel.add(btnBox);
			//Add action listeners
			btns[btnCnt].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					File currFile = getDirFromChooser(flags[0]);
					setBtnFunction(currFile, flags, index);
				}
			});
			//Add drag-and-drop
			btns[btnCnt].setTransferHandler(new TransferHandler() {
				public boolean canImport(TransferHandler.TransferSupport ts) {
					return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
				}
				@SuppressWarnings("unchecked")
				public boolean importData(TransferHandler.TransferSupport ts) {
					if (!canImport(ts)) return false;
					try {
						List<File> files = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						for (File f: files) {
							if (f.isFile() || f.isDirectory()) {
								lastDir = f;
								setBtnFunction(f, flags, index);
							}
						}
					}
					catch (Exception e) {
						return false;
					}
					return true;
				}
			});
		}
		mainPanel.add(gridPanel, BorderLayout.CENTER);
		frame.add(mainPanel);
		//Set frame properties
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(img);
		frame.setMinimumSize(minSize);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}