public class LookAtLog20 {
    public static void main(String[] args) {
        System.out.println("Wait! I just realized why it might be vanishing!");
        System.out.println("In `getCaretBounds`, I added a `catch (Exception e) { // Ignore nodes that do not have getIndex() }`");
        System.out.println("But inside the `try` block, I put EVERYTHING:");
        System.out.println("```");
        System.out.println("try {");
        System.out.println("  Method getIndexMethod = cell.getClass().getMethod(\"getIndex\");");
        System.out.println("  Object indexVal = getIndexMethod.invoke(cell);");
        System.out.println("  if (indexVal == targetPara) {");
        System.out.println("     ... textFlow.getLayoutInfo().caretInfoAt(charIdx, true);");
        System.out.println("     ... localToScene ...");
        System.out.println("     ... return ...");
        System.out.println("  }");
        System.out.println("} catch (Exception e) {}");
        System.out.println("```");
        System.out.println("If ANY exception occurs in `caretInfoAt` or `localToScene`, it is swallowed by the `catch` and it returns NULL!");
        System.out.println("Could `caretInfoAt` throw an exception for `firstNonWsIdx`? No, we saw it works.");
        System.out.println("Could `textFlow.localToScene(...)` throw? We saw it doesn't.");
        System.out.println("Could `bracketLineOverlay.sceneToLocal(...)` throw?");
        System.out.println("In `TestLookup17`, `p.sceneToLocal` returned bounds. But wait, what if `bracketLineOverlay` is NOT visible? It's visible, it was working before.");
        System.out.println("Wait! `localCaret.getWidth()` and `localCaret.getHeight()` might be returning unexpected types or throwing if `caretInfo` is invalid?");
        System.out.println("No, `Rectangle2D` returns `double`.");
        System.out.println("What if the `TextCell` is found, but the `TextFlow` has no `LayoutInfo`?");
        System.out.println("Then it skips `if (layoutInfo != null)` and hits `break;` returning `null`!");
        System.out.println("Is it possible `TextFlow` has NO `LayoutInfo`? Yes, if it hasn't been rendered yet!");
        System.out.println("But if it hasn't been rendered, how did it work previously?");
        System.out.println("Previously, I searched `editorCodeArea.lookupAll(\"TextFlow\")` and checked `flowText.equals(modelText)`.");
        System.out.println("If a `TextFlow` was not rendered, it wouldn't be in the scene graph with text! So it would skip it and maybe find the rendered one?");
        System.out.println("Wait, if `TextCell` is found, it IS rendered!");
        System.out.println("Let's just change `>=` and also add logging!");
    }
}
