import java.awt.Color;
import java.awt.Graphics2D;


public class Modifier {
	int modify_switcher = 0;        //手写功能时的修改功能控制位
	int modify_flag = 0;            //选中图形标志位
	int start_x1 = -1, start_y1 = -1, start_x2 = -1, start_y2 = -1;
	int index;
	int press_x = -1, press_y = -1;
	
	public void clear_modifier(){
		modify_switcher = modify_flag = 0;            
		start_x1 = start_y1 = start_x2 = start_y2 = index = press_x = press_y = -1;
	}
}
