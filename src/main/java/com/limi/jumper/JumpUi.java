package com.limi.jumper;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.RandomAccessFile;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class JumpUi {

	private UiListener uiListener;
	private double scale = 1;
	private JPanel imagePanel;
	private String imagePath;
	byte[] cachedImage = null;
	long lastModify = 0;

	public void setListener(UiListener onRange) {

		this.uiListener = onRange;
	}

	public void setScale(double scale) {

		this.scale = scale;
	}

	public void refresh() {

		if (imagePanel != null) {

			imagePanel.repaint();
			// imagePanel.updateUI();
		}
	}

	public void show() {

		// 显示应用 GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void createAndShowGUI() {

		// 确保一个漂亮的外观风格
		JFrame.setDefaultLookAndFeelDecorated(true);

		// 创建 JFrame 实例
		final JFrame frame = new JFrame("Jump Helper");
		// Setting the width and height of frame
		// frame.setSize(350, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {

				uiListener.onClose();
			}
		});

		imagePanel = new JPanel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private int count = 1;

			protected void paintComponent(Graphics g) {

				// if (!isImageChanged()) {
				// super.paintComponent(g);
				// return;
				// }
				System.out.println("paint: " + count);
				count++;
				try {
					byte[] imageBytes = readFile(imagePath);
					ImageIcon icon = new ImageIcon(imageBytes);
					Image img = icon.getImage();
					int width = (int) (icon.getIconWidth() * scale);
					int height = (int) (icon.getIconHeight() * scale);
					g.drawImage(img, 0, 0, width, height, icon.getImageObserver());
					frame.setSize(width, height);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		imagePanel.addMouseListener(new MouseAdapter() {

			private int x1;
			private int y1;
			private int x2;
			private int y2;

			@Override
			public void mousePressed(MouseEvent e) {

				x1 = e.getX();
				y1 = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				x2 = e.getX();
				y2 = e.getY();

				if (uiListener != null) {

					// onRange.onRange(JumpUi.this, x1, y1, x2, y2);
					uiListener.onRange(JumpUi.this, (int) (x1 / scale), (int) (y1 / scale), (int) (x2 / scale),
							(int) (y2 / scale));
				}
			}
		});

		frame.getContentPane().add(imagePanel);

		frame.pack();
		// 设置界面可见
		frame.setVisible(true);
	}

	public interface UiListener {

		void onRange(JumpUi jumpUi, int x1, int y1, int x2, int y2);

		void onClose();
	}

	public void setImage(String imagePath) {
		this.imagePath = imagePath;
	}

	private boolean isImageChanged() {

		if (cachedImage != null) {

			File file = new File(imagePath);
			if (file.lastModified() <= lastModify) {
				return false;
			}
		}
		return true;
	}

	private byte[] readFile(String imagePath) {

		if (!isImageChanged()) {
			return cachedImage;
		}

		{
			File file = new File(imagePath);
			lastModify = file.lastModified();
		}

		byte[] result = null;
		try {
			RandomAccessFile file = new RandomAccessFile(imagePath, "r");
			result = new byte[(int) file.length()];
			file.read(result);
			file.close();
			cachedImage = result;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
