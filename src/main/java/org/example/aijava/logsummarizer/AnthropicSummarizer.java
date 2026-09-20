package org.example.aijava.logsummarizer;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

/**
 * AnthropicのMessages API（Claude Haiku 4.5）を使ってログを要約する {@link Summarizer} 実装です。
 */
public class AnthropicSummarizer implements Summarizer {

    /**
     * AIに与える役割・出力方針の指示（システムプロンプト）。
     * {@link OpenAiSummarizer}と同じ内容にして、どちらのプロバイダを使っても
     * 出力形式が揃うようにしています。
     */
    private static final String SYSTEM_PROMPT =
            "あなたは障害対応を支援するアシスタントです。"
                    + "与えられたログを読み、発生時刻・概要・影響範囲・"
                    + "推定原因を簡潔な日本語でまとめてください。";

    /**
     * Anthropic APIと通信するためのクライアント。
     */
    private final AnthropicClient client;

    /**
     * 指定されたAPIキーでAnthropicクライアントを初期化します。
     *
     * @param apiKey AnthropicのAPIキー
     */
    public AnthropicSummarizer(String apiKey) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * ログ本文をAnthropicのMessages APIに1回リクエストし、
     * 返ってきた最初のテキストブロックを要約結果として返します。
     *
     * @param logText 要約対象のログ本文
     * @return AIが生成した要約テキスト
     */
    @Override
    public String summarize(String logText) {
        MessageCreateParams params = MessageCreateParams
                .builder()
                .model(Model.CLAUDE_HAIKU_4_5)
                .maxTokens(1024L)
                .system(SYSTEM_PROMPT)
                .addUserMessage(logText)
                .build();

        Message message = client.messages().create(params);
        // 応答のcontentは複数ブロックになり得るが、テキストのみを想定しているため先頭を取得する。
        // テキストブロックでなかった場合はorElseThrowで例外にする
        return message.content().getFirst()
                .text().orElseThrow().text();
    }
}

