package util;

import java.awt.Dimension;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.text.Font;

public class TextUtils {
	
	public static Dimension getTextDimensions(final Font font, final String text) {
		final FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
		
		long n = text.chars()
			.filter((c) -> c == '\n')
			.count()
		;
		
		int width = (int) Math.ceil(Stream.of(text.split("\n"))
			.unordered()
			.sequential()
			.flatMap((line) -> Stream.of(fm.computeStringWidth(line)))
			.reduce(BinaryOperator.maxBy((left, right) -> Float.compare(left, right)))
			.get()
		);
		
		int height = (int) Math.ceil((fm.getAscent() + fm.getDescent()) * n);

		return new Dimension(width, height);
	}

}
