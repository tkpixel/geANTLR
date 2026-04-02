public class LookAtLog10 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`.");
        System.out.println("What if the line is empty? Then `firstNonWsIdx` is 0. But `openLineText.length()` is 0.");
        System.out.println("If it's empty, `firstNonWsIdx = openPos.offset()`. `openPos` would be 0.");
        System.out.println("Is `caretInfoAt` available for `charIdx = length` of `TextFlow`? Yes, to place caret at the end.");
        System.out.println("Wait. Look at `image.png`.");
        System.out.println("The user clicked on `{` of `FEHLER {`. The vertical line did NOT appear!");
        System.out.println("Wait, if `closeCaret.getMinY() > openCaret.getMaxY()` is failing, maybe `getMinY()` == `getMaxY()` because `Rectangle2D localCaret = caretInfo.getSegmentAt(0);` returns a 0-height rect?");
        System.out.println("Let's look at `TestLookup11.java`: `caretInfo(0)=[Rectangle2D [minX=0.0, minY=0.0, maxX=0.0, maxY=15.1328125, width=0.0, height=15.1328125]]`.");
        System.out.println("Ah! The bounds are constructed like this: `BoundingBox(localCaret.getMinX(), localCaret.getMinY(), localCaret.getWidth(), localCaret.getHeight())`");
        System.out.println("Wait! `localCaret.getWidth()` is 0.0!");
        System.out.println("Does `BoundingBox` accept 0 width? Yes.");
        System.out.println("Then `overlayBounds` has width 0.");
        System.out.println("Does `Rectangle2D` accept 0 width? Yes.");
        System.out.println("So `getMinY()` and `getMaxY()` should be 0.0 and 15.13.");
        System.out.println("Wait. `closeCaret.getMinY() > openCaret.getMaxY()` ?");
        System.out.println("If they are adjacent lines, `openCaret.getMaxY()` might be 15.13.");
        System.out.println("`closeCaret.getMinY()` might be 15.13.");
        System.out.println("So `15.13 > 15.13` is FALSE!");
        System.out.println("So it DOES fail for adjacent lines!");
        System.out.println("But in the image, the `}` is NOT adjacent! There is an `ERGEBNIS` line between them!");
        System.out.println("So `closeCaret.getMinY()` would be ~30.26, which is > 15.13.");
        System.out.println("So it MUST be that `indentCaret` or `openCaret` or `closeCaret` is returning NULL!");
        System.out.println("Why would they return NULL?");
    }
}
