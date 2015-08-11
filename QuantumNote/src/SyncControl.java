import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class SyncControl extends JFrame{
	Timer timer;
	char type;
	int Sync_Switcher;
	File fileName;
	JFileChooser fileChooser;
	MyPaint mainPain;
	
	public SyncControl(MyPaint m){
		timer = new Timer();
		Sync_Switcher = 0;	
		mainPain = m;
	}
	
	public int setAttribute(){
		if(type == 't'){
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result = fileChooser.showSaveDialog(this);
			if(result == JFileChooser.CANCEL_OPTION)
				return -1;
			fileName = fileChooser.getSelectedFile();
			fileName.canWrite();
			if(fileName == null || fileName.getName().equals(""))
				JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name",JOptionPane.ERROR_MESSAGE);
		}
		else if(type == 's'){			
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result = fileChooser.showOpenDialog(this);
			if(result == JFileChooser.CANCEL_OPTION)
				return -1;
			fileName = fileChooser.getSelectedFile();
			fileName.canRead();	
			if(fileName == null || fileName.getName().equals(""))
				JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name",JOptionPane.ERROR_MESSAGE);
		}
		return 0;
	}

	public void Sync_function(){
			timer.schedule(new timertask(), 1 * 1000);
	}
	
	class timertask extends TimerTask{
		 public void run(){
			 if(type == 't'){
				 try{
					 fileName.delete();
					 FileOutputStream fos=new FileOutputStream(fileName);
					 mainPain.output=new ObjectOutputStream(fos);
					 mainPain.output.writeInt(mainPain.index+1);
					 for(int i = 0; i <= mainPain.index; i++){
						 drawings p = mainPain.itemList.get(i);
						 mainPain.output.writeObject(p);
						 mainPain.output.flush();       
					 }
					 mainPain.output.flush();						
					 mainPain.output.close();
					 fos.close();
				 }
				 catch(IOException ioe){
					ioe.printStackTrace();
				 }
				 
				 if(Sync_Switcher == 1)
						 timer.schedule(new timertask(), 3 * 1000);
				 else 
					 timer.cancel();
			 }
			 else if(type == 's'){
				 try{
					FileInputStream fis = new FileInputStream(fileName);
					mainPain.input = new ObjectInputStream(fis);
					drawings inputRecord;  
					int countNumber = 0;
					countNumber = mainPain.input.readInt();
					mainPain.sync_itemList.clear();
					for(mainPain.sync_index = 0; mainPain.sync_index < countNumber; mainPain.sync_index++){
						inputRecord = (drawings)mainPain.input.readObject();
						mainPain.sync_itemList.add(inputRecord);
						mainPain.drawingArea.repaint();
					}
					mainPain.sync_index--;
					mainPain.input.close();		
					mainPain.drawingArea.repaint();
				 }
				 catch(EOFException endofFileException){
//					 JOptionPane.showMessageDialog(this,"No more document", "Do not find the class",JOptionPane.ERROR_MESSAGE );
				 }
				 catch(ClassNotFoundException classNotFoundException){
//					 JOptionPane.showMessageDialog(this,"Cannot create the object", "To the terminal",JOptionPane.ERROR_MESSAGE );
				 }
				 catch (IOException ioException){
//					 JOptionPane.showMessageDialog(this,"Errors when reading files", "Reading error",JOptionPane.ERROR_MESSAGE );
				 }
				 
				 if(Sync_Switcher == 1)
					 timer.schedule(new timertask(), 2 * 1000);
				 else 
					 timer.cancel();
			 }
		 }
	}

}
