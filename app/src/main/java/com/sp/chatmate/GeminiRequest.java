package com.sp.chatmate;

public class GeminiRequest {
    private String contents;

    public GeminiRequest(String prompt) {
        this.contents = prompt;
        this.contents= "{\"contents\": [{\"parts\":[{\"text\": \"Write a story about a magic backpack.\"}]}]}";
        //this.prompt= "{ \"contents\": [ { \"role\": \"user\", \"parts\": [ { \"text\": \"input: \" }, { \"text\": \"output: \" } ] } ], \"generationConfig\": { \"temperature\": 1, \"topK\": 64, \"topP\": 0.95, \"maxOutputTokens\": 8192, \"responseMimeType\": \"text/plain\" } }";
    }

    public String getPrompt() {
        return contents;
    }

    public void setPrompt(String prompt) {
        this.contents = prompt;
    }
}


