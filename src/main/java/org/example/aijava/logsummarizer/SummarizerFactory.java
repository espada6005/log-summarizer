package org.example.aijava.logsummarizer;

/**
 * プロバイダ名（"openai" や "anthropic"）の文字列から、
 * 対応する {@link Summarizer} の実装インスタンスを生成するファクトリクラスです。
 * CLIの --provider オプションで指定された値をもとに、ここで実装を切り替えます。
 */
public final class SummarizerFactory {

    /**
     * ユーティリティクラスのためインスタンス化を禁止します。
     */
    private SummarizerFactory() {
    }

    /**
     * 指定されたプロバイダ名に対応するSummarizerを生成します。
     *
     * @param provider "openai" または "anthropic"
     * @param apiKey   そのプロバイダ用のAPIキー
     * @return 生成されたSummarizerの実装
     * @throws IllegalArgumentException 未対応のプロバイダ名が渡された場合
     */
    public static Summarizer create(
            String provider, String apiKey) {
        return switch (provider) {
            case "openai" -> new OpenAiSummarizer(apiKey);
            case "anthropic" ->
                    new AnthropicSummarizer(apiKey);
            default -> throw new IllegalArgumentException(
                    // エラーメッセージにユーザー入力をそのまま含めるため、
                    // 改行などでログを汚染されないようサニタイズしてから埋め込む
                    "未対応のプロバイダです: "
                            + LogRedactor.sanitizeForLog(provider));
        };
    }
}
