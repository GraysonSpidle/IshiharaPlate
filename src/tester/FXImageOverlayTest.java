package tester;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.IshiharaPlateFX;

public class FXImageOverlayTest extends Application {
	
	List<Circle> circles;
	IshiharaPlateFX plate;
	Pane root;
	
	@Override
	public void init() throws Exception {
		root = new Pane();
		circles = new ArrayList<Circle>() {
			@Override
			public boolean add(Circle e) {
				try {
					return super.add(e);
				} finally {
					root.getChildren().add(e);
				}
			}
			
			@Override
			public boolean remove(Object o) {
				try {
					return super.remove(o);
				} finally {
					root.getChildren().remove(o);
				}
			}
			
		};
		
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double radius = Math.min(screenSize.getWidth(), screenSize.getHeight()) / 2;
		plate = new IshiharaPlateFX(radius);
		plate.setLayoutX(radius);
		plate.setLayoutY(radius);
		double[] radii = new double[] {
				radius * 0.023, 
				radius * 0.02, 
				radius * 0.0175,
				radius * 0.01
		};
		plate.generateRandomCircles(radii);
		
		final Image img = new Image(new FileInputStream(new File("8.png")));
		radius = 5;
		final double step = 2 * radius;
		final double xCap = img.getWidth() / step;
		final double yCap = img.getHeight() / step;
		for (double y = 1; y < yCap; ++y) {
			for (double x = 1; x < xCap; ++x) {
				Circle toAdd = new Circle(x*step,y*step,radius);
				circles.add(toAdd);
			}
		}
		
		Predicate<Circle> pred = new Predicate<Circle>() {

			@Override
			public boolean test(Circle c) {
				return false;
			}
			
		};
		
		System.out.println("Generated the circles");
		final PixelReader reader = img.getPixelReader();
		for (int y = 0; y < img.getHeight(); ++y) {
			System.out.println((y / img.getHeight()) * 100 + "%");
			for (int x = 0; x < img.getWidth(); ++x) {
				Color color = reader.getColor(x, y).grayscale();
				if (color.getRed() < 0.5 && color.getGreen() < 0.5 && color.getBlue() < 0.5) {
					Circle circle = getCircleAt(x, y);
					if (!Objects.isNull(circle)) {
						circle.setFill(Color.RED);
					}
				}
			}
		}
		
		
	}
	
	private Circle getCircleAt(final double x, final double y) {
		Optional<Circle> output = circles.stream()
			.unordered()
			.sequential()
			.filter((circle) -> Math.sqrt(Math.pow(circle.getCenterX() - x, 2) + Math.pow(circle.getCenterY() - y, 2)) <= circle.getRadius())
			.findFirst();
		return output.isPresent() ? output.get() : null;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		stage.initStyle(StageStyle.UNDECORATED);
		
		final Scene scene = new Scene(root);
		
		stage.setTitle("Image Overlay Test");
		stage.setScene(scene);
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
