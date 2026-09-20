package org.example.aijava.logsummarizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ログ要約処理全体の流れ（分割 → マスキング → AI要約 → 統合）を取りまとめるクラスです。
 * 長いログは{@link LogChunker}で分割し、各チャンクを並行して要約したうえで、
 * 最後にそれらの要約をさらに1つの要約へまとめ直します。
 */
public class SummarizationPipeline {

    /**
     * 1チャンクあたりの最大文字数。AIモデルに一度に渡せる量の目安として設定しています。
     */
    private static final int MAX_CHUNK_CHARS = 6000;

    /**
     * 各チャンクの要約を実際に行うSummarizer実装。
     */
    private final Summarizer summarizer;

    /**
     * 使用するSummarizer実装を指定してパイプラインを構築します。
     *
     * @param summarizer 各チャンクの要約に使うSummarizer実装
     */
    public SummarizationPipeline(Summarizer summarizer) {
        this.summarizer = summarizer;
    }

    /**
     * ログ本文を要約し、結果を{@link SummaryResult}として返します。
     * チャンクが1つだけなら単純に要約し、複数あれば並行要約後に1つへ統合します。
     *
     * @param logText 要約対象のログ本文全体
     * @return 要約結果
     * @throws Exception 並行要約中に発生した例外（AI API呼び出しの失敗など）
     */
    public SummaryResult run(String logText) throws Exception {
        List<String> chunks = LogChunker.splitIntoChunks(
                logText, MAX_CHUNK_CHARS);

        String summary = (chunks.size() == 1)
                ? summarizeOne(chunks.get(0))
                : summarizeInParallel(chunks);

        return new SummaryResult(
                summary, logText.length(), chunks.size());
    }

    /**
     * 1チャンクを要約する共通処理です。AIへ送る前に必ずマスキングを通します。
     *
     * @param chunk 要約対象の1チャンク分のテキスト
     * @return 要約結果
     */
    private String summarizeOne(String chunk) {
        return summarizer.summarize(LogRedactor.mask(chunk));
    }

    /**
     * 複数チャンクを仮想スレッドで並行に要約し、それぞれの部分要約を
     * さらにもう一度AIに渡して1つの要約へ統合します。
     *
     * @param chunks 分割済みのチャンク一覧
     * @return 統合後の1つの要約
     * @throws Exception 並行実行タスクの待ち受け中に発生した例外
     */
    private String summarizeInParallel(List<String> chunks)
            throws Exception {
        List<Future<String>> futures = new ArrayList<>();
        List<String> partials = new ArrayList<>();

        // 各チャンクの要約はI/O待ちが中心で軽量なため、仮想スレッドで並行実行してレイテンシを短縮する
        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {
            for (String chunk : chunks) {
                futures.add(executor.submit(
                        () -> summarizeOne(chunk)));
            }
            // 投入順にget()することで、部分要約の順序を元のログの順序と一致させる
            for (Future<String> future : futures) {
                partials.add(future.get());
            }
        }

        // 各チャンクの部分要約を1つにつなげ、最後にもう一度AIへ渡して全体要約へまとめ直す
        String combined = String.join("\n---\n", partials);
        return summarizeOne(
                "以下は障害ログの区間ごとの要約です。"
                        + "全体を1つの要約にまとめてください。\n"
                        + combined);
    }
}
