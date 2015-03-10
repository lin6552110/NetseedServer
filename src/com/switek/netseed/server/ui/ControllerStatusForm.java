package com.switek.netseed.server.ui;

import java.text.Collator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.SessionManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.SocketPacket;

public class ControllerStatusForm {

	protected Shell shell;
	private Table table;

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		showControllerStatus();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(779, 441);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());

		final Button btnRefresh = new Button(composite, SWT.NONE);
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					btnRefresh.setEnabled(false);
					showControllerStatus();
				} catch (Exception ex) {
					MessageBox.show(ex.toString());
				}finally{
					btnRefresh.setEnabled(true);					
				}
			}
		});
		FormData fd_btnRefresh = new FormData();
		fd_btnRefresh.top = new FormAttachment(0, 10);
		fd_btnRefresh.right = new FormAttachment(100, -10);
		btnRefresh.setLayoutData(fd_btnRefresh);
		btnRefresh.setText("&Refresh");

		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(btnRefresh, 358, SWT.BOTTOM);
		fd_table.top = new FormAttachment(btnRefresh, 5);
		fd_table.right = new FormAttachment(btnRefresh, 0, SWT.RIGHT);
		fd_table.left = new FormAttachment(0, 10);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnControllerid = new TableColumn(table, SWT.NONE);
		tblclmnControllerid.setWidth(144);
		tblclmnControllerid.setText("ControllerId");
		addSorter(table, tblclmnControllerid);

		TableColumn tblclmnIsonline = new TableColumn(table, SWT.NONE);
		tblclmnIsonline.setWidth(100);
		tblclmnIsonline.setText("IsOnline");
		addSorter(table, tblclmnIsonline);
		
		TableColumn tblclmnLastHeartbeat = new TableColumn(table, SWT.NONE);
		tblclmnLastHeartbeat.setWidth(163);
		tblclmnLastHeartbeat.setText("Last Heartbeat");
		addSorter(table, tblclmnLastHeartbeat);
		
		TableColumn tblclmnSession = new TableColumn(table, SWT.NONE);
		tblclmnSession.setWidth(329);
		tblclmnSession.setText("Session");
		addSorter(table, tblclmnSession);

	}

	public static void addSorter(final Table table, final TableColumn column) {  
        column.addListener(SWT.Selection, new Listener() {  
            boolean isAscend = true;  
            Collator comparator = Collator.getInstance(Locale.getDefault());  
  
            @Override
			public void handleEvent(Event e) {  
                int columnIndex = getColumnIndex(table, column);  
                TableItem[] items = table.getItems();  
  
                for (int i = 1; i < items.length; i++) {  
                    String value2 = items[i].getText(columnIndex);  
                    for (int j = 0; j < i; j++) {  
                        String value1 = items[j].getText(columnIndex);  
                        boolean isLessThan = comparator.compare(value2, value1) < 0;  
                        if ((isAscend && isLessThan) || (!isAscend && !isLessThan)) {
                        	Color color = items[i].getBackground();
                            String[] values = getTableItemText(table, items[i]);  
                            Object obj = items[i].getData();  
                            items[i].dispose();  
  
                            TableItem item = new TableItem(table, SWT.NONE, j);  
                            item.setText(values);  
                            item.setData(obj);  
                            item.setBackground(color);
                            items = table.getItems();  
                            break;  
                        }  
                    }  
                }  
  
                table.setSortColumn(column);  
                table.setSortDirection((isAscend ? SWT.UP : SWT.DOWN));  
                isAscend = !isAscend;  
            }  
        });  
    }  
  
    public static int getColumnIndex(Table table, TableColumn column) {  
        TableColumn[] columns = table.getColumns();  
        for (int i = 0; i < columns.length; i++) {  
            if (columns[i].equals(column))  
                return i;  
        }  
        return -1;  
    }  
  
    public static String[] getTableItemText(Table table, TableItem item) {  
        int count = table.getColumnCount();  
        String[] strs = new String[count];  
        for (int i = 0; i < count; i++) {  
            strs[i] = item.getText(i);  
        }  
        return strs;  
    }  
    
	protected void showControllerStatus() {
		table.removeAll();
		Hashtable<String, Controller> htControllers = DeviceManager
				.getAllControllers();
		Enumeration<Controller> en = htControllers.elements();
		SocketPacket packet = null;
		while (en.hasMoreElements()) {
			Controller ctrl = en.nextElement();
			String Id = ctrl.getControllerId();
			TableItem item = new TableItem(table, SWT.NONE);
			boolean isOnline = ctrl.isOnline();
			packet = SessionManager.getConnection(Id);
			String session = "";
			if (packet != null) {
				session = packet.getFromClient().getRemoteAddress().toString();
			}
			Color color = null;
			if (isOnline){
				color = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
			}else{
				color = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
			}
			item.setBackground(color);
			item.setText(new String[] {
					Id,
					String.valueOf(isOnline),
					Utils.formatDateTime(ctrl.getLastHeartbeatDT(),
							"yyyy/MM/dd mm:hh:ss"), session });
		}

	}
}
