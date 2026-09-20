package org.example.aijava;

import org.example.aijava.logsummarizer.SummarizationPipeline;
import org.example.aijava.logsummarizer.Summarizer;
import org.example.aijava.logsummarizer.SummaryResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SummarizationPipeline} のテストクラスです。
 * 実際のAI APIは呼ばず、常に固定文字列を返す {@link FakeSummarizer} で置き換えることで、
 * チャンク分割・統合の挙動だけを検証しています。
 */
class SummarizationPipelineTest {

    /**
     * AI APIを実際に呼ばずにテストするためのダミー実装。常に固定の要約文字列を返します。
     */
    private static class FakeSummarizer implements Summarizer {

        /**
         * 受け取ったログ本文の内容にかかわらず、常に固定文字列 "FAKE SUMMARY" を返します。
         *
         * @param logText 要約対象のログ本文（テストでは内容を見ない）
         * @return 常に "FAKE SUMMARY"
         */
        @Override
        public String summarize(String logText) {
            return "FAKE SUMMARY";
        }
    }

    /**
     * ログが1チャンクに収まる場合、分割されずにそのまま要約されることを検証します。
     */
    @Test
    void singleChunkReturnsSummary()
            throws Exception {
        SummarizationPipeline pipeline = new SummarizationPipeline(
                new FakeSummarizer());

        SummaryResult result =
                pipeline.run("エラーが発生しました\n");

        assertEquals(1, result.chunkCount());
        assertEquals("FAKE SUMMARY", result.summary());
    }

    /**
     * ログが複数チャンクに分割される場合、各チャンクの要約結果が正しく統合され、
     * 元ログの文字数も正しく記録されることを検証します。
     */
    @Test
    void multiChunkSummarizesAndMerges()
            throws Exception {
        SummarizationPipeline pipeline = new SummarizationPipeline(
                new FakeSummarizer());
        // MAX_CHUNK_CHARS（6000文字）を超えるよう、あえて長いテキストを生成して分割を発生させる
        String longText = "エラー発生\n".repeat(1200);

        SummaryResult result = pipeline.run(longText);

        assertTrue(result.chunkCount() > 1);
        assertEquals(longText.length(), result.sourceCharCount());
        assertEquals("FAKE SUMMARY", result.summary());
    }
}
