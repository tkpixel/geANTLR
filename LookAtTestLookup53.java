public class LookAtTestLookup53 {
    public static void main(String[] args) {
        System.out.println("Wait! Is there an EASIER way?");
        System.out.println("Can we use `Node.getUserData()`?");
        System.out.println("No, we printed it, it was null.");
        System.out.println("Can we do `((javafx.scene.control.Cell) cell).getIndex()` ?");
        System.out.println("`TextCell` DOES NOT extend `Cell`.");
        System.out.println("Can we do `editorCodeArea.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.ITEM_AT_INDEX, ...)` ?");
        System.out.println("Maybe `CodeArea` returns `TextCell`s by index in accessibility methods?");
        System.out.println("Can we just match contiguous strings?");
        System.out.println("`[T_0, T_1, T_2, ... T_n]` is a list of `TextFlow` texts.");
        System.out.println("We know the full `[P_0, P_1, P_2, ... P_M]` model texts.");
        System.out.println("There might be MULTIPLE matches of `T` in `P`.");
        System.out.println("But wait! The CARET is at `editorCodeArea.getCaretPosition().index()`.");
        System.out.println("Since the caret is ON screen, the index `editorCodeArea.getCaretPosition().index()` MUST be in the visible set!");
        System.out.println("So we just find the match of `T` in `P` that INCLUDES `caretPosition.index()`!");
        System.out.println("Let's write a simple pattern matcher!");
    }
}
