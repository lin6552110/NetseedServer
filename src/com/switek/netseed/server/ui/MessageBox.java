/**
 * 
 */
package com.switek.netseed.server.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.switek.netseed.server.Settings;

/**
 * @author Feng Peng
 * 
 */
public class MessageBox {

	static Display display = Display.getDefault();
	

	/**
	 * 
	 */
	public MessageBox() {
		// TODO Auto-generated constructor stub
	}

	public enum MessageType {
		Info, Warn, Error, Question, BarCode
	}

	public static boolean show(String msg, MessageType type) {
		return show(Settings.getAppName(), msg, type);
	}

	public static boolean show(String msg) {
		return show(Settings.getAppName(), msg, MessageType.Info);
	}

	public static boolean show(final String title, final String msg,
			final MessageType type) {
		if (Display.getCurrent() == null) {
			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					_show(title, msg, type);
				}
			});
		} else {
			return _show(title, msg, type);
		}
		return true;
	}

	private static boolean _show(String title, String msg,
			MessageType type) {

		Shell shell = new Shell(display, SWT.ON_TOP);
		int x = (display.getPrimaryMonitor().getClientArea().width - shell
				.getSize().x) / 2;
		int y = (display.getPrimaryMonitor().getClientArea().height - shell
				.getSize().y) / 2;
		
		shell.setLocation(x, y);
		
		switch (type) {
		case Info:
			MessageDialog.openInformation(shell, title, msg);
			break;
		case Warn:
			MessageDialog.openWarning(shell, title, msg);
			break;
		case Error:
			MessageDialog.openError(shell, title, msg);
			break;
		case Question:
			return MessageDialog.openQuestion(shell, title, msg);
		default:
			break;
		}

		return true;
	}

}
