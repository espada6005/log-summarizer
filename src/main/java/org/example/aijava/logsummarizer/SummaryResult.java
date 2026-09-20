package org.example.aijava.logsummarizer;

/**
 * ログ要約の結果をまとめて保持するデータクラスです（Javaのrecordなので不変オブジェクト）。
 *
 * @param summary         AIが生成した最終的な要約文
 * @param sourceCharCount 要約前の元ログの文字数（長さの目安として表示に使う）
 * @param chunkCount      元ログを分割したチャンク（塊）の数。1なら分割なしで要約したことを意味する
 */
public record SummaryResult(String summary, int sourceCharCount, int chunkCount) {
}
