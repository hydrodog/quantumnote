import java.awt.FileDialog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.*; 
import java.util.*; 
import java.awt.geom.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 

import javax.swing.*; 

import com.sun.image.codec.jpeg.*; 
import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;


class savetoJPG_PDF {
	BufferedImage image;
	File file, file_jpg, file_pdf;
	MyPaint mainPaint;
	String del_file;
	JFileChooser fileChooser;
	int result;
	
	public savetoJPG_PDF(MyPaint mainPaint){
		this.mainPaint = mainPaint;
	}
	
	public void savetoFunction(int saveMode){
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		result = fileChooser.showSaveDialog(mainPaint);
		if(result == JFileChooser.CANCEL_OPTION)
			return ;
		file = fileChooser.getSelectedFile();	
		if(file == null || file.getName().equals(""))
			JOptionPane.showMessageDialog(fileChooser,"Invalid file name", "Invalid file name",JOptionPane.ERROR_MESSAGE);
		if( file != null){
			del_file = "";
			del_file = fileChooser.toString();
			image = new BufferedImage(mainPaint.noteWidth, mainPaint.noteHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = image.createGraphics();
			Rectangle2D rect = new Rectangle2D.Double(0, 0, mainPaint.noteWidth, mainPaint.noteHeight); 
			g2d.setColor(mainPaint.getBackground()); 
			g2d.fill(rect); //将图像的底色设置为和画布相同 			
			for(int i = 0; i < mainPaint.sync_itemList.size(); i++){
				 mainPaint.sync_itemList.get(i).draw(g2d);
			}
			for(int i = 0; i <  mainPaint.itemList.size(); i++){
				mainPaint.itemList.get(i).draw(g2d);
			}			
			try{ 
				String tempName = file.toString();
				if(!tempName.substring(tempName.length()-4, tempName.length()).equals(".jpg") || !tempName.substring(tempName.length()-4, tempName.length()).equals(".JPG")){
					if(tempName.substring(tempName.length()-4, tempName.length()).equals(".pdf") || tempName.substring(tempName.length()-4, tempName.length()).equals(".PDF")){
						tempName = tempName.substring(0, tempName.length()-4);
					}	
					file = new File(tempName);
					tempName += ".jpg";
					file_jpg = new File(tempName);
				}
				else{ 
					file_jpg = file;
					tempName = tempName.substring(0, tempName.length()-4);
					file = new File(tempName);
				}
				FileOutputStream out = new FileOutputStream(file_jpg); 
				//获得一个JPEG编码器 
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
				//获取解码器的编码参数 
				JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
				//将图像质量设置为100％ 
				param.setQuality(1.0f,false); 
				encoder.setJPEGEncodeParam(param); 
				//编码器负责将image写入输出流out 
				encoder.encode(image); 
				out.close(); 
			} 
			catch(Exception EE) 
			{ 
				JOptionPane.showMessageDialog(fileChooser,EE, "ERROR",JOptionPane.ERROR_MESSAGE);
			} 
		} 
		
		if(saveMode == 1){
			try {
				String tempName = file.toString();
				tempName += ".pdf";
				file_pdf = new File(tempName);				
				BufferedImage img = ImageIO.read(file_jpg);
				FileOutputStream fos = new FileOutputStream(file_pdf.toString());
				Document doc = new Document(null, 0, 0, 0, 0);
				doc.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
				Image image = Image.getInstance(file_jpg.toString());
				PdfWriter.getInstance(doc, fos);
				doc.open();
				doc.add(image);
				doc.close();			
				del_file = fileChooser.getSelectedFile().getPath().toString();
			    del_file += ".jpg";
				File delFile = new File(del_file);
				delFile.delete();				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BadElementException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
	} 

}