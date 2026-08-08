# spring-ai

## Running examples

1. Create `.env` file in the project root directory with `OPENAI_API_KEY=sk-...` variable
2. Run `just.demo.openai.DemoPrompt.main` or any other example

## Redis

Required by some examples as a vector store

```
docker compose up --force-recreate
```

## Ollama

http://localhost:11434/

```
ollama serve
ollama stop

ollama ls
ollama run llama3.2

ollama pull llama3.2
ollama rm llama3.2
```