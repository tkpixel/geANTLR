import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;

public class FindMdi {
    public static void main(String[] args) throws Exception {
        for (var e : MaterialDesignF.values()) {
            if (e.name().contains("FORMAT_LETTER_CASE") || e.name().contains("FORMAT_TEXT") || e.name().contains("TEXT")) {
                System.out.println(e.name());
            }
        }
        for (var e : MaterialDesignW.values()) {
            if (e.name().contains("WORD")) {
                System.out.println(e.name());
            }
        }
        for (var e : MaterialDesignR.values()) {
            if (e.name().contains("REGEX")) {
                System.out.println(e.name());
            }
        }
        for (var e : MaterialDesignA.values()) {
            if (e.name().contains("ALPHA")) {
                System.out.println(e.name());
            }
        }
        for (var e : MaterialDesignC.values()) {
            if (e.name().contains("CASE")) {
                System.out.println(e.name());
            }
        }
    }
}
