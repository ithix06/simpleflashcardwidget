package focus.flashcardwidget;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Flashcard {
    private String front;
    private String back;
    private boolean displayFront = true;
    private boolean starred = false;

    public Flashcard(String front, String back, boolean displayFront) {
        this.front = front;
        this.back = back;
        this. displayFront = displayFront;
    }

    public void setDisplayFront(boolean toDisplay) {
        this.displayFront = toDisplay;
    }

    public boolean isDisplayFront() {
        return displayFront;
    }

    public void setStarred(boolean isStarred) {
        starred = isStarred;
    }

    public boolean isStarred() {
        return this.starred;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }
}
