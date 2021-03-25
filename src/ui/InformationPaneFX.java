package ui;

import java.util.ArrayList;
import java.util.Objects;

import javax.xml.transform.Templates;

import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class InformationPaneFX extends Group {
	
	private Color selectedColor = null;
	
	private Canvas colorPreview;
	private Text colorName;
	
	public InformationPaneFX(double width, double height) {
		colorPreview = new Canvas(width, height - 12);
		colorName = new Text(0, height - 12, "null");
		
		
		this.getChildren().add(colorPreview);
		this.getChildren().add(colorName);
	}
	
	public void setSelectedColor(Color arg0) {
		if (selectedColor == arg0)
			selectedColor = null;
		
		colorPreview.setVisible(!Objects.isNull(selectedColor));
		colorName.setVisible(!Objects.isNull(selectedColor));
		
		if (Objects.isNull(selectedColor))
			return;
		
		GraphicsContext gc = colorPreview.getGraphicsContext2D();
		gc.setFill(arg0);
		gc.fillRect(0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
		gc.save();
		
		colorName.setText(arg0.toString());
	}
	
	

}
