package tester;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Toolkit;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class FXColorsTest extends Application {
	/* Colors that look identical
	 * 0xf9ff00ff 0xffff00ff
	 * 0xf9ff00ff 0xf5ff00ff
	 * 
	 * These colors look identical when on the ishihara plate
	 * 0xd18407ff 0xff0000ff
	 * 
	 * Ranges that I'm color blind to
	 * 0xff50ff-0xff00ff
	 * s
	 * 
	 * 
	 */
	
	private GridPane grid;
	private Rectangle left;
	private Rectangle right;
	private Rectangle selected = left;
	
	private EventHandler<KeyEvent> keyboardControls;
	private EventHandler<MouseEvent> mouseControls;
	
	@Override
	public void init() throws Exception {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final double width = screenSize.getWidth() / 2; 
		
		grid = new GridPane();
		left = new Rectangle(width, screenSize.getHeight(), Color.web("0xf9ff00ff"));
		right = new Rectangle(width, screenSize.getHeight(), Color.web("0xffff00ff"));
		grid.addRow(0, left, right);
		
		final double step = 0.0025; // 1/255 is about 0.0039
		
		keyboardControls = new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
					Color color = (Color) selected.getFill();
					double red = color.getRed();
					double green = color.getGreen();
					double blue = color.getBlue();
					if (event.getCode().equals(KeyCode.Q))
						red += step;
					if (event.getCode().equals(KeyCode.A))
						red -= step;
					if (event.getCode().equals(KeyCode.W))
						green += step;
					if (event.getCode().equals(KeyCode.S))
						green -= step;
					if (event.getCode().equals(KeyCode.E))
						blue += step;
					if (event.getCode().equals(KeyCode.D))
						blue -= step;
					if (event.getCode().equals(KeyCode.ESCAPE)) {
						left.setFill(Color.rgb(255, 0, 0));
						right.setFill(Color.rgb(0, 255, 0));
						return;
					}
					if (event.getCode().equals(KeyCode.SPACE)) {
						System.out.print(left.getFill().toString() + " ");
						System.out.println(right.getFill().toString());
						return;
					}
					red = Math.min(Math.max(red, 0), 1);
					green = Math.min(Math.max(green, 0), 1);
					blue = Math.min(Math.max(blue, 0), 1);
					
					selected.setFill(new Color(red,green,blue,1));
					color = (Color) selected.getFill();
					System.out.printf("r: %f, g: %f, b: %f\n", color.getRed(), color.getGreen(), color.getBlue());
				}
			}
			
		};
		
		mouseControls = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
					selected = (Rectangle) event.getPickResult().getIntersectedNode();
					System.out.println("New Target: " + selected.getFill().toString() );
				}
			}
			
		};
		
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		final Scene scene = new Scene(grid);
		scene.setCamera(new ParallelCamera());
		scene.addEventHandler(KeyEvent.ANY, keyboardControls);
		scene.addEventHandler(MouseEvent.ANY, mouseControls);
		
		primaryStage.setTitle("JavaFX Colors Test");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}

}
