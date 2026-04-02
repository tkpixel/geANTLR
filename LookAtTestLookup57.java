public class LookAtTestLookup57 {
    public static void main(String[] args) {
        System.out.println("Wait! `.para-1` returned NOTHING?");
        System.out.println("Ah! `SyntaxDecorator` does NOT run unless it's set BEFORE text is set?");
        System.out.println("Wait, I did `area.setText()` before `setSyntaxDecorator()`.");
        System.out.println("Does `SyntaxDecorator` refresh the text automatically?");
        System.out.println("Let's set `SyntaxDecorator` BEFORE `setText`.");
    }
}
