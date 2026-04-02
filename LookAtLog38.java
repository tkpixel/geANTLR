public class LookAtLog38 {
    public static void main(String[] args) {
        System.out.println("Yes! That's it!");
        System.out.println("If `TextCell` is a non-exported package class, we CANNOT invoke its methods via reflection without `.setAccessible(true)` AND an open module!");
        System.out.println("Wait! We can't even `setAccessible(true)` because it's in a closed module!");
        System.out.println("So we CANNOT use `getIndex()` from `TextCell`!");
        System.out.println("This is why the whole thing vanishes!");
        System.out.println("How did I match them before? I used `editorCodeArea.getModel().getPlainText(targetPara)` and compared it to `flowText`!");
        System.out.println("But that had the bug where identical lines (like `FEHLER {`) mapped to the first rendered `TextCell` with that text, causing jumping lines and missing lines.");
        System.out.println("How do I match the EXACT `TextCell` without `getIndex()`?");
        System.out.println("Wait, does `TextFlow` or `TextCell` have a user-data or property that has the index?");
        System.out.println("No, not necessarily.");
        System.out.println("BUT wait! `CodeArea` is highly virtualized.");
        System.out.println("Is there ANY way to get the bounds of a paragraph without `getIndex()`?");
        System.out.println("Yes! The visible `TextCell` nodes are sequentially ordered by Y coordinate!");
        System.out.println("Can we determine the visible range of paragraph indices?");
        System.out.println("Actually, the `index` property on `IndexedCell` is publicly exposed!");
        System.out.println("Wait! Is `TextCell` extending `IndexedCell`?");
        System.out.println("In `LookAtTextCell.java`, I printed: `TextCell superclass: javafx.scene.layout.BorderPane`.");
        System.out.println("So it DOES NOT extend `IndexedCell` or `Cell`.");
        System.out.println("So there is NO public interface that defines `getIndex()` for `TextCell`.");
        System.out.println("Is there ANY way to map a paragraph to a `TextFlow`?");
    }
}
