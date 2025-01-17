package net.coding.program.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaochen on 14-10-25.
 */
public class HtmlContent {

    private static final String REGX_PHOTO = "(?:<br>)? ?<a href=\"(?:[^\\\"]*)?\" (?:alt=\"\" )?target=\"_blank\" class=\"bubble-markdown-image-link\".*?><img src=\"(.*?)\" alt=\"(.*?)\".*?></a>(?:<br>)? ?";
    private static final String REGX_PHOTO_OLD = "<div class='message-image-box'><a href=\'(?:[^\\\']*)?\' target='_blank'><img class='message-image' src='(.*?)'/?></a></div>";
    private static final String REPLACE_PHOTO = "[图片]";

    private static final String REGX_MONKEY = "<img class=\"emotion monkey\" src=\".*?\" title=\"(.*?)\">";

    private static final String REGX_CODE = "(<pre>)?<code .*(\\n)?</code>(</pre>)?";

    private static final String REPLACE_CODE = "[代码]";

    private static final String REGX_HTML = "<([A-Za-z][A-Za-z0-9]*)[^>]*>(.*?)</\\1>";

    public static Global.MessageParse parseMaopao(String s) {
        return createMessageParse(s, REGX_PHOTO);
    }

    public static Global.MessageParse parseMessage(String s) {
        Global.MessageParse parse = createMessageParse(s, REGX_PHOTO_OLD);
        if (parse.uris.size() > 0) {
            return parse;
        } else {
            return parseMaopao(s);
        }
    }

    public static String parseDynamic(String s) {
        return parseReplacePhoto(s).text;
    }

    public static Global.MessageParse parseReplacePhoto(String s) {
        Global.MessageParse parse = new Global.MessageParse();

        String replaceImage = s.replaceAll(HtmlContent.REGX_PHOTO, REPLACE_PHOTO);

        replaceImage = replaceImage.replaceAll(REGX_MONKEY, "<img src=\"$1\">");

        parse.text = replaceAllSpace(replaceImage);

        return parse;
    }

    public static String parseToText(String s) {
        return s.replaceAll(REGX_MONKEY, "[$1]")
                .replaceAll(REGX_PHOTO, REPLACE_PHOTO)
                .replaceAll(REGX_PHOTO_OLD, REPLACE_PHOTO)
                .replaceAll(REGX_CODE, REPLACE_CODE)
                .replaceAll(REGX_HTML, "$2");
    }

    private static Global.MessageParse createMessageParse(String s, String regx) {
        Global.MessageParse parse = new Global.MessageParse();

        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            parse.uris.add(matcher.group(1));
        }

        String replaceImage = s.replaceAll(regx, "");
        parse.text = replaceAllSpace(replaceImage);

        return parse;
    }

    private static String replaceAllSpace(String s) {
        final String br = "<br>";
        s = s.replaceAll("<p>", "").replaceAll("</p>", br);
        if (s.endsWith(br)) {
            s = s.substring(0, s.length() - br.length());
        }

        if (s.startsWith(br)) {
            s = s.substring(br.length(), s.length());
        }

        String temp = s.replaceAll("<br>", "").replaceAll(" ", "").replaceAll("\n", "");
        if (temp.isEmpty()) {
            return "";
        }

        return s;
    }

    public static String createUserHtml(String globalKey, String name) {
        final String format = "<font color='#3bbd79'><a href=\"/u/%s\">%s</a></font>";
        return String.format(format, globalKey, name);
    }
}
