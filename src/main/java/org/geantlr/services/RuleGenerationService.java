package org.geantlr.services;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.FunctionTool;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.geantlr.services.AntlrValidationTool;
import java.time.Duration;

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
                .timeout(Duration.ofMinutes(5))
                .build();

        LangChain4j llmModel = new LangChain4j(chatModel);

        // Create tool from annotated method. FunctionTool handles @Schema annotated methods
        FunctionTool functionTool = FunctionTool.create(validationTool, "validateCode");

        LlmAgent agent = LlmAgent.builder()
                .name("Rule Generator")
                .instruction("You are an autonomous expert DSL developer. Your ONLY task is to output syntactically valid code that conforms to the ANTLR grammar. DO NOT output conversational text, greetings, explanations, or markdown blocks (no ```). You MUST use the validateCode tool to check your code. If the tool returns a syntax error, you MUST analyze the error and output the corrected code. Only return the final, valid code string.")
                .model(llmModel)
                .tools(functionTool)
                .build();

        LoopAgent loopAgent = LoopAgent.builder()
                .name("Self-Correcting Generator")
                .subAgents(agent)
                .maxIterations(5)
                .build();

        com.google.adk.runner.InMemoryRunner runner = new com.google.adk.runner.InMemoryRunner(loopAgent);

        String explicitPrompt = "Generate the DSL code for the following business rule. Use the tools to validate it:\n\n" + naturalLanguagePrompt;

        com.google.genai.types.Content content = com.google.genai.types.Content.builder()
                .role("user")
                .parts(java.util.List.of(com.google.genai.types.Part.fromText(explicitPrompt)))
                .build();

        // Create the session for the given appName/userId
        com.google.adk.sessions.Session session = runner.sessionService()
                .createSession("default", "default")
                .blockingGet();

        String result = runner.runAsync(session, content, com.google.adk.agents.RunConfig.builder().build())
                .filter(e -> e.content().isPresent() && e.content().get().text() != null && !e.content().get().text().isEmpty())
                .map(com.google.adk.events.Event::stringifyContent)
                .scan((a, b) -> a + b) // Accumulate if there are multiple parts
                .blockingLast(""); // Provide default empty string instead of throwing if empty

        System.out.println("LLM Output Result:\n" + result);

        // Strip markdown backticks if present
        if (result != null && result.contains("```")) {
            result = result.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "").trim();
        }

        return result;
    }
}
