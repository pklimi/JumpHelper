package com.limi.jumper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.limi.jumper.JumpUi.UiListener;

public class JumpMain {

	private static String sdPath = "/sdcard/screenshot/jump.png";
	private static String pcPath1 = "screenshot/jump_1.png";
	private static String pcPath2 = "screenshot/jump_2.png";
	private static boolean isThreadRunning = true;

	private static double rate = 1.38;
	private static double scale = 0.55;

	public static void main(String[] args) {

		// adbScreenShot();
		// long mills = 50;
		// swipe(50, 250, 250, 250, mills);
		// log("完成");

		JumpUi jumpUi = new JumpUi();
		jumpUi.setListener(createListener());
		jumpUi.setScale(scale);
		jumpUi.setImage(pcPath2);
		jumpUi.show();

		ScreenShotThread thread = new ScreenShotThread();
		thread.setJumpUi(jumpUi);
		thread.start();
	}

	private static UiListener createListener() {

		return new UiListener() {

			public void onRange(JumpUi jumpUi, int x1, int y1, int x2, int y2) {

				int dx = x2 - x1;
				int dy = y2 - y1;
				log("[onRange] " + String.format("%s,%s", dx, dy));

				if (Math.abs(dx) > 30 && Math.abs(dy) > 30) {

					swipe(x1, y1, x2, y2);
				}
			}

			public void onClose() {

				isThreadRunning = false;
			}
		};
	}

	private static void swipe(int x1, int y1, int x2, int y2) {

		int dx = x2 - x1;
		int dy = y2 - y1;
		double range = Math.sqrt(dx * dx + dy * dy);
		long mills = (long) (range * rate);
		log("[swipe] " + mills + "ms");
		String swipeCmd = String.format("shell input swipe %s %s %s %s %s", x1, y1, x2, y2, mills);
		adbCommand(swipeCmd);
	}

	private static void adbScreenShot() {

		String shotCmd = String.format("shell /system/bin/screencap -p %s", sdPath);
		String pullCmd = String.format("pull %s %s", sdPath, pcPath1);

		adbCommand(shotCmd);
		adbCommand(pullCmd);
	}

	private static String adbCommand(String adbCmd) {

		String adbPath = "E:/tools/android-sdk-windows/platform-tools/adb.exe";
		return runCommand(String.format("%s %s", adbPath, adbCmd));
	}

	private static String runCommand(String cmd) {

		log("[cmd] " + cmd);
		String result = null;
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));
			String line = null;
			StringBuffer b = new StringBuffer();
			while ((line = br.readLine()) != null) {
				b.append(line + "\n");
			}
			result = b.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		log("[cmd] " + result);
		return result;
	}

	private static void copyScreenShot() {

		try {

			InputStream inputStream = new FileInputStream(pcPath1);
			OutputStream outputStream = new FileOutputStream(pcPath2);
			byte[] buff = new byte[1024];
			int len = 1024;
			while ((len = inputStream.read(buff, 0, len)) != -1) {

				outputStream.write(buff, 0, len);
			}
			inputStream.close();
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class ScreenShotThread extends Thread {

		private JumpUi jumpUi;

		@Override
		public void run() {
			super.run();

			while (isThreadRunning) {

				adbScreenShot();
				copyScreenShot();
				jumpUi.refresh();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void setJumpUi(JumpUi jumpUi) {
			this.jumpUi = jumpUi;
		}
	}

	private static void log(Object log) {

		String logStr = String.valueOf(log);
		if (logStr.startsWith("[cmd]")) {
			return;
		}
		if (logStr.startsWith("[onRange]")) {
			return;
		}

		System.out.println(logStr);
	}
}
