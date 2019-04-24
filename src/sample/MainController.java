package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
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

    @FXML
    public void openURL() {
        String src = text_url.getText();
        StringBuilder respond = new StringBuilder();
        BufferedReader reader = null;
        if (src != null && !src.isEmpty()) {
            try {
                URL url = new URL(src);
                URLConnection connection = url.openConnection();

                //HTTP特殊处理
                HttpURLConnection httpURLConnection = null;
                if (connection instanceof HttpURLConnection) {
                    httpURLConnection = (HttpURLConnection) connection;
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
                    httpURLConnection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
                    httpURLConnection.addRequestProperty("Accept-Encoding", "gzip");
                    httpURLConnection.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
                    httpURLConnection.addRequestProperty("Cache-Control", "max-age=0");
                    httpURLConnection.addRequestProperty("Connection", "keep-alive");
                    httpURLConnection.addRequestProperty("Upgrade-Insecure-Requests", "1");
                    httpURLConnection.addRequestProperty("Cookie", "_uuid=00C616CA-4E42-C879-8CA4-08A4FCAD9C2540245infoc; buvid3=FAB8EA3F-AF0C-48CF-A628-63098690201B5457infoc; rpdid=pspoixwxxdospiksmxww; CURRENT_FNVAL=16; _uuid=2E935632-9C1C-4E27-8A1F-FA52CBD64F9B93052infoc; LIVE_BUVID=AUTO1415426957952781; UM_distinctid=1672fd66d4f40-0a7e1ea1005e81-6313363-144000-1672fd66d50562; sid=9bhiyq9v; fts=1543317658; Hm_lvt_8a6e55dbd2870f0f5bc9194cddf32a02=1544965879; DedeUserID=354241872; DedeUserID__ckMd5=eb6b63bcc1830f97; SESSDATA=4cbd35f1%2C1557232096%2Cbf7b0a41; bili_jct=2705564ca691b593ff1e442ffde3f187; CURRENT_QUALITY=80; bp_t_offset_354241872=202411500742088189; _dfcaptcha=7b8170c330f8a6fc99e92e19f8535ec5; stardustvideo=-1");
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

                reader = new BufferedReader(new InputStreamReader(is, charset));

                //若是HTTP请求，则输入响应信息
                if (httpURLConnection != null) {
                    System.out.println("http code : " + httpURLConnection.getResponseCode());
                    System.out.println("http message : " + httpURLConnection.getResponseMessage());
                    System.out.println("http res header : ");
                    httpURLConnection.getHeaderFields().forEach((key, value) -> {
                        System.out.print("\t" + key + ":");
                        System.out.print(value + "\n");
                    });
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    respond.append(line);
                    respond.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        text_respond.setText(respond.toString());
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
}
