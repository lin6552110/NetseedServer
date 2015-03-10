package com.switek.netseed.server.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

import com.switek.netseed.server.dal.DB;


/**
 * 开发：Fen  注释：Lin
 *	导入标准码库工具类  
 *	文件路径格式   type/brandCodew/index.bin
 */
public class ImportIRCodeForm {

	protected Shell shell;
	private Text tbxFolder;
	private Table table;
	static Logger logger = Logger.getLogger(ImportIRCodeForm.class);
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ImportIRCodeForm window = new ImportIRCodeForm();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
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
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(975, 409);
		shell.setText("SWT Application");
		shell.setLayout(new FormLayout());

		Label lblScanFolder = new Label(shell, SWT.NONE);
		FormData fd_lblScanFolder = new FormData();
		fd_lblScanFolder.right = new FormAttachment(0, 83);
		fd_lblScanFolder.top = new FormAttachment(0, 10);
		fd_lblScanFolder.left = new FormAttachment(0, 10);
		lblScanFolder.setLayoutData(fd_lblScanFolder);
		lblScanFolder.setText("Scan Folder:");

		tbxFolder = new Text(shell, SWT.BORDER);
		tbxFolder.setText("E:\\测试");
		FormData fd_tbxFolder = new FormData();
		fd_tbxFolder.left = new FormAttachment(lblScanFolder, 6);
		fd_tbxFolder.top = new FormAttachment(0, 4);
		tbxFolder.setLayoutData(fd_tbxFolder);

		Button btnPickupFolder = new Button(shell, SWT.NONE);
		fd_tbxFolder.right = new FormAttachment(btnPickupFolder, -6);
		FormData fd_btnPickupFolder = new FormData();
		fd_btnPickupFolder.left = new FormAttachment(100, -139);
		fd_btnPickupFolder.top = new FormAttachment(0, 4);
		fd_btnPickupFolder.right = new FormAttachment(100, -91);
		btnPickupFolder.setLayoutData(fd_btnPickupFolder);
		btnPickupFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 新建文件对话框，并设置为打开的方式
				DirectoryDialog filedlg = new DirectoryDialog(shell, SWT.OPEN);
				// 设置文件对话框的标题
				filedlg.setText("Pickup a folder to scan pls.");
				// 设置初始路径
				filedlg.setFilterPath("SystemRoot");
				// 打开文件对话框，返回选中文件的绝对路径
				String selected = filedlg.open();
				if (selected == null) {
					return;
				}
				tbxFolder.setText(selected);
			}
		});
		btnPickupFolder.setText("...");
		
		

		Button btnScan = new Button(shell, SWT.NONE);
		btnScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String folder = tbxFolder.getText().trim();
				if (folder.length() == 0) {
					MessageBox.show("Input folder pls");
					return;
				}

				File root = new File(folder);
				if (!root.isDirectory()) {
					MessageBox.show("Pickup a folder pls");
					return;
				}

				if (!root.exists()) {
					MessageBox.show("The folder doesn't exists.");
					return;
				}

				try {
					table.removeAll();
					scanFile(root);
				} catch (Exception ex) {
					MessageBox.show(ex.getLocalizedMessage());
				}
			}
		});
		
		
		FormData fd_btnScan = new FormData();
		fd_btnScan.left = new FormAttachment(100, -71);
		fd_btnScan.right = new FormAttachment(100, -10);
		fd_btnScan.top = new FormAttachment(0, 4);
		btnScan.setLayoutData(fd_btnScan);
		btnScan.setText("Scan");
		

		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(btnPickupFolder, 2);
		fd_table.left = new FormAttachment(0, 20);
		fd_table.right = new FormAttachment(100, -10);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		initTable();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						logger.debug("shell.isVisible(): " + shell.isVisible() + String.valueOf(System.currentTimeMillis()));
						
					}
				});
				
			}
		}, 1*1000, 5* 1000);

		Button btnImport = new Button(shell, SWT.NONE);
		fd_table.bottom = new FormAttachment(btnImport, -6);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importIRCode();
			}
		});
		FormData fd_btnImport = new FormData();
		fd_btnImport.left = new FormAttachment(100, -93);
		fd_btnImport.right = new FormAttachment(100, -10);
		fd_btnImport.bottom = new FormAttachment(100, -10);
		btnImport.setLayoutData(fd_btnImport);
		btnImport.setText("Import");
		
		Button brnExport=new Button(shell,SWT.NONE);
		brnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				exportIRcode();
			}
		});
		
		FormData fd_brnExport=new FormData();
		fd_table.bottom = new FormAttachment(brnExport, -6);
		fd_brnExport.left=new FormAttachment(50, 0);
		fd_brnExport.bottom=new FormAttachment(100, 0);
		brnExport.setLayoutData(fd_brnExport);
		brnExport.setText("brnExport");

	}
	
	

	protected void importIRCode() {
		TableItem[] items = table.getItems();
		for (TableItem tableItem : items) {
			
			String typeCode = tableItem.getText(1);
			String brandCode = tableItem.getText(2);
			String index = tableItem.getText(3);
			String filePath = tableItem.getText(4);
			
			if (typeCode.equalsIgnoreCase("UNKNOWN")){
				
				continue;
			}
			
			FileInputStream fileInputStream=null;
			String result= "DONE";
			try {
				fileInputStream = new FileInputStream(filePath);
				int len = fileInputStream.available();
				byte[] bFile = new byte[len];
				fileInputStream.read(bFile);
			
			
				List<Object> params = new ArrayList<>();
				params.add(typeCode);
				params.add(brandCode);
				params.add(index);
				
				DB.executeSQL("delete from adircode where devicetype=? and brandcode=? and ircodeindex=?", params);
				
				params.add(bFile);
				DB.executeSQL(
						"insert into adircode VALUES (?, ?, ?, ?, '', CURRENT_TIMESTAMP,'LIN' )",
						params);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				result=e.getLocalizedMessage();
			}finally{
				if (fileInputStream !=null){
					try {
						fileInputStream.close();
						fileInputStream=null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			tableItem.setText(5, result);
		
		}
		
	}
	


	private void scanFile(File dir) {

		
		for (File irFile : dir.listFiles()) {
			if (irFile.isDirectory()) {
				scanFile(irFile);
				continue;
			}

			String type = irFile.getParentFile().getParentFile().getName();
			String typeCode = "";
			switch (type) {
			case "空调":
				typeCode = "1";
				break;
			case "电视":
				typeCode = "2";
				break;
			case "机顶盒":
				typeCode = "3";
				break;

			default:
				typeCode = "UNKNOWN";
			}
			String brandCode = irFile.getParentFile().getName();
			int index = Integer.valueOf(irFile.getName().substring(0,
					irFile.getName().length() - 4)) - 1;
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { type, typeCode, brandCode,
					String.valueOf(index), irFile.getAbsolutePath(), "" });
			

		}
	}
	
	

	private void initTable() {
		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(table, SWT.CENTER);
		TableColumn tc4 = new TableColumn(table, SWT.CENTER);
		TableColumn tc5 = new TableColumn(table, SWT.CENTER);
		TableColumn tc6 = new TableColumn(table, SWT.CENTER);
		tc1.setText("Type");
		tc2.setText("TypeCode");
		tc3.setText("BrandCode");
		tc4.setText("Index");
		tc5.setText("File");
		tc6.setText("Import Result");

		tc1.setWidth(70);
		tc2.setWidth(70);
		tc3.setWidth(70);
		tc4.setWidth(70);
		tc5.setWidth(300);
		tc6.setWidth(300);
		table.setHeaderVisible(true);
	}
	
	public void exportIRcode(){
		String sql="select * from adircode";
		List<Object> params=new ArrayList<Object>();
	
		String root=tbxFolder.getText().trim();
		StringBuffer filtURL=new StringBuffer();
		filtURL.append(root);
		FileOutputStream fileOutputStream=null;
		try {
			ResultSet rs=DB.executeQuery(sql, params);
			while(rs.next()){
				filtURL.delete(root.length(), filtURL.length());
				int type=rs.getInt("DeviceType");
				String brandCode=rs.getString("BrandCode");
				int irCodeIndex=rs.getInt("IRCodeIndex");
				irCodeIndex++;
				byte[] irCodeData=rs.getBytes("IRCodeData");
				String deviceType="UNKNOWN";
				switch (type) {
				case 1:
					deviceType="空调";
					break;
				case 2:
					deviceType="电视";
					break;
				case 3:
					deviceType="机顶盒";
					break;
				}
				filtURL.append("\\"+deviceType+"\\"+brandCode+"\\"+irCodeIndex+".bin");
				System.out.println(filtURL.toString());
				
				 fileOutputStream=new FileOutputStream(createFile(filtURL.toString()));
				 fileOutputStream.write(irCodeData);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(null!=fileOutputStream){
				try {
					fileOutputStream.close();
					fileOutputStream=null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	public File createFile(String url){
		File file=new File(url);
		if(file.exists()){
			file.delete();
			createFile(url);
		}else{
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
		
	}
}
