package org.example.aijava.logsummarizer;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 環境変数を取得するためのユーティリティクラスです。
 * プロジェクト直下の .env ファイル（存在する場合）を読み込みつつ、
 * OS側で設定された環境変数があればそちらを優先して参照するようにしています。
 */
public final class EnvConfig {

    /**
     * .envファイルを読み込んでキャッシュしておく{@link Dotenv}インスタンス。
     * アプリ起動時に一度だけ読み込み、以降はこの静的フィールドを使い回します。
     * ignoreIfMissing() を指定しているため、.env が存在しなくても例外にはなりません
     * （必要ならこの指定を外してください）。
     */
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    /**
     * ユーティリティクラスのためインスタンス化を禁止します。
     */
    private EnvConfig() {
    }

    /**
     * 必須の環境変数を取得します。
     * 未設定なら例外を投げます。
     *
     * @param name 環境変数名
     * @return 環境変数の値
     * @throws IllegalStateException 環境変数が未設定または空文字の場合
     */
    public static String requireEnv(String name) {
        String value = dotenv.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "環境変数 " + name + " が設定されていません。\n"
                            + ".env に " + name + "=... を記述するか、"
                            + "OS側で export " + name + "=... を設定してください。");
        }
        return value;
    }

    /**
     * 任意の環境変数を取得します。
     * 未設定なら既定値へフォールバックします。
     *
     * @param name         環境変数名
     * @param defaultValue 未設定時に使う既定値
     * @return 環境変数の値、未設定なら{@code defaultValue}
     */
    public static String optionalEnv(String name, String defaultValue) {
        String value = dotenv.get(name);
        return (value == null || value.isBlank())
                ? defaultValue
                : value;
    }

}
