package com.example.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

	private final ChatClient chatClient;

	private final VectorStore simpleVectorStore;

	@Value("classpath:员工考勤管理制度.pdf")
	private Resource resource;

	@Autowired
	public RagController(ChatClient chatClient, VectorStore simpleVectorStore) {
		this.chatClient = chatClient;
		this.simpleVectorStore = simpleVectorStore;
	}

	@GetMapping("/importDocument")
	public void importDocument() {
		// 加载文档
		DocumentReader reader = new PagePdfDocumentReader(resource);
		List<Document> documents = reader.get();
		// 切分文档
		List<Document> splitDocuments = new TokenTextSplitter().apply(documents);
		// 向向量库中添加文档
		simpleVectorStore.add(splitDocuments);
	}

	@GetMapping(value = "/chat", produces = "text/plain; charset=UTF-8")
	public String chat(@RequestParam("message") String message) {
		// 发起聊天请求并处理响应
		return chatClient.prompt()
				.user(message)
				.advisors(
						// 实现RAG的Advisor
						QuestionAnswerAdvisor.builder(simpleVectorStore)
								// 自定义PromptTemplate
//								.promptTemplate()
								// 自定义SearchRequest
//								.searchRequest()
								.build()
				)
				.call()
				.content();
	}

}

