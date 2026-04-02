public class LookAtLog24 {
    public static void main(String[] args) {
        System.out.println("Wait! In `updateBracketLine`, if I click the bracket, `onCaretPositionChanged` fires, updates property, triggers listener.");
        System.out.println("Listener runs `Platform.runLater(updateBracketLine)`. ");
        System.out.println("At this point, the node is clearly rendered and CSS is applied.");
        System.out.println("Wait... what if `indexVal` is NOT an `Integer`? What if it's a `Long` or something? `int` gets boxed to `Integer`.");
        System.out.println("We saw `indexVal instanceof Integer` was `true`.");
        System.out.println("Wait! Let's review the code:");
        System.out.println("```java");
        System.out.println("for (javafx.scene.Node cell : editorCodeArea.lookupAll(\".content > *\")) {");
        System.out.println("    boolean isMatch = false;");
        System.out.println("    try {");
        System.out.println("        java.lang.reflect.Method getIndexMethod = cell.getClass().getMethod(\"getIndex\");");
        System.out.println("        Object indexVal = getIndexMethod.invoke(cell);");
        System.out.println("        if (indexVal instanceof Integer && ((Integer) indexVal) == targetPara) {");
        System.out.println("            isMatch = true;");
        System.out.println("        }");
        System.out.println("    } catch (Exception e) {}");
        System.out.println("    if (isMatch) { ... break; }");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Wait, if `indexVal` is boxed to `Integer`, `==` compares object references!");
        System.out.println("Because `targetPara` is an `int`? `((Integer) indexVal) == targetPara` unboxes `indexVal` to `int` and does primitive comparison!");
        System.out.println("Yes, Java unboxes `Integer` when comparing with `int`.");
        System.out.println("Let's test this in Java.");
    }
}
