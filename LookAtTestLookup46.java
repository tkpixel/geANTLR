public class LookAtTestLookup46 {
    public static void main(String[] args) {
        System.out.println("If we can't use `getIndex()`, how can we fix `getTextPosition(x, y)`?");
        System.out.println("Wait! `getTextPosition` on `CodeArea` returns index `0` for ALL of them in my script.");
        System.out.println("Why? Because `getTextPosition` expects `x` and `y` relative to `CodeArea`!");
        System.out.println("I did `cell.localToScreen` and then `area.screenToLocal`.");
        System.out.println("Wait, if `CodeArea` has scrollbars, `y` must be relative to the viewport!");
        System.out.println("Let's look at `TestLookup41.java`:");
        System.out.println("`Bounds bounds = cell.localToScreen(cell.getBoundsInLocal());`");
        System.out.println("`javafx.geometry.Point2D pt = area.screenToLocal(bounds.getMinX() + 5, bounds.getMinY() + bounds.getHeight() / 2);`");
        System.out.println("`TextPos pos = area.getTextPosition(pt.getX(), pt.getY());`");
        System.out.println("Why did it return `index=0`? Let's check!");
    }
}
