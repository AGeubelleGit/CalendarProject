package sample;

import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

public class JavaFXUtilities {

    public static Label createLabel(String text, int width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setMaxWidth(width);
        label.setWrapText(true);
        return label;
    }

    public static Rectangle createRect(int xPos, int yPos, int width, int height) {
        Rectangle rect = new Rectangle();
        rect.setX(xPos);
        rect.setY(yPos);
        rect.setWidth(width);
        rect.setHeight(height);
        return rect;
    }

}
