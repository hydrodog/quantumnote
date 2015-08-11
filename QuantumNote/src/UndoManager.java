import java.util.ArrayList;

class drawing_info{
	int startPos, endPos;
	int type;
	int currentChoice;
	int max_x, max_y, min_x, min_y;
	ArrayList<drawings> item;

	public drawing_info(){
		startPos = 0;
		endPos = 0;
		type = 0;
		currentChoice = -1;
		max_x = max_y = min_x = min_y = 0;
		item = new ArrayList<drawings>(50);
	}
	
	public void copy_drawings(int s, int e, int currentChoice, ArrayList<drawings> head){
		int count = 0;
		this.currentChoice = currentChoice;
		for(int i = s; i <= e; i++){
			item.add(new drawings(head.get(i)));
			count++;
		}
	}

}

public class UndoManager {
	char operation_flag;
	char operation_type_flag;
	int draw_count, redo_count;
	int write_flag;
	int handwrite_draw_count;
	int handwrite_redo_count;
	ArrayList<drawing_info> drawList;
	ArrayList<drawing_info> redoList;
	ArrayList<drawings> handwrite_drawList;
	ArrayList<drawings> handwrite_redoList;
	
	public UndoManager(){
		operation_flag = 'n';
		operation_type_flag = 'd';
		draw_count = -1;
		redo_count = -1;
		write_flag = 0;
		drawList = new ArrayList<drawing_info>(2000);
		redoList = new ArrayList<drawing_info>(2000);
	}
	
	public void undoOperation(int s, int e, int t, ArrayList<drawings> head){
		if(write_flag == 1){
			redoList.clear();
			redo_count = -1;
		}
		operation_flag = 'u';
		write_flag = 0;
		redo_count++;			
		redoList.add(drawList.get(draw_count));
	}
	
	public void redoOperation(ArrayList<drawings> head){
		if(write_flag == 0){
			operation_flag = 'r';
			drawing_info res = redoList.get(redo_count);
			for(int i = redoList.get(redo_count).startPos, j = 0; i <= redoList.get(redo_count).endPos; i++, j++){
				head.get(i).equal_operation(redoList.get(redo_count).item.get(j));					
			}
			redoList.remove(redo_count);
			redo_count--;
		}
		else{
			redoList.clear();
			redo_count = -1;
		}
	}
	
	public void set_drawing_info_max_min_value(){
		int temp_max_x, temp_max_y, temp_min_x, temp_min_y;
		for(int i = 0; i <= draw_count; i++){
			if(drawList.get(i).item.get(0).shapeType == 'w'){
				drawList.get(i).min_x = drawList.get(i).item.get(0).x1;
				drawList.get(i).min_y = drawList.get(i).item.get(0).y1 - drawList.get(i).item.get(0).height;
				drawList.get(i).max_x = drawList.get(i).item.get(0).x1 + drawList.get(i).item.get(0).width;
				drawList.get(i).max_y = drawList.get(i).item.get(0).y1;
			}
			else{
				drawList.get(i).max_x = (drawList.get(i).item.get(0).x1 > drawList.get(i).item.get(0).x2) ? drawList.get(i).item.get(0).x1 : drawList.get(i).item.get(0).x2;
				drawList.get(i).max_y = (drawList.get(i).item.get(0).y1 > drawList.get(i).item.get(0).y2) ? drawList.get(i).item.get(0).y1 : drawList.get(i).item.get(0).y2;
				drawList.get(i).min_x = drawList.get(i).item.get(0).x1 + drawList.get(i).item.get(0).x2 - drawList.get(i).max_x;
				drawList.get(i).min_y = drawList.get(i).item.get(0).y1 + drawList.get(i).item.get(0).y2 - drawList.get(i).max_y;
				
				for(int j = 1; j < drawList.get(i).item.size(); j++){
					temp_max_x = (drawList.get(i).item.get(j).x1 > drawList.get(i).item.get(j).x2) ? drawList.get(i).item.get(j).x1 : drawList.get(i).item.get(j).x2;
					temp_max_y = (drawList.get(i).item.get(j).y1 > drawList.get(i).item.get(j).y2) ? drawList.get(i).item.get(j).y1 : drawList.get(i).item.get(j).y2;
					temp_min_x = drawList.get(i).item.get(j).x1 + drawList.get(i).item.get(j).x2 - temp_max_x;
					temp_min_y = drawList.get(i).item.get(j).y1 + drawList.get(i).item.get(j).y2 - temp_max_y;
					drawList.get(i).max_x = (drawList.get(i).max_x > temp_max_x) ? drawList.get(i).max_x : temp_max_x;
					drawList.get(i).max_y = (drawList.get(i).max_y > temp_max_y) ? drawList.get(i).max_y : temp_max_y;
					drawList.get(i).min_x = (drawList.get(i).min_x < temp_min_x) ? drawList.get(i).min_x : temp_min_x;
					drawList.get(i).min_y = (drawList.get(i).min_y < temp_min_y) ? drawList.get(i).min_y : temp_min_y;
				}							
			}
		}	
	}
	
	public void Undomanager_Reset(){
		operation_flag = 'n';
		operation_type_flag = 'd';
		draw_count = -1;
		redo_count = -1;
		write_flag = 0;
		drawList.clear();
		redoList.clear();
	}
	
}
