package org.example.aijava.logsummarizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * picocliで定義されたCLIコマンド本体です。
 * コマンドライン引数（入力ファイル・プロバイダ・出力形式）を受け取り、
 * ログの読み込みからAIによる要約、結果表示までの一連の流れを実行します。
 */
@Command(
        name = "log-summarizer",
        mixinStandardHelpOptions = true,
        version = "log-summarizer 1.0.0",
        description = "障害ログを要約するCLIツールです。")
public class LogSummarizerCommand
        implements Callable<Integer> {

    /**
     * 要約対象のログファイルのパス（必須オプション）。
     */
    @Option(names = {"-i", "--input"}, required = true,
            description = "要約対象のログファイルのパス")
    private Path input;

    /**
     * 使用するAIプロバイダ。"openai" または "anthropic" を指定します（既定値: openai）。
     */
    @Option(names = {"-p", "--provider"},
            defaultValue = "openai",
            description = "使用するプロバイダ: openai または anthropic")
    private String provider;

    /**
     * 結果の出力形式。"text"（人間向け）または "json" を指定します（既定値: text）。
     */
    @Option(names = {"-f", "--format"},
            defaultValue = "text",
            description = "出力形式: text または json")
    private String format;

    /**
     * コマンド実行本体。picocliにより自動的に呼び出されます。
     *
     * @return プロセスの終了コード（0固定＝正常終了。異常時は例外がスローされる）
     */
    @Override
    public Integer call() throws Exception {
        if (!"text".equalsIgnoreCase(format)
                && !"json".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException(
                    "未対応の出力形式です: "
                            + LogRedactor.sanitizeForLog(format));
        }

        String logText = Files.readString(input);

        // プロバイダごとに参照するAPIキーの環境変数名を切り替える
        String envName = provider.equals("anthropic")
                ? "CLAUDE_API_KEY" : "OPENAI_API_KEY";
        String apiKey = EnvConfig.requireEnv(envName);

        Summarizer summarizer =
                SummarizerFactory.create(provider, apiKey);
        SummarizationPipeline pipeline =
                new SummarizationPipeline(summarizer);
        SummaryResult result = pipeline.run(logText);

        printResult(result);
        return 0;
    }

    /**
     * --format の指定に応じて、結果を整形済みJSONまたは人間向けテキストで標準出力に表示します。
     *
     * @param result 表示対象の要約結果
     */
    private void printResult(SummaryResult result)
            throws Exception {
        if ("json".equalsIgnoreCase(format)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            System.out.println(
                    mapper.writeValueAsString(result));
            return;
        }
        System.out.println("=== 障害ログ要約 ===");
        System.out.println(result.summary());
        System.out.println();
        System.out.println(
                "元ログ文字数: " + result.sourceCharCount());
        System.out.println(
                "分割チャンク数: " + result.chunkCount());
    }
}
