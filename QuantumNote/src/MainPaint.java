import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.undo.*;


public class MainPaint{  
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel(UIManager.
					getSystemLookAndFeelClassName());
	    }			//加载系统界面风格
	    catch (Exception e){
	    }
		new MyPaint();
	}
}

class MyPaint extends JFrame{
	JMenuBar jmenuBar;
	ObjectInputStream  input;
	ObjectOutputStream output; //定义输入输出流，用来调用和保存图像文件
	private JButton choices[];         //按钮数组
	private String names[]={
			"Select",       //选择模式
			"Undo",
			"Redo",
			"Pencil",		//自由画笔
			"Line",			//直线
			"Rect",			//空心矩形
			"fRect",		//实心矩形
			"Oval",			//空心椭圆
			"fOval",		//实心椭圆
			"Circle",		//圆形
			"fCircle",		//实心圆形
			"RoundRect",	//圆角矩形
			"fRect",		//实心圆角矩形
			"3DRect",		//3D矩形
			"f3DRect",		//实心3D矩形
			"Cube",			//立方体
			"Eraser",		//橡皮擦
			"bgColor",		//背景色 
			"Color",		//画笔颜色
			"Stroke",		//画笔大小
			"Word"			//文本输入
		};
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	String styleNames[] = ge.getAvailableFontFamilyNames();  //加载系统字体
	private Icon items[];
	private String tipText[]={"Select",  "Undo", "Redo", "Pencil","Line","Hollow Rectangle",
			"Filled Rectangle","Hollow Oval","Filled Oval","Circle","Filled Circle","Hollow RoundRectangle",
			"Filled RoundRectangle","3D Rectangle","Filled 3D Rectangle","3D Cuboid","Eraser", "Setting Backgound Color","Brush Color",
			"Brush Thickness","Adding Text"};			//按钮提示说明
	private JToolBar buttonPanel;			//定义按钮面板
	private JToolBar simpleButtonPanel;
	private JLabel statusBar;				//显示鼠标状态的提示条 
	DrawPanel drawingArea;			//定义画图区域
	
	int index = -1;							//已绘制图形数目 
	int start_index = -1;
	ArrayList<drawings> itemList = new ArrayList<drawings>(2000);	//用来存放基本图形的数组
	int sync_index = -1;
	ArrayList<drawings> sync_itemList = new ArrayList<drawings>(2000);
	private selectItems selectitem;
	private UndoManager myUndomanager;
	private int currentChoice = 3;			//设置初始画笔为自由画笔
	private int previousChoice = -1;
	private Color color=Color.black;		//画笔颜色  
	int R,G,B;								//颜色值  
	int f1,f2;								//存放当前字体风格 
	int copy_count = 0;
	String style1;							//存放当前字体
	private float stroke = 2.0f;		    //设置画笔粗细
	static int thickness = 10;				//立方体宽度
	JCheckBox bold,italic;					//定义字体风格选择框               
	JComboBox styles;						//字体选择框
	int screenWidth;
	int screenHeight;
	int noteWidth;
    int noteHeight;
    char userMode; 
	private HandWrite myHandWrite;
	private Modifier myModifier;
	private SyncControl mySyncControllor;
	private savetoJPG_PDF myJPG_PDF;
	String sync_status = "Sync Function: OFF";
	String handwrite_status = "Handwrite Function: OFF";
	
	public MyPaint(){
		Object[] options = { " Teacher Mode ", " Student Mode " };
		int s = JOptionPane.showOptionDialog(null, "Thank you for using NotePad and please choose your model ", "NotePad Information", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		switch (s) {
			case 0: userMode = 't'; 
					break;
			case 1: userMode = 's';
					break;	
			default: System.exit(0);
		}
		
		Toolkit kit = Toolkit.getDefaultToolkit();              
        Dimension screenSize = kit.getScreenSize();             
        screenWidth = screenSize.width;                     
        screenHeight = screenSize.height;
        noteWidth = 1000;
        noteHeight = 650;		
        
        this.addKeyListener(new KeyAdapter(){    
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_DELETE){
					for(int i = selectitem.selectIndex; i >= 0; i--){
						for(int j = myUndomanager.drawList.get(i).startPos; j <= myUndomanager.drawList.get(i).endPos; j++){
							itemList.remove(myUndomanager.drawList.get(i).startPos);
							index--;
						}
						myUndomanager.drawList.remove(selectitem.selectitemList[i]);
						myUndomanager.draw_count--;
						for(int j = i; j < selectitem.selectIndex; j++){
							selectitem.selectitemList[j] = selectitem.selectitemList[j+1];
						}
						selectitem.selectitemList[selectitem.selectIndex] = 0;
						selectitem.selectIndex--;
					}
					drawingArea.repaint();
				}
		    }  
		});

        this.addComponentListener(new ComponentAdapter(){
        	public void componentResized(ComponentEvent e){
        		noteWidth = (int) e.getComponent().getSize().getWidth();
        		noteHeight = (int) e.getComponent().getSize().getHeight();
        	}
        });
        
        setTitle("NotePad");
        setLocation((screenWidth-noteWidth)/2,(screenHeight-noteHeight)/2);
		setSize(noteWidth,noteHeight);					//画图板大小
		setVisible(true);
		setCursor(new Cursor(Cursor.HAND_CURSOR));		//设置画图板鼠标样式
		drawingArea = new DrawPanel();
		myUndomanager = new UndoManager();
		myHandWrite = new HandWrite();
		myModifier = new Modifier();
		mySyncControllor = new SyncControl(this);
		myJPG_PDF = new savetoJPG_PDF(this);
				
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {			
				Object[] options = { " No(N) ", " Yes(Y) " };
				int s = JOptionPane.showOptionDialog(null, "Are you sure to exit？", "Exit Information", JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				switch (s) {
				case 1:
					System.exit(0);	
				}
			}
		});					//关闭确认提示
		
		getJMenuBar();      //获取菜单栏
		items=new ImageIcon[names.length];
		
		//创建各种基本图形的按钮 
		choices=new JButton[names.length];
		buttonPanel = new JToolBar( JToolBar.VERTICAL);
		buttonPanel = new JToolBar( JToolBar.HORIZONTAL);
		simpleButtonPanel = new JToolBar(JToolBar.VERTICAL);
		ButtonHandler handler=new ButtonHandler();
		ButtonHandler1 handler1=new ButtonHandler1();
		ButtonHandler_Select handler_select= new ButtonHandler_Select();
		ButtonHandler_Undo handler_undo = new ButtonHandler_Undo();
		ButtonHandler_Redo handler_redo = new ButtonHandler_Redo();
		selectitem = new selectItems();
		buttonPanel.setBackground(new Color(255,255,255));	 //工具栏背景色设置
		buttonPanel.setVisible(false);
		simpleButtonPanel.setVisible(true);	

		//导入图形图标，图标存放在项目文件夹下的Icons目录内 
		for(int i = 0; i < choices.length; i++){
			choices[i] = new JButton(names[i]);
			buttonPanel.add(choices[i]);
		}
		
		ToolMenu(); 		 //工具栏右击事件调用
		
		//将动作侦听器加入按钮里面
		for(int i = 3; i < choices.length-4; i++){
			choices[i].addActionListener(handler);
		}
		choices[0].addActionListener(handler_select);
		choices[1].addActionListener(handler_undo);
		choices[2].addActionListener(handler_redo);
		choices[choices.length-4].addActionListener(handler1); //背景色
		choices[choices.length-3].addActionListener(handler1); //画笔颜色
		choices[choices.length-2].addActionListener(handler1); //画笔大小
		choices[choices.length-1].addActionListener(handler1); //文本输入
		
		//字体风格选择
		styles=new JComboBox(styleNames);
		styles.setMaximumRowCount(10);
		styles.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				style1=styleNames[styles.getSelectedIndex()];
			}
		});
		
		//字体选择
		bold=new JCheckBox("Overstriking");
		italic=new JCheckBox("Tilt");
		checkBoxHandler cHandler=new checkBoxHandler();
		bold.addItemListener(cHandler);
		italic.addItemListener(cHandler);
		bold.setBackground(new Color(0,255,0));
		italic.setBackground(new Color(0,255,0));
		buttonPanel.setLayout(new GridLayout(2, names.length));
		buttonPanel.add(bold);
		buttonPanel.add(italic);
		buttonPanel.addSeparator();
		buttonPanel.add(new JLabel("Font:"));
		buttonPanel.add(styles);
		buttonPanel.setFloatable(false);
		styles.setMinimumSize(new Dimension(100,20));		//字体选框大小设置
		styles.setMaximumSize(new Dimension(120,20));
		Container c = getContentPane();
		c.add(buttonPanel,BorderLayout.NORTH);
		
		simpleButtonPanel.setLayout(new GridLayout(8, 1));
		JButton def_pencil = new JButton("Pencil");
		JButton def_eraser = new JButton("Eraser");
		JButton def_undo = new JButton("Undo");
		JButton def_redo = new JButton("Redo");
		JButton def_select = new JButton("Select");
		JButton def_modify = new JButton("Modify");
		JButton def_copy = new JButton("Copy");
		JButton def_delete = new JButton("Delete");
		simpleButtonPanel.add(def_pencil);
		simpleButtonPanel.add(def_eraser);
		simpleButtonPanel.add(def_undo);
		simpleButtonPanel.add(def_redo);
		simpleButtonPanel.add(def_select);
		simpleButtonPanel.add(def_modify);
		simpleButtonPanel.add(def_copy);
		simpleButtonPanel.add(def_delete);
		
		def_pencil.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {                                            
					drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					previousChoice = currentChoice = 3;
				} catch (CannotRedoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot use pencil！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		def_eraser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					if(myHandWrite.handwrite_switcher == 0){
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						previousChoice = currentChoice = 16;
					}
					else{
						Object[] options = { "OK"};
						int s = JOptionPane.showOptionDialog(null, "Eraser cannot be used when handwrite function is working. Please turn off the handwrite function before using eraser, thanks", "Handwrite Information", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					}
				} catch (CannotRedoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot use eraser！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		//撤销菜单项的功能实现
		def_undo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {                                            
					drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					if(index >= 0){
						myUndomanager.undoOperation(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);	
						for(int i = myUndomanager.drawList.get(myUndomanager.draw_count).startPos; i <= myUndomanager.drawList.get(myUndomanager.draw_count).endPos; i++){							
							itemList.remove(itemList.size()-1);	
						}
						drawingArea.repaint();
						index = myUndomanager.drawList.get(myUndomanager.draw_count).startPos-1;
						myUndomanager.drawList.remove(myUndomanager.draw_count--);
					}
				} catch (CannotUndoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot undo！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		//恢复菜单项的功能实现
		def_redo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {                                            
					drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					if(myUndomanager.redo_count >= 0){
						if(myUndomanager.write_flag == 0){
							myUndomanager.drawList.add(myUndomanager.redoList.get(myUndomanager.redo_count));
							myUndomanager.draw_count++;
							for(int i = myUndomanager.redoList.get(myUndomanager.redo_count).startPos; i <= myUndomanager.redoList.get(myUndomanager.redo_count).endPos; i++){
								index++;
								currentChoice = myUndomanager.redoList.get(myUndomanager.redo_count).currentChoice;
								createNewItem(currentChoice);
							}
							myUndomanager.redoOperation(itemList);
							repaint();
						}
						else
							myUndomanager.redoOperation(itemList);
					}						
				} catch (CannotRedoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot redo！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		//选择菜单项的功能实现
		def_select.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {                                            
					drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					currentChoice = 0;
				} catch (CannotRedoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot select！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		//修改菜单项的功能实现
		def_modify.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					previousChoice = currentChoice;
					currentChoice = -1;
					myModifier.modify_switcher = 1;
				} catch (CannotRedoException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Cannot modify！","Edit Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		//拷贝菜单项的功能实现
		def_copy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				copy_count++;
				int temp_start_index = 0;
				for (int i = 0; i <= selectitem.selectIndex; i++) {
					temp_start_index = index + 1;
					selectitem.Check_max_min_border(myUndomanager.drawList, itemList);
					
					if (myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice != 17) {
						for (int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++) {
							index++;
							createNewItem(myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice);
							itemList.get(index).x1 = itemList.get(j).x1 + selectitem.max_width * copy_count;
							itemList.get(index).y1 = itemList.get(j).y1 + selectitem.max_height * copy_count;
							itemList.get(index).x2 = itemList.get(j).x2 + selectitem.max_width * copy_count;
							itemList.get(index).y2 = itemList.get(j).y2 + selectitem.max_height * copy_count;
							itemList.get(index).R = itemList.get(j).R_backup;
							itemList.get(index).G = itemList.get(j).G;
							itemList.get(index).B = itemList.get(j).B;
							itemList.get(index).stroke = itemList.get(j).stroke;
							itemList.get(index).type = itemList.get(j).type;
							itemList.get(index).thickness = itemList.get(j).thickness;
							itemList.get(index).shapeType = itemList.get(j).shapeType;	
						}		
						
						myUndomanager.drawList.add(new drawing_info());
						myUndomanager.draw_count++;
						myUndomanager.drawList.get(myUndomanager.draw_count).startPos = temp_start_index;
						myUndomanager.drawList.get(myUndomanager.draw_count).endPos = temp_start_index + myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos - myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos;
						myUndomanager.drawList.get(myUndomanager.draw_count).type = myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice;
						myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);									
						myUndomanager.write_flag = 1;
					}
					else{
						index++;
						createNewItem(myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice);
						itemList.get(index).x1 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x1 + selectitem.max_width * copy_count;
						itemList.get(index).y1 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y1 + selectitem.max_height * copy_count;
						itemList.get(index).x2 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x2 + selectitem.max_width * copy_count;
						itemList.get(index).y2 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y2 + selectitem.max_height * copy_count;
						itemList.get(index).R = itemList.get(selectitem.selectitemList[i]).R_backup;
						itemList.get(index).G = itemList.get(selectitem.selectitemList[i]).G;
						itemList.get(index).B = itemList.get(selectitem.selectitemList[i]).B;
						itemList.get(index).stroke = itemList.get(selectitem.selectitemList[i]).stroke;
						itemList.get(index).type = itemList.get(selectitem.selectitemList[i]).type;
						itemList.get(index).thickness = itemList.get(selectitem.selectitemList[i]).thickness;
						itemList.get(index).shapeType = itemList.get(selectitem.selectitemList[i]).shapeType;
						itemList.get(index).s1 = itemList.get(selectitem.selectitemList[i]).s1;
	                    itemList.get(index).x2 = itemList.get(selectitem.selectitemList[i]).x2;
	                    itemList.get(index).y2 = itemList.get(selectitem.selectitemList[i]).y2;
	                    itemList.get(index).width = itemList.get(selectitem.selectitemList[i]).width;
	                    itemList.get(index).height = itemList.get(selectitem.selectitemList[i]).height;
	                    itemList.get(index).s2 = itemList.get(selectitem.selectitemList[i]).s2;
	                    myUndomanager.drawList.add(new drawing_info());
						myUndomanager.draw_count++;
						myUndomanager.drawList.get(myUndomanager.draw_count).startPos = index;
						myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
						myUndomanager.drawList.get(myUndomanager.draw_count).type = myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice;
						myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);												
					}
				}
				drawingArea.repaint();
			}
		});
		
		//删除菜单项的功能实现
				def_delete.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try {                                           
							for(int i = selectitem.selectIndex; i >= 0; i--){																				
								for(int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++){
									itemList.remove(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos);
									index--;
								}
								int delete_count = myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos - myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos + 1;
								myUndomanager.drawList.remove(selectitem.selectitemList[i]);
								myUndomanager.draw_count--;
								for(int j = selectitem.selectitemList[i]; j < myUndomanager.drawList.size(); j++){
									myUndomanager.drawList.get(j).startPos -= delete_count;
									myUndomanager.drawList.get(j).endPos -= delete_count;
								}
								
								for(int j = i; j < selectitem.selectIndex; j++){
									selectitem.selectitemList[j] = selectitem.selectitemList[j+1];
								}
								selectitem.selectitemList[selectitem.selectIndex] = 0;
								selectitem.selectIndex--;
							}
							drawingArea.repaint();
						} catch (CannotRedoException ex) {
							JOptionPane.showMessageDialog(new JFrame(),
									"Cannot delete！","Edit Information",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}
				});
		
		c.add(simpleButtonPanel,BorderLayout.WEST);
		c.add(drawingArea,BorderLayout.CENTER);
		statusBar=new JLabel();
		c.add(statusBar,BorderLayout.SOUTH);
		setSize(noteWidth,noteHeight);
		setVisible(true);

	}
	
	//按钮侦听器ButtonHanler类，内部类，用来侦听基本按钮的操作
	public class ButtonHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			for(int j = 3; j < choices.length-4; j++){
				if(e.getSource() == choices[j]){
					if(j >= 3) {previousChoice = currentChoice;}
					currentChoice = j;
				}
			}
		}
	}
	
	//按钮侦听器ButtonHanler1类，用来侦听颜色选择、画笔粗细设置、文字输入按钮的操作
	public class ButtonHandler1 implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource()==choices[choices.length-4]){
				SetbgColor();
			}
			if(e.getSource()==choices[choices.length-3]){
				chooseColor();
			}
			if(e.getSource()==choices[choices.length-2]){
				setStroke();
			}
			if(e.getSource()==choices[choices.length-1]){				
				Object[] options = { " OK " };
				int s = JOptionPane.showOptionDialog(null, "Add the text in the position where mouse clicks",
						"Add Text", JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				currentChoice = 17;
			}
		}
	}
	
	public class ButtonHandler_Select implements ActionListener{
		public void actionPerformed(ActionEvent e){
			currentChoice = 0;
			drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public class ButtonHandler_Undo implements ActionListener{
		public void actionPerformed(ActionEvent e){
			drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(index >= 0){
				myUndomanager.undoOperation(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);	
				for(int i = myUndomanager.drawList.get(myUndomanager.draw_count).startPos; i <= myUndomanager.drawList.get(myUndomanager.draw_count).endPos; i++){
					itemList.remove(itemList.size()-1);	
				}
				drawingArea.repaint();
				index = myUndomanager.drawList.get(myUndomanager.draw_count).startPos-1;
				myUndomanager.drawList.remove(myUndomanager.draw_count--);
			}
		}
	}
	
	public class ButtonHandler_Redo implements ActionListener{
		public void actionPerformed(ActionEvent e){
			drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(myUndomanager.redo_count >= 0){
				if(myUndomanager.write_flag == 0){
					myUndomanager.drawList.add(myUndomanager.redoList.get(myUndomanager.redo_count));
					myUndomanager.draw_count++;
					for(int i = myUndomanager.redoList.get(myUndomanager.redo_count).startPos; i <= myUndomanager.redoList.get(myUndomanager.redo_count).endPos; i++){
						index++;
						currentChoice = myUndomanager.redoList.get(myUndomanager.redo_count).currentChoice;
						createNewItem(currentChoice);
					}
					myUndomanager.redoOperation(itemList);
					repaint();
				}
				else
					myUndomanager.redoOperation(itemList);
			}
		}
	}
	
	//鼠标事件mouseA类，继承了MouseAdapter，用来完成鼠标相应事件操作
	class mouseA extends MouseAdapter{
		public void mousePressed(MouseEvent e){
			statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示
			if(currentChoice >= 3){
				start_index = index;
				index++;	
				createNewItem(currentChoice);
				itemList.get(index).x1=itemList.get(index).x2=e.getX();
				itemList.get(index).y1=itemList.get(index).y2=e.getY();
				myUndomanager.drawList.add(new drawing_info());
				myUndomanager.draw_count++;
				myUndomanager.drawList.get(myUndomanager.draw_count).startPos = index;
				myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
				myUndomanager.drawList.get(myUndomanager.draw_count).type = currentChoice;
				myUndomanager.write_flag = 1;
				
				if(myHandWrite.handwrite_switcher == 1){
					myHandWrite.Add_to_pointList(itemList.get(index).x1, itemList.get(index).y1, index);
					myHandWrite.turn_point_list.get(0).index = index;
				}
				
				//如果当前选择的图形是随笔画或者橡皮擦，则进行下面的操作 
				if(currentChoice == 3 || currentChoice == 16){
					index++;
					createNewItem(currentChoice);
					itemList.get(index).x1=itemList.get(index).x2=e.getX();
					itemList.get(index).y1=itemList.get(index).y2=e.getY();
				}
				//如果当前选择的图形式文字输入，则进行下面操作
				if(currentChoice == 17){
					itemList.get(index).x1=e.getX();
					itemList.get(index).y1=e.getY();
					String input;
					input=JOptionPane.showInputDialog("Input the content: ");
					
                    if(input != null){
                        itemList.get(index).s1=input;
                        itemList.get(index).x2=f1;
                        itemList.get(index).y2=f2;
                        itemList.get(index).width = input.length()*10;
                        itemList.get(index).height = 20;
                        itemList.get(index).s2=style1;
                        myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, currentChoice, itemList);
                        drawingArea.repaint();
                    }
				}
			}
			else if(currentChoice == 0){
				if(selectitem.select_flag == 0){
					selectitem.selectRange_x1 = selectitem.selectRange_x2 = e.getX();
					selectitem.selectRange_y1 = selectitem.selectRange_y2 = e.getY();
				}
				else if(selectitem.select_flag == 1){
					int press_pointX = e.getX();
					int press_pointY = e.getY();
										
					for(int i = 0; i <= selectitem.selectIndex; i++){
						if(myUndomanager.drawList.get(selectitem.selectitemList[i]).max_x >= press_pointX && myUndomanager.drawList.get(selectitem.selectitemList[i]).min_x <= press_pointX && myUndomanager.drawList.get(selectitem.selectitemList[i]).max_y >= press_pointY && myUndomanager.drawList.get(selectitem.selectitemList[i]).min_y <= press_pointY){
							selectitem.move_x1 = selectitem.move_x2 = press_pointX;
							selectitem.move_y1 = selectitem.move_y2 = press_pointY;
							selectitem.move_flag = 1;
							break;
						}
					}

					if(selectitem.move_flag == 0){
						selectitem.select_flag = 0;
						selectitem.move_x1 = selectitem.move_x2 = selectitem.move_y1 = selectitem.move_y2 = 0;
						selectitem.move_height = selectitem.move_width = 0;
						selectitem.selectRange_x1 = selectitem.selectRange_x2 = e.getX();
						selectitem.selectRange_y1 = selectitem.selectRange_y2 = e.getY();
						copy_count = 0;
						
						for(int i = 0; i <= selectitem.selectIndex; i++){
							for(int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++){
								itemList.get(j).R = itemList.get(j).R_backup;
							}
							selectitem.selectitemList[i] = 0;
						}
						repaint();
						selectitem.selectIndex = -1;
					}
					selectitem.move_flag = 0;
				}
			}
			
			else if(myModifier.modify_switcher == 1){
				int press_pointX = e.getX();
				int press_pointY = e.getY();
				for(int i = 0; i < myUndomanager.drawList.size(); i++){
					int stop_flag = 0;
					if(myUndomanager.drawList.get(i).currentChoice == 3){
						for(int j = myUndomanager.drawList.get(i).startPos; j <= myUndomanager.drawList.get(i).endPos; j++){
							itemList.get(j).set_max_min_value();
							if(itemList.get(j).max_x_drawings >= press_pointX && itemList.get(j).min_x_drawings <= press_pointX && itemList.get(j).max_y_drawings >= press_pointY && itemList.get(j).min_y_drawings <= press_pointY){
								Object[] options = { " OK " };
								int s = JOptionPane.showOptionDialog(null, "This curve cannot be modified!", "Modification Information", JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE, null, options, options[0]);
								myModifier.clear_modifier();
								stop_flag = 1;
								break;
							}							
						}
						if(stop_flag == 1) break;
					}
					else if(myUndomanager.drawList.get(i).currentChoice == 17){
						myUndomanager.drawList.get(i).item.get(0).set_max_min_value();
						if(myUndomanager.drawList.get(i).item.get(0).max_x_drawings >= press_pointX && myUndomanager.drawList.get(i).item.get(0).min_x_drawings <= press_pointX && myUndomanager.drawList.get(i).item.get(0).max_y_drawings >= press_pointY && myUndomanager.drawList.get(i).item.get(0).min_y_drawings <= press_pointY){
							Object[] options = { " OK " };
							int s = JOptionPane.showOptionDialog(null, "This word cannot be modified!", "Modification Information", JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE, null, options, options[0]);
							myModifier.clear_modifier();
							break;
						}		
					}
					else{
						myUndomanager.drawList.get(i).item.get(0).set_max_min_value();
						if(myUndomanager.drawList.get(i).item.get(0).max_x_drawings >= press_pointX && myUndomanager.drawList.get(i).item.get(0).min_x_drawings <= press_pointX && myUndomanager.drawList.get(i).item.get(0).max_y_drawings >= press_pointY && myUndomanager.drawList.get(i).item.get(0).min_y_drawings <= press_pointY){
							itemList.get(myUndomanager.drawList.get(i).startPos).set_max_min_value();
							myModifier.modify_flag = 1;
							myModifier.modify_switcher = 1;
							myModifier.press_x = press_pointX;
							myModifier.press_y = press_pointY;
							myModifier.start_x1 = itemList.get(myUndomanager.drawList.get(i).startPos).x1;
							myModifier.start_y1 = itemList.get(myUndomanager.drawList.get(i).startPos).y1;
							myModifier.start_x2 = itemList.get(myUndomanager.drawList.get(i).startPos).x2;
							myModifier.start_y2 = itemList.get(myUndomanager.drawList.get(i).startPos).y2;
							myModifier.index = i;
							break;
						}
					}
				}
			}			
		}
		
		public void mouseReleased(MouseEvent e){
			if(currentChoice >= 3){
				statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示
				if(myHandWrite.handwrite_switcher == 0){
					if(currentChoice == 3 || currentChoice == 16){
						index++;
						createNewItem(currentChoice);
						itemList.get(index).x1=e.getX();
						itemList.get(index).y1=e.getY();
						myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
					}  
					itemList.get(index).x2=e.getX();
					itemList.get(index).y2=e.getY();
					repaint();
					myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, currentChoice, itemList);				
				}
				else{
					myHandWrite.TurnPointRecognization();				
					myHandWrite.Add_to_pointList(e.getX(), e.getY(), index);
					int temp_index = index;
					for(int i = start_index+1; i <= temp_index; i++){
						itemList.remove(itemList.size()-1);
					}
					index = myHandWrite.turn_point_list.get(0).index-1;
					drawingArea.repaint();
					myHandWrite.ShapeRecognization();
//					System.out.println("type is: " + myHandWrite.shape_flag);
//					System.out.println("size = " + myHandWrite.turn_point_list.size());
					if(myHandWrite.shape_flag == 'l' && myHandWrite.line_flag == 1){	   //识别出的图形为line
						currentChoice = 4;
						index++;
						createNewItem(currentChoice);
						itemList.get(index).x1 = myHandWrite.turn_point_list.get(0).x;
						itemList.get(index).y1 = myHandWrite.turn_point_list.get(0).y;
						itemList.get(index).x2 = myHandWrite.turn_point_list.get(myHandWrite.turn_point_count).x;
						itemList.get(index).y2 = myHandWrite.turn_point_list.get(myHandWrite.turn_point_count).y;
						drawingArea.repaint();
						myUndomanager.drawList.get(myUndomanager.draw_count).startPos = myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
						myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, currentChoice, itemList);											
					}
					
					else if(myHandWrite.shape_flag == 'c'){    //识别出的图形为circle
						index++;
						currentChoice = 9;
						createNewItem(currentChoice);						
						index = myHandWrite.turn_point_list.get(0).index;
						itemList.get(index).x1 = myHandWrite.turn_point_list.get(0).x;
						itemList.get(index).y1 = myHandWrite.turn_point_list.get(0).y;
						itemList.get(index).x2 = myHandWrite.max_x_for_circle;
						itemList.get(index).y2 = myHandWrite.max_y_for_circle;	
						drawingArea.repaint();
						myUndomanager.drawList.get(myUndomanager.draw_count).startPos = myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
						myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, currentChoice, itemList);											
					}
					
					else if(myHandWrite.shape_flag == 'r'){     //识别出的图形为rectangle
						index++;
						currentChoice = 5;
						createNewItem(currentChoice);						
						index = myHandWrite.turn_point_list.get(0).index;
						itemList.get(index).x1 = myHandWrite.turn_point_list.get(0).x;
						itemList.get(index).y1 = myHandWrite.turn_point_list.get(0).y;
						itemList.get(index).x2 = myHandWrite.turn_point_list.get(2).x;
						itemList.get(index).y2 = myHandWrite.turn_point_list.get(2).y;	
						drawingArea.repaint();
						myUndomanager.drawList.get(myUndomanager.draw_count).startPos = myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
						myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, currentChoice, itemList);											
					}
					
					if(myHandWrite.shape_flag != 'l' && myHandWrite.shape_flag != 'c' && myHandWrite.shape_flag != 'r'){
						myUndomanager.drawList.remove(myUndomanager.draw_count);
						myUndomanager.draw_count--;
					}
					myHandWrite.Handwrite_Reset();
					currentChoice = 3;					
				}
			}
			else if(currentChoice == 0){
				if(selectitem.select_flag == 0){
					int temp_max_x, temp_max_y, temp_min_x, temp_min_y;
					Graphics g = drawingArea.getGraphics();
					Graphics2D g2d = (Graphics2D)g;
					g2d.setXORMode(Color.WHITE);
					selectitem.draw(g2d);
					selectitem.selectRange_x2 = e.getX();
					selectitem.selectRange_y2 = e.getY();
					g2d.setXORMode(Color.WHITE);
					selectitem.set_max_min_value();
					
					for(int i = 0; i <= myUndomanager.draw_count; i++){
						if(myUndomanager.drawList.get(i).item.get(0).shapeType == 'w'){
							myUndomanager.drawList.get(i).min_x = myUndomanager.drawList.get(i).item.get(0).x1;
							myUndomanager.drawList.get(i).min_y = myUndomanager.drawList.get(i).item.get(0).y1 - myUndomanager.drawList.get(i).item.get(0).height;
							myUndomanager.drawList.get(i).max_x = myUndomanager.drawList.get(i).item.get(0).x1 + myUndomanager.drawList.get(i).item.get(0).width;
							myUndomanager.drawList.get(i).max_y = myUndomanager.drawList.get(i).item.get(0).y1;
						}
						else{
							myUndomanager.drawList.get(i).max_x = (myUndomanager.drawList.get(i).item.get(0).x1 > myUndomanager.drawList.get(i).item.get(0).x2) ? myUndomanager.drawList.get(i).item.get(0).x1 : myUndomanager.drawList.get(i).item.get(0).x2;
							myUndomanager.drawList.get(i).max_y = (myUndomanager.drawList.get(i).item.get(0).y1 > myUndomanager.drawList.get(i).item.get(0).y2) ? myUndomanager.drawList.get(i).item.get(0).y1 : myUndomanager.drawList.get(i).item.get(0).y2;
							myUndomanager.drawList.get(i).min_x = myUndomanager.drawList.get(i).item.get(0).x1 + myUndomanager.drawList.get(i).item.get(0).x2 - myUndomanager.drawList.get(i).max_x;
							myUndomanager.drawList.get(i).min_y = myUndomanager.drawList.get(i).item.get(0).y1 + myUndomanager.drawList.get(i).item.get(0).y2 - myUndomanager.drawList.get(i).max_y;
							
							for(int j = 1; j < myUndomanager.drawList.get(i).item.size(); j++){
								temp_max_x = (myUndomanager.drawList.get(i).item.get(j).x1 > myUndomanager.drawList.get(i).item.get(j).x2) ? myUndomanager.drawList.get(i).item.get(j).x1 : myUndomanager.drawList.get(i).item.get(j).x2;
								temp_max_y = (myUndomanager.drawList.get(i).item.get(j).y1 > myUndomanager.drawList.get(i).item.get(j).y2) ? myUndomanager.drawList.get(i).item.get(j).y1 : myUndomanager.drawList.get(i).item.get(j).y2;
								temp_min_x = myUndomanager.drawList.get(i).item.get(j).x1 + myUndomanager.drawList.get(i).item.get(j).x2 - temp_max_x;
								temp_min_y = myUndomanager.drawList.get(i).item.get(j).y1 + myUndomanager.drawList.get(i).item.get(j).y2 - temp_max_y;
								myUndomanager.drawList.get(i).max_x = (myUndomanager.drawList.get(i).max_x > temp_max_x) ? myUndomanager.drawList.get(i).max_x : temp_max_x;
								myUndomanager.drawList.get(i).max_y = (myUndomanager.drawList.get(i).max_y > temp_max_y) ? myUndomanager.drawList.get(i).max_y : temp_max_y;
								myUndomanager.drawList.get(i).min_x = (myUndomanager.drawList.get(i).min_x < temp_min_x) ? myUndomanager.drawList.get(i).min_x : temp_min_x;
								myUndomanager.drawList.get(i).min_y = (myUndomanager.drawList.get(i).min_y < temp_min_y) ? myUndomanager.drawList.get(i).min_y : temp_min_y;
							}							
						}
					
						if(selectitem.max_x_select >= myUndomanager.drawList.get(i).max_x && selectitem.min_x_select <= myUndomanager.drawList.get(i).min_x && selectitem.max_y_select >= myUndomanager.drawList.get(i).max_y && selectitem.min_y_select <= myUndomanager.drawList.get(i).min_y){
							selectitem.selectitemList[++selectitem.selectIndex] = i;
							for(int j = myUndomanager.drawList.get(i).startPos; j <= myUndomanager.drawList.get(i).endPos; j++){
								itemList.get(j).R_backup = itemList.get(j).R;
								itemList.get(j).R = 255;
							}							
							repaint();
						}
					}			
					selectitem.select_flag = 1;
				}
								
				else if(selectitem.select_flag == 1){
					selectitem.move_x1 = selectitem.move_x2;
					selectitem.move_y1 = selectitem.move_y2;
					selectitem.move_x2 = e.getX();
					selectitem.move_y2 = e.getY();	
					selectitem.move_width = selectitem.move_x1 - selectitem.move_x2;
					selectitem.move_height = selectitem.move_y1 - selectitem.move_y2;
										
					for(int i = 0; i <= selectitem.selectIndex; i++){
						for(int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++){
							itemList.get(j).x1 -= selectitem.move_width;
							itemList.get(j).x2 -= selectitem.move_width;
							itemList.get(j).y1 -= selectitem.move_height;
							itemList.get(j).y2 -= selectitem.move_height;
							myUndomanager.drawList.get(selectitem.selectitemList[i]).item.get(j-myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x1 = itemList.get(j).x1;
							myUndomanager.drawList.get(selectitem.selectitemList[i]).item.get(j-myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x2 = itemList.get(j).x2;
							myUndomanager.drawList.get(selectitem.selectitemList[i]).item.get(j-myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y1 = itemList.get(j).y1;
							myUndomanager.drawList.get(selectitem.selectitemList[i]).item.get(j-myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y2 = itemList.get(j).y2;
						}						
					}
					drawingArea.repaint();
				}
			}
			else if(myModifier.modify_switcher == 1){
				if(myModifier.modify_flag == 1){
					int move_x = e.getX() - myModifier.press_x;
					int move_y = e.getY() - myModifier.press_y;
					itemList.get(myUndomanager.drawList.get(myModifier.index).startPos).x2 = myModifier.start_x2 + move_x;
					itemList.get(myUndomanager.drawList.get(myModifier.index).startPos).y2 = myModifier.start_y2 + move_y;
					drawingArea.repaint();
					myUndomanager.drawList.get(myModifier.index).item.get(0).x2 = itemList.get(myUndomanager.drawList.get(myModifier.index).startPos).x2;
					myUndomanager.drawList.get(myModifier.index).item.get(0).y2 = itemList.get(myUndomanager.drawList.get(myModifier.index).startPos).y2;
				}
				myModifier.clear_modifier();
				currentChoice = previousChoice;
			}
		}
		
		public void mouseEntered(MouseEvent e){
			statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示
		}
		public void mouseExited(MouseEvent e){
			statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示			
		}
	}
	
	//鼠标事件mouseB类继承了MouseMotionAdapter，用来完成鼠标拖动和鼠标移动时的相应操作
	class mouseB extends MouseMotionAdapter{
		public void mouseDragged(MouseEvent e){
			if(currentChoice >= 3){
				statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示
				if(currentChoice == 3 || currentChoice == 16){
					index++;
					createNewItem(currentChoice);
					itemList.get(index-1).x1=itemList.get(index).x2=itemList.get(index).x1=e.getX();
					itemList.get(index-1).y1=itemList.get(index).y2=itemList.get(index).y1=e.getY();
					
					if(myHandWrite.handwrite_switcher == 1){
						myHandWrite.line_list.add(new Handwrite_Drawing_Info(itemList.get(index-1).x2, itemList.get(index-1).y2, itemList.get(index).x1, itemList.get(index).y1, index));
						myHandWrite.line_flag = 1;
					}					
				}
				else{
					itemList.get(index).x2 = e.getX();
					itemList.get(index).y2 = e.getY();
				}
				repaint();
			}
			else if(currentChoice == 0){
				if(selectitem.select_flag == 0){
					Graphics g = drawingArea.getGraphics();
					Graphics2D g2d = (Graphics2D)g;
					g2d.setXORMode(Color.WHITE);
					selectitem.draw(g2d);
					selectitem.selectRange_x2 = e.getX();
					selectitem.selectRange_y2 = e.getY();
					selectitem.draw(g2d);
				}
				else if(selectitem.select_flag == 1){
					Graphics g = drawingArea.getGraphics();
					Graphics2D g2d = (Graphics2D)g;
					g2d.setXORMode(Color.WHITE);
					selectitem.draw(g2d);
					selectitem.move_x1 = selectitem.move_x2;
					selectitem.move_y1 = selectitem.move_y2;
					selectitem.move_x2 = e.getX();
					selectitem.move_y2 = e.getY();	
					selectitem.move_width = selectitem.move_x1 - selectitem.move_x2;
					selectitem.move_height = selectitem.move_y1 - selectitem.move_y2;
					
					for(int i = 0; i <= selectitem.selectIndex; i++){
						for(int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++){
							itemList.get(j).x1 -= selectitem.move_width;
							itemList.get(j).x2 -= selectitem.move_width;
							itemList.get(j).y1 -= selectitem.move_height;
							itemList.get(j).y2 -= selectitem.move_height;
						}
						for(int j = 0; j < myUndomanager.drawList.size(); j++){
							myUndomanager.drawList.get(j).max_x -= selectitem.move_width;
							myUndomanager.drawList.get(j).max_y -= selectitem.move_height;
							myUndomanager.drawList.get(j).min_x -= selectitem.move_width;
							myUndomanager.drawList.get(j).min_y -= selectitem.move_height;
						}
					}
					drawingArea.repaint();
				}
			}
			else if(myModifier.modify_switcher == 1){
				if(myModifier.modify_flag == 1){				
					int move_x = e.getX() - myModifier.press_x;
					int move_y = e.getY() - myModifier.press_y;
					itemList.get(myUndomanager.drawList.get(myModifier.index).endPos).x2= myModifier.start_x2 + move_x;
					itemList.get(myUndomanager.drawList.get(myModifier.index).endPos).y2 = myModifier.start_y2 + move_y;
					drawingArea.repaint();
				}
			}
		}
		
		public void mouseMoved(MouseEvent e){
			statusBar.setText("Mouse clicks:["+e.getX()+","+e.getY()+"], " + sync_status + ", " + handwrite_status);	//设置状态提示
		}
	}
	
	//选择字体风格时候用到的事件侦听器类，加入到字体风格的选择框中
	private class checkBoxHandler implements ItemListener{
		public void itemStateChanged(ItemEvent e){
			if(e.getSource()==bold)			//设置字体为加粗
				if(e.getStateChange()==ItemEvent.SELECTED)
					f1=Font.BOLD;
				else
					f1=Font.PLAIN;
			if(e.getSource()==italic)		//设置字体为倾斜
				if(e.getStateChange()==ItemEvent.SELECTED)
					f2=Font.ITALIC;
				else
					f2=Font.PLAIN;
		}
	}
	
	//画图面板类，用来画图
	class DrawPanel extends JPanel{
		public DrawPanel(){
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			setBackground(Color.white);		//设置画图面板初始颜色为白色
			addMouseListener(new mouseA());
			addMouseMotionListener(new mouseB());
		}
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d=(Graphics2D)g;
			//定义画笔
			int j = 0;
			while(j < sync_itemList.size()){
				draw(g2d, sync_itemList.get(j));
				j++;
			}
			
			j = 0;
			while (j <= index){
				draw(g2d, itemList.get(j));
				j++;
			}
		}
		void draw(Graphics2D g2d,drawings i){
			i.draw(g2d);		//将画笔传入到各个子类中，用来完成各自的绘图
		}
	}
	
	//新建一个画图基本单元对象的程序段
	void createNewItem(int currentChoice){
		if(currentChoice == 17)//选择文本时鼠标为文本输入形
			drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		else				//其他情况十字形
			drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		switch (currentChoice){
			case 3:itemList.add(new Pencil()); 
				   	itemList.get(index).shapeType = 's';
				   	break;
			case 4:itemList.add(new Line()); 
					itemList.get(index).shapeType = 's';
					break;
			case 5:itemList.add(new Rect()); 
					itemList.get(index).shapeType = 's';
					break;
			case 6:itemList.add(new fillRect()); 
					itemList.get(index).shapeType = 's';
					break;
			case 7:itemList.add(new Oval()); 
					itemList.get(index).shapeType = 's';
					break;
			case 8:itemList.add(new fillOval()); 
					itemList.get(index).shapeType = 's';
					break;
			case 9:itemList.add(new Circle()); 
					itemList.get(index).shapeType = 's';
					break;
			case 10:itemList.add(new fillCircle()); 
					itemList.get(index).shapeType = 's';
					break;
			case 11:itemList.add(new RoundRect()); 
					itemList.get(index).shapeType = 's';
					break;
			case 12:itemList.add(new fillRoundRect()); 
					itemList.get(index).shapeType = 's';
					break;
			case 13:itemList.add(new Rect3D()); 
					itemList.get(index).shapeType = 's';
					break;
			case 14:itemList.add(new fillRect3D()); 
					itemList.get(index).shapeType = 's';
					break;
			case 15:itemList.add(new Cube()); 
					itemList.get(index).shapeType = 's';
					break;
			case 16:itemList.add(new Rubber()); 
					itemList.get(index).shapeType = 's';
					break;
			case 17:itemList.add(new Word()); 
					itemList.get(index).shapeType = 'w';
					break;
		}
			itemList.get(index).type=currentChoice;
			itemList.get(index).R=R;
			itemList.get(index).G=G;
			itemList.get(index).B=B;
			itemList.get(index).stroke=stroke;
			itemList.get(index).thickness=thickness;
	}
	
	//选择当前颜色程序段
	public void chooseColor(){
		color=JColorChooser.showDialog(MyPaint.this,"Choose brush color",color);
		R=color.getRed();
		G=color.getGreen();
		B=color.getBlue();
		itemList.get(index).R=R;
		itemList.get(index).G=G;
		itemList.get(index).B=B;
	}
	
	//选择背景颜色程序段
	public void SetbgColor(){
		color=JColorChooser.showDialog(MyPaint.this,"Choose background color",color);
		R=color.getRed();
		G=color.getGreen();
		B=color.getBlue();
		drawingArea.setBackground(new Color(R,G,B));
	}
	
	//选择当前线条粗细程序段
	public void setStroke(){
		String input;
		input=JOptionPane.showInputDialog("Input the thickness of brush：");
		stroke=Float.parseFloat(input);
		itemList.get(index).stroke=stroke;
	}
	
	//选择立方体宽度
	public void setthickness(){
		String input;
		input=JOptionPane.showInputDialog("Input the width of the cube：");
		thickness=(int) Float.parseFloat(input);
		itemList.get(index).thickness=thickness;
		createNewItem(currentChoice);
		repaint();
	}
	
	//新建一个文件程序段
	public void newFile(){
		if(myUndomanager.draw_count >= 0 || sync_itemList.size() > 0){
			Object[] options = { " Cancel ", " No(N) ", " Yes(Y) " };
			int s = JOptionPane.showOptionDialog(null,
							"Do you want to save the current document before creating a new one?",
							"Information", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
			if(s != 0){
				if(s == 2){
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int result =fileChooser.showSaveDialog(this);
					if(result == JFileChooser.CANCEL_OPTION) 
						return;
					File fileName = fileChooser.getSelectedFile();
					fileName.canWrite();
					if(fileName == null || fileName.getName().equals(""))
						JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name",JOptionPane.ERROR_MESSAGE);
					else{
						try{
							fileName.delete();
							FileOutputStream fos = new FileOutputStream(fileName);
							output=new ObjectOutputStream(fos);
							output.writeInt(index+sync_itemList.size()-1);
							for(int i = 0; i <= index; i++){
								drawings p = itemList.get(i);
								output.writeObject(p);
								output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
							}
							for(int i = 0; i < sync_itemList.size(); i++){
								drawings p = sync_itemList.get(i);
								output.writeObject(p);
								output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
							}	
							output.flush();
							output.close();
							fos.close();
						}
						catch(IOException ioe){
							ioe.printStackTrace();
						}
					}
				}
			
				currentChoice = 3;
				color=Color.black;
				previousChoice = -1;
				start_index = index = -1;						
				drawingArea.setBackground(Color.white);
				stroke = 2.0f;
				myHandWrite.Handwrite_Reset();
				selectitem.Selectfunc_Reset();
				myUndomanager.Undomanager_Reset();
				sync_itemList.clear();
				sync_index = -1;
				repaint();				//将有关值设置为初始状态，并且重画
			}
		}
		
		else{
			currentChoice = 3;
			color=Color.black;
			previousChoice = -1;
			index = -1;						
			drawingArea.setBackground(Color.white);
			stroke = 2.0f;
			myHandWrite.Handwrite_Reset();
			selectitem.Selectfunc_Reset();
			myUndomanager.Undomanager_Reset();
			sync_itemList.clear();
			sync_index = -1;
			repaint();				//将有关值设置为初始状态，并且重画
		}
	}
	
	//打开一个图形文件程序段
	public void loadFile(){
		if(myUndomanager.draw_count >= 0 || sync_itemList.size() > 0){
			Object[] options = { " Cancel ", " No(N) ", " Yes(Y) " };
			int s = JOptionPane.showOptionDialog(null,
							"Do you want to save the current document before opening a new one?",
							"Information", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
			if(s != 0){
				if(s == 2){
					JFileChooser fileChooser=new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int result =fileChooser.showSaveDialog(this);
					if(result == JFileChooser.CANCEL_OPTION) 
						return;
					File fileName = fileChooser.getSelectedFile();
					fileName.canWrite();
					if(fileName == null || fileName.getName().equals(""))
						JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name",JOptionPane.ERROR_MESSAGE);
					else{
						try{
							fileName.delete();
							FileOutputStream fos = new FileOutputStream(fileName);
							output=new ObjectOutputStream(fos);
							output.writeInt(index+sync_itemList.size()-1);
							for(int i = 0; i <= index; i++){
								drawings p = itemList.get(i);
								output.writeObject(p);
								output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
							}
							for(int i = 0; i < sync_itemList.size(); i++){
								drawings p = sync_itemList.get(i);
								output.writeObject(p);
								output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
							}	
							output.flush();
							output.close();
							fos.close();
						}
						catch(IOException ioe){
							ioe.printStackTrace();
						}
					}
				}
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int result = fileChooser.showOpenDialog(this);
				if(result == JFileChooser.CANCEL_OPTION)
					return;
				File fileName = fileChooser.getSelectedFile();
				fileName.canRead();
				if (fileName == null || fileName.getName().equals(""))
					JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name", JOptionPane.ERROR_MESSAGE);
				else {
					try{
						FileInputStream fis = new FileInputStream(fileName);
						input = new ObjectInputStream(fis);
						drawings inputRecord;  
						int countNumber = 0;
						countNumber = input.readInt();
						for(index = 0; index < countNumber; index++){
							inputRecord = (drawings)input.readObject();
							itemList.add(inputRecord);
						}
						index--;
						input.close();
						repaint();
					}
					catch(EOFException endofFileException){
						JOptionPane.showMessageDialog(this,"No more document", "Do not find the class",JOptionPane.ERROR_MESSAGE );
					}
					catch(ClassNotFoundException classNotFoundException){
						JOptionPane.showMessageDialog(this,"Cannot create the object", "To the terminal",JOptionPane.ERROR_MESSAGE );
					}
					catch (IOException ioException){
						JOptionPane.showMessageDialog(this,"Errors when reading files", "Reading error",JOptionPane.ERROR_MESSAGE );
					}
				}
			}
		}
		
		else{
			JFileChooser fileChooser=new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result =fileChooser.showOpenDialog(this);
			if(result==JFileChooser.CANCEL_OPTION)
				return;
			File fileName=fileChooser.getSelectedFile();
			fileName.canRead();
			if (fileName == null || fileName.getName().equals(""))
				JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name", JOptionPane.ERROR_MESSAGE);
			else {
				try{
					FileInputStream fis=new FileInputStream(fileName);
					input=new ObjectInputStream(fis);
					drawings inputRecord;  
					int countNumber = 0;
					countNumber=input.readInt();
					for(index = 0; index < countNumber; index++){
						inputRecord=(drawings)input.readObject();
						itemList.add(inputRecord);
					}
					index--;
					input.close();
					repaint();
				}
				catch(EOFException endofFileException){
					JOptionPane.showMessageDialog(this,"No more document", "Do not find the class",JOptionPane.ERROR_MESSAGE );
				}
				catch(ClassNotFoundException classNotFoundException){
					JOptionPane.showMessageDialog(this,"Cannot create the object", "To the terminal",JOptionPane.ERROR_MESSAGE );
				}
				catch (IOException ioException){
					JOptionPane.showMessageDialog(this,"Errors when reading files", "Reading error",JOptionPane.ERROR_MESSAGE );
				}
			}
		}
		
	}
	
	//保存图形文件程序段
	public void saveFile(){
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result =fileChooser.showSaveDialog(this);
		if(result==JFileChooser.CANCEL_OPTION)
			return ;
		File fileName=fileChooser.getSelectedFile();
		fileName.canWrite();
		if(fileName==null||fileName.getName().equals(""))
			JOptionPane.showMessageDialog(fileChooser,"Invalid file name",
					"Invalid file name",JOptionPane.ERROR_MESSAGE);
		else{
			try {
				fileName.delete();
				FileOutputStream fos = new FileOutputStream(fileName);
				output = new ObjectOutputStream(fos);
				output.writeInt(index+sync_itemList.size()-1);
				for(int i = 0; i <= index; i++){
					drawings p = itemList.get(i);
					output.writeObject(p);
					output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
				}
				for(int i = 0; i < sync_itemList.size(); i++){
					drawings p = sync_itemList.get(i);
					output.writeObject(p);
					output.flush();       //将所有图形信息强制转换成父类线性化存储到文件中
				}				
				output.flush();				
				output.close();
				fos.close();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
	}
	
	//保存JPG文件程序段
	public void saveasJPG(){
		myJPG_PDF.savetoFunction(0);
	}
	
	//保存PDF文件程序段
	public void saveasPDF(){
		myJPG_PDF.savetoFunction(1);
	}
	
	public JMenuBar getJMenuBar(){
		if(jmenuBar == null){
			JMenuBar Jmenu = new JMenuBar();
			setJMenuBar(Jmenu);
			JMenu filemenu = new JMenu("File(F)");
			JMenu editmenu = new JMenu("Edit(E)");
			JMenu assisantmenu = new JMenu("Asisstant");
			JMenu setmenu = new JMenu("Setting(P)");
			JMenu helpmenu = new JMenu("Help(H)");
			Jmenu.add(filemenu);
			Jmenu.add(editmenu);
			Jmenu.add(assisantmenu);
			Jmenu.add(setmenu);
			Jmenu.add(helpmenu);
			
			//创建新建菜单上的各个菜单项并添加到菜单上
			JMenuItem newitem = new JMenuItem("New (N)");
			JMenuItem openitem = new JMenuItem("Open (O)");
			JMenuItem saveitem = new JMenuItem("Save (S)");
			JMenuItem saveasitem = new JMenuItem("Save As (A)");
			JMenuItem saveasjpgitem = new JMenuItem("Save As JPG");
			JMenuItem saveaspdfitem = new JMenuItem("Save As PDF");
			JMenuItem exititem = new JMenuItem("Exit (X)");
			newitem.setAccelerator(KeyStroke.getKeyStroke  //快捷键设置
					(KeyEvent.VK_N,InputEvent.CTRL_MASK));
			openitem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_O,InputEvent.CTRL_MASK));
			saveitem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_S,InputEvent.CTRL_MASK));
			exititem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_F4,InputEvent.ALT_MASK));
			filemenu.add(newitem);
			filemenu.add(openitem);
			filemenu.add(saveitem);
			filemenu.add(saveasitem);
			filemenu.add(saveasjpgitem);
			filemenu.add(saveaspdfitem);
			filemenu.addSeparator();
			filemenu.add(exititem);
			
			//新建菜单项事件
			newitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					newFile();
				}   
			});
			
			//打开菜单项事件
			openitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					loadFile();
				}	   
			});
			
			//保存菜单项事件
			saveitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveFile();
				}
			});
			
			//另存为菜单项事件
			saveasitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveFile();
				}
			});
			
			saveasjpgitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveasJPG();
				}
			});
			
			saveaspdfitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveasPDF();
				}
			});
			
			//退出菜单项的功能实现
			exititem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					Object[] options = {" No(N) ", " Yes(Y) " };
					int s = JOptionPane.showOptionDialog(null, "Are you sure to exit？",
							"Exit Information", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[2]);
					switch (s) {
					case 1:
						System.exit(0);		
					}
				}
			});
						
			//创建编辑菜单上的各个菜单项并添加到菜单上
			JMenuItem pencilitem_op = new JMenuItem("Pencil (P)");
			JMenuItem undoitem_op = new JMenuItem("Undo (U)");
			JMenuItem redoitem_op = new JMenuItem("Redo (R)");
			JMenuItem selectitem_op = new JMenuItem("Select (S)");
			JMenuItem modifyitem_op = new JMenuItem("Modify (M)");
			JMenuItem deleteitem_op = new JMenuItem("Delete (D)");
			JMenuItem copyitem_op = new JMenuItem("Copy (C)");
			
			pencilitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_P,InputEvent.CTRL_MASK));
			undoitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_Z,InputEvent.CTRL_MASK));
			redoitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_Y,InputEvent.CTRL_MASK));
			selectitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_S,InputEvent.CTRL_MASK));
			modifyitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_M,InputEvent.CTRL_MASK));
			deleteitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_D,InputEvent.CTRL_MASK));
			copyitem_op.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_C,InputEvent.CTRL_MASK));
			
			editmenu.add(pencilitem_op);
			editmenu.add(undoitem_op);
			editmenu.add(redoitem_op);
			editmenu.add(selectitem_op);
			editmenu.add(modifyitem_op);
			editmenu.add(deleteitem_op);
			editmenu.add(copyitem_op);
			
			pencilitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {                                            
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						previousChoice = currentChoice = 3;
					} catch (CannotRedoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot use pencil！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//撤销菜单项的功能实现
			undoitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {                                            
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						if(index >= 0){
							myUndomanager.undoOperation(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);	
							for(int i = myUndomanager.drawList.get(myUndomanager.draw_count).startPos; i <= myUndomanager.drawList.get(myUndomanager.draw_count).endPos; i++){														
								itemList.remove(itemList.size()-1);
							}
							drawingArea.repaint();
							index = myUndomanager.drawList.get(myUndomanager.draw_count).startPos-1;
							myUndomanager.drawList.remove(myUndomanager.draw_count--);
						}
					} catch (CannotUndoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot undo！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//恢复菜单项的功能实现
			redoitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {                                            
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						if(myUndomanager.redo_count >= 0){
							if(myUndomanager.write_flag == 0){
								myUndomanager.drawList.add(myUndomanager.redoList.get(myUndomanager.redo_count));
								myUndomanager.draw_count++;
								for(int i = myUndomanager.redoList.get(myUndomanager.redo_count).startPos; i <= myUndomanager.redoList.get(myUndomanager.redo_count).endPos; i++){
									index++;
									currentChoice = myUndomanager.redoList.get(myUndomanager.redo_count).currentChoice;
									createNewItem(currentChoice);
								}
								myUndomanager.redoOperation(itemList);
								repaint();
							}
							else
								myUndomanager.redoOperation(itemList);
						}						
					} catch (CannotRedoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot redo！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//选择菜单项的功能实现
			selectitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {                                            
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						currentChoice = 0;
					} catch (CannotRedoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot select！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//修改菜单项的功能实现
			modifyitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						previousChoice = currentChoice;
						currentChoice = -1;
						myModifier.modify_switcher = 1;
					} catch (CannotRedoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot modify！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//删除菜单项的功能实现
			deleteitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {                                           
						for(int i = selectitem.selectIndex; i >= 0; i--){																				
							for(int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++){
								itemList.remove(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos);
								index--;
							}
							int delete_count = myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos - myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos + 1;
							myUndomanager.drawList.remove(selectitem.selectitemList[i]);
							myUndomanager.draw_count--;
							for(int j = selectitem.selectitemList[i]; j < myUndomanager.drawList.size(); j++){
								myUndomanager.drawList.get(j).startPos -= delete_count;
								myUndomanager.drawList.get(j).endPos -= delete_count;
							}
							
							for(int j = i; j < selectitem.selectIndex; j++){
								selectitem.selectitemList[j] = selectitem.selectitemList[j+1];
							}
							selectitem.selectitemList[selectitem.selectIndex] = 0;
							selectitem.selectIndex--;
						}
						drawingArea.repaint();
					} catch (CannotRedoException ex) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Cannot delete！","Edit Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
			
			//拷贝菜单项
			copyitem_op.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					copy_count++;
					int temp_start_index = 0;
					for (int i = 0; i <= selectitem.selectIndex; i++) {
						temp_start_index = index + 1;
						selectitem.Check_max_min_border(myUndomanager.drawList, itemList);
						
						if (myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice != 17) {
							for (int j = myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos; j <= myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos; j++) {
								index++;
								createNewItem(myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice);
								itemList.get(index).x1 = itemList.get(j).x1 + selectitem.max_width * copy_count;
								itemList.get(index).y1 = itemList.get(j).y1 + selectitem.max_height * copy_count;
								itemList.get(index).x2 = itemList.get(j).x2 + selectitem.max_width * copy_count;
								itemList.get(index).y2 = itemList.get(j).y2 + selectitem.max_height * copy_count;
								itemList.get(index).R = itemList.get(j).R_backup;
								itemList.get(index).G = itemList.get(j).G;
								itemList.get(index).B = itemList.get(j).B;
								itemList.get(index).stroke = itemList.get(j).stroke;
								itemList.get(index).type = itemList.get(j).type;
								itemList.get(index).thickness = itemList.get(j).thickness;
								itemList.get(index).shapeType = itemList.get(j).shapeType;	
							}		
							
							myUndomanager.drawList.add(new drawing_info());
							myUndomanager.draw_count++;
							myUndomanager.drawList.get(myUndomanager.draw_count).startPos = temp_start_index;
							myUndomanager.drawList.get(myUndomanager.draw_count).endPos = temp_start_index + myUndomanager.drawList.get(selectitem.selectitemList[i]).endPos - myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos;
							myUndomanager.drawList.get(myUndomanager.draw_count).type = myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice;
							myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);									
							myUndomanager.write_flag = 1;
						}
						else{
							index++;
							createNewItem(myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice);
							itemList.get(index).x1 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x1 + selectitem.max_width * copy_count;
							itemList.get(index).y1 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y1 + selectitem.max_height * copy_count;
							itemList.get(index).x2 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).x2 + selectitem.max_width * copy_count;
							itemList.get(index).y2 = itemList.get(myUndomanager.drawList.get(selectitem.selectitemList[i]).startPos).y2 + selectitem.max_height * copy_count;
							itemList.get(index).R = itemList.get(selectitem.selectitemList[i]).R_backup;
							itemList.get(index).G = itemList.get(selectitem.selectitemList[i]).G;
							itemList.get(index).B = itemList.get(selectitem.selectitemList[i]).B;
							itemList.get(index).stroke = itemList.get(selectitem.selectitemList[i]).stroke;
							itemList.get(index).type = itemList.get(selectitem.selectitemList[i]).type;
							itemList.get(index).thickness = itemList.get(selectitem.selectitemList[i]).thickness;
							itemList.get(index).shapeType = itemList.get(selectitem.selectitemList[i]).shapeType;
							itemList.get(index).s1 = itemList.get(selectitem.selectitemList[i]).s1;
		                    itemList.get(index).x2 = itemList.get(selectitem.selectitemList[i]).x2;
		                    itemList.get(index).y2 = itemList.get(selectitem.selectitemList[i]).y2;
		                    itemList.get(index).width = itemList.get(selectitem.selectitemList[i]).width;
		                    itemList.get(index).height = itemList.get(selectitem.selectitemList[i]).height;
		                    itemList.get(index).s2 = itemList.get(selectitem.selectitemList[i]).s2;
		                    myUndomanager.drawList.add(new drawing_info());
							myUndomanager.draw_count++;
							myUndomanager.drawList.get(myUndomanager.draw_count).startPos = index;
							myUndomanager.drawList.get(myUndomanager.draw_count).endPos = index;
							myUndomanager.drawList.get(myUndomanager.draw_count).type = myUndomanager.drawList.get(selectitem.selectitemList[i]).currentChoice;
							myUndomanager.drawList.get(myUndomanager.draw_count).copy_drawings(myUndomanager.drawList.get(myUndomanager.draw_count).startPos, myUndomanager.drawList.get(myUndomanager.draw_count).endPos, myUndomanager.drawList.get(myUndomanager.draw_count).type, itemList);												
						}
					}
					drawingArea.repaint();
				}
			});	
			
			//创建辅助菜单上的各个菜单项并添加到菜单上
			JMenuItem ShowDrawMenuItem = new JMenuItem("Showing draw bar");
			JMenuItem HideDrawMenuItem = new JMenuItem("Hiding draw bar");
			JMenuItem handwriteMenuItem = new JMenuItem("Handwrite (ON/OFF)");
			JMenuItem syncMenuItem = new JMenuItem("Sync (ON/OFF)");
			assisantmenu.add(ShowDrawMenuItem);
			assisantmenu.add(HideDrawMenuItem);
			assisantmenu.add(handwriteMenuItem);
			assisantmenu.add(syncMenuItem);
			
			ShowDrawMenuItem.addActionListener(new ActionListener(){              
				public void actionPerformed(ActionEvent e){
					if(myHandWrite.handwrite_switcher == 0){
						buttonPanel.setVisible(true);
					}
					else{
						Object[] options = { " OK " };
						int s = JOptionPane.showOptionDialog(null, "Please turn off the handwrite function before using the draw bar, thanks!", "HandWrite Information", JOptionPane.DEFAULT_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					}
				}
			});
			
			HideDrawMenuItem.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
					 buttonPanel.setVisible(false);
					 }
			});
			
			handwriteMenuItem.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
					 buttonPanel.setVisible(false);
					 simpleButtonPanel.setVisible(false);
					 currentChoice = 3;
					 previousChoice = 3;
					 drawingArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					 
					 if(myHandWrite.handwrite_switcher == 0){
						 Object[] options = { " OK " };
						 int s = JOptionPane.showOptionDialog(null, "Handwrite funciton turns on", "Handwrite Function Information", JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[0]);
						 myHandWrite.handwrite_switcher = 1;
						 handwrite_status = "Handwrite Function: ON";
					 }
					 else{
						 simpleButtonPanel.setVisible(true);
						 Object[] options = { " OK " };
						 int s = JOptionPane.showOptionDialog(null, "Handwrite funciton turns off", "Handwrite Function Information", JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[0]);
						 myHandWrite.handwrite_switcher = 0;
						 handwrite_status = "Handwrite Function: OFF";
					 }
				}
			});
			
			syncMenuItem.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
					 if(mySyncControllor.Sync_Switcher == 0){	
						mySyncControllor.type = userMode;
						JOptionPane.showMessageDialog(null,"Opening the Sync function and please select the file path!");			
						if(mySyncControllor.setAttribute() == 0){
							mySyncControllor.Sync_function();
							mySyncControllor.Sync_Switcher = 1;
							sync_status = "Sync Function: ON";
						}
						else{
							JOptionPane.showMessageDialog(null,"Cannot find the file path and fail to open the sync function!");
							sync_status = "Sync Function: OFF";
						}							
					 }				 
					 else{
						 JOptionPane.showMessageDialog(null,"Closing the Sync function and thanks for using!");
						 mySyncControllor.Sync_Switcher = 0;
						 sync_status = "Sync Function: OFF";
					 }				 
				}
			});
			
			//创建设置菜单上的各个菜单项并添加到菜单上
			JMenuItem coloritem = new JMenuItem("Brush Color (C)");
			JMenuItem strokeitem = new JMenuItem("Brush Thickness (S)");
			JMenuItem cubeitem = new JMenuItem("Width of Cube (W)");
			setmenu.add(coloritem);
			setmenu.add(strokeitem);
			setmenu.add(cubeitem);
			coloritem.addActionListener(new ActionListener(){              
				public void actionPerformed(ActionEvent e){
					chooseColor();
					}
			});
			strokeitem.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
					 setStroke();
					 }
			});
			cubeitem.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
					setthickness();
				}
			});
			
			//创建帮助菜单上的各个菜单项并添加到菜单上
			JMenuItem aboutboxitem = new JMenuItem("About the NotePad (A)");
			JMenuItem writeritem = new JMenuItem("About the author (S)");
			helpmenu.addSeparator();
			helpmenu.add(aboutboxitem);
			helpmenu.addSeparator();
			helpmenu.add(writeritem);
			aboutboxitem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					Object[] options = {" OK " };
					int s = JOptionPane.showOptionDialog(null, "Thanks for using the NotePad", "About the NotePad", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}
			});
			writeritem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){					
					Object[] options = {" OK " };
					int s = JOptionPane.showOptionDialog(null, "Developed by Summer Research Team No.6\n" +
							"Feng Chen, Qiming Zhao, Ziao Ye", "About the authors", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}
			});
		}
		return jmenuBar;
	}
	
	// 工具栏右击菜单，设置工具栏是否可拖动
	void ToolMenu() {
		final JPopupMenu ToolMenu;
		ToolMenu = new JPopupMenu();
		final JCheckBox move = new JCheckBox("工具栏是否可拖动");
		move.setBackground(new Color(0, 255, 0));
		ToolMenu.add(move);
		buttonPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getModifiers() == InputEvent.BUTTON3_MASK)
					ToolMenu.show(buttonPanel, e.getX(), e.getY());
			}
		});
		move.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (move.isSelected()) {
					buttonPanel.setFloatable(true);
				} else {
					buttonPanel.setFloatable(false);
				}
			}
		});
	}
}
