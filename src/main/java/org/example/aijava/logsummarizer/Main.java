package org.example.aijava.logsummarizer;

import picocli.CommandLine;

/**
 * このアプリケーションのエントリポイント（起動時に最初に呼ばれるクラス）です。
 * 実際のCLI引数の解析や処理内容は {@link LogSummarizerCommand} に委譲しています。
 */
public class Main {

    /**
     * アプリケーションの起動関数。
     * picocliに引数解析と {@link LogSummarizerCommand#call()} の実行を任せ、
     * その戻り値（0=成功、それ以外=異常）をプロセスの終了コードとして返します。
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(
                new LogSummarizerCommand()).execute(args);
        System.exit(exitCode);
    }
}
