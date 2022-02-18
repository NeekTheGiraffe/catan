package ui;

import java.awt.event.MouseEvent;

public interface Clickable extends Drawable {

    boolean isInside(MouseEvent e);
}
