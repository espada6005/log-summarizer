package org.example.aijava.logsummarizer;

/**
 * ログ本文をAI（OpenAIやAnthropicなど）に渡して要約させるための共通インターフェースです。
 * プロバイダごとの実装（{@link OpenAiSummarizer}, {@link AnthropicSummarizer}）を
 * このインターフェース越しに扱うことで、呼び出し側はどのAIを使っているかを意識せずに済みます。
 */
public interface Summarizer {

    /**
     * ログのテキストを受け取り、AIによる要約結果の文字列を返します。
     *
     * @param logText 要約したいログ本文（1チャンク分）
     * @return AIが生成した要約テキスト
     */
    String summarize(String logText);

}
