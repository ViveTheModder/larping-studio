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
import cmd.Bt4CharaDump;
import cmd.Bt4CharaPak;
import cmd.Bt4CharaPck;
import cmd.Main;

public class Program {
	public static final String TITLE = "Larping Studio v2.0 / OpenBT4 by ViveTheJoestar";
	static Toolkit tk = Toolkit.getDefaultToolkit();
	private static File lastDir;
	private static String lastExt;
	private static final String[] EXTENSIONS = { "pak", "pck", "dump" };
	
	public static void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setUI();
		}
		catch (Exception e) {
			err(e);
		}
	}
	
	static void setBtnFunction(File currFile, boolean[] flags, int index) {
		try {
			String arg = flags[1] ? "-u" : "-r";
			String msg = null;
			if (currFile != null) {
				if (flags[0]) {
					if (currFile.isFile()) {
						String name = currFile.getName().toLowerCase();
						Bt4CharaPak pak = new Bt4CharaPak(currFile);
						if (pak.isValid() && name.endsWith(".pak"))
							msg = Main.chooseOptionFromArg(pak, arg);
						else if (name.endsWith(".pck")) {
							Bt4CharaPck pck = new Bt4CharaPck(currFile);
							msg = Main.chooseOptionFromArg(pck, arg);
						}
						else if (name.endsWith(".dump")) {
							Bt4CharaDump dump = new Bt4CharaDump(currFile);
							if (arg.equals("-u")) {
								if (name.contains("costumes")) arg = "-sC";
								else if (name.contains("anm")) arg = "-sA";
								else if (name.contains("params")) arg = "-sP";
								msg = Main.chooseOptionFromArg(dump, arg);
							}
						}
					}
				}
				else {
					if (currFile.isDirectory()) {
						Progress.fileCnt = 0;
						File[] pakFiles = currFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".pak"));
						if (pakFiles.length > 0) {
							Bt4CharaPak[] paks = new Bt4CharaPak[pakFiles.length];
							for (int pakCnt = 0; pakCnt < paks.length; pakCnt++) {
								paks[pakCnt] = new Bt4CharaPak(pakFiles[pakCnt]);
								msg = Main.chooseOptionFromArg(paks[pakCnt], arg);
								if (msg != null) msg = "SUCCESS: PAK Folder Operation completed in " + String.format("%.4f", Main.totalTime) + " seconds!";
								Progress.updateProgBar(paks.length);
								Progress.updateProgLabel(paks[pakCnt].getName());
							}
						}
						else {
							File[] pckFiles = currFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".pck"));
							Bt4CharaPck[] pcks = new Bt4CharaPck[pckFiles.length];
							for (int pckCnt = 0; pckCnt < pcks.length; pckCnt++) {
								pcks[pckCnt] = new Bt4CharaPck(pckFiles[pckCnt]);
								msg = Main.chooseOptionFromArg(pcks[pckCnt], arg);
								if (msg != null) msg = "SUCCESS: PCK Folder Operation completed in " + String.format("%.4f", Main.totalTime) + " seconds!";
								Progress.updateProgBar(pcks.length);
								Progress.updateProgLabel(pcks[pckCnt].getName());
							}
						}
					}
				}
			}
			if (msg != null) {
				tk.beep();
				JOptionPane.showMessageDialog(null, msg, TITLE, JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (Exception e) {
			err(e);
		}
	}
	private static File getDirFromChooser(boolean isFile) {
		File dir = null;
		int mode = isFile ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY;
		String title = isFile ? "Open File" : "Open Folder";
		JFileChooser fc = new JFileChooser();
		if (lastDir != null) {
			fc.setCurrentDirectory(lastDir);
			if (lastDir.isFile()) {
				String lastDirName = lastDir.getName();
				lastExt = lastDirName.substring(lastDirName.lastIndexOf('.') + 1);
			}
		}
		fc.setDialogTitle(title);
		if (isFile) {
			fc.setAcceptAllFileFilterUsed(false);
			for (String ext: EXTENSIONS) {
				String extUpper = ext.toUpperCase();
				FileNameExtensionFilter ff = new FileNameExtensionFilter(extUpper + " File (." + ext + ")", ext);
				fc.addChoosableFileFilter(ff);
				if (lastExt != null) {
					if (lastExt.equals(ext)) fc.setFileFilter(ff);
				}
			}
		}
		else fc.setAcceptAllFileFilterUsed(true);
		fc.setFileSelectionMode(mode);
		int result = fc.showOpenDialog(null);
		if (result == JFileChooser.OPEN_DIALOG) {
			dir = fc.getSelectedFile();
			lastDir = dir;
		}
		return dir;
	}
	private static void err(Exception e) {
		String err = e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
		StackTraceElement[] ste = e.getStackTrace();
		for (StackTraceElement el: ste) {
			//Add new lines and remove brackets
			String line = el.toString().replace("[", "").replace("]", "");
			if (line.startsWith("gui") || line.startsWith("cmd")) err += line + "\n";
		}
		tk.beep();
		JOptionPane.showMessageDialog(null, err, TITLE, JOptionPane.ERROR_MESSAGE);
	}
	private static void setUI() {
		final boolean[] flags = new boolean[2];
		String[] btnText = { "Unpack/Split", "Unpack/Split All", "Repack/Merge", "Repack/Merge All" };
		String[] btnTips = { 
			"Unpacks a given BT4 character costume file (or splits it<br>"
			+ "into several files in a folder named after said file).", 
			"Unpacks/splits all the BT4 character costume files provided in a folder.",
			"Repacks the contents of a given BT4 character costume file,<br>"
			+ "if already unpacked (or in other words, merges the unpacked files into one).", 
			"Repacks/merges all the BT4 character costume files provided in a folder, if already unpacked."
		};
		//Set components
		Color bgColor = new Color(34, 31, 30);
		Color unpackBtnColor = new Color(109, 103, 202);
		Color repackBtnColor = new Color(146, 152, 53);
		Dimension minSize = new Dimension(600, 600);
		Font btnFont = new Font("Tahoma", Font.BOLD, 26);
		JButton[] btns = new JButton[btnText.length];
		JFrame frame = new JFrame(TITLE);
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridLayout(2, 2));
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
			btns[btnCnt].setToolTipText("<html><div style='text-align: center; font-style: italic; font-size: 14px;'>" + btnTips[btnCnt] + "</div></html>");
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
					flags[0] = index % 2 == 0 ? true : false;
					flags[1] = index < 2 ? true : false;
					File currFile = getDirFromChooser(flags[0]);
					Progress.setUI(flags, currFile, index, bgColor, btnFont, img, frame);
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
								flags[0] = index % 2 == 0 ? true : false;
								flags[1] = index < 2 ? true : false;
								Progress.setUI(flags, f, index, bgColor, btnFont, img, frame);
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