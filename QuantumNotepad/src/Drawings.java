import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.Serializable;
//画图类，基本图形单元，用到串行化接口，保存时所用

class drawings implements java.io.Serializable{
	int x1,y1,x2,y2;	//定义坐标属性
	int max_x_drawings, max_y_drawings, min_x_drawings, min_y_drawings;
	int R,G,B;			//定义色彩属性
	int R_backup;
	float stroke;		//定义线条粗细属性
	int type;			//定义字体属性
	String s1;
	String s2;			//定义字体风格属性
	int thickness;
	char shapeType;
	int width, height;
	
	public drawings(){}
	public drawings(int x1, int x2, int y1, int y2){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	public drawings(drawings orig){
		this.x1 = orig.x1;
		this.x2 = orig.x2;
		this.y1 = orig.y1;
		this.y2 = orig.y2;
		this.R = orig.R;
		this.G = orig.G;
		this.B = orig.B;
		this.R_backup = orig.R_backup;
		this.stroke = orig.stroke;
		this.type = orig.type;
		this.s1 = orig.s1;
		this.s2 = orig.s2;
		this.thickness = orig.thickness;
		this.shapeType = orig.shapeType;
		this.width = orig.width;
		this.height = orig.height;
	}
	
	public void equal_operation(drawings orig){
		this.x1 = orig.x1;
		this.x2 = orig.x2;
		this.y1 = orig.y1;
		this.y2 = orig.y2;
		this.R = orig.R;
		this.G = orig.G;
		this.B = orig.B;
		this.R_backup = orig.R_backup;
		this.stroke = orig.stroke;
		this.type = orig.type;
		this.s1 = orig.s1;
		this.s2 = orig.s2;
		this.thickness = orig.thickness;
		this.shapeType = orig.shapeType;
		this.width = orig.width;
		this.height = orig.height;
	}
	
	public void set_max_min_value(){
		if(shapeType == 's'){
			max_x_drawings = (x1 > x2) ? x1 : x2;
			max_y_drawings = (y1 > y2) ? y1 : y2;
			min_x_drawings = x1 + x2 - max_x_drawings;
			min_y_drawings = y1 + y2 - max_y_drawings;
		}
		else if(shapeType == 'w'){
			min_x_drawings = x1;
			min_y_drawings = y1 - height;
			max_x_drawings = x1 + width;
			max_y_drawings = y1;		
		}
	}
	
	void draw(Graphics2D g2d){
		
	};//定义绘图函数
}
//各种基本图形单元的子类，都继承自父类drawings
//随笔画类
class Pencil extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
		g2d.drawLine(x1,y1,x2,y2);
	}
}
//直线类
class Line extends drawings {
//	private Graphics2D t;
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
		g2d.drawLine(x1,y1,x2,y2);
//		t = g2d;
	}	
//	public Graphics2D get_shape(){
//		return t;
//	}
}
//矩形类
class Rect extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.drawRect(Math.min(x1,x2),Math.min(y1,y2),
				Math.abs(x1-x2),Math.abs(y1-y2));
	}
}
//实心矩形类
class fillRect extends drawings{
	void draw(Graphics2D g2d){
	g2d.setPaint(new Color(R,G,B));
	g2d.setStroke(new BasicStroke(stroke));
	g2d.fillRect(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x1-x2),Math.abs(y1-y2));
	}
}
//椭圆类
class Oval extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.drawOval(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x1-x2),Math.abs(y1-y2));
	}
}
//实心椭圆
class fillOval extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.fillOval(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x1-x2),Math.abs(y1-y2));
	}
}
//圆类
class Circle extends drawings{
	void draw(Graphics2D g2d) {
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.drawOval(Math.min(x1,x2),Math.min(y1,y2),
				Math.max(Math.abs(x1-x2),Math.abs(y1-y2)),
				Math.max(Math.abs(x1-x2),Math.abs(y1-y2)));
	}
}
//实心圆
class fillCircle extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.fillOval(Math.min(x1,x2),Math.min(y1,y2),
				Math.max(Math.abs(x1-x2),Math.abs(y1-y2)),
				Math.max(Math.abs(x1-x2),Math.abs(y1-y2)));
	}
}
//圆角矩形类
class RoundRect extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.drawRoundRect(Math.min(x1,x2),Math.min(y1,y2),
				Math.abs(x1-x2),Math.abs(y1-y2),50,35);
	}
}
//实心圆角矩形类
class fillRoundRect extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.fillRoundRect(Math.min(x1,x2),Math.min(y1,y2),
				Math.abs(x1-x2),Math.abs(y1-y2),50,35);
	}
}
//3D矩形类
class Rect3D extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.draw3DRect(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x1-x2),
				Math.abs(y1-y2),false);
	}
}
//填充的3D矩形类
class fillRect3D extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		g2d.fill3DRect(Math.min(x1,x2),Math.min(y1,y2),
				Math.abs(x1-x2),Math.abs(y1-y2),false);
	}
}
//立方体类 
class Cube extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setStroke(new BasicStroke(stroke));
		thickness=MyPaint.thickness;
		for (int i = thickness - 1; i >= 0; i--){
			g2d.fill3DRect(Math.min(x1,x2) + i, Math.min(y1,y2) - i,
					Math.abs(x1-x2), Math.abs(y1-y2), true);
		}
	}
}
//橡皮擦类
class Rubber extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(255,255,255));
		g2d.setStroke(new BasicStroke(stroke+4,BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_BEVEL));
		g2d.drawLine(x1,y1,x2,y2);
	}
}
//输入文字类
class Word extends drawings{
	void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.setFont(new Font(s2,x2+y2,((int)stroke)*18));
		if (s1!= null){
			width = s1.length()*10;
			height = 20;
			g2d.drawString(s1,x1,y1);
		}
	}
}