# log-summarizer

障害ログを AI（OpenAI または Anthropic）で要約する Java 製の CLI ツールです。
長大なログファイルをチャンク単位に分割し、メールアドレスや電話番号などの個人情報をマスキングしたうえで AI に送信し、要約結果をテキストまたは JSON で出力します。

## 特徴

- **複数の AI プロバイダに対応**: `openai` / `anthropic` を切り替え可能
- **長大ログの自動分割**: 1 チャンクあたり最大 6000 文字で行単位に分割し、仮想スレッドで並行要約
- **個人情報のマスキング**: AI へ送信する前にメールアドレス・電話番号らしき文字列を自動でマスク
- **柔軟な出力形式**: 人間向けテキスト / JSON を選択可能

## 必要環境

- Java 25（`pom.xml` で `maven-compiler-plugin` の `release` を 25 に指定）
- Maven 3.6.3 以上（Maven Wrapper は同梱されていないため、`mvn` コマンドを直接使用します。使用している `maven-compiler-plugin`/`maven-surefire-plugin`/`exec-maven-plugin` がいずれも Maven 3.6.3 以上を要求します。動作確認済みバージョン: 3.9.16）
- OpenAI または Anthropic の API キー（使用するプロバイダ側のみで可）

## 環境変数

`-p/--provider` に指定したプロバイダに応じて、以下のいずれかの環境変数が**必須**です（`EnvConfig` が読み込み、未設定の場合は起動時にエラーになります）。

| 環境変数 | 対応プロバイダ | 説明 |
| --- | --- | --- |
| `OPENAI_API_KEY` | `openai`（既定） | OpenAI の API キー（Chat Completions API, `gpt-4o-mini` を使用） |
| `CLAUDE_API_KEY` | `anthropic` | Anthropic の API キー（Messages API, `claude-haiku-4-5` を使用） |

環境変数はプロジェクトルートの `.env` ファイルに記述するか、OS 側で `export` してください（`.env` より OS 側の環境変数が優先されます）。`.env` は `.gitignore` 済みです。

```
# .env の例
OPENAI_API_KEY=sk-...
CLAUDE_API_KEY=sk-ant-...
```

## セットアップ

```sh
git clone <このリポジトリ>
cd log-summarizer
```

プロジェクトルートに `.env` ファイルを作成し、上記「環境変数」の内容に従って使用するプロバイダの API キーを設定してください。

## 実行方法

`exec-maven-plugin`（メインクラス: `org.example.aijava.logsummarizer.Main`）を使って実行します。

```sh
mvn -q compile exec:java -Dexec.args="-i <ログファイルパス> [-p openai|anthropic] [-f text|json]"
```

### オプション

| オプション | 説明 | 既定値 |
| --- | --- | --- |
| `-i`, `--input` | 要約対象のログファイルのパス（必須） | - |
| `-p`, `--provider` | 使用する AI プロバイダ（`openai` または `anthropic`） | `openai` |
| `-f`, `--format` | 出力形式（`text` または `json`） | `text` |
| `-h`, `--help` | ヘルプを表示 | - |
| `-V`, `--version` | バージョンを表示 | - |

### 実行例

```sh
# OpenAI（既定）でテキスト出力
mvn -q compile exec:java -Dexec.args="-i laravel.log"

# Anthropic を使って JSON 出力
mvn -q compile exec:java -Dexec.args="-i laravel.log -p anthropic -f json"
```

実行には、指定した `-p` に対応する API キー（`OPENAI_API_KEY` または `CLAUDE_API_KEY`）が環境変数として設定されている必要があります。未設定の場合は以下のようなエラーで終了します。

```
環境変数 OPENAI_API_KEY が設定されていません。
.env に OPENAI_API_KEY=... を記述するか、OS側で export OPENAI_API_KEY=... を設定してください。
```

## プロジェクト構成

```
src/main/java/org/example/aijava/logsummarizer/
├── Main.java                  # エントリポイント
├── LogSummarizerCommand.java  # CLI引数の定義と実行フロー
├── EnvConfig.java             # 環境変数（.env）の読み込み
├── SummarizerFactory.java     # プロバイダ名から実装を生成
├── Summarizer.java            # 要約処理の共通インターフェース
├── OpenAiSummarizer.java      # OpenAI 実装
├── AnthropicSummarizer.java   # Anthropic 実装
├── SummarizationPipeline.java # 分割・マスキング・要約・統合の一連の流れ
├── LogChunker.java            # ログの分割処理
├── LogRedactor.java           # 個人情報のマスキング処理
└── SummaryResult.java         # 要約結果を保持するレコード
```
