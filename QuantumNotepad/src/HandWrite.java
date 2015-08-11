import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;


class Point{
	int x, y;
	int index;
	
	public Point(){
		x = -1; 
		y = -1;
		index = -1;
	}
	public Point(int x, int y, int index){
		this.x = x;
		this.y = y;
		this.index = index;
	}
}

class Handwrite_Drawing_Info{
	int x1, y1, x2, y2;
	int index;
	double cos_angle;
	
	public Handwrite_Drawing_Info(){}
	public Handwrite_Drawing_Info(int x1, int y1, int x2, int y2, int index){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;		
		this.index = index;
		cos_angle = (double)(x2-x1)/Math.sqrt( (double)(x2-x1)*(x2-x1) + (double)(y2-y1)*(y2-y1) );		
	}
}

public class HandWrite {
	int handwrite_switcher;
	int turn_point_count;
	int max_x_for_circle, max_y_for_circle;
	int line_flag;
	char shape_flag;
	ArrayList<Point> turn_point_list;
	ArrayList<Handwrite_Drawing_Info> line_list;

	public HandWrite(){
		handwrite_switcher = 0;
		turn_point_count = -1;
		max_x_for_circle = -1;
		max_y_for_circle = -1;
		shape_flag = 'n';
		line_flag = 0;
		turn_point_list = new ArrayList<Point>(10);
		line_list = new ArrayList<Handwrite_Drawing_Info>(50);		
	}
	
	public void Add_to_pointList(int x, int y, int index){
		turn_point_list.add(new Point(x, y, index));
		turn_point_count++;
	}
	
	public void TurnPointRecognization(){
		double res;
		for(int i = 0; i < line_list.size()-1; i++){
			res = line_list.get(i).cos_angle - line_list.get(i+1).cos_angle;	
			if( (res >= 0.75 && res <= 1) || (res <= -0.75 && res >= -1) ){
				Add_to_pointList(line_list.get(i).x1, line_list.get(i).y1, line_list.get(i).index);
			}
		}
	}
	
	public void ShapeRecognization(){
		double distance = Math.sqrt( (double)(turn_point_list.get(0).x-turn_point_list.get(turn_point_list.size()-1).x)*(turn_point_list.get(0).x-turn_point_list.get(turn_point_list.size()-1).x) + (double)(turn_point_list.get(0).y-turn_point_list.get(turn_point_list.size()-1).y)*(turn_point_list.get(0).y-turn_point_list.get(turn_point_list.size()-1).y) );		
		if(turn_point_list.size() == 2){
			if(line_list.size() >= 1){
				for(int i = 0; i < line_list.size();i++){
					if(Math.abs(line_list.get(0).cos_angle - line_list.get(i).cos_angle) >= 1 && distance <= 100){
						shape_flag = 'c';
						max_x_for_circle = (max_x_for_circle > line_list.get(i).x1)? max_x_for_circle : line_list.get(i).x1;
						max_y_for_circle = (max_y_for_circle > line_list.get(i).y1)? max_y_for_circle : line_list.get(i).y1;
					}
				}
			}
			if(shape_flag == 'n') {shape_flag = 'l';}
		}
		
		else if(turn_point_list.size() == 5 && distance <= 55){
			shape_flag = 'r';
		}		
	}
	
	public void Handwrite_Reset(){
		turn_point_count = -1;
		turn_point_list.clear();
		line_list.clear();
		shape_flag = 'n';
		max_x_for_circle = -1;
		max_y_for_circle = -1;
		line_flag = 0;
	}
	
}
