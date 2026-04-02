public class LookAtTestLookup42 {
    public static void main(String[] args) {
        System.out.println("Wait! `getTextPosition` mapped all of them to index 0??");
        System.out.println("Ah! Look at the X coordinate! `bounds.getMinX() + 5`!");
        System.out.println("Wait! The cell `TextFlow` doesn't span the whole width, it spans only the text width!");
        System.out.println("No, `getTextPosition` uses the bounding box of `CodeArea`!");
        System.out.println("If it returned 0 for all, `getTextPosition` is not reliable.");
        System.out.println("Wait! `CodeArea` is highly virtualized! Is there NO property on `TextCell`?");
        System.out.println("Let's look at `Node.getProperties()`.");
        System.out.println("If we iterate over `cell.getProperties()`, do we see the index?");
    }
}
