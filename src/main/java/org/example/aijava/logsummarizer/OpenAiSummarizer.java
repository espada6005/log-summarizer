package org.example.aijava.logsummarizer;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.
        ChatCompletionCreateParams;

/**
 * OpenAIのChat Completions API（GPT-4o mini）を使ってログを要約する {@link Summarizer} 実装です。
 */
public class OpenAiSummarizer implements Summarizer {

    /**
     * AIに与える役割・出力方針の指示（システムプロンプト）。
     */
    private static final String SYSTEM_PROMPT =
            "あなたは障害対応を支援するアシスタントです。"
                    + "与えられたログを読み、発生時刻・概要・影響範囲・"
                    + "推定原因を簡潔な日本語でまとめてください。";

    /**
     * OpenAI APIと通信するためのクライアント。
     */
    private final OpenAIClient client;

    /**
     * 指定されたAPIキーでOpenAIクライアントを初期化します。
     *
     * @param apiKey OpenAIのAPIキー
     */
    public OpenAiSummarizer(String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * ログ本文をOpenAIのChat Completions APIに1回リクエストし、
     * 返ってきた最初の回答メッセージの本文を要約結果として返します。
     *
     * @param logText 要約対象のログ本文
     * @return AIが生成した要約テキスト
     */
    @Override
    public String summarize(String logText) {
        ChatCompletionCreateParams params =
                ChatCompletionCreateParams.builder()
                        .model(ChatModel.GPT_4O_MINI)
                        .addSystemMessage(SYSTEM_PROMPT)
                        .addUserMessage(logText)
                        .build();

        ChatCompletion completion =
                client.chat().completions().create(params);

        // choices()の先頭（通常1件のみ返る）から回答本文を取り出す。
        // 万一本文が無い場合は空文字にフォールバックする
        return completion.choices().getFirst()
                .message().content().orElse("");
    }
}
