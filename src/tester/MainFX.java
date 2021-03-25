package tester;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ui.InformationPaneFX;
import ui.IshiharaPlateFX;
import util.TextUtils;

public class MainFX extends Application {

	private Pane root;
	private Camera camera;
	private IshiharaPlateFX plate;
	private InformationPaneFX info;
	private Image img;
	private Text t;
	
	@Override
	public void init() throws Exception {
		final Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Random rng = new Random();
		
		root = new Pane();
		camera = new ParallelCamera();
		img = new Image(new FileInputStream(new File(String.format("%d.png", rng.nextInt(10)))));
		double radius = Math.min(screenSize.getWidth(), screenSize.getHeight()) / 2;
		
		Rectangle background = new Rectangle(screenSize.getWidth(), screenSize.getHeight(), Color.BLACK);
		root.getChildren().add(background);
		
		plate = new IshiharaPlateFX(radius);
		plate.setLayoutX(radius);
		plate.setLayoutY(radius);
		root.getChildren().add(plate);
		
		info = new InformationPaneFX(100, 100);
		info.setLayoutX(0);
		info.setLayoutY(0);
		root.getChildren().add(info);
		
		t = new Text();
		t.setVisible(false);
		t.setLayoutX(0);
		t.setLayoutY(0);
		t.toFront();
		root.getChildren().add(t);
		
		initCircles();
	}
	
	
	public void initCircles() {		
		final Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		double radius = Math.min(screenSize.getWidth(), screenSize.getHeight()) / 2;
		
		double[] radii = new double[] {
				radius * 0.023, 
				radius * 0.02, 
				radius * 0.0175,
				radius * 0.01
		};
		plate.generateRandomCircles(radii);
		System.out.println("Generated Random Circles");
		
		List<Color> randomColors = generateRandomColors()
			.limit(6)
			.collect(Collectors.toList());
	
		Color[] colors = new Color[3];
		Color[] colors2 = new Color[3];
		
		randomColors.subList(0, 3).toArray(colors);
		randomColors.subList(3, 6).toArray(colors2);
		
		
		
		/*Color[] colors = new Color[] {
			Color.hsb(115, 1, 1),
			Color.hsb(110, 1, 1),
			Color.hsb(105, 1, 1)
		};
		Color[] colors2 = new Color[] {
			Color.hsb(130, 1, 1),
			Color.hsb(125, 1, 1),
			Color.hsb(120, 1, 1)
		};*/
		
		
		plate.colorizeOverlappedCirclesRandomly(img, colors, colors2);
		
		plate.circles.forEach((circle) -> circle.addEventHandler(MouseEvent.MOUSE_RELEASED, this::circleMouseEventHandler));
		
		initTextAreas(colors, colors2, screenSize, radius);
	}
	
	private void initTextAreas(final Color[] overlayedColors, final Color[] otherColors, final Dimension screenSize, final double radius) {
		Font font = new Font(20);
		
		int nLines = Math.max(overlayedColors.length, otherColors.length);
		
		String.format("");
		
		StringBuilder sb = new StringBuilder();
		String header = "Overlayed  |   Other";
		sb.append(header);
		for (int i = 0; i < nLines; i++) {
			sb.append(String.format("%n%10s   |   %-10s",
				i >= overlayedColors.length ? "" : overlayedColors[i].toString(),
				i >= otherColors.length ? "" : otherColors[i].toString()
			));
		}
		
		Dimension dim = TextUtils.getTextDimensions(font, sb.toString());
		
		boolean monitorIsHorizontal = screenSize.getWidth() >= screenSize.getHeight();
		Text content = new Text(monitorIsHorizontal ? Math.min(radius * 2, screenSize.getWidth() - dim.width) : 0, !monitorIsHorizontal ? 0 : radius * 2, sb.toString());
		
		content.setFont(font);
		content.setFill(Color.WHITE);
		content.setTranslateY(content.getTranslateY() - dim.height);
		
		root.getChildren().add(content);
	}
 	
	private void circleMouseEventHandler(MouseEvent event) {
		Circle clicked = (Circle) event.getPickResult().getIntersectedNode();
		info.setSelectedColor((Color) clicked.getFill());
		if (!t.isVisible()) {
			t.setText("Selected Color\n" + clicked.getFill());
			t.setVisible(true);
			t.toFront();
		} else
			t.setVisible(false);
		System.out.println("test");
	}
	
	private Stream<Color> generateRandomColors() {	
		return new Random(System.currentTimeMillis()).ints(70, 146)
			.boxed()
			.unordered()
			.sequential()
			.flatMap((Integer i) -> {
				return Stream.of(Color.hsb(i, 1, 1));
			})
			.distinct()
		;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		stage.initStyle(StageStyle.UNDECORATED);
		
		final Scene scene = new Scene(root);
		scene.setCamera(camera);
		scene.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if (event.getCode() == KeyCode.S && event.isControlDown() && !event.isAltDown() && !event.isMetaDown() && !event.isShiftDown()) {
				FileChooser fc = new FileChooser();
				File file = fc.showSaveDialog(stage);
				if (Objects.isNull(file))
					return;
				
				try {
					if (!file.exists())
						file.createNewFile();
					OutputStream os = Files.newOutputStream(file.toPath(), StandardOpenOption.TRUNCATE_EXISTING);
					os.write(plate.toJson().toString().getBytes());
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		});
		
		
		stage.setTitle("Ishihara Plate");
		stage.setScene(scene);
		stage.setX(0);
		stage.setY(0);
		stage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
