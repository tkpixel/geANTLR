public class LookAtLog26 {
    public static void main(String[] args) {
        System.out.println("Wait! Look at `TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);`");
        System.out.println("Wait! Is `openPos.index()` the WRONG index? No, it's the paragraph index.");
        System.out.println("Wait, I used `TextPos.ofLeading(...)` but I did not IMPORT `TextPos.ofLeading`.");
        System.out.println("No, `TextPos.ofLeading` is a static method. It exists.");
        System.out.println("Wait, could `firstNonWsIdx` be WRONG?");
        System.out.println("What if the user's `FEHLER {` has a TAB `\\t`?");
        System.out.println("`Character.isWhitespace('\\t')` is TRUE.");
        System.out.println("`firstNonWsIdx` points to `F`.");
        System.out.println("Wait, what if `caretInfoAt` with a TAB character fails or behaves weirdly?");
        System.out.println("No, it should work.");
        System.out.println("Let's review the ENTIRE `EditorViewController.java`!");
    }
}
