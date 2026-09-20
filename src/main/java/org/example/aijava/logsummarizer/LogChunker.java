package org.example.aijava.logsummarizer;

import java.util.ArrayList;
import java.util.List;

/**
 * 長いログテキストを、AIに渡すのに適したサイズの塊（チャンク）に分割するユーティリティです。
 * AIモデルには一度に渡せる文字数（トークン数）に上限があるため、
 * 長大なログはそのまま渡せず、事前にこのクラスで分割してから要約する必要があります。
 */
public final class LogChunker {

    /**
     * ユーティリティクラスのためインスタンス化を禁止します。
     */
    private LogChunker() {
    }

    /**
     * テキストを行単位で見ていき、1チャンクの文字数がmaxCharsを超えないように分割します。
     * 行の途中で分割してしまうとログの意味が壊れるため、必ず行の区切りでのみ分割します。
     *
     * @param text     分割対象のログ本文
     * @param maxChars 1チャンクあたりの最大文字数の目安
     * @return 分割されたチャンクのリスト（元テキストが空でなければ最低1要素）
     */
    public static List<String> splitIntoChunks(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            // この行を足すとmaxCharsを超えてしまうかどうかを事前に判定する
            boolean wouldOverflow = current.length()
                    + line.length() + 1 > maxChars;
            if (wouldOverflow && !current.isEmpty()) {
                // 超えるなら、この行を足す前に今までの内容を1チャンクとして確定させる
                chunks.add(current.toString());
                current.setLength(0);
            }
            current.append(line).append('\n');
        }

        // 最後に残った分（maxCharsに達せず終わった端数）も忘れずにチャンクとして追加する
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }

        return chunks;
    }

}
