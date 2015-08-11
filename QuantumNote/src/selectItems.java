import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.ArrayList;


public class selectItems {
	int selectRange_x1, selectRange_y1, selectRange_x2, selectRange_y2;
	int move_x1, move_y1, move_x2, move_y2, move_width, move_height;
	int max_x_select, max_y_select, min_x_select, min_y_select;
	int max_x_border, max_y_border, min_x_border, min_y_border, max_width, max_height;
	int select_flag, move_flag;
	int[] selectitemList;
	int selectIndex;
	int R, G, B;

	public selectItems(){        
		selectRange_x1 = 0;
		selectRange_y1 = 0;
		selectRange_x2 = 0;
		selectRange_y2 = 0;
		move_x1 = 0;
		move_y1 = 0;
		move_x2 = 0;
		move_y2 = 0;
		move_width = 0;
		move_height = 0;
		R = 255;
		G = 0;
		B = 0;
		select_flag = 0;
		move_flag = 0;
		selectitemList = new int[10000];
		selectIndex = -1;
		max_x_border = 0;
		max_y_border = 0;
		min_x_border = 0;
		min_y_border = 0;
		max_width = 0;
		max_height = 0;		
	}
	public void set_max_min_value(){
		max_x_select = (selectRange_x1 > selectRange_x2) ? selectRange_x1 : selectRange_x2;
		max_y_select = (selectRange_y1 > selectRange_y2) ? selectRange_y1 : selectRange_y2;
		min_x_select = selectRange_x1 + selectRange_x2 - max_x_select;
		min_y_select = selectRange_y1 + selectRange_y2 - max_y_select;
	}

	public void draw(Graphics2D g2d){
		g2d.setPaint(new Color(R,G,B));
		g2d.drawRect(Math.min(selectRange_x1,selectRange_x2),Math.min(selectRange_y1,selectRange_y2), Math.abs(selectRange_x1-selectRange_x2),Math.abs(selectRange_y1-selectRange_y2));	
	}
	
	public void Selectfunc_Reset(){
		selectRange_x1 = 0;
		selectRange_y1 = 0;
		selectRange_x2 = 0;
		selectRange_y2 = 0;
		move_x1 = 0;
		move_y1 = 0;
		move_x2 = 0;
		move_y2 = 0;
		move_width = 0;
		move_height = 0;
		R = 255;
		G = 0;
		B = 0;
		select_flag = 0;
		move_flag = 0;
		selectIndex = -1;
		
		for(int i = 0; i < 5000; i++)
			selectitemList[i] = 0;
	}
	
	public void Check_max_min_border(ArrayList<drawing_info> head, ArrayList<drawings> itemList){
		if(head.get(selectitemList[0]).currentChoice != 17){
			max_x_border = (itemList.get(head.get(selectitemList[0]).startPos).x1 > itemList.get(head.get(selectitemList[0]).startPos).x2) ? itemList.get(head.get(selectitemList[0]).startPos).x1 : itemList.get(head.get(selectitemList[0]).startPos).x2;
			max_y_border = (itemList.get(head.get(selectitemList[0]).startPos).y1 > itemList.get(head.get(selectitemList[0]).startPos).y2) ? itemList.get(head.get(selectitemList[0]).startPos).y1 : itemList.get(head.get(selectitemList[0]).startPos).y2;		
			min_x_border = itemList.get(head.get(selectitemList[0]).startPos).x1 + itemList.get(head.get(selectitemList[0]).startPos).x2 - max_x_border;
			min_y_border = itemList.get(head.get(selectitemList[0]).startPos).y1 + itemList.get(head.get(selectitemList[0]).startPos).y2 - max_y_border;
		}
		else{
			max_x_border = itemList.get(head.get(selectitemList[0]).startPos).x1 + itemList.get(head.get(selectitemList[0]).startPos).width;
			max_y_border = itemList.get(head.get(selectitemList[0]).startPos).y1;
			min_x_border = itemList.get(head.get(selectitemList[0]).startPos).x1;
			min_y_border = itemList.get(head.get(selectitemList[0]).startPos).y1 - itemList.get(head.get(selectitemList[0]).startPos).height;
		}
		max_width = 0;
		max_height = 0;
				
		for(int i = 0; i <= selectIndex; i++){
			if(head.get(selectitemList[i]).currentChoice != 17){
				for(int j = head.get(selectitemList[i]).startPos; j <= head.get(selectitemList[i]).endPos; j++){
					if(itemList.get(j).x1 > max_x_border) max_x_border = itemList.get(j).x1;
					if(itemList.get(j).x2 > max_x_border) max_x_border = itemList.get(j).x2;
					if(itemList.get(j).y1 > max_y_border) max_y_border = itemList.get(j).y1;
					if(itemList.get(j).y2 > max_y_border) max_y_border = itemList.get(j).y2;
					if(itemList.get(j).x1 < min_x_border) min_x_border = itemList.get(j).x1;
					if(itemList.get(j).x2 < min_x_border) min_x_border = itemList.get(j).x2;
					if(itemList.get(j).y1 < min_y_border) min_y_border = itemList.get(j).y1;
					if(itemList.get(j).y2 < min_y_border) min_y_border = itemList.get(j).y2;	
				}
			}
			else{
				if( (head.get(selectitemList[i]).item.get(0).x1+head.get(selectitemList[i]).item.get(0).width) > max_x_border) max_x_border = (head.get(selectitemList[i]).item.get(0).x1+head.get(selectitemList[i]).item.get(0).width);
				if(head.get(selectitemList[i]).item.get(0).y1 > max_y_border) max_y_border = head.get(selectitemList[i]).item.get(0).y1;
				if(head.get(selectitemList[i]).item.get(0).x1 < min_x_border) min_x_border = head.get(selectitemList[i]).item.get(0).x1;
				if( (head.get(selectitemList[i]).item.get(0).y1 - head.get(selectitemList[i]).item.get(0).height) < min_y_border ) min_y_border = (head.get(selectitemList[i]).item.get(0).y1 - head.get(selectitemList[i]).item.get(0).height);
			}
		}		
		max_width = max_x_border - min_x_border;
		max_height = max_y_border - min_y_border;
	}
	
}
