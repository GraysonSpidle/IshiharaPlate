package ui;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.internal.bind.JsonTreeWriter;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class IshiharaPlateFX extends Group implements Serializable {
	private static final long serialVersionUID = 8669327917441458458L;
	
	public ArrayList<Circle> circles;
	public Circle basePlate;
	public Group colorPane;
	
	public ArrayList<Color> overlayedColors;
	public ArrayList<Color> otherColors;
	private double[] radii;
	
	public IshiharaPlateFX(final double radius) {
		this.basePlate = new Circle(radius, Color.rgb(210,180,140,1));
		circles = new ArrayList<Circle>();
		overlayedColors = new ArrayList<>();
		otherColors = new ArrayList<>();
		this.getChildren().add(basePlate);
	}
	
	public void generateRandomCircles(final double...radii) {
		this.radii = radii;
		this.getChildren().clear();
		this.getChildren().add(basePlate);
		this.circles.clear();
		
		final Random rand = new Random();
		
		final BiFunction<Double,Double,Double> getRandomDouble = (min,max) -> min + rand.nextDouble() * (max - min); 
		
		final Supplier<Circle> randomCircles = new Supplier<Circle>() {

			@Override
			public Circle get() {
				final double x = getRandomDouble.apply(basePlate.getCenterX() - basePlate.getRadius(), basePlate.getCenterX() + basePlate.getRadius());
				final double y = getRandomDouble.apply(basePlate.getCenterY() - basePlate.getRadius(), basePlate.getCenterY() + basePlate.getRadius());
				final double radius = radii[rand.nextInt(radii.length)];
				return new Circle(x,y,radius,Color.BLACK);
			}
		};
		
		final Predicate<Circle> intersectionTest = new Predicate<Circle>() {
			@Override
			public boolean test(Circle t) {
				final Point2D tCenter = new Point2D(t.getCenterX(), t.getCenterY());
				double distance = tCenter.distance(basePlate.getCenterX(), basePlate.getCenterY());
				if (distance + t.getRadius() > basePlate.getRadius())
					return false;
				for (final Circle c : circles) {
					distance = tCenter.distance(c.getCenterX(), c.getCenterY());
					if (distance - t.getRadius() <= c.getRadius())
						return false;
				}
				return true;
			}
		};
		
		final Iterator<Circle> circles = Stream.generate(randomCircles)
				.unordered()
				.sequential()
				.filter(intersectionTest)
				.peek((circle) -> this.circles.add(circle))
				.peek((circle) -> this.getChildren().add(circle))
				.iterator();
		
		final double coveragePercentage = 50; // ~#% of the base plate will be covered by circles (50 is pretty solid, anything above 60 will take a while)
		final double threshold = (Math.pow(basePlate.getRadius(), 2) * Math.PI) * (coveragePercentage / 100);
		double areaCovered = 0;
		
		while (circles.hasNext() && areaCovered <= threshold) {
			Circle circle = circles.next();
			areaCovered += Math.pow(circle.getRadius(), 2) * Math.PI;
		}
	}

	public void colorizeRandomly(final Color...colors) {
		otherColors.clear();
		Collections.addAll(otherColors, colors);
		
		final Random rand = new Random();
		final Iterator<Color> randomColors = Stream.generate(() -> colors[rand.nextInt(colors.length)])
				.sequential()
				.unordered()
				.iterator();
		
		for (final Circle circle : this.circles) {
			circle.setFill(randomColors.next());
		}
	}
	
	public void colorizeOverlappedCirclesRandomly2(final Image binaryImg, final Color[] colors, final Color[] otherColors) {
		overlayedColors.clear();
		this.otherColors.clear();
		
		Collections.addAll(overlayedColors, colors);
		Collections.addAll(this.otherColors, otherColors);
		
		final Random rand = new Random();
		final Supplier<Color> randomColor = () -> colors[rand.nextInt(colors.length)];
		final Supplier<Color> randomColor2 = () -> otherColors[rand.nextInt(otherColors.length)];
		final PixelReader reader = binaryImg.getPixelReader();
		for (int y = 0; y < binaryImg.getHeight(); ++y) {
			for (int x = 0; x < binaryImg.getWidth(); ++x) {
				if (!basePlate.contains(basePlate.getCenterX() + this.getLayoutX() - x, basePlate.getCenterY() + this.getLayoutY() - y)) continue;
				
				Color c = reader.getColor(x, y).grayscale();
				Circle circle = circleAt(x,y);
				if (Objects.isNull(circle))
					continue;
				if (c.getRed() < 0.5 && c.getGreen() < 0.5 && c.getBlue() < 0.5) {
					circle.setFill(randomColor.get());
				} else {
					circle.setFill(randomColor2.get());
				}
			}
//			System.out.println(y / binaryImg.getHeight());
		}
	}
	
	public void colorizeOverlappedCirclesRandomly(final Image binaryImg, final Color[] colors, final Color[] otherColors) {
		overlayedColors.clear();
		this.otherColors.clear();
		
		Collections.addAll(overlayedColors, colors);
		Collections.addAll(this.otherColors, otherColors);
		
		final Random rand = new Random();
		final Supplier<Color> randomColor = () -> colors[rand.nextInt(colors.length)];
		final Supplier<Color> randomColor2 = () -> otherColors[rand.nextInt(otherColors.length)];
		
		circles.stream()
			.forEach((circle) -> circle.setFill(randomColor2.get()));
		
		getOverlayedCircles(binaryImg)
			.forEach((circle) -> circle.setFill(randomColor.get()));
	}
	
	public Stream<Circle> getOverlayedCircles(final Image binaryImg) {
		// Warning, duplicates will occur
		
		Supplier<Point> pointSupplier = new Supplier<Point>() {
			int x, y = 0;
			
			@Override
			public Point get() {
				if (x >= binaryImg.getWidth()) {
					y++;
					x = 0;
				}
				if (y >= binaryImg.getHeight())
					return null;
				
				return new Point(x++, y);
			}
		};
		
		final PixelReader pr = binaryImg.getPixelReader();
		
		return Stream.generate(pointSupplier)
			.sequential()
			.limit((long) (binaryImg.getWidth() * binaryImg.getHeight()))
//			.filter((p) -> {
//				return basePlate.contains(basePlate.getCenterX() + this.getLayoutX() - p.x, basePlate.getCenterY() + this.getLayoutY() - p.y);
//			})
			.filter((p) -> {
				Color c = pr.getColor(p.x, p.y).grayscale();
				return c.getRed() < 0.5 && c.getGreen() < 0.5 && c.getBlue() < 0.5;
			})
			.flatMap((point) -> circleAtImpl(point.getX(), point.getY()))
			.filter((c) -> !Objects.isNull(c))
		;
	}
	
	public Circle circleAt(double x, double y) {
		Optional<Circle> output = circleAtImpl(x, y)
			.findFirst();
		return output.isPresent() ? output.get() : null;
	}
	
	protected Stream<Circle> circleAtImpl(double x, double y) {
		if (!basePlate.contains(basePlate.getCenterX() + this.getLayoutX() - x, basePlate.getCenterY() + this.getLayoutY() - y))
			return Stream.of((Circle) null);
		return circles.stream()
			.unordered()
			.sequential()
			.filter((circle) -> Math.sqrt(Math.pow((circle.getCenterX() + this.getLayoutX()) - x, 2) + Math.pow((circle.getCenterY() + this.getLayoutY()) - y, 2)) <= circle.getRadius());
	}
	
	public double getRadius() {
		return this.basePlate.getRadius();
	}
	
	public Point2D getCenter() {
		return new Point2D(this.basePlate.getCenterX(), this.basePlate.getCenterY());
	}
	
	public JsonElement toJson() throws IOException {
		BiFunction<JsonTreeWriter, List<Color>, JsonTreeWriter> writeColors = (jtw, colors) -> {
			colors.forEach((color) -> {
				try {
					jtw.value(color.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			return jtw;
		};
		
		
		Function<JsonTreeWriter, JsonTreeWriter> writeCircles = (jtw) -> {
			circles.stream()
				.unordered().sequential()
				.forEach((circle) -> {
					try {
						jtw.beginObject()
							.name("radius").value(circle.getRadius())
							.name("x").value(circle.getCenterX())
							.name("y").value(circle.getCenterY())
							.name("color").value(((Color) circle.getFill()).toString())
						.endObject();
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			return jtw;
		};
		
		
		JsonTreeWriter jtw = new JsonTreeWriter();
		jtw.beginObject()
			.name("radius").value(basePlate.getRadius())
			.name("color").value(((Color) basePlate.getFill()).toString())
			.name("overlayedColors").beginArray();
				writeColors.apply(jtw, overlayedColors)
			.endArray()
			.name("otherColors").beginArray();
				writeColors.apply(jtw, otherColors)
			.endArray()
			.name("circles").beginArray();
				writeCircles.apply(jtw)
			.endArray()
		.endObject();
		
		return jtw.get();
	}
	
}
