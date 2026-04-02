public class LookAtTestLookup48 {
    public static void main(String[] args) {
        System.out.println("It returns 0 for EVERYTHING!");
        System.out.println("Ah! `CodeArea.getTextPosition(x,y)` was buggy? Or the layout isn't fully ready?");
        System.out.println("Actually... how is it matching `targetPara` currently without `getIndex()`?");
        System.out.println("We CAN'T use `getIndex` because of `IllegalAccessException`.");
        System.out.println("Wait! We can get the `Cell` Y coordinates!");
        System.out.println("`double y = cell.getBoundsInParent().getMinY()`");
        System.out.println("We can collect ALL `TextCell`s, sort them by `getMinY()`, and we get the VISUAL order!");
        System.out.println("`[Cell_0, Cell_1, Cell_2...]`");
        System.out.println("We want to find which one is `targetPara`.");
        System.out.println("If we can't get the `index` property, and we can't use `getTextPosition`...");
        System.out.println("Wait... what if `targetPara` is `0`?");
        System.out.println("If `targetPara` is `0`, is it ALWAYS the first cell?");
        System.out.println("No, if we scrolled, paragraph 0 is out of view!");
        System.out.println("Wait... is there ANY string in `cell.toString()` or `cell.getId()`?");
    }
}
