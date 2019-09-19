package sample;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.SimpleBookmark;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * IText5 测试类
 *
 * PDF书签结构：
 *
 */
public class ITextDemo {
    /**
     * 检查PDF中的书签
     *
     * @param filename
     * @throws IOException
     * @throws DocumentException
     */
    public static void inspectPdf(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        List<HashMap<String, Object>> bookmarks = SimpleBookmark.getBookmark(reader);
        for (int i = 0; i < bookmarks.size(); i++) {
            showTitle(bookmarks.get(i));
        }
        reader.close();
    }

    private static void showTitle(HashMap<String, Object> bm) {
        System.out.println((String) bm.get("Title"));
        List<HashMap<String, Object>> kids = (List<HashMap<String, Object>>) bm.get("Kids");
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                showTitle(kids.get(i));
            }
        }
    }

    /**
     * 操纵PDF的书签
     *
     * @param src 源文件路径
     * @param dest 输出文件路径
     * @throws IOException
     * @throws DocumentException
     */
    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        //PDF的压模
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        List<HashMap<String, Object>> list = SimpleBookmark.getBookmark(reader);
        changeList(list);
        stamper.setOutlines(list);
        stamper.close();
        reader.close();
    }

    private static void changeList(List<HashMap<String, Object>> list) {
        for (HashMap<String, Object> entry : list) {
            for (String key : entry.keySet()) {
                if ("Kids".equals(key)) {
                    Object o = entry.get(key);
                    changeList((List<HashMap<String, Object>>)o);
                }
                else if ("Page".equals(key)) {
                    String dest = (String)entry.get(key);
                    entry.put("Page", dest.replaceAll("Fit", "FitV 60"));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, DocumentException {
        final String src = "F:\\test\\testpdf.pdf";
        final String dest="F:\\test\\test_pdf_new.pdf";
        final String demo="F:\\test\\pdf_has_bookmarks.pdf";

        inspectPdf(demo);
    }

}
