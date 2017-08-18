package focus.flashcardwidget;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Flashcard {
    private String front;
    private String back;
    private boolean displayFront = true;
}
