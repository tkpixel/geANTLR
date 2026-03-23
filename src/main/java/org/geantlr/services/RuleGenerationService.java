package org.geantlr.services;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.FunctionTool;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.geantlr.services.AntlrValidationTool;

@Singleton
public class RuleGenerationService {

    private final AntlrValidationTool validationTool;
    @Inject
    public RuleGenerationService(AntlrValidationTool validationTool) {
        this.validationTool = validationTool;
    }

    public String generateRule(String naturalLanguagePrompt) {
        ChatModel chatModel = dev.langchain4j.model.ollama.OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5-coder:7b")
                .build();

        LangChain4j llmModel = new LangChain4j(chatModel);

        // Create tool from annotated method. FunctionTool handles @Schema annotated methods
        FunctionTool functionTool = FunctionTool.create(validationTool, "validateCode");

        LlmAgent agent = LlmAgent.builder()
                .name("Rule Generator")
                .instruction("You are an expert DSL developer. Given a natural language prompt, you generate correct DSL code conforming to the ANTLR grammar. If the validation tool fails, analyze the ANTLR syntax error and fix the code. ONLY output the valid code, no markdown.")
                .model(llmModel)
                .tools(functionTool)
                .build();

        LoopAgent loopAgent = LoopAgent.builder()
                .name("Self-Correcting Generator")
                .subAgents(agent)
                .maxIterations(5)
                .build();

        com.google.adk.runner.InMemoryRunner runner = new com.google.adk.runner.InMemoryRunner(loopAgent);

        com.google.genai.types.Content content = com.google.genai.types.Content.builder()
                .parts(java.util.List.of(com.google.genai.types.Part.fromText(naturalLanguagePrompt)))
                .build();

        // Create the session for the given appName/userId
        com.google.adk.sessions.Session session = runner.sessionService()
                .createSession("default", "default")
                .blockingGet();

        String result = runner.runAsync(session, content, com.google.adk.agents.RunConfig.builder().build())
                .blockingLast()
                .stringifyContent();

        return result;
    }
}
