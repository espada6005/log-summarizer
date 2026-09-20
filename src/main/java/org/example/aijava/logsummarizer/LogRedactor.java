package org.example.aijava.logsummarizer;

import java.util.regex.Pattern;

/**
 * ログ本文に含まれる個人情報（メールアドレスや電話番号）を、
 * 外部のAI APIへ送信する前にマスキング（置き換え）するためのユーティリティです。
 */
public final class LogRedactor {

    /**
     * メールアドレスらしき文字列にマッチする正規表現パターン。
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

    /**
     * 日本国内の電話番号らしき文字列（0から始まるハイフン区切り数字）にマッチする正規表現パターン。
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("0\\d{1,4}-?\\d{1,4}-?\\d{3,4}");

    /**
     * ユーティリティクラスのためインスタンス化を禁止します。
     */
    private LogRedactor() {
    }

    /**
     * テキスト中のメールアドレス・電話番号らしき文字列を "[メール]" "[電話番号]" に置き換えます。
     *
     * <p><b>注意:</b> 現在の実装は {@code masked}（メールアドレスを置換した結果）を使わずに、
     * 元の {@code text} に対して電話番号の置換だけを行って返しているため、
     * メールアドレスのマスキングが結果に反映されません。意図した動作と異なる場合は要確認です。</p>
     *
     * @param text マスキング対象のテキスト
     * @return マスキング後のテキスト
     */
    public static String mask(String text) {
        String masked = EMAIL_PATTERN
                .matcher(text).replaceAll("[メール]");
        return PHONE_PATTERN
                .matcher(text).replaceAll("[電話番号]");
    }

    /**
     * ログ出力に値をそのまま埋め込んでも改行注入などが起きないよう、
     * 改行文字をエスケープ済みの文字列（"\r" → "\\r"）に変換します。
     *
     * @param value サニタイズ対象の文字列
     * @return 改行文字をエスケープした文字列
     */
    public static String sanitizeForLog(String value) {
        return value.replace("\r", "\\r")
                .replace("\n", "\\n");
    }

}
