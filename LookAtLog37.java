public class LookAtLog37 {
    public static void main(String[] args) {
        System.out.println("Let's look at `EditorViewController.java` at `if (closeCaret.getMinY() >= openCaret.getMaxY())`.");
        System.out.println("What if the `bracketLineOverlay` does NOT have its scene set yet?");
        System.out.println("It does, we checked in `TestLookup17` that `sceneToLocal` works without a scene.");
        System.out.println("Wait! `TextCell` gets reused. `getIndex()` gives the paragraph index.");
        System.out.println("BUT, what if the user scrolls, and `TextCell` is updated, but `TextFlow` is NOT YET populated with `LayoutInfo`?");
        System.out.println("Since `updateBracketLine` is triggered by `needsLayoutProperty()` and `boundsInLocalProperty()`...");
        System.out.println("If it runs BEFORE layout is complete, `layoutInfo` might be NULL!");
        System.out.println("If `layoutInfo` is NULL, it returns NULL. And `connectionLine.setVisible(false)`!");
        System.out.println("And then, when layout finishes, does it trigger `updateBracketLine` again?");
        System.out.println("`needsLayout` goes from true to false! Which triggers the listener!");
        System.out.println("BUT wait, `TextFlow.getLayoutInfo()`... what if `caretInfoAt` returns null?");
        System.out.println("Wait! If the user says: `the vertical bracket connection line is still missing for every {} bracket`, it means it vanishes IMMEDIATELY when clicked, even without scrolling.");
        System.out.println("Wait! Is `bracketLineOverlay` actually visible?");
        System.out.println("Yes, it was working before.");
        System.out.println("Wait! In `EditorViewController.java` at `if (indexVal instanceof Integer && ((Integer) indexVal) == targetPara)`:");
        System.out.println("Wait! I am doing `java.lang.reflect.Method getIndexMethod = cell.getClass().getMethod(\"getIndex\");`!");
        System.out.println("Is `getIndex` a PUBLIC method of `TextCell`?");
        System.out.println("Yes, `public final int com.sun.jfx.incubator.scene.control.richtext.TextCell.getIndex()`.");
        System.out.println("But `TextCell` is NOT a public class! It's in `com.sun.jfx.incubator.scene.control.richtext` which is an INTERNAL package!");
        System.out.println("Can we call a public method of a non-public class via reflection?");
        System.out.println("NO! `IllegalAccessException` is thrown!");
        System.out.println("Wait... `cell.getClass().getMethod(\"getIndex\")` gets the `Method` object.");
        System.out.println("Then `invoke(cell)` is called.");
        System.out.println("If the class is not public, `invoke` throws `IllegalAccessException`!");
        System.out.println("Why did it work in my test scripts? Because I used `--add-exports jfx.incubator.richtext/com.sun.jfx.incubator.scene.control.richtext=ALL-UNNAMED` in the test scripts!");
        System.out.println("Ah! My test scripts had `--add-exports`! But the application DOES NOT HAVE `--add-exports` for that internal package!");
        System.out.println("So `invoke(cell)` THROWS `IllegalAccessException`!");
        System.out.println("And the `catch (Exception e) {}` SWALLOWS it!");
        System.out.println("And `isMatch` is NEVER TRUE!");
        System.out.println("So `getCaretBounds` ALWAYS returns NULL!");
    }
}
