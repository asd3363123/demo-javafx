package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MainController {
    @FXML
    private TextField text_regex;
    @FXML
    private TextField text_replace;
    @FXML
    private TextArea text_src;
    @FXML
    private TextArea text_result;
    @FXML
    private Button button_run;
    @FXML
    private Button button_replace;
    @FXML
    private TextField text_url;
    @FXML
    private TextArea text_respond;
    @FXML
    private Button button_connect;
    @FXML
    private TextField text_charset;
    @FXML
    private CheckBox check_gzip;
    @FXML
    private TextArea text_http_request_header;
    @FXML
    private CheckBox check_output_file;
    @FXML
    private TextField text_file_path;
    @FXML
    private TextField text_github_url;
    //    @FXML
//    private TextField text_github_file_dir;
    @FXML
    private Button button_github_get_code;

    /**
     * 默认文件路径
     */
    private static final String DEFAULT_FILE_PATH = "default.file";

    /**
     * http请求默认请求头
     */
    private static final Map<String, String> DEFAULT_HTTP_HEADER = new HashMap<String, String>() {{
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        put("Accept-Encoding", "gzip");
        put("Accept-Language", "zh-CN,zh;q=0.9");
        put("Cache-Control", "max-age=0");
        put("Connection", "keep-alive");
        put("Upgrade-Insecure-Requests", "1");
        put("Cookie", "_uuid=00C616CA-4E42-C879-8CA4-08A4FCAD9C2540245infoc; buvid3=FAB8EA3F-AF0C-48CF-A628-63098690201B5457infoc; rpdid=pspoixwxxdospiksmxww; CURRENT_FNVAL=16; _uuid=2E935632-9C1C-4E27-8A1F-FA52CBD64F9B93052infoc; LIVE_BUVID=AUTO1415426957952781; UM_distinctid=1672fd66d4f40-0a7e1ea1005e81-6313363-144000-1672fd66d50562; sid=9bhiyq9v; fts=1543317658; Hm_lvt_8a6e55dbd2870f0f5bc9194cddf32a02=1544965879; DedeUserID=354241872; DedeUserID__ckMd5=eb6b63bcc1830f97; SESSDATA=4cbd35f1%2C1557232096%2Cbf7b0a41; bili_jct=2705564ca691b593ff1e442ffde3f187; CURRENT_QUALITY=80; bp_t_offset_354241872=202411500742088189; _dfcaptcha=7b8170c330f8a6fc99e92e19f8535ec5; stardustvideo=-1");
    }};

    public MainController() {
    }

    /**
     * 正则匹配
     */
    @FXML
    public void getMarcher() {
        Pattern pattern = Pattern.compile(text_regex.getText());
        Matcher matcher = pattern.matcher(text_src.getText());
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group(0));
            sb.append("\n");
        }
        text_result.setText(sb.toString());
        System.out.println(sb.toString());
    }

    /**
     * 正则替换
     */
    @FXML
    public void getReplace() {
        String src = text_src.getText();
        String result = src.replaceAll(text_regex.getText(), text_replace.getText());
        System.out.println(result);
        text_result.setText(result);
    }

    /**
     * 是否输出到文件
     */
    @FXML
    public void clickForCheckBoxFile() {
        boolean flag = check_output_file.isSelected();
        text_file_path.setDisable(!flag);
        text_file_path.setEditable(flag);
        text_file_path.setText(flag ? DEFAULT_FILE_PATH : null);
    }

    /**
     * 获取数据
     */
    @FXML
    public void openURL() {
        String src = text_url.getText();
        BufferedReader reader = null;
        FileOutputStream fos = null;
        if (src != null && !src.isEmpty()) {
            try {
                URL url = new URL(src);
                URLConnection connection = url.openConnection();

                //HTTP特殊处理
                HttpURLConnection httpURLConnection = null;
                if (connection instanceof HttpURLConnection) {
                    httpURLConnection = (HttpURLConnection) connection;
                    httpURLConnection.setRequestMethod("GET");
                    //默认请求头
                    Map<String, String> header = new HashMap<>(DEFAULT_HTTP_HEADER);
                    //指定的请求头，若key相同则覆盖默认值
                    String headerInput = text_http_request_header.getText();
                    if (headerInput != null && !(headerInput = headerInput.trim()).isEmpty()) {
                        String[] headerLines = headerInput.split("\n");
                        for (String line : headerLines) {
                            int mark = line.indexOf(':');
                            if (mark > 0) {
                                String key = line.substring(0, mark).trim();
                                String value = line.substring(mark + 1).trim();
                                header.put(key, value);
                            }
                        }
                    }
                    header.forEach(httpURLConnection::addRequestProperty);
                }

                //指定字符集
                String charset = text_charset.getText();
                try {
                    charset = Charset.isSupported(charset) ? charset : Charset.defaultCharset().name();
                } catch (IllegalCharsetNameException e) {
                    charset = Charset.defaultCharset().name();
                }
                System.out.println("chaset = " + charset);

                //返回流
                InputStream is = connection.getInputStream();
                //是否使用gzip解压
                if (check_gzip.isSelected()) {
                    is = new GZIPInputStream(is);
                }
                //若是HTTP请求，则输出响应信息
                if (httpURLConnection != null) {
                    System.out.println("http code : " + httpURLConnection.getResponseCode());
                    System.out.println("http message : " + httpURLConnection.getResponseMessage());
                    System.out.println("http res header : ");
                    httpURLConnection.getHeaderFields().forEach((key, value) -> {
                        System.out.print("\t" + key + ":");
                        System.out.print(value + "\n");
                    });
                }

                if (check_output_file.isSelected()) {
                    //直接写入文件
                    String filePath = text_file_path.getText();
                    if (filePath == null || (filePath = filePath.trim()).isEmpty()) {
                        filePath = DEFAULT_FILE_PATH;
                    }
                    fos = new FileOutputStream(filePath);
                    byte[] temp = new byte[1024];
                    int len;
                    while ((len = is.read(temp)) > 0) {
                        fos.write(temp, 0, len);
                    }
                } else {
                    //返回到文本框
                    reader = new BufferedReader(new InputStreamReader(is, charset));
                    StringBuilder respond = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        respond.append(line);
                        respond.append("\n");
                    }
                    text_respond.setText(respond.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 爬取github代码
     * 因为有些不需要的代码实在不想clone啊。。
     */
    @FXML
    public void getGithubCodes() {
        getGithubCode(null, null);
    }

    private void getGithubCode(Set<String> tasks, Set<String> completes) {
        String url = text_github_url.getText();
        if (url.contains("github.com")) {
            return;
        }
        if (tasks == null) {
            tasks = new HashSet<String>() {{
                add(url);
            }};
        }
        if (tasks.isEmpty()) {
            System.out.println("不能没有任务！");
            return;
        }
        if (completes == null) {
            completes = new HashSet<>();
        }

        final Set<String> finalCompletes = completes;
        final Set<String> newTasks = new HashSet<>();
        final Set<String> finalCompletes1 = completes;
        tasks.forEach(task -> {
            if (task != null) {
                synchronized (task) {
                    if (!finalCompletes.contains(task)) {
                        finalCompletes.add(task);
                        InputStream is = null;
                        FileWriter fw = null;
                        try {
                            URL target = new URL(task);
                            System.out.println(task);
                            HttpURLConnection connection = (HttpURLConnection) target.openConnection();
                            connection.setRequestMethod("GET");
                            DEFAULT_HTTP_HEADER.forEach(connection::addRequestProperty);
                            Document document = Jsoup.parse(new GZIPInputStream(is = connection.getInputStream()), "utf-8", target.toURI().toString());
                            Elements elements = document.getElementsByClass("blob-code-inner");

                            //html提取
                            final StringBuilder sb = new StringBuilder();
                            elements.forEach(element -> {
//                        System.out.println(element.toString());

                                String line = element.toString();
                                int start = 0, end = 0, len = line.length() - 1;
                                StringBuilder lineCodeSB = new StringBuilder();
                                while ((start = line.indexOf('>')) > 0 && start < len) {
                                    line = line.substring(Math.min(start + 1, len));
                                    len = line.length() - 1;
                                    end = line.indexOf('<');
                                    if (end > 0 && end < len) {
                                        lineCodeSB.append(line, 0, end);
                                    }
                                }
                                String lineCode = lineCodeSB.toString();
                                sb.append(lineCode);
                                sb.append("\n");

                                String newUrl = getCImportURL(lineCode, task);
                                if (!newUrl.isEmpty() && !finalCompletes1.contains(newUrl)) {
                                    newTasks.add(newUrl);
                                }
                            });

                            //写入文件
                            String fileDir = "f:\\test\\github";
                            String filePath = fileDir + target.getPath();
                            System.out.println(filePath + " : file path");
                            File file = new File(filePath);
                            file.getParentFile().mkdirs();
                            fw = new FileWriter(file);
                            fw.write(sb.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fw != null) {
                                try {
                                    fw.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });

        if (!newTasks.isEmpty()) {
            getGithubCode(newTasks, finalCompletes);
        }

    }

    private static String getJavaImportURL(String lineCode, String url) {
        if (lineCode.contains("import") && !lineCode.contains("java.")
                && !lineCode.contains("sun.") && !lineCode.contains("javax.")
                && !lineCode.contains(".*")) {
            lineCode = lineCode.trim();
            String imp = lineCode.substring(lineCode.indexOf(' ') + 1, lineCode.indexOf(';'));
            String uri = url.replaceAll("https://github.com/", "");
            String[] src = uri.split("/");
            String[] rep = imp.split("\\.");
            int len1 = src.length, len2 = rep.length;
            for (int i = 0; i < len2; i++) {
                src[len1 - i - 1] = rep[len2 - i - 1];
            }
            StringBuilder newURL = new StringBuilder("https://github.com");
            for (String s : src) {
                newURL.append('/');
                newURL.append(s);
            }
            return newURL.toString();
        }
        return "";
    }

    private static String getCImportURL(String lineCode, String url) {
        if (lineCode.contains("#include ") && !lineCode.contains("<") && !lineCode.contains(">")
                && !lineCode.contains("&lt;") && !lineCode.contains("&gt;")) {
            lineCode = lineCode.trim();
            String imp = lineCode.substring(lineCode.indexOf('"') + 1, lineCode.lastIndexOf('"'));
            String uri = url.replaceAll("https://github.com/", "");
            String[] src = uri.split("/");
            String[] rep = imp.split("/");
            int len1 = src.length, len2 = rep.length;
            for (int i = 0; i < len2; i++) {
                src[len1 - i - 1] = rep[len2 - i - 1];
            }
            StringBuilder newURL = new StringBuilder("https://github.com");
            for (String s : src) {
                newURL.append('/');
                newURL.append(s);
            }
            return newURL.toString();
        }
        return "";
    }

    /**
     * ===================================== PDF Util Start ===========================================
     */
    //PDF文件路径
    @FXML
    public TextField text_pdf_file_path;
    //开始生成书签
    @FXML
    public Button button_pdf_work;
    //目录开始的张数
    @FXML
    public TextField text_pdf_directory_start;
    //正文开始的张数
    @FXML
    public TextField text_pdf_body_start;

    @FXML
    public void pdfWorkStart() {
        String pdfFilePath = text_pdf_file_path.getText();
        String directoryStart = text_pdf_directory_start.getText();
        String bodyStart = text_pdf_body_start.getText();
        if (pdfFilePath == null || directoryStart == null || bodyStart == null) {
            return;
        }


    }

    public TextField getText_regex() {
        return text_regex;
    }

    public void setText_regex(TextField text_regex) {
        this.text_regex = text_regex;
    }

    public TextField getText_replace() {
        return text_replace;
    }

    public void setText_replace(TextField text_replace) {
        this.text_replace = text_replace;
    }

    public TextArea getText_src() {
        return text_src;
    }

    public void setText_src(TextArea text_src) {
        this.text_src = text_src;
    }

    public TextArea getText_result() {
        return text_result;
    }

    public void setText_result(TextArea text_result) {
        this.text_result = text_result;
    }

    public Button getButton_run() {
        return button_run;
    }

    public void setButton_run(Button button_run) {
        this.button_run = button_run;
    }

    public Button getButton_replace() {
        return button_replace;
    }

    public void setButton_replace(Button button_replace) {
        this.button_replace = button_replace;
    }

    public TextField getText_url() {
        return text_url;
    }

    public void setText_url(TextField text_url) {
        this.text_url = text_url;
    }

    public TextArea getText_respond() {
        return text_respond;
    }

    public void setText_respond(TextArea text_respond) {
        this.text_respond = text_respond;
    }

    public Button getButton_connect() {
        return button_connect;
    }

    public void setButton_connect(Button button_connect) {
        this.button_connect = button_connect;
    }

    public TextField getText_charset() {
        return text_charset;
    }

    public void setText_charset(TextField text_charset) {
        this.text_charset = text_charset;
    }

    public CheckBox getCheck_gzip() {
        return check_gzip;
    }

    public void setCheck_gzip(CheckBox check_gzip) {
        this.check_gzip = check_gzip;
    }

    public TextArea getText_http_request_header() {
        return text_http_request_header;
    }

    public void setText_http_request_header(TextArea text_http_request_header) {
        this.text_http_request_header = text_http_request_header;
    }

    public CheckBox getCheck_output_file() {
        return check_output_file;
    }

    public void setCheck_output_file(CheckBox check_output_file) {
        this.check_output_file = check_output_file;
    }

    public TextField getText_file_path() {
        return text_file_path;
    }

    public void setText_file_path(TextField text_file_path) {
        this.text_file_path = text_file_path;
    }

    public TextField getText_github_url() {
        return text_github_url;
    }

    public void setText_github_url(TextField text_github_url) {
        this.text_github_url = text_github_url;
    }

    public Button getButton_github_get_code() {
        return button_github_get_code;
    }

    public void setButton_github_get_code(Button button_github_get_code) {
        this.button_github_get_code = button_github_get_code;
    }

    public TextField getText_pdf_file_path() {
        return text_pdf_file_path;
    }

    public void setText_pdf_file_path(TextField text_pdf_file_path) {
        this.text_pdf_file_path = text_pdf_file_path;
    }

    public Button getButton_pdf_work() {
        return button_pdf_work;
    }

    public void setButton_pdf_work(Button button_pdf_work) {
        this.button_pdf_work = button_pdf_work;
    }

    public TextField getText_pdf_directory_start() {
        return text_pdf_directory_start;
    }

    public void setText_pdf_directory_start(TextField text_pdf_directory_start) {
        this.text_pdf_directory_start = text_pdf_directory_start;
    }

    public TextField getText_pdf_body_start() {
        return text_pdf_body_start;
    }

    public void setText_pdf_body_start(TextField text_pdf_body_start) {
        this.text_pdf_body_start = text_pdf_body_start;
    }
}
